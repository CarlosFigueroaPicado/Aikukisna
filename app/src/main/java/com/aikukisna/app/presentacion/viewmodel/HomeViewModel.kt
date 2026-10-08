package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.CambiarIdiomaMetaUseCase
import com.aikukisna.app.domain.usecase.ObtenerProximaLeccionUseCase
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
    private val obtenerProximaLeccionUseCase: ObtenerProximaLeccionUseCase,
    private val sincronizarLeccionesPendientesUseCase: SincronizarLeccionesPendientesUseCase,
    private val cambiarIdiomaMetaUseCase: CambiarIdiomaMetaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Cargando)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        reintentarLeccionesPendientes()
        viewModelScope.launch {
            usuarioRepository.observarIdiomaMeta().collect { idioma ->
                val actual = (_uiState.value as? HomeUiState.Exito)?.usuario?.idiomaMeta
                if (idioma == null || idioma.id != actual?.id) cargarDatos()
            }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Cargando
            try {
                val userId = authRepository.usuarioActualId()
                if (userId != null) {
                    val usuario = usuarioRepository.obtenerUsuario(userId)
                    if (usuario != null) {
                        val proximaLeccion = usuario.idiomaMeta?.let { idioma ->
                            obtenerProximaLeccionUseCase(userId, idioma.id)
                        }
                        _uiState.value = HomeUiState.Exito(usuario, proximaLeccion)
                    } else {
                        _uiState.value = HomeUiState.Error(t(R.string.home_no_se_encontro_el_perfil))
                    }
                } else {
                    _uiState.value = HomeUiState.Error(t(R.string.home_sesion_no_iniciada))
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(t(R.string.home_error_de_conexion, e.message.orEmpty()))
            }
        }
    }

    fun cambiarIdioma(nuevoIdioma: Idioma) {
        val estadoActual = _uiState.value
        if (estadoActual !is HomeUiState.Exito) return
        viewModelScope.launch {
            try {
                // El perfil local emite el idioma nuevo y cada pantalla se recarga sola.
                cambiarIdiomaMetaUseCase(estadoActual.usuario, nuevoIdioma)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: t(R.string.home_no_se_pudo_cambiar_el))
            }
        }
    }

    fun cerrarSesion(onCompletado: () -> Unit) {
        viewModelScope.launch {
            // La sesión local se limpia siempre: el estudiante debe poder salir aunque no haya red.
            runCatching { authRepository.cerrarSesion() }
            onCompletado()
        }
    }

    private fun reintentarLeccionesPendientes() {
        viewModelScope.launch {
            sincronizarLeccionesPendientesUseCase()
        }
    }
}
