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
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import com.aikukisna.app.domain.usecase.RegistrarUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registrarUsuarioUseCase: RegistrarUsuarioUseCase,
    private val iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase,
    private val proveedorTokenGoogle: ProveedorTokenGoogle,
    private val authRepository: AuthRepository,
    private val obtenerUsuarioUseCase: ObtenerUsuarioUseCase,
    private val cambiarIdiomaMetaUseCase: CambiarIdiomaMetaUseCase
) : ViewModel() {

    var nombre by mutableStateOf("")
        private set
    var nombreUsuario by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmarPassword by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
    var isLoadingGoogle by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var registroExitoso by mutableStateOf(false)
        private set
    var requiereSeleccionIdioma by mutableStateOf(false)
        private set
    private var registroGoogle by mutableStateOf(false)

    fun onNombreChange(valor: String) { nombre = valor }

    fun onNombreUsuarioChange(valor: String) { nombreUsuario = valor }

    fun onEmailChange(valor: String) { email = valor }

    fun onPasswordChange(valor: String) { password = valor }

    fun onConfirmarPasswordChange(valor: String) { confirmarPassword = valor }


    fun validarCampos(): Boolean {
        if (nombre.isBlank() || nombreUsuario.isBlank() ||
            email.isBlank() || password.isBlank() || confirmarPassword.isBlank()
        ) {
            errorMessage = t(R.string.register_completa_todos_los_campos)
            return false
        }
        if (password != confirmarPassword) {
            errorMessage = t(R.string.register_las_contrasenas_no_coinciden)
            return false
        }
        errorMessage = null
        return true
    }

    fun registrarConIdioma(idioma: Idioma) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                registrarUsuarioUseCase(
                    correo = email,
                    contrasena = password,
                    nombre = nombre,
                    apellido = "",
                    nombreUsuario = nombreUsuario,
                    edad = 0,
                    pais = "",
                    ciudad = "",
                    idiomaMeta = idioma
                )
                registroExitoso = true
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.register_error_al_conectar_con_el)
            } finally {
                isLoading = false
            }
        }
    }

    fun continuarConIdioma(idioma: Idioma) {
        if (!registroGoogle) {
            registrarConIdioma(idioma)
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId() ?: error(t(R.string.register_sesion_no_iniciada))
                val usuario = obtenerUsuarioUseCase(userId) ?: error(t(R.string.register_no_se_encontro_el_perfil))
                cambiarIdiomaMetaUseCase(usuario, idioma)
                requiereSeleccionIdioma = false
                registroExitoso = true
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.register_no_se_pudo_guardar_el)
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
                val userId = iniciarSesionConGoogleUseCase(credencial.idToken, credencial.nonce)
                val usuario = obtenerUsuarioUseCase(userId)
                registroGoogle = true
                requiereSeleccionIdioma = usuario?.idiomaMeta == null
                registroExitoso = !requiereSeleccionIdioma
            } catch (e: Exception) {
                errorMessage = if (e.message?.contains("cancel", ignoreCase = true) == true) {
                    t(R.string.register_registro_cancelado)
                } else {
                    e.message ?: t(R.string.register_error_al_continuar_con_google)
                }
            } finally {
                isLoadingGoogle = false
            }
        }
    }
}
