package com.aikukisna.app.domain.model

import java.time.Instant
import java.util.UUID

data class MemoriaTuki(
    val id: Int,
    val usuarioId: UUID,
    val tipo: String,
    val resumen: String,
    val fecha: Instant
)