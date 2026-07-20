package com.aikukisna.app.domain.model

data class FuenteDocumento(
    val id: Int,
    val titulo: String,
    val autor: String?,
    val anio: Int?,
    val institucion: String?
)