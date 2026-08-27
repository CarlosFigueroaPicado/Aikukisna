package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.MensajeChat


interface IaRepository {

    suspend fun preguntar(prompt: String): String


    suspend fun conversar(historial: List<MensajeChat>, contexto: String? = null): String


    suspend fun preguntarConImagen(
        prompt: String,
        imagenBase64: String,
        mimeType: String = "image/jpeg"
    ): String
}