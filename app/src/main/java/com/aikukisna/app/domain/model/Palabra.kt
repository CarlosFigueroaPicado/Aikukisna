package com.aikukisna.app.domain.model

data class Palabra(
    val id: Int,
    val idioma: Idioma,
    val texto: String,
    val categoria: Categoria?,
    val fuente: FuenteDocumento
)