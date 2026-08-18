package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.presentacion.pantallas.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// NOTA: recibe repositorios directamente, no casos de uso. Es una excepción
// al patrón del resto del proyecto (donde la UI siempre pasa por un
// UseCase), pero funciona porque ambas interfaces ya están resueltas por
// Hilt en RepositoryModule. Queda como mejora pendiente para más adelante,
// no bloquea la conexión de hoy.
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Cargando
            try {
                val userId = authRepository.usuarioActualId()
                if (userId != null) {
                    val usuario = usuarioRepository.obtenerUsuario(userId)
                    if (usuario != null) {
                        _uiState.value = HomeUiState.Exito(usuario)
                    } else {
                        _uiState.value = HomeUiState.Error("No se encontró el perfil")
                    }
                } else {
                    _uiState.value = HomeUiState.Error("Sesión no iniciada")
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Error de conexión: ${e.message}")
            }
        }
    }
}