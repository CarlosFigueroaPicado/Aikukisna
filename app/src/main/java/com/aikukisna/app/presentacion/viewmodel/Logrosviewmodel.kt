package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.usecase.LogroConEstado
import com.aikukisna.app.domain.usecase.ObtenerLogrosConEstadoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogrosViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerLogrosConEstadoUseCase: ObtenerLogrosConEstadoUseCase
) : ViewModel() {

    var logros by mutableStateOf<List<LogroConEstado>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId()
                    ?: throw IllegalStateException(t(R.string.logros_sesion_no_iniciada))
                logros = obtenerLogrosConEstadoUseCase(userId)
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.logros_error_al_cargar_logros)
            } finally {
                isLoading = false
            }
        }
    }
}