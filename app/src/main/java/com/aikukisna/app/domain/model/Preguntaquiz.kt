package com.aikukisna.app.domain.model


data class PreguntaQuiz(
    val textoPregunta: String,
    val respuestaCorrecta: String,
    val opciones: List<String>
)