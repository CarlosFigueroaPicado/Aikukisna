package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgresoLeccionUpsertDto(
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("leccion_id") val leccionId: Int,
    val estado: String,
    val puntaje: Int? = null,
    @SerialName("fecha_completado") val fechaCompletado: String? = null
)