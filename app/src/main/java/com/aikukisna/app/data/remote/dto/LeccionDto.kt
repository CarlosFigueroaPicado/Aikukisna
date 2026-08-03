package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LeccionDto(
    val id: Int,
    val titulo: String,
    @SerialName("capitulo_numero") val capituloNumero: Int? = null,
    val nivel: Int,
    val categoria: CategoriaDto? = null
)