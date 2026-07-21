package com.aikukisna.app.domain.model

data class Logro(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val condicionTipo: String,
    val condicionValor: Int,
    val categoria: Categoria?
)