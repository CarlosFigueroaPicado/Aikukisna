package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.domain.repository.TraductorAutomaticoLocal
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * Traductor de la pantalla de traducción y del modo conversación. Funciona sin conexión:
 * 1. Frase, expresión o palabra verificada ([TraducirTextoUseCase], la política estricta).
 * 2. Español ↔ inglés con el modelo local, guiado por el glosario verificado.
 * 3. Composición literal con el diccionario, marcando las palabras sin equivalencia.
 * Los pasos 2 y 3 se etiquetan para que nunca se confundan con contenido validado.
 */
class TraducirOracionUseCase @Inject constructor(
    private val traducirTexto: TraducirTextoUseCase,
    private val traductorLocal: TraductorAutomaticoLocal
) {
    suspend operator fun invoke(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): ResultadoTraduccion {
        require(texto.isNotBlank()) { "El texto a traducir no puede estar vacío" }
        val limpio = texto.trim()
        val errorEstricto = try {
            return traducirTexto(limpio, idiomaOrigenId, idiomaDestinoId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e
        }

        val generativo = traductorLocal.soporta(idiomaOrigenId, idiomaDestinoId) && traductorLocal.disponible()
        val tokens = tokenizar(limpio)
        // Una sola palabra sin equivalencia: el mensaje estricto (con sugerencias) es más útil.
        if (tokens.size <= 1 && !generativo) throw errorEstricto

        // En frases muy cortas el modelo inventa más de lo que ayuda ("yang yapti" → "es mi hijo"):
        // si el diccionario conoce cada palabra, se prefiere la versión literal documentada.
        val segmentosCortos = if (tokens.size <= MAX_PALABRAS_LITERAL_PRIMERO) componer(tokens, idiomaOrigenId, idiomaDestinoId) else null
        if (segmentosCortos != null && segmentosCortos.all { it.destino != null }) {
            return literal(segmentosCortos)
        }

        if (generativo) {
            // Armar el glosario consulta el diccionario fragmento por fragmento: solo si el modelo lo usa.
            val glosario = if (traductorLocal.usaGlosario()) {
                componer(tokens, idiomaOrigenId, idiomaDestinoId)
                    .filter { it.destino != null }
                    .map { "${it.origen} = ${it.destino}" }
            } else emptyList()
            val automatica = try {
                traductorLocal.traducir(limpio, idiomaOrigenId, idiomaDestinoId, glosario)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
            if (automatica != null && !pareceDefectuosa(automatica, tokens.size)) {
                return ResultadoTraduccion(
                    texto = automatica,
                    fuente = FuenteTraduccion.IA,
                    tipo = TipoTraduccion.AUTOMATICA,
                    nota = "Traducción automática sin conexión. Úsala para entender; no está validada por el equipo lingüístico."
                )
            }
        }

        val segmentos = segmentosCortos ?: componer(tokens, idiomaOrigenId, idiomaDestinoId)
        if (segmentos.none { it.destino != null }) throw errorEstricto
        return literal(segmentos)
    }

    /**
     * El modelo a veces imita las entradas del diccionario ("es mi hijo (es mi hijo)", "este (es)")
     * o devuelve una o dos palabras para una oración: eso no es una traducción.
     */
    private fun pareceDefectuosa(salida: String, palabrasEntrada: Int): Boolean =
        '(' in salida || tokenizar(salida).size * 3 < palabrasEntrada

    private fun literal(segmentos: List<Segmento>): ResultadoTraduccion {
        val desconocidas = segmentos.filter { it.destino == null }.map { it.origen }
        return ResultadoTraduccion(
            texto = segmentos.joinToString(" ") { it.destino ?: "[${it.origen}]" },
            fuente = FuenteTraduccion.DICCIONARIO,
            tipo = TipoTraduccion.LITERAL,
            nota = buildString {
                append("Traducción literal palabra por palabra con el diccionario; el orden de la oración puede no ser natural.")
                if (desconocidas.isNotEmpty()) append(" Sin equivalencia: ${desconocidas.joinToString()}.")
            }
        )
    }

    /** Busca primero el fragmento más largo (hasta 4 palabras) para respetar expresiones del corpus. */
    private suspend fun componer(tokens: List<String>, origen: Int, destino: Int): List<Segmento> {
        val segmentos = mutableListOf<Segmento>()
        var indice = 0
        while (indice < tokens.size) {
            var encontrado: Segmento? = null
            for (largo in minOf(MAX_PALABRAS_FRAGMENTO, tokens.size - indice) downTo 1) {
                val fragmento = tokens.subList(indice, indice + largo).joinToString(" ")
                val traduccion = traducirFragmento(fragmento, origen, destino) ?: continue
                encontrado = Segmento(fragmento, traduccion, largo)
                break
            }
            segmentos += encontrado ?: Segmento(tokens[indice], null, 1)
            indice += encontrado?.palabras ?: 1
        }
        return segmentos
    }

    private suspend fun traducirFragmento(fragmento: String, origen: Int, destino: Int): String? = try {
        val resultado = traducirTexto(fragmento, origen, destino)
        // En palabras ambiguas se toma la primera acepción registrada.
        resultado.alternativas.firstOrNull()?.substringBefore(" — ") ?: resultado.texto
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    }

    private fun tokenizar(texto: String): List<String> = texto
        .split(Regex("\\s+"))
        .map { palabra -> palabra.trim { !it.isLetterOrDigit() && it != '\'' && it != '-' } }
        .filter { NormalizadorLinguistico.normalizar(it).isNotBlank() }

    private data class Segmento(val origen: String, val destino: String?, val palabras: Int)

    private companion object {
        const val MAX_PALABRAS_FRAGMENTO = 4
        const val MAX_PALABRAS_LITERAL_PRIMERO = 3
    }
}
