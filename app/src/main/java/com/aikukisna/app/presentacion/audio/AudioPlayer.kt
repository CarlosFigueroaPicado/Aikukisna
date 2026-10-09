package com.aikukisna.app.presentacion.audio

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null
    private var archivoTemporal: File? = null

    suspend fun reproducir(audio: ByteArray) {
        liberar()
        if (audio.isEmpty()) return

        val esWav = audio.size >= 4 &&
            audio[0] == 'R'.code.toByte() &&
            audio[1] == 'I'.code.toByte() &&
            audio[2] == 'F'.code.toByte() &&
            audio[3] == 'F'.code.toByte()
        val archivo = withContext(Dispatchers.IO) {
            File.createTempFile(
                "pronunciacion_",
                if (esWav) ".wav" else ".mp3",
                context.cacheDir
            ).apply { writeBytes(audio) }
        }
        val nuevoPlayer = MediaPlayer()
        archivoTemporal = archivo
        player = nuevoPlayer

        runCatching {
            nuevoPlayer.setDataSource(archivo.absolutePath)
            nuevoPlayer.setOnCompletionListener { liberar() }
            nuevoPlayer.setOnErrorListener { _, _, _ ->
                liberar()
                true
            }
            nuevoPlayer.prepare()
            nuevoPlayer.start()
        }.onFailure { liberar() }
    }

    fun liberar() {
        player?.runCatching { stop() }
        player?.release()
        player = null
        archivoTemporal?.delete()
        archivoTemporal = null
    }
}
