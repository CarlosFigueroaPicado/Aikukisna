package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.domain.model.ProgresoLeccion

sealed interface ProgresoUiState {
    data object Cargando : ProgresoUiState
    data class Exito(val listaProgreso: List<ProgresoLeccion>) : ProgresoUiState
    data class Error(val mensaje: String) : ProgresoUiState
}