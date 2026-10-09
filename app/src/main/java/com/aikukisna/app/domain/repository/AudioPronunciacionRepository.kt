package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.AudioPronunciacion
import com.aikukisna.app.domain.model.Idioma

interface AudioPronunciacionRepository {
    suspend fun buscarVerificados(palabraId: Int, idioma: Idioma): List<AudioPronunciacion>
    /** Grabaciones humanas verificadas de esa forma escrita, cuando la pantalla no conoce el id de la palabra. */
    suspend fun buscarHumanosPorTexto(texto: String, idioma: Idioma): List<AudioPronunciacion> = emptyList()
    suspend fun registrar(audio: AudioPronunciacion)
    suspend fun estaDisponibleOffline(audio: AudioPronunciacion): Boolean
    suspend fun leerAudio(audio: AudioPronunciacion): ByteArray?
}
