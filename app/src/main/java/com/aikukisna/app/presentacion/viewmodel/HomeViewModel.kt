package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.SincronizarDatosOfflineUseCase
import com.aikukisna.app.domain.usecase.SincronizarLeccionesPendientesUseCase
import com.aikukisna.app.presentacion.pantallas.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val authRepository: AuthRepository,
    private val sincronizarDatosOfflineUseCase: SincronizarDatosOfflineUseCase,
    private val sincronizarLeccionesPendientesUseCase: SincronizarLeccionesPendientesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()


    private val _estadoSincronizacion = MutableStateFlow<EstadoSincronizacion?>(null)
    val estadoSincronizacion: StateFlow<EstadoSincronizacion?> = _estadoSincronizacion.asStateFlow()

    init {
        cargarDatos()
        iniciarSincronizacionSiHaceFalta()
        reintentarLeccionesPendientes()
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


    private fun iniciarSincronizacionSiHaceFalta() {
        viewModelScope.launch {
            if (sincronizarDatosOfflineUseCase.yaHayDatos()) return@launch
            sincronizarDatosOfflineUseCase.invoke().collect { estado ->
                _estadoSincronizacion.value = estado
                if (estado is EstadoSincronizacion.Completado) {
                    _estadoSincronizacion.value = null
                }
            }
        }
    }

private fun reintentarLeccionesPendientes() {
        viewModelScope.launch {
            sincronizarLeccionesPendientesUseCase()
        }
    }
}