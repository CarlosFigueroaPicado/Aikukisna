package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.ObtenerProgresoUseCase
import com.aikukisna.app.presentacion.pantallas.ProgresoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ProgresoViewModel(
    private val obtenerProgresoUseCase: ObtenerProgresoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgresoUiState>(ProgresoUiState.Cargando)
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    fun cargarProgreso(usuarioId: UUID) {
        viewModelScope.launch {
            _uiState.value = ProgresoUiState.Cargando
            try {
                val progreso = obtenerProgresoUseCase(usuarioId)
                _uiState.value = ProgresoUiState.Exito(listaProgreso = progreso)
            } catch (e: Exception) {
                _uiState.value = ProgresoUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cargar el progreso"
                )
            }
        }
    }
}
