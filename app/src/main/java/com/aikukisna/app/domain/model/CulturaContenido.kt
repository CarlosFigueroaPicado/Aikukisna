package com.aikukisna.app.domain.model

data class CulturaContenido(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val rangoPaginaInicio: Int?,
    val rangoPaginaFin: Int?,
    val fuente: FuenteDocumento
)