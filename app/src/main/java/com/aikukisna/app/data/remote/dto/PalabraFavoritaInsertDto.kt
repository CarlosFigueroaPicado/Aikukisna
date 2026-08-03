package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PalabraFavoritaInsertDto(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("palabra_id") val palabraId: Int
)