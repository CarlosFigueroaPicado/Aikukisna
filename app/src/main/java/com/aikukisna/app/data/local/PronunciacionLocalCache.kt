package com.aikukisna.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PronunciacionLocalCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val directorio = File(context.filesDir, "pronunciaciones").apply { mkdirs() }

    fun leer(texto: String, voiceId: String?): ByteArray? = archivo(texto, voiceId)
        .takeIf { it.isFile && it.length() > 0 }
        ?.readBytes()

    fun contiene(texto: String, voiceId: String?): Boolean = archivo(texto, voiceId).let { it.isFile && it.length() > 0 }

    fun guardar(texto: String, voiceId: String?, audio: ByteArray) {
        if (audio.isEmpty()) return
        val destino = archivo(texto, voiceId)
        val temporal = File(directorio, "${destino.name}.tmp")
        temporal.writeBytes(audio)
        if (!temporal.renameTo(destino)) {
            temporal.delete()
            error("No se pudo guardar la pronunciación local")
        }
    }

    private fun archivo(texto: String, voiceId: String?): File {
        val clave = "${voiceId.orEmpty()}|${texto.trim()}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(directorio, "$hash.mp3")
    }
}
