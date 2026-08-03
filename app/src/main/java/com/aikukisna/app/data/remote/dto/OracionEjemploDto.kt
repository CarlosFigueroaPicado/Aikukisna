package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OracionEjemploDto(
    val id: Int,
    @SerialName("texto_miskito") val textoMiskito: String,
    @SerialName("texto_espanol") val textoEspanol: String,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto
)