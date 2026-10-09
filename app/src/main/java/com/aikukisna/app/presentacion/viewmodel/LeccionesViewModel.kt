package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

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
    var nivelesDesbloqueados by mutableStateOf<Set<Int>>(emptySet())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var idiomaMetaNombre by mutableStateOf("")
        private set

    private var idiomaMetaId: Int? = null

    init {
        // Recarga el mapa cada vez que cambia el idioma meta (incluida la primera emisión).
        viewModelScope.launch {
            usuarioRepository.observarIdiomaMeta().collect { idioma ->
                if (idioma != null && idioma.id == idiomaMetaId) return@collect
                if (idiomaMetaId != null) {
                    nivelSeleccionado = NIVELES_CEFR.first().first
                    lecciones = emptyList()
                }
                cargar()
            }
        }
    }

    fun seleccionarNivel(nivel: Int) {
        if (nivel == nivelSeleccionado || nivel !in nivelesDesbloqueados) return
        nivelSeleccionado = nivel
        cargarLecciones()
    }

    fun recargar() {
        if (idiomaMetaId == null) cargar() else cargarLecciones()
    }

    private fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId()
                    ?: throw IllegalStateException(t(R.string.lecciones_sesion_no_iniciada))
                val usuario = usuarioRepository.obtenerUsuario(userId)
                    ?: throw IllegalStateException(t(R.string.lecciones_no_se_encontro_el_perfil))
                idiomaMetaNombre = usuario.idiomaMeta?.nombre.orEmpty()
                idiomaMetaId = usuario.idiomaMeta?.id
                    ?: throw IllegalStateException(t(R.string.lecciones_todavia_no_elegiste_un_idioma))
                cargarLecciones()
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.lecciones_error_al_cargar_lecciones)
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
                val mapa = obtenerMapaLeccionesUseCase(userId, idioma, nivelSeleccionado)
                lecciones = mapa.lecciones
                nivelesDesbloqueados = mapa.nivelesDesbloqueados
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.lecciones_error_al_cargar_lecciones)
            } finally {
                isLoading = false
            }
        }
    }
}
