package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.IaRepository
import com.aikukisna.app.data.local.ConectividadHelper
import com.aikukisna.app.data.local.OfflineSpeechRecognizer
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

private val NOMBRE_IDIOMA_VOZ = mapOf(
    "mi" to "Miskito",
    "es" to "Español",
    Idioma.CODIGO_KRIOL to "Inglés Kriol",
    "en" to "Inglés Estándar"
)

class TranscribirAudioUseCase @Inject constructor(
    private val iaRepository: IaRepository,
    private val offlineSpeechRecognizer: OfflineSpeechRecognizer,
    private val conectividad: ConectividadHelper
) {
    suspend operator fun invoke(
        audioBase64: String,
        audioMimeType: String = "audio/aac",
        idiomaCodigo: String = "es"
    ): String {
        require(audioBase64.isNotBlank()) { "El audio no puede estar vacío" }
        val codigo = if (Idioma.esKriol(idiomaCodigo)) Idioma.CODIGO_KRIOL else idiomaCodigo

        if (codigo == "es" || codigo == "en") {
            offlineSpeechRecognizer.transcribir(audioBase64, codigo)
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
        }

        if (conectividad.hayConexion()) {
            try {
                val idioma = NOMBRE_IDIOMA_VOZ[codigo] ?: error("Idioma no compatible")
                return iaRepository.preguntarConAudio(
                    prompt = "Transcribí exactamente este audio hablado en $idioma. " +
                            "Respondé únicamente con la transcripción, sin explicaciones ni comillas.",
                    audioBase64 = audioBase64,
                    audioMimeType = audioMimeType
                ).trim().removeSurrounding("\"")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Continuar con reconocimiento local si la llamada remota falla.
            }
        }

        if (codigo == "mi" || codigo == Idioma.CODIGO_KRIOL) {
            // No existe un modelo acústico de Miskito ni de Kriol: sin conexión se usa el más
            // cercano (Kriol es de base inglesa; la ortografía miskita es fonética como el español).
            // El resultado es aproximado y la pantalla pide revisarlo antes de traducir.
            val modeloCercano = if (codigo == Idioma.CODIGO_KRIOL) "en" else "es"
            offlineSpeechRecognizer.transcribir(audioBase64, modeloCercano)
                ?.takeIf { it.isNotBlank() }
                ?.let { return it }
            val idioma = NOMBRE_IDIOMA_VOZ.getValue(codigo)
            error("No se entendió el audio en $idioma. Intenta hablar más despacio o escribe la frase")
        }

        error("No se pudo reconocer el audio sin conexión")
    }
}
