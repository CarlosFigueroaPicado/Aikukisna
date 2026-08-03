package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FuenteDocumentoDto(
    val id: Int,
    val titulo: String,
    val autor: String? = null,
    val anio: Int? = null,
    val institucion: String? = null
)