package com.aikukisna.app.domain.model

data class Idioma(
    val id: Int,
    val codigo: String,
    val nombre: String
) {
    companion object {

        /** ISO 639-3 del inglés criollo nicaragüense (no "jam", que es el criollo jamaicano). */
        const val CODIGO_KRIOL = "bzk"

        /** "jam" era el código anterior; puede seguir en réplicas locales sin sincronizar. */
        private const val CODIGO_KRIOL_ANTERIOR = "jam"

        fun esKriol(codigo: String): Boolean =
            codigo == CODIGO_KRIOL || codigo == CODIGO_KRIOL_ANTERIOR

        val DISPONIBLES = listOf(
            Idioma(id = 1, codigo = "mi", nombre = "Miskito"),
            Idioma(id = 2, codigo = "es", nombre = "Español"),
            Idioma(id = 3, codigo = CODIGO_KRIOL, nombre = "Inglés Kriol"),
            Idioma(id = 4, codigo = "en", nombre = "Inglés Estándar")
        )
    }
}