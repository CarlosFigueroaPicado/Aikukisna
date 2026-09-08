package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.SolicitarRestablecimientoContrasenaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class RecuperarContrasenaViewModel @Inject constructor(
    private val solicitarRestablecimiento: SolicitarRestablecimientoContrasenaUseCase
) : ViewModel() {
    var correo by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var enviado by mutableStateOf(false)
        private set

    fun onCorreoChange(valor: String) {
        correo = valor
        errorMessage = null
        enviado = false
    }

    fun solicitar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                solicitarRestablecimiento(correo)
                enviado = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo enviar el correo de recuperación"
            } finally {
                isLoading = false
            }
        }
    }
}
