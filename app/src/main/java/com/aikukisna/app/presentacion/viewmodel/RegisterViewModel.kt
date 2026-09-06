package com.aikukisna.app.presentacion.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.data.auth.ProveedorTokenGoogle
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.usecase.IniciarSesionConGoogleUseCase
import com.aikukisna.app.domain.usecase.RegistrarUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registrarUsuarioUseCase: RegistrarUsuarioUseCase,
    private val iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase,
    private val proveedorTokenGoogle: ProveedorTokenGoogle
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

    fun onNombreChange(valor: String) { nombre = valor }

    fun onNombreUsuarioChange(valor: String) { nombreUsuario = valor }

    fun onEmailChange(valor: String) { email = valor }

    fun onPasswordChange(valor: String) { password = valor }

    fun onConfirmarPasswordChange(valor: String) { confirmarPassword = valor }


    fun validarCampos(): Boolean {
        if (nombre.isBlank() || nombreUsuario.isBlank() ||
            email.isBlank() || password.isBlank() || confirmarPassword.isBlank()
        ) {
            errorMessage = "Completa todos los campos"
            return false
        }
        if (password != confirmarPassword) {
            errorMessage = "Las contraseñas no coinciden"
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
                errorMessage = e.message ?: "Error al conectar con el servidor"
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
                registroExitoso = true
            } catch (e: Exception) {
                errorMessage = if (e.message?.contains("cancel", ignoreCase = true) == true) {
                    "Registro cancelado"
                } else {
                    e.message ?: "Error al continuar con Google"
                }
            } finally {
                isLoadingGoogle = false
            }
        }
    }
}