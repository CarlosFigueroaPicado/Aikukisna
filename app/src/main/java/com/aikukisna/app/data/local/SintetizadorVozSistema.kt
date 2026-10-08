package com.aikukisna.app.data.local

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aikukisna.app.domain.repository.SintesisVozLocal
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Voz de Android (TextToSpeech) generada a un archivo WAV, sin conexión cuando el teléfono
 * tiene la voz del idioma instalada. Así la app reproduce el audio con el mismo AudioPlayer.
 */
@Singleton
class SintetizadorVozSistema @Inject constructor(
    @ApplicationContext private val context: Context
) : SintesisVozLocal {

    private val mutex = Mutex()
    private var tts: TextToSpeech? = null

    override suspend fun sintetizar(texto: String, codigoIdioma: String): ByteArray? = mutex.withLock {
        val motor = tts ?: inicializar()?.also { tts = it } ?: return@withLock null
        val idioma = when (codigoIdioma) {
            "en" -> Locale.US
            else -> Locale("es", "NI")
        }
        val disponibilidad = motor.setLanguage(idioma)
        if (disponibilidad == TextToSpeech.LANG_MISSING_DATA || disponibilidad == TextToSpeech.LANG_NOT_SUPPORTED) {
            return@withLock null
        }
        val archivo = withContext(Dispatchers.IO) { File.createTempFile("tts_", ".wav", context.cacheDir) }
        try {
            val completado = withTimeoutOrNull(TIEMPO_MAXIMO_MS) { generarArchivo(motor, texto, archivo) } == true
            if (!completado) return@withLock null
            withContext(Dispatchers.IO) { archivo.readBytes() }.takeIf { it.size > 44 }
        } finally {
            archivo.delete()
        }
    }

    private suspend fun inicializar(): TextToSpeech? = suspendCancellableCoroutine { continuacion ->
        var motor: TextToSpeech? = null
        motor = TextToSpeech(context.applicationContext) { estado ->
            if (continuacion.isActive) {
                continuacion.resume(if (estado == TextToSpeech.SUCCESS) motor else null)
            }
        }
        continuacion.invokeOnCancellation { motor.shutdown() }
    }

    private suspend fun generarArchivo(motor: TextToSpeech, texto: String, archivo: File): Boolean =
        suspendCancellableCoroutine { continuacion ->
            val id = UUID.randomUUID().toString()
            motor.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) = Unit
                override fun onDone(utteranceId: String?) {
                    if (utteranceId == id && continuacion.isActive) continuacion.resume(true)
                }
                @Deprecated("Requerido por la API anterior a nivel 21")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == id && continuacion.isActive) continuacion.resume(false)
                }
                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == id && continuacion.isActive) continuacion.resume(false)
                }
            })
            val resultado = motor.synthesizeToFile(texto, Bundle(), archivo, id)
            if (resultado != TextToSpeech.SUCCESS && continuacion.isActive) continuacion.resume(false)
        }

    private companion object {
        const val TIEMPO_MAXIMO_MS = 15_000L
    }
}
