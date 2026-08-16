package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import kotlinx.coroutines.launch

class LoginViewModel(
    private val iniciarSesionUseCase: IniciarSesionUseCase
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
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
}