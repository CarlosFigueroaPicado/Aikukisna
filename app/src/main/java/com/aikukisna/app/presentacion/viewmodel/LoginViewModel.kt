package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.data.auth.ProveedorTokenGoogle
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.usecase.CambiarIdiomaMetaUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionConGoogleUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase,
    private val proveedorTokenGoogle: ProveedorTokenGoogle,
    private val authRepository: AuthRepository,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val cambiarIdiomaMetaUseCase: CambiarIdiomaMetaUseCase
) : ViewModel() {


    var identificador by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isLoadingGoogle by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var loginExitoso by mutableStateOf(false)
        private set
    var requiereSeleccionIdioma by mutableStateOf(false)
        private set

    fun onIdentificadorChange(valor: String) { identificador = valor }
    fun onPasswordChange(valor: String) { password = valor }

    fun intentarLogin() {
        if (identificador.isBlank() || password.isBlank()) {
            errorMessage = t(R.string.login_completa_todos_los_campos)
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                iniciarSesionUseCase(identificador, password)
                continuarTrasAutenticacion()
            } catch (e: Exception) {
                errorMessage = IniciarSesionUseCase.MENSAJE_CREDENCIALES_INVALIDAS
            } finally {
                isLoading = false
            }
        }
    }


    fun iniciarSesionConGoogle(context: Context) {
        viewModelScope.launch {
            isLoadingGoogle = true
            errorMessage = null
            try {
                val credencial = proveedorTokenGoogle.obtenerCredencial(context)
                iniciarSesionConGoogleUseCase(credencial.idToken, credencial.nonce)
                continuarTrasAutenticacion()
            } catch (e: Exception) {

                errorMessage = if (e.message?.contains("cancel", ignoreCase = true) == true) {
                    t(R.string.login_inicio_de_sesion_cancelado)
                } else {
                    e.message ?: t(R.string.login_error_al_iniciar_sesion_con)
                }
            } finally {
                isLoadingGoogle = false
            }
        }
    }

    private suspend fun continuarTrasAutenticacion() {
        val userId = authRepository.usuarioActualId()
        requiereSeleccionIdioma = userId?.let { obtenerUsuarioUseCase(it)?.idiomaMeta == null } == true
        loginExitoso = !requiereSeleccionIdioma
    }

    fun confirmarIdiomaSeleccionado(idioma: Idioma) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId() ?: error(t(R.string.login_sesion_no_iniciada))
                val usuario = obtenerUsuarioUseCase(userId) ?: error(t(R.string.login_no_se_encontro_el_perfil))
                cambiarIdiomaMetaUseCase(usuario, idioma)
                requiereSeleccionIdioma = false
                loginExitoso = true
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.login_no_se_pudo_guardar_el)
            } finally {
                isLoading = false
            }
        }
    }

    fun consumirSolicitudIdioma() {
        requiereSeleccionIdioma = false
    }
}
