package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PalabraFavoritaDto(
    @SerialName("usuario_id") val usuarioId: String,
    val palabra: PalabraDto
)