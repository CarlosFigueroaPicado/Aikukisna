package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.repository.DiccionarioRepository
import javax.inject.Inject

data class RespuestaTukiLocal(
    val texto: String,
    val concluyente: Boolean,
    val conversacional: Boolean = false
)

class TukiOfflineResponder @Inject constructor(
    private val diccionarioRepository: DiccionarioRepository,
    private val traducirTextoUseCase: TraducirTextoUseCase
) {
    constructor(diccionarioRepository: DiccionarioRepository) : this(
        diccionarioRepository,
        TraducirTextoUseCase(diccionarioRepository)
    )

    suspend fun responder(
        mensaje: String,
        idiomaMetaId: Int,
        historial: List<MensajeChat> = emptyList()
    ): RespuestaTukiLocal {
        responderConversacionBasica(mensaje, idiomaMetaId, historial)?.let {
            return RespuestaTukiLocal(it, concluyente = true, conversacional = true)
        }
        val consulta = extraerConsulta(mensaje)
            ?: return responderConsultaLibre(mensaje, idiomaMetaId)
        DEFINICIONES_INSTITUCIONALES[normalizar(consulta.texto)]?.let {
            return RespuestaTukiLocal(
                "“${consulta.texto}” significa: $it. ¿Quieres consultar otra expresión?",
                true
            )
        }

        val idiomaOrigenId = if (consulta.esSignificado) {
            consulta.idiomaIndicado ?: idiomaMetaId
        } else {
            IDIOMA_ESPANOL
        }
        val idiomaDestinoId = if (consulta.esSignificado) {
            IDIOMA_ESPANOL
        } else {
            consulta.idiomaIndicado ?: idiomaMetaId
        }
        diccionarioRepository.buscarOracionExacta(consulta.texto, idiomaOrigenId, idiomaDestinoId)?.let {
            return RespuestaTukiLocal(presentarRespuesta(consulta, listOf(it), idiomaDestinoId), true)
        }
        val traducciones = traduccionesExactas(consulta.texto, idiomaOrigenId, idiomaDestinoId)
        if (traducciones.isNotEmpty()) {
            return RespuestaTukiLocal(presentarRespuesta(consulta, traducciones, idiomaDestinoId), true)
        }
        runCatching {
            traducirTextoUseCase(consulta.texto, idiomaOrigenId, idiomaDestinoId).texto
        }.getOrNull()?.let {
            return RespuestaTukiLocal(presentarRespuesta(consulta, listOf(it), idiomaDestinoId), true)
        }
        traducirDesdeCualquierIdioma(consulta.texto, idiomaDestinoId)?.let {
            return RespuestaTukiLocal(presentarRespuesta(consulta, listOf(it), idiomaDestinoId), true)
        }
        return buscarCoincidenciasRelacionadas(consulta.texto, idiomaMetaId)
            ?: RespuestaTukiLocal(
                "Busqué “${consulta.texto}” en el contenido local, pero no encontré una equivalencia verificada. Puedes escribir solo la palabra o indicar el idioma; por ejemplo: “¿qué significa naksa?” o “¿cómo se dice casa en miskito?”.",
                true
            )
    }

    suspend fun construirContexto(mensaje: String, idiomaMetaId: Int): List<String> {
        val palabras = mensaje.lowercase()
            .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
            .split(' ')
            .filter { it.length >= 2 }
            .distinct()
            .take(8)
        val idiomasConsulta = (listOf(idiomaMetaId, IDIOMA_ESPANOL) + IDIOMAS_SOPORTADOS).distinct()
        return palabras.flatMap { texto ->
            idiomasConsulta.flatMap { idiomaOrigen ->
                diccionarioRepository.buscarPalabras(texto, idiomaOrigen, 10)
                    .filter { normalizar(it.texto) == normalizar(texto) }
                    .flatMap { palabra -> diccionarioRepository.obtenerTraducciones(palabra.id) }
                    .map { traduccion ->
                        val origen = NOMBRES_IDIOMA[idiomaOrigen] ?: idiomaOrigen.toString()
                        val destinoId = traduccion.palabraDestino.idioma.id
                        val destino = NOMBRES_IDIOMA[destinoId] ?: destinoId.toString()
                        "${palabraConIdioma(texto, origen)} = ${traduccion.palabraDestino.texto} ($destino)"
                    }
            }
        }.distinct().take(12)
    }

    private fun palabraConIdioma(texto: String, idioma: String) = "$texto ($idioma)"

    private suspend fun traduccionesExactas(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): List<String> =
        diccionarioRepository.buscarPalabras(texto, idiomaOrigenId, 10)
            .filter { normalizar(it.texto) == normalizar(texto) }
            .flatMap { palabra ->
                diccionarioRepository.obtenerTraducciones(palabra.id)
                    .filter { it.palabraDestino.idioma.id == idiomaDestinoId }
                    .map { it.palabraDestino.texto }
            }.distinct()

    private suspend fun traducirDesdeCualquierIdioma(texto: String, idiomaDestinoId: Int): String? {
        val resultados = IDIOMAS_SOPORTADOS
            .filter { it != idiomaDestinoId }
            .mapNotNull { idiomaOrigenId ->
                runCatching {
                    traducirTextoUseCase(texto, idiomaOrigenId, idiomaDestinoId).texto
                }.getOrNull()
            }
            .distinctBy(::normalizar)
        return resultados.singleOrNull()
    }

    private suspend fun responderConsultaLibre(mensaje: String, idiomaMetaId: Int): RespuestaTukiLocal {
        val texto = mensaje.trim(' ', '?', '¿', '.', '!', '"', '\'')
        if (texto.isBlank()) return RespuestaTukiLocal(MENSAJE_GUIA, false)

        val equivalencias = mutableListOf<String>()
        for (idiomaOrigenId in IDIOMAS_SOPORTADOS) {
            val palabras = diccionarioRepository.buscarPalabras(texto, idiomaOrigenId, 10)
                .filter { normalizar(it.texto) == normalizar(texto) }
            for (palabra in palabras) {
                val traducciones = diccionarioRepository.obtenerTraducciones(palabra.id)
                val preferidas = traducciones.filter { traduccion ->
                    val destinoPreferido = if (idiomaOrigenId == idiomaMetaId) IDIOMA_ESPANOL else idiomaMetaId
                    traduccion.palabraDestino.idioma.id == destinoPreferido
                }.ifEmpty { traducciones }
                preferidas.take(4).forEach { traduccion ->
                    val origen = NOMBRES_IDIOMA[idiomaOrigenId] ?: idiomaOrigenId.toString()
                    val destinoId = traduccion.palabraDestino.idioma.id
                    val destino = NOMBRES_IDIOMA[destinoId] ?: destinoId.toString()
                    equivalencias += "${palabra.texto} ($origen) = ${traduccion.palabraDestino.texto} ($destino)"
                }
            }
        }
        val unicas = equivalencias.distinct().take(6)
        if (unicas.isNotEmpty()) {
            return RespuestaTukiLocal(
                "Encontré estas equivalencias verificadas para “$texto”:\n" +
                    unicas.joinToString("\n") { "• $it" } +
                    "\nPuedes preguntarme por otra palabra, una frase, una lección o un tema cultural.",
                true
            )
        }

        traducirDesdeCualquierIdioma(texto, idiomaMetaId)?.let { traduccion ->
            val idioma = NOMBRES_IDIOMA[idiomaMetaId] ?: "el idioma seleccionado"
            return RespuestaTukiLocal(
                "En $idioma, la traducción verificada de “$texto” es:\n• $traduccion\n¿Practicamos otra palabra o frase?",
                true
            )
        }

        // Sin datos exactos la plantilla guía no es una respuesta real: un motor generativo
        // (Gemma local o Gemini) puede contestar, usando las coincidencias como contexto.
        return buscarCoincidenciasRelacionadas(texto, idiomaMetaId)
            ?: RespuestaTukiLocal(MENSAJE_GUIA, false)
    }

    private suspend fun buscarCoincidenciasRelacionadas(
        texto: String,
        idiomaMetaId: Int
    ): RespuestaTukiLocal? {
        val terminos = normalizar(texto)
            .split(' ')
            .filter { it.length >= 3 && it !in PALABRAS_VACIAS }
            .distinct()
            .take(4)
        if (terminos.isEmpty()) return null

        val resultados = mutableListOf<String>()
        for (termino in terminos) {
            for (idiomaOrigenId in IDIOMAS_SOPORTADOS) {
                val coincidencias = diccionarioRepository.buscarPalabras(termino, idiomaOrigenId, 5)
                    .filter { normalizar(it.texto).contains(termino) }
                for (palabra in coincidencias) {
                    val traducciones = diccionarioRepository.obtenerTraducciones(palabra.id)
                    val destinoPreferido = if (idiomaOrigenId == idiomaMetaId) IDIOMA_ESPANOL else idiomaMetaId
                    traducciones.filter { it.palabraDestino.idioma.id == destinoPreferido }
                        .take(2)
                        .forEach { traduccion ->
                            resultados += "${palabra.texto} = ${traduccion.palabraDestino.texto}"
                        }
                }
            }
        }
        val unicos = resultados.distinct().take(5)
        if (unicos.isEmpty()) return null
        return RespuestaTukiLocal(
            "No encontré una coincidencia exacta, pero sí estas entradas verificadas relacionadas:\n" +
                unicos.joinToString("\n") { "• $it" } +
                "\nEscribe una de ellas si quieres revisar su significado.",
            false
        )
    }

    private fun presentarRespuesta(consulta: Consulta, resultados: List<String>, idiomaDestinoId: Int): String {
        val valores = resultados.joinToString("\n") { "• $it" }
        return if (consulta.esSignificado) {
            "Encontré ${resultados.size} ${if (resultados.size == 1) "significado verificado" else "significados verificados"} para “${consulta.texto}”:\n$valores\n¿Quieres consultar otra palabra?"
        } else {
            val idioma = NOMBRES_IDIOMA[idiomaDestinoId] ?: "el idioma seleccionado"
            "En $idioma, encontré esta traducción verificada para “${consulta.texto}”:\n$valores\n¿Quieres practicar otra palabra o frase?"
        }
    }

    private fun responderConversacionBasica(
        mensaje: String,
        idiomaMetaId: Int,
        historial: List<MensajeChat>
    ): String? {
        val texto = normalizar(mensaje).trim(' ', '?', '¿', '.', '!')
        if (texto in setOf("hola", "buenos dias", "buenas tardes", "buenas noches", "hey", "saludos") ||
            texto.startsWith("hola ")) {
            val idioma = NOMBRES_IDIOMA[idiomaMetaId] ?: "tu idioma"
            return "¡Hola! Estoy contigo para aprender $idioma. Podemos traducir una palabra o frase, revisar una lección, consultar tu progreso o conversar sobre cultura. ¿Con qué empezamos?"
        }
        if (texto in setOf("como estas", "que tal", "como te va")) {
            return "Estoy listo para acompañarte. Podemos empezar con una palabra, continuar una lección o explorar un tema cultural. ¿Qué te gustaría practicar?"
        }
        if (texto in setOf("bien", "muy bien", "todo bien")) {
            return "Me alegra. Aprovechemos para avanzar: escribe una palabra, una frase o dime “quiero aprender”."
        }
        if (texto in setOf("no se", "no se que hacer", "no se por donde empezar")) {
            return "Empecemos juntos. Puedo elegir tu siguiente lección, traducir una palabra o mostrarte temas culturales verificados. Escribe “quiero aprender” y te guío."
        }
        if (texto in setOf("gracias", "muchas gracias", "te agradezco")) {
            return "Con gusto. Sigo aquí contigo. Podemos practicar otra palabra, revisar una lección o explorar un tema cultural."
        }
        if (texto in setOf("adios", "hasta luego", "nos vemos")) {
            return "Hasta luego. Cuando regreses, continuamos desde aquí."
        }
        if (CAPACIDADES.any(texto::contains) || texto.contains("quien eres") || texto == "ayuda" || texto == "ayudame") {
            return mensajeCapacidades(idiomaMetaId)
        }
        if (texto.contains("leccion") && AYUDA_LECCIONES.any(texto::contains)) {
            return "Claro. Puedo ayudarte a continuar tus lecciones, explicarte qué estudiar y practicar el vocabulario verificado. Dime “quiero aprender” para revisar la siguiente lección disponible."
        }
        if (texto.contains("quiero practicar") || texto.contains("practiquemos") || texto.contains("ayudame a aprender")) {
            val idioma = NOMBRES_IDIOMA[idiomaMetaId] ?: "el idioma seleccionado"
            return "Claro. Practiquemos $idioma. Escribe una palabra o frase, o dime “¿cómo se dice casa?” para buscarla en el diccionario local."
        }
        if (texto in setOf("si", "sí", "claro", "de acuerdo", "esta bien", "ok")) {
            val respuestaAnterior = historial.asReversed()
                .firstOrNull { it.rol == RolChat.TUKI }
                ?.texto
                .orEmpty()
            return when {
                respuestaAnterior.contains("otra palabra", ignoreCase = true) ->
                    "Perfecto. Escribe la palabra que quieres consultar y buscaré sus equivalencias verificadas."
                respuestaAnterior.contains("otra palabra o frase", ignoreCase = true) ->
                    "Perfecto. Escribe la palabra o frase que quieres practicar."
                else -> mensajeCapacidades(idiomaMetaId)
            }
        }
        if (texto.contains("no entiendo") || texto.contains("explica de nuevo") || texto == "repite") {
            val respuestaAnterior = historial.asReversed()
                .firstOrNull { it.rol == RolChat.TUKI }
                ?.texto
                ?.takeIf { it.isNotBlank() }
            return if (respuestaAnterior != null) {
                "Vamos paso a paso. Mi respuesta anterior fue:\n$respuestaAnterior\nDime qué palabra o parte quieres revisar."
            } else {
                mensajeCapacidades(idiomaMetaId)
            }
        }
        return null
    }

    private fun mensajeCapacidades(idiomaMetaId: Int): String {
        val idioma = NOMBRES_IDIOMA[idiomaMetaId] ?: "el idioma seleccionado"
        return "¡Puedo acompañarte como tu maestro interino de $idioma! " +
            "Sin conexión puedo buscar palabras y expresiones verificadas, traducir el contenido disponible entre español y $idioma, " +
            "mostrar pronunciaciones que hayan sido validadas, ayudarte a continuar tus lecciones, revisar tu progreso y consultar la información cultural guardada en la aplicación. " +
            "También recuerdo el tema de nuestra conversación para que podamos avanzar paso a paso. ¿Quieres comenzar con una palabra, una lección o un tema cultural?"
    }

    private fun extraerConsulta(mensaje: String): Consulta? {
        val limpio = mensaje.trim(' ', '?', '¿', '.', '!')
        val significado = listOf("qué significa", "que significa", "cuál es el significado de", "cual es el significado de", "significado de", "qué quiere decir", "que quiere decir")
        val traduccion = listOf("cómo se dice", "como se dice", "cómo digo", "como digo", "dime cómo se dice", "dime como se dice", "traduce", "traducir", "traducción de", "traduccion de", "quiero traducir")
        significado.firstOrNull { limpio.contains(it, true) }?.let {
            val resto = limpio.substringDespuesDe(it)
            return Consulta(resto.limpiarConsulta(), true, idiomaIndicado(resto)).takeIf { valor -> valor.texto.isNotBlank() }
        }
        traduccion.firstOrNull { limpio.contains(it, true) }?.let {
            val resto = limpio.substringDespuesDe(it)
            return Consulta(resto.limpiarConsulta(), false, idiomaIndicado(resto)).takeIf { valor -> valor.texto.isNotBlank() }
        }
        return null
    }

    private fun String.limpiarConsulta(): String = trim(' ', ':', ',', '"', '\'', '?', '¿', '.', '!')
        .replace(Regex("\\s+(en|al)\\s+(miskito|español|inglés kriol|ingles kriol|inglés estándar|ingles estandar|inglés|ingles)$", RegexOption.IGNORE_CASE), "")
        .trim()

    private fun String.substringDespuesDe(fragmento: String): String {
        val indice = indexOf(fragmento, ignoreCase = true)
        return if (indice < 0) this else substring(indice + fragmento.length)
    }

    private fun normalizar(texto: String): String = java.text.Normalizer
        .normalize(texto.trim().lowercase(), java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("[\\p{P}\\p{S}]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    private fun idiomaIndicado(texto: String): Int? {
        val normalizado = normalizar(texto)
        return NOMBRES_ALTERNATIVOS_IDIOMA.entries.firstOrNull { (nombre, _) ->
            normalizado.endsWith(" en $nombre") || normalizado.endsWith(" al $nombre")
        }?.value
    }

    private data class Consulta(
        val texto: String,
        val esSignificado: Boolean,
        val idiomaIndicado: Int?
    )

    companion object {
        const val IDIOMA_ESPANOL = 2
        const val MENSAJE_GUIA = "Estoy aquí para ayudarte a avanzar con información verificada. Puedes escribir una palabra directamente, pedirme una traducción, decir “quiero aprender”, preguntar por tu siguiente lección o consultar un tema cultural."
        val DEFINICIONES_INSTITUCIONALES = mapOf("aikukisna" to "Juntos somos más", "yawansa tech" to "Nosotros somos tecnología")
        val NOMBRES_IDIOMA = mapOf(1 to "Miskito", 2 to "Español", 3 to "Inglés Kriol", 4 to "Inglés Estándar")
        val IDIOMAS_SOPORTADOS = NOMBRES_IDIOMA.keys
        val NOMBRES_ALTERNATIVOS_IDIOMA = mapOf(
            "miskito" to 1,
            "espanol" to 2,
            "ingles kriol" to 3,
            "ingles estandar" to 4,
            "ingles" to 4
        )
        val PALABRAS_VACIAS = setOf(
            "como", "cual", "donde", "cuando", "quien", "quiero", "puedes", "podrias",
            "ayuda", "ayudame", "decir", "dime", "sobre", "para", "esta", "este", "esto",
            "miskito", "espanol", "ingles", "kriol", "traduce", "significa"
        )
        val CAPACIDADES = listOf(
            "que puedes hacer", "que mas puedes hacer", "para que sirves",
            "en que me puedes ayudar", "como puedes ayudarme", "que sabes hacer"
        )
        val AYUDA_LECCIONES = listOf("ayudar", "ayuda", "guiar", "explicar", "continuar")
    }
}
