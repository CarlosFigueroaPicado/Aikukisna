package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.domain.model.Usuario

sealed interface PerfilUiState {
    data object Cargando : PerfilUiState
    data class Exito(
        val usuario: Usuario,
        val rachaDias: Int,
        val totalPuntos: Int,
        val leccionesCompletadas: Int
    ) : PerfilUiState
    data class Error(val mensaje: String) : PerfilUiState
    data object CerrarSesion : PerfilUiState
}