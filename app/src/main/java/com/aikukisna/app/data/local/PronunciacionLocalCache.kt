package com.aikukisna.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import com.aikukisna.app.domain.model.ResultadoPronunciacion
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.domain.repository.PronunciationStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PronunciacionLocalCache @Inject constructor(
    @ApplicationContext private val context: Context
) : PronunciationStorage {
    private val directorio = File(context.filesDir, "pronunciaciones").apply { mkdirs() }
    private val pronunciacionesIncluidas by lazy {
        context.assets.list(DIRECTORIO_ASSETS).orEmpty().toSet()
    }

    override suspend fun leer(solicitud: SolicitudPronunciacion): ResultadoPronunciacion? = withContext(Dispatchers.IO) {
        clavesTexto(solicitud.texto).forEach { clave ->
            archivo(clave, solicitud.idioma.codigo, solicitud.voiceId)
                .takeIf { it.isFile && it.length() > 0 }
                ?.readBytes()
                ?.let { return@withContext ResultadoPronunciacion.AudioCacheado(it) }

            val nombre = "${PronunciationCacheKey.crear(clave, solicitud.idioma.codigo, solicitud.voiceId)}.wav"
            if (nombre in pronunciacionesIncluidas) {
                return@withContext runCatching {
                    context.assets.open("$DIRECTORIO_ASSETS/$nombre").use {
                        ResultadoPronunciacion.AudioLocal(it.readBytes())
                    }
                }.getOrNull()
            }

        }
        null
    }

    override suspend fun guardar(solicitud: SolicitudPronunciacion, audio: ByteArray) = withContext(Dispatchers.IO) {
        if (audio.isEmpty()) return@withContext
        val destino = archivo(solicitud.texto, solicitud.idioma.codigo, solicitud.voiceId)
        val temporal = File(directorio, "${destino.name}.tmp")
        temporal.writeBytes(audio)
        if (!temporal.renameTo(destino)) {
            temporal.delete()
            error("No se pudo guardar la pronunciación local")
        }
    }

    private fun archivo(texto: String, idiomaCodigo: String, voiceId: String?): File {
        return File(directorio, "${PronunciationCacheKey.crear(texto, idiomaCodigo, voiceId)}.mp3")
    }

    private fun clavesTexto(texto: String): List<String> {
        val exacta = texto.trim()
        val sinPuntuacion = exacta.trim { caracter ->
            caracter.isWhitespace() || caracter in PUNTUACION_EXTERIOR
        }
        return listOf(
            exacta,
            sinPuntuacion,
            sinPuntuacion.lowercase(),
            sinPuntuacion.replaceFirstChar { it.titlecase() }
        ).filter { it.isNotBlank() }.distinct()
    }

    private companion object {
        const val DIRECTORIO_ASSETS = "pronunciaciones"
        val PUNTUACION_EXTERIOR = setOf('¡', '!', '¿', '?', '.', ',', ';', ':', '…', '"', '\'', '“', '”')
    }
}

object PronunciationCacheKey {
    fun crear(texto: String, idiomaCodigo: String, voiceId: String?): String {
        val clave = "${idiomaCodigo.trim().lowercase()}|${voiceId.orEmpty()}|${texto.trim()}"
        return MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

}
