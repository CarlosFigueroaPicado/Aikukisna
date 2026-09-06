package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.LeccionConEstado
import com.aikukisna.app.domain.usecase.ObtenerMapaLeccionesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


val NIVELES_CEFR = listOf(1 to "A0", 2 to "A1", 3 to "A2", 4 to "B1", 5 to "B2", 6 to "C1", 7 to "C2")

@HiltViewModel
class LeccionesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val obtenerMapaLeccionesUseCase: ObtenerMapaLeccionesUseCase
) : ViewModel() {

    var nivelSeleccionado by mutableStateOf(NIVELES_CEFR.first().first)
        private set
    var lecciones by mutableStateOf<List<LeccionConEstado>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var idiomaMetaId: Int? = null

    init {
        cargar()
    }

    fun seleccionarNivel(nivel: Int) {
        if (nivel == nivelSeleccionado) return
        nivelSeleccionado = nivel
        cargarLecciones()
    }

    private fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId()
                    ?: throw IllegalStateException("Sesión no iniciada")
                val usuario = usuarioRepository.obtenerUsuario(userId)
                    ?: throw IllegalStateException("No se encontró el perfil")
                idiomaMetaId = usuario.idiomaMeta?.id
                    ?: throw IllegalStateException("Todavía no elegiste un idioma")
                cargarLecciones()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar lecciones"
                isLoading = false
            }
        }
    }

    private fun cargarLecciones() {
        val idioma = idiomaMetaId ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId() ?: return@launch
                lecciones = obtenerMapaLeccionesUseCase(userId, idioma, nivelSeleccionado)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar lecciones"
            } finally {
                isLoading = false
            }
        }
    }
}