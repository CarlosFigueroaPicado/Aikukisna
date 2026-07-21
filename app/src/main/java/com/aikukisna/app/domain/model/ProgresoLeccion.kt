package com.aikukisna.app.domain.model

import java.time.Instant
import java.util.UUID

data class ProgresoLeccion(
    val usuarioId: UUID,
    val leccion: Leccion,
    val estado: String,
    val puntaje: Int?,
    val fechaCompletado: Instant?
)