package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PalabraDto(
    val id: Int,
    val idioma: IdiomaDto,
    val texto: String,
    val categoria: CategoriaDto? = null,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto
)