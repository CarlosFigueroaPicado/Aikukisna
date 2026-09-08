package com.aikukisna.app.data.local

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val models = mutableMapOf<String, Model>()

    suspend fun transcribir(audioBase64: String, idiomaCodigo: String = "es"): String? = withContext(Dispatchers.Default) {
        runCatching {
            val audioFile = File.createTempFile("aikukisna_audio", ".aac", context.cacheDir)
            try {
                FileOutputStream(audioFile).use { output ->
                    output.write(Base64.decode(audioBase64.substringAfter(","), Base64.DEFAULT))
                }
                val pcm = decodificarAac(audioFile)
                if (pcm.bytes.isEmpty()) return@runCatching null
                val recognizer = Recognizer(obtenerModelo(idiomaCodigo), pcm.sampleRate.toFloat())
                recognizer.use {
                    it.acceptWaveForm(pcm.bytes, pcm.bytes.size)
                    JSONObject(it.finalResult).optString("text").trim().ifBlank { null }
                }
            } finally {
                audioFile.delete()
            }
        }.getOrNull()
    }

    private fun obtenerModelo(idiomaCodigo: String): Model {
        val codigo = if (idiomaCodigo == "en") "en" else "es"
        return models[codigo] ?: copyModel(codigo).also { models[codigo] = it }
    }

    private fun copyModel(codigo: String): Model {
        val assetName = if (codigo == "en") "vosk-model-en" else "vosk-model-es"
        val destination = File(context.filesDir, assetName)
        if (!File(destination, "am/final.mdl").exists()) {
            copyAssetDirectory(assetName, destination)
        }
        return Model(destination.absolutePath)
    }

    private fun copyAssetDirectory(assetPath: String, destination: File) {
        destination.mkdirs()
        context.assets.list(assetPath).orEmpty().forEach { child ->
            val source = "$assetPath/$child"
            val target = File(destination, child)
            if (context.assets.list(source).orEmpty().isNotEmpty()) {
                copyAssetDirectory(source, target)
            } else {
                context.assets.open(source).use { input -> target.outputStream().use(input::copyTo) }
            }
        }
    }

    private data class PcmAudio(val bytes: ByteArray, val sampleRate: Int)

    private fun decodificarAac(file: File): PcmAudio {
        val extractor = MediaExtractor()
        extractor.setDataSource(file.absolutePath)
        val track = (0 until extractor.trackCount).firstOrNull { index ->
            extractor.getTrackFormat(index).getString("mime")?.startsWith("audio/") == true
        } ?: error("El audio no contiene una pista compatible")
        extractor.selectTrack(track)
        val format = extractor.getTrackFormat(track)
        val mime = format.getString("mime") ?: error("Formato de audio inválido")
        val sampleRate = format.getInteger("sample-rate")
        val codec = MediaCodec.createDecoderByType(mime)
        val output = java.io.ByteArrayOutputStream()
        var terminado = false
        var entradaTerminada = false
        try {
            codec.configure(format, null, null, 0)
            codec.start()
            val info = MediaCodec.BufferInfo()
            while (!terminado) {
                if (!entradaTerminada) {
                    val inputIndex = codec.dequeueInputBuffer(10_000)
                    if (inputIndex >= 0) {
                        val buffer = codec.getInputBuffer(inputIndex) ?: continue
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            codec.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            entradaTerminada = true
                        } else {
                            codec.queueInputBuffer(inputIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }
                val outputIndex = codec.dequeueOutputBuffer(info, 10_000)
                if (outputIndex >= 0) {
                    val buffer: ByteBuffer? = codec.getOutputBuffer(outputIndex)
                    if (buffer != null && info.size > 0) {
                        val bytes = ByteArray(info.size)
                        buffer.position(info.offset)
                        buffer.get(bytes)
                        output.write(bytes)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) terminado = true
                }
            }
        } finally {
            codec.stop()
            codec.release()
            extractor.release()
        }
        return PcmAudio(output.toByteArray(), sampleRate)
    }
}
