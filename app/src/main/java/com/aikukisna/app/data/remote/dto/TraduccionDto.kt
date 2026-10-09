package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TraduccionDto(
    val id: Int,
    @SerialName("palabra_origen") val palabraOrigen: PalabraDto,
    @SerialName("palabra_destino") val palabraDestino: PalabraDto,
    val nota: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("fuente_id") val fuenteId: Int? = null,
    @SerialName("nivel_confianza") val nivelConfianza: Double? = null,
    @SerialName("es_preferida") val esPreferida: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null
)