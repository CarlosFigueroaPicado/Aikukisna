package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.usecase.ProximaLeccion

sealed interface HomeUiState {
    data object Cargando : HomeUiState
    data class Exito(
        val usuario: Usuario,
        val proximaLeccion: ProximaLeccion?
    ) : HomeUiState
    data class Error(val mensaje: String) : HomeUiState
}