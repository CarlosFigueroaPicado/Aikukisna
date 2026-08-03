package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoriaDto(
    val id: Int,
    val nombre: String
)