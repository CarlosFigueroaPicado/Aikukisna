package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject

private val IDIOMA_POR_NOMBRE = mapOf(
    "español" to 2,
    "inglés" to 4,
    "ingles" to 4,
    "kriol" to 3
)

private const val PROMPT_TRANSCRIPCION =
    "Este audio tiene una persona hablando en Español, Inglés, o Kriol " +
            "(inglés criollo de la costa caribeña de Nicaragua). Identificá cuál " +
            "de los tres es, y transcribí exactamente lo que dice. Respondé " +
            "ÚNICAMENTE en este formato, sin texto adicional:\n" +
            "IDIOMA: <Español, Inglés, o Kriol>\n" +
            "TEXTO: <la transcripción>"

/**
 * Flujo completo para el micrófono único del Traductor: recibe el audio
 * grabado, sin saber de antemano en qué idioma habló el estudiante — Gemini
 * identifica el idioma y transcribe en el mismo paso, y de ahí se reutiliza
 * TraducirTextoUseCase (diccionario primero, Gemini de respaldo) para
 * llegar al idioma de destino que se pida — cualquiera de los 4, no solo
 * Miskito. El Traductor deja elegir destino; Tuki y Cámara le pasan acá
 * el idioma que el estudiante está aprendiendo (`usuario.idiomaMeta`).
 *
 * ADVERTENCIA para quien grabe el audio en la pantalla: Gemini no acepta
 * el contenedor M4A por defecto de Android. Grabar con
 * MediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS) —
 * produce audio/aac crudo, que sí está soportado. El M4A estándar
 * (MediaRecorder.OutputFormat.MPEG_4) no está en la lista de formatos
 * que acepta la Edge Function.
 *
 * Miskito no es una opción de *entrada* (hablada) acá a propósito — no hay
 * manera confiable de reconocerlo por voz con las herramientas disponibles
 * hoy (mismo motivo documentado en ResolverIdiomaReconocimientoVozUseCase).
 * Sí puede ser el idioma de *destino* — traducir de Español hablado hacia
 * Miskito escrito funciona igual que antes, solo que ahora también hacia
 * Kriol o Inglés si se pide.
 */
class TranscribirYTraducirAudioUseCase @Inject constructor(
    private val iaRepository: IaRepository,
    private val traducirTextoUseCase: TraducirTextoUseCase
) {
    suspend operator fun invoke(
        audioBase64: String,
        idiomaDestinoId: Int,
        audioMimeType: String = "audio/aac"
    ): ResultadoTraduccion {
        require(audioBase64.isNotBlank()) { "El audio no puede estar vacío" }

        val respuesta = iaRepository.preguntarConAudio(
            prompt = PROMPT_TRANSCRIPCION,
            audioBase64 = audioBase64,
            audioMimeType = audioMimeType
        )

        val (idiomaOrigenId, texto) = parsearRespuesta(respuesta)
            ?: error("No se pudo identificar el idioma ni transcribir el audio")

        return traducirTextoUseCase(
            texto = texto,
            idiomaOrigenId = idiomaOrigenId,
            idiomaDestinoId = idiomaDestinoId
        )
    }

    private fun parsearRespuesta(respuesta: String): Pair<Int, String>? {
        val lineaIdioma = respuesta.lineSequence().firstOrNull { it.startsWith("IDIOMA:", ignoreCase = true) }
        val lineaTexto = respuesta.lineSequence().firstOrNull { it.startsWith("TEXTO:", ignoreCase = true) }
            ?: return null

        val nombreIdioma = lineaIdioma
            ?.substringAfter(":")
            ?.trim()
            ?.lowercase()
            ?: return null

        val idiomaId = IDIOMA_POR_NOMBRE[nombreIdioma] ?: return null
        val texto = lineaTexto.substringAfter(":").trim()

        if (texto.isBlank()) return null

        return idiomaId to texto
    }
}