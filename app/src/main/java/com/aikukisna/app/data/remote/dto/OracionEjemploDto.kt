package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OracionEjemploDto(
    val id: Int,
    @SerialName("texto_origen") val textoOrigen: String,
    @SerialName("texto_destino") val textoDestino: String,
    @SerialName("idioma_origen_id") val idiomaOrigenId: Int,
    @SerialName("idioma_destino_id") val idiomaDestinoId: Int,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("updated_at") val updatedAt: String? = null
)
