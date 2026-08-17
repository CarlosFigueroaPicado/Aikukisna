package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OracionEjemploDto(
    val id: Int,
    @SerialName("texto_origen") val textoOrigen: String,
    @SerialName("texto_destino") val textoDestino: String,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto
)