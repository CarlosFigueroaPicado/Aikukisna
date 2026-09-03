package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.TraducirTextoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TraductorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val traducirTextoUseCase: TraducirTextoUseCase
) : ViewModel() {

    var idiomaOrigen by mutableStateOf(Idioma.DISPONIBLES.first { it.codigo == "es" })
        private set
    var idiomaDestino by mutableStateOf(Idioma.DISPONIBLES.first { it.codigo == "mi" })
        private set
    var texto by mutableStateOf("")
        private set
    var resultado by mutableStateOf<ResultadoTraduccion?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            val userId = authRepository.usuarioActualId()
            val idiomaMeta = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta }
            if (idiomaMeta != null) {
                idiomaDestino = idiomaMeta
            }
        }
    }

    fun onTextoChange(valor: String) {
        texto = valor
        resultado = null
    }

    fun onSeleccionarOrigen(idioma: Idioma) {
        idiomaOrigen = idioma
        resultado = null
    }

    fun onSeleccionarDestino(idioma: Idioma) {
        idiomaDestino = idioma
        resultado = null
    }

    fun intercambiarIdiomas() {
        val temp = idiomaOrigen
        idiomaOrigen = idiomaDestino
        idiomaDestino = temp
        resultado = null
    }

    fun traducir() {
        if (texto.isBlank() || isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                resultado = traducirTextoUseCase(texto, idiomaOrigen.id, idiomaDestino.id)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo traducir"
            } finally {
                isLoading = false
            }
        }
    }
}