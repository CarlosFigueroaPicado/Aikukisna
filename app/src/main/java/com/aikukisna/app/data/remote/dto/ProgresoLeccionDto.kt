package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgresoLeccionDto(
    @SerialName("usuario_id") val usuarioId: String,
    val leccion: LeccionDto,
    val estado: String,
    val puntaje: Int? = null,
    @SerialName("fecha_completado") val fechaCompletado: String? = null
)