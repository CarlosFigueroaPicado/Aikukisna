package com.aikukisna.app.domain.model

data class Traduccion(
    val id: Int,
    val palabraOrigen: Palabra,
    val palabraDestino: Palabra,
    val nota: String?
)