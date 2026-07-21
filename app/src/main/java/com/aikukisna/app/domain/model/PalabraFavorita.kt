package com.aikukisna.app.domain.model

import java.util.UUID

data class PalabraFavorita(
    val usuarioId: UUID,
    val palabra: Palabra
)