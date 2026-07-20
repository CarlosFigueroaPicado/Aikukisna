package com.aikukisna.app.domain.model

import java.time.LocalDate
import java.util.UUID

data class Usuario(
    val id: UUID,
    val nombre: String?,
    val apellido: String?,
    val nombreUsuario: String?,
    val correo: String?,
    val edad: Int?,
    val pais: String?,
    val ciudad: String?,
    val idiomaMeta: Idioma?,
    val xp: Int,
    val rachaActual: Int,
    val rachaMaxima: Int,
    val ultimaActividad: LocalDate?
)