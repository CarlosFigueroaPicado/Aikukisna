package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.ResultadoPronunciacion
import com.aikukisna.app.domain.model.SolicitudPronunciacion

interface PronunciationEngine {
    suspend fun resolver(solicitud: SolicitudPronunciacion): ResultadoPronunciacion
}

interface PronunciationStorage {
    suspend fun leer(solicitud: SolicitudPronunciacion): ResultadoPronunciacion?
    suspend fun guardar(solicitud: SolicitudPronunciacion, audio: ByteArray)
}

fun interface NetworkAvailability {
    fun hayConexion(): Boolean
}

/** Voz del sistema que funciona sin conexión; devuelve null si el idioma no tiene voz instalada. */
fun interface SintesisVozLocal {
    suspend fun sintetizar(texto: String, codigoIdioma: String): ByteArray?
}
