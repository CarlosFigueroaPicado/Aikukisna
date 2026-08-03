package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsuarioDto(
    val id: String,
    val nombre: String? = null,
    val apellido: String? = null,
    @SerialName("nombre_usuario") val nombreUsuario: String? = null,
    val correo: String? = null,
    val edad: Int? = null,
    val pais: String? = null,
    val ciudad: String? = null,
    @SerialName("idioma_meta") val idiomaMeta: IdiomaDto? = null,
    val xp: Int,
    @SerialName("racha_actual") val rachaActual: Int,
    @SerialName("racha_maxima") val rachaMaxima: Int,
    @SerialName("ultima_actividad") val ultimaActividad: String? = null
)