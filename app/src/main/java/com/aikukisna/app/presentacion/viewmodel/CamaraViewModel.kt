package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.ReconocerObjetoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CamaraViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val reconocerObjetoUseCase: ReconocerObjetoUseCase
) : ViewModel() {

    var resultado by mutableStateOf<ResultadoReconocimiento?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var idiomaMetaId: Int? = null

    init {
        viewModelScope.launch {
            val userId = authRepository.usuarioActualId()
            idiomaMetaId = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta?.id }
        }
    }

    fun analizarImagen(imagenBase64: String) {
        val idioma = idiomaMetaId
        if (idioma == null) {
            errorMessage = "No se pudo determinar tu idioma"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            resultado = null
            try {
                resultado = reconocerObjetoUseCase(imagenBase64, idioma)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo reconocer el objeto"
            } finally {
                isLoading = false
            }
        }
    }

    fun escanearOtraVez() {
        resultado = null
        errorMessage = null
    }
}