package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogroDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    @SerialName("condicion_tipo") val condicionTipo: String,
    @SerialName("condicion_valor") val condicionValor: Int,
    val categoria: CategoriaDto? = null
)