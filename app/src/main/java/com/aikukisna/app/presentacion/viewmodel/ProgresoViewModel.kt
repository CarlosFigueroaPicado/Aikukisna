package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgresoViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgresoUiState>(ProgresoUiState.Cargando)
    val uiState: StateFlow<ProgresoUiState> = _uiState.asStateFlow()

    init {
        cargarProgreso()
    }

    fun cargarProgreso() {
        viewModelScope.launch {
            _uiState.value = ProgresoUiState.Cargando
            try {
                val userId = authRepository.usuarioActualId()
                if (userId != null) {
                    val lista = usuarioRepository.obtenerProgreso(userId)
                    _uiState.value = ProgresoUiState.Exito(lista)
                } else {
                    _uiState.value = ProgresoUiState.Error("No se pudo identificar al usuario")
                }
            } catch (e: Exception) {
                _uiState.value = ProgresoUiState.Error(e.message ?: "Error al cargar progreso")
            }
        }
    }
}
