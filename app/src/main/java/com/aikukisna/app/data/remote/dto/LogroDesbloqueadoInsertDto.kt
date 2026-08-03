package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogroDesbloqueadoInsertDto(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("logro_id") val logroId: Int,
    val fecha: String
)