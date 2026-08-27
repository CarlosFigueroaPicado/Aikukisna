package com.aikukisna.app.domain.repository

interface VozRepository {

    suspend fun sintetizarVoz(texto: String, voiceId: String? = null): ByteArray
}