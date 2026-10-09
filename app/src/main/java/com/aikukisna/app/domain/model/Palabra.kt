package com.aikukisna.app.domain.model

data class Palabra(
    val id: Int,
    val idioma: Idioma,
    val texto: String,
    val categoria: Categoria?,
    val fuente: FuenteDocumento,
    val pronunciacion: String? = null,
    val pronunciacionFonetica: String? = null,
    val pronunciacionVerificada: Boolean = false,
    val textoNormalizado: String = "",
    val estadoValidacion: String = "importada",
    val createdAtEpochMs: Long = 0,
    val updatedAtEpochMs: Long = 0
)
