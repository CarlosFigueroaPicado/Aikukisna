package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TraduccionDto(
    val id: Int,
    @SerialName("palabra_origen") val palabraOrigen: PalabraDto,
    @SerialName("palabra_destino") val palabraDestino: PalabraDto,
    val nota: String? = null
)