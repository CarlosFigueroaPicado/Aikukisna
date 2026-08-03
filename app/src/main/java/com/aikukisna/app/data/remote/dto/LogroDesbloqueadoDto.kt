package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogroDesbloqueadoDto(
    @SerialName("usuario_id") val usuarioId: String,
    val logro: LogroDto,
    val fecha: String
)