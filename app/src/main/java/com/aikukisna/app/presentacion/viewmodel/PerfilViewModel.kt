package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerfilUiState>(PerfilUiState.Cargando)
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.value = PerfilUiState.Cargando
            try {
                val usuarioMock = Usuario(
                    id = UUID.randomUUID(),
                    nombre = "Pedro",
                    apellido = "Pérez",
                    nombreUsuario = "pedroperez",
                    correo = "pedro@ejemplo.com",
                    edad = 25,
                    pais = "Nicaragua",
                    ciudad = "Estelí",
                    idiomaMeta = null,
                    xp = 1250,
                    rachaActual = 5,
                    rachaMaxima = 10,
                    ultimaActividad = null
                )

                _uiState.value = PerfilUiState.Exito(
                    usuario = usuarioMock,
                    rachaDias = usuarioMock.rachaActual,
                    totalPuntos = usuarioMock.xp,
                    leccionesCompletadas = 18
                )
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cargar la información del perfil"
                )
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                authRepository.cerrarSesion()
                _uiState.value = PerfilUiState.CerrarSesion
            } catch (e: Exception) {
                _uiState.value = PerfilUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cerrar sesión"
                )
            }
        }
    }
}