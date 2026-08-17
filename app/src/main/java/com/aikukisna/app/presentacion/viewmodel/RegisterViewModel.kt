package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.usecase.RegistrarUsuarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registrarUsuarioUseCase: RegistrarUsuarioUseCase
) : ViewModel() {

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var nombre by mutableStateOf("")
        private set
    var apellido by mutableStateOf("")
        private set
    var nombreUsuario by mutableStateOf("")
        private set
    var edad by mutableStateOf(0)
        private set
    var pais by mutableStateOf("")
        private set
    var ciudad by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var registroExitoso by mutableStateOf(false)
        private set

    fun onEmailChange(valor: String) { email = valor }

    fun onPasswordChange(valor: String) { password = valor }

    fun onNombreChange(valor: String) { nombre = valor }

    fun onApellidoChange(valor: String) { apellido = valor }

    fun onNombreUsuarioChange(valor: String) { nombreUsuario = valor }

    fun onEdadChange(valor: Int) { edad = valor }

    fun onPaisChange(valor: String) { pais = valor }

    fun onCiudadChange(valor: String) { ciudad = valor }

    fun onRegisterSuccess(valor: Boolean) { registroExitoso = valor }

    fun onErrorMessage(valor: String?) { errorMessage = valor }

    fun onIsLoading(valor: Boolean) { isLoading = valor }

    fun intentarRegistro() {
        if (email.isBlank() || password.isBlank() || nombre.isBlank() || apellido.isBlank() || nombreUsuario.isBlank() || edad == 0 || pais.isBlank() || ciudad.isBlank()) {
            errorMessage = "Completa todos los campos"
            return
        } else {
            viewModelScope.launch {
                isLoading = true
                errorMessage = null
                try {
                    registrarUsuarioUseCase(
                        correo = email,
                        contrasena = password,
                        nombre = nombre,
                        apellido = apellido,
                        nombreUsuario = nombreUsuario,
                        edad = edad,
                        pais = pais,
                        ciudad = ciudad,
                        idiomaMeta = Idioma(
                            id = 1,
                            nombre = "Español",
                            codigo = "es"
                        )
                    )

                    registroExitoso = true
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Error al conectar con el servidor"
                } finally {
                    isLoading = false
                }

        }
    }
}
}
