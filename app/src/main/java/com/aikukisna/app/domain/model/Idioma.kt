package com.aikukisna.app.domain.model

data class Idioma(
    val id: Int,
    val codigo: String,
    val nombre: String
) {
    companion object {

        val DISPONIBLES = listOf(
            Idioma(id = 1, codigo = "mi", nombre = "Miskito"),
            Idioma(id = 2, codigo = "es", nombre = "Español"),
            Idioma(id = 3, codigo = "jam", nombre = "Inglés Kriol"),
            Idioma(id = 4, codigo = "en", nombre = "Inglés Estándar")
        )
    }
}