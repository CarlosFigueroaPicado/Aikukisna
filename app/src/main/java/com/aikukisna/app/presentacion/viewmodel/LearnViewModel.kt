package com.aikukisna.app.presentacion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Leccion // Ajusta a tu paquete de modelos
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject // Usamos javax.inject.Inject habitual en Android/Hilt

// Sealed interface para representar los estados de la UI
sealed interface LearnUiState {
    data object Cargando : LearnUiState
    data class Exito(val lecciones: List<Leccion>) : LearnUiState
    data class Error(val mensaje: String) : LearnUiState
}

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val leccionesRepository: LeccionRepository, // Corregida la convención camelCase
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearnUiState>(LearnUiState.Cargando)
    val uiState: StateFlow<LearnUiState> = _uiState.asStateFlow()

    init {
        obtenerLecciones()
    }

    fun obtenerLecciones() {
        viewModelScope.launch {
            _uiState.value = LearnUiState.Cargando
            try {
                // Obtenemos el ID del usuario actual
                val uid = authRepository.usuarioActualId()

                if (uid != null) {
                    // Obtenemos datos del usuario (opcionalmente para filtrar lecciones en el futuro)
                    val usuario = usuarioRepository.obtenerUsuario(uid)
                    
                    // Llamada al repositorio para obtener las lecciones
                    val lecciones = leccionesRepository.obtenerLecciones()

                    _uiState.value = LearnUiState.Exito(lecciones)
                } else {
                    _uiState.value = LearnUiState.Error("No hay una sesión activa")
                }
            } catch (e: Exception) {
                _uiState.value = LearnUiState.Error(
                    mensaje = e.localizedMessage ?: "Error al cargar las lecciones"
                )
            }
        }
    }
}