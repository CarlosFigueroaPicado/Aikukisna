package com.aikukisna.app.data.local.ia

import com.aikukisna.app.domain.repository.TraductorAutomaticoLocal
import javax.inject.Inject

class GemmaTraductorLocal @Inject constructor(
    private val motor: MotorGemmaLocal,
    private val modelo: ModeloTukiLocal
) : TraductorAutomaticoLocal {

    override fun disponible(): Boolean = motor.disponible()

    // El modelo ajustado se entrenó con la instrucción sola, sin glosario.
    override fun usaGlosario(): Boolean = !modelo.ajustado

    /**
     * El Gemma base traduce bien español ↔ inglés, pero no conoce Miskito ni Kriol. El modelo
     * ajustado con el corpus de Aikukisna (Release modelo-tuki-v2) traduce entre los cuatro.
     */
    override fun soporta(idiomaOrigenId: Int, idiomaDestinoId: Int): Boolean {
        val idiomas = if (modelo.ajustado) NOMBRES_AJUSTADO.keys else NOMBRES_BASE.keys
        return idiomaOrigenId in idiomas && idiomaDestinoId in idiomas && idiomaOrigenId != idiomaDestinoId
    }

    override suspend fun traducir(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int,
        glosario: List<String>
    ): String {
        if (!modelo.ajustado) return traducirBase(texto, idiomaOrigenId, idiomaDestinoId, glosario)
        // El corpus no tiene pares Miskito ↔ Kriol/Inglés: se pasa por el español, como en el diccionario.
        if (Pair(idiomaOrigenId, idiomaDestinoId) in SIN_PARES_DIRECTOS) {
            val intermedio = traducirAjustado(texto, idiomaOrigenId, ESPANOL)
            return traducirAjustado(intermedio, ESPANOL, idiomaDestinoId)
        }
        return traducirAjustado(texto, idiomaOrigenId, idiomaDestinoId)
    }

    /** Misma instrucción con la que se entrenó (scripts/preparar_dataset_gemma.py). */
    private suspend fun traducirAjustado(texto: String, origenId: Int, destinoId: Int): String {
        val instruccion = "Traduce del ${NOMBRES_AJUSTADO.getValue(origenId)} al ${NOMBRES_AJUSTADO.getValue(destinoId)}. " +
            "Responde solo con la traducción."
        val prompt = "<start_of_turn>user\n$instruccion\n\n${texto.take(600)}<end_of_turn>\n<start_of_turn>model\n"
        return generar(prompt, texto)
    }

    private suspend fun traducirBase(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int,
        glosario: List<String>
    ): String {
        val prompt = buildString {
            append("<start_of_turn>user\n")
            append("Traduce del ${NOMBRES_BASE.getValue(idiomaOrigenId)} al ${NOMBRES_BASE.getValue(idiomaDestinoId)}. ")
            append("Responde solo con la traducción, sin comillas ni explicaciones.")
            if (glosario.isNotEmpty()) {
                append("\nRespeta estas equivalencias:\n")
                glosario.take(12).forEach { append("- ").append(it).append('\n') }
            }
            append("\n\n").append(texto.take(1200))
            append("<end_of_turn>\n<start_of_turn>model\n")
        }
        return generar(prompt, texto)
    }

    private suspend fun generar(prompt: String, texto: String): String =
        motor.generar(prompt, temperatura = 0.2f, topK = 20, maxCaracteres = texto.length * 2 + 60, unaLinea = true)
            .removeSuffix("<end_of_turn>")
            .trim()
            .removeSurrounding("\"")
            .let { conservarNombres(texto, it) }
            .also { check(it.isNotBlank()) { "No se generó una traducción" } }

    /**
     * El modelo pequeño a veces cambia un nombre propio por otro ("Karla" → "Carlos"). Si la salida
     * trae un nombre en mayúscula que no estaba en el original y falta uno del original, se repone.
     */
    private fun conservarNombres(original: String, traduccion: String): String {
        fun nombres(texto: String) = texto.split(Regex("\\s+")).drop(1)
            .map { it.trim { c -> !c.isLetter() } }
            .filter { it.length > 1 && it.first().isUpperCase() }
        val delOriginal = nombres(original)
        val faltantes = delOriginal.filter { !traduccion.contains(it) }.toMutableList()
        if (faltantes.isEmpty()) return traduccion
        var resultado = traduccion
        nombres(traduccion).filter { it !in delOriginal }.forEach { intruso ->
            val reemplazo = faltantes.removeFirstOrNull() ?: return resultado
            resultado = resultado.replace(Regex("\\b${Regex.escape(intruso)}\\b"), reemplazo)
        }
        return resultado
    }

    private companion object {
        const val ESPANOL = 2
        val NOMBRES_BASE = mapOf(2 to "español", 4 to "inglés")
        // Deben coincidir con NOMBRES en scripts/preparar_dataset_gemma.py.
        val NOMBRES_AJUSTADO = mapOf(1 to "miskito", 2 to "español", 3 to "inglés kriol", 4 to "inglés")
        val SIN_PARES_DIRECTOS = setOf(1 to 3, 3 to 1, 1 to 4, 4 to 1)
    }
}
