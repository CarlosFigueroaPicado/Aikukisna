package com.aikukisna.app.data.local

import com.aikukisna.app.domain.model.Idioma
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var reconocedor: SpeechRecognizer? = null
    private var escuchando = false

    fun iniciar(
        codigoIdioma: String,
        alResultado: (String) -> Unit,
        alError: (String) -> Unit,
        alCambiarEstado: (Boolean) -> Unit
    ) {
        if (escuchando) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            alError("El reconocimiento de voz no está disponible en este dispositivo")
            return
        }
        liberar()
        reconocedor = SpeechRecognizer.createSpeechRecognizer(context).also { speechRecognizer ->
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit

                override fun onResults(results: Bundle?) {
                    val texto = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull().orEmpty().trim()
                    finalizar(alCambiarEstado)
                    if (texto.isNotEmpty()) alResultado(texto) else alError("No se reconoció ninguna palabra")
                }

                override fun onError(error: Int) {
                    finalizar(alCambiarEstado)
                    alError(mensajeError(error))
                }
            })
        }
        val configuracion = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale(codigoIdioma))
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        escuchando = true
        alCambiarEstado(true)
        reconocedor?.startListening(configuracion)
    }

    fun detener(alCambiarEstado: (Boolean) -> Unit = {}) {
        if (!escuchando) return
        reconocedor?.stopListening()
        escuchando = false
        alCambiarEstado(false)
    }

    fun liberar() {
        reconocedor?.destroy()
        reconocedor = null
        escuchando = false
    }

    private fun finalizar(alCambiarEstado: (Boolean) -> Unit) {
        escuchando = false
        alCambiarEstado(false)
        reconocedor?.destroy()
        reconocedor = null
    }

    private fun locale(codigo: String): String = when (codigo) {
        "en" -> "en-US"
        // No hay reconocedor de Kriol; el inglés jamaicano es el más cercano disponible.
        Idioma.CODIGO_KRIOL, "jam" -> "en-JM"
        "mi" -> "mi-NI"
        else -> "es-NI"
    }

    private fun mensajeError(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO -> "No se pudo procesar el audio"
        SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció ninguna palabra"
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "No se pudo completar el reconocimiento de voz"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "El micrófono está ocupado"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Falta permiso para usar el micrófono"
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> "El reconocimiento no está disponible para este idioma"
        else -> "No se pudo reconocer la voz"
    }
}
