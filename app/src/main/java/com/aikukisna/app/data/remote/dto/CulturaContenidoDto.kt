package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CulturaContenidoDto(
    val id: Int,
    val titulo: String,
    val contenido: String,
    @SerialName("rango_pagina_inicio") val rangoPaginaInicio: Int? = null,
    @SerialName("rango_pagina_fin") val rangoPaginaFin: Int? = null,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto
)