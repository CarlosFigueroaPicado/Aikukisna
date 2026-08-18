package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.domain.model.Usuario

sealed interface HomeUiState {
    data object Cargando : HomeUiState
    data class Exito(val usuario: Usuario) : HomeUiState
    data class Error(val mensaje: String) : HomeUiState
}