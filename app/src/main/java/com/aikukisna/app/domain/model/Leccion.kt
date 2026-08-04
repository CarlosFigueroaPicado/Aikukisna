package com.aikukisna.app.domain.model

data class Leccion(
    val id: Int,
    val titulo: String,
    val capituloNumero: Int?,
    val nivel: Int,
    val categoria: Categoria?,
    val idiomaMeta: Idioma
)