package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.usecase.ActualizarContrasenaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class NuevaContrasenaViewModel @Inject constructor(
    private val actualizarContrasena: ActualizarContrasenaUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    var nuevaContrasena by mutableStateOf("")
        private set
    var confirmacion by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var completado by mutableStateOf(false)
        private set

    fun onNuevaContrasenaChange(valor: String) {
        nuevaContrasena = valor
        errorMessage = null
    }

    fun onConfirmacionChange(valor: String) {
        confirmacion = valor
        errorMessage = null
    }

    fun guardar() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                actualizarContrasena(nuevaContrasena, confirmacion)
                authRepository.cerrarSesion()
                completado = true
            } catch (e: IllegalArgumentException) {
                errorMessage = e.message ?: t(R.string.nuevacontrasena_revisa_la_nueva_contrasena)
            } catch (_: Exception) {
                errorMessage = t(R.string.nuevacontrasena_no_se_pudo_actualizar_la)
            } finally {
                isLoading = false
            }
        }
    }
}
