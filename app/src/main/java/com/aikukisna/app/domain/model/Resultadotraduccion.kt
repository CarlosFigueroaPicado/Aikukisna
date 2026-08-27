package com.aikukisna.app.domain.model


enum class FuenteTraduccion { DICCIONARIO, IA }

data class ResultadoTraduccion(
    val texto: String,
    val fuente: FuenteTraduccion
)