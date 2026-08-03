package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class IdiomaDto(
    val id: Int,
    val codigo: String,
    val nombre: String
)