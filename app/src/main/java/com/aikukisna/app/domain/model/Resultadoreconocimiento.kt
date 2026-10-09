package com.aikukisna.app.domain.model


data class ResultadoReconocimiento(
    val objetoDetectado: String,
    val traduccion: String?,
    /** Idioma de [traduccion] cuando no es el que aprende el estudiante (texto que ya estaba en ese idioma). */
    val idiomaTraduccionId: Int? = null
)