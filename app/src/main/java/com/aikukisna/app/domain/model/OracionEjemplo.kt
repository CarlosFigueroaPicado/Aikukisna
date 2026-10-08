package com.aikukisna.app.domain.model

data class OracionEjemplo(
    val id: Int,
    val textoOrigen: String,
    val textoDestino: String,
    val idiomaOrigenId: Int,
    val idiomaDestinoId: Int,
    val leccion: Leccion?,
    val fuente: FuenteDocumento,
    val estadoValidacion: String = "importada",
    val updatedAtEpochMs: Long = 0
)
