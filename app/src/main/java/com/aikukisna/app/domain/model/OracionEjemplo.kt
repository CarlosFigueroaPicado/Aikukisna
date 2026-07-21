package com.aikukisna.app.domain.model

data class OracionEjemplo(
    val id: Int,
    val textoMiskito: String,
    val textoEspanol: String,
    val leccion: Leccion?,
    val fuente: FuenteDocumento
)