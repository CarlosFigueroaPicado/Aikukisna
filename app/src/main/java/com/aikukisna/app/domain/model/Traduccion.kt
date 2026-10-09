package com.aikukisna.app.domain.model

data class Traduccion(
    val id: Int,
    val palabraOrigen: Palabra,
    val palabraDestino: Palabra,
    val nota: String?,
    val estadoValidacion: String = "importada",
    val fuenteId: Int? = null,
    val nivelConfianza: Double? = null,
    val esPreferida: Boolean = false,
    val updatedAtEpochMs: Long = 0
)