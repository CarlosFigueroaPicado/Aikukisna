package com.aikukisna.app.domain.model

data class OracionEjemplo(
    val id: Int,
    val textoOrigen: String,
    val textoDestino: String,
    val leccion: Leccion?,
    val fuente: FuenteDocumento
)