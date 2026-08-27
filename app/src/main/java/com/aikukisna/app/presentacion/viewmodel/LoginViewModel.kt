package com.aikukisna.app.presentacion.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.data.auth.ProveedorTokenGoogle
import com.aikukisna.app.domain.usecase.IniciarSesionConGoogleUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase,
    private val proveedorTokenGoogle: ProveedorTokenGoogle
) : ViewModel() {

    var email by mutableStateOf("")
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

    fun onEmailChange(valor: String) { email = valor }
    fun onPasswordChange(valor: String) { password = valor }

    fun intentarLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Completa todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                iniciarSesionUseCase(email, password)
                loginExitoso = true
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
                loginExitoso = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al iniciar sesión con Google"
            } finally {
                isLoadingGoogle = false
            }
        }
    }
}