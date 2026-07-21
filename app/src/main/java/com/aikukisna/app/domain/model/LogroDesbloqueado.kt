package com.aikukisna.app.domain.model

import java.time.Instant
import java.util.UUID

data class LogroDesbloqueado(
    val usuarioId: UUID,
    val logro: Logro,
    val fecha: Instant
)