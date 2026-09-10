package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.DetallePalabra
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.usecase.MarcarFavoritoUseCase
import com.aikukisna.app.domain.usecase.ObtenerFavoritosUseCase
import com.aikukisna.app.domain.usecase.QuitarFavoritoUseCase
import com.aikukisna.app.domain.usecase.ObtenerPalabraDetalleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetallePalabraViewModel @Inject constructor(
    private val obtenerPalabraDetalleUseCase: ObtenerPalabraDetalleUseCase,
    private val authRepository: AuthRepository,
    private val obtenerFavoritosUseCase: ObtenerFavoritosUseCase,
    private val marcarFavoritoUseCase: MarcarFavoritoUseCase,
    private val quitarFavoritoUseCase: QuitarFavoritoUseCase
) : ViewModel() {
    var detalle by mutableStateOf<DetallePalabra?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var esFavorita by mutableStateOf(false)
        private set
    private var palabraIdCargada: Int? = null

    fun cargar(palabraId: Int) {
        if (palabraIdCargada == palabraId) return
        palabraIdCargada = palabraId
        viewModelScope.launch {
            try {
                detalle = obtenerPalabraDetalleUseCase(palabraId)
                if (detalle == null) errorMessage = "No se encontró la palabra"
                val userId = authRepository.usuarioActualId()
                esFavorita = userId?.let { id -> obtenerFavoritosUseCase(id).any { it.palabra.id == palabraId } } == true
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo cargar el detalle"
            } finally {
                isLoading = false
            }
        }
    }

    fun cambiarFavorito() {
        val palabraId = detalle?.palabra?.id ?: return
        viewModelScope.launch {
            try {
                val userId = authRepository.usuarioActualId() ?: return@launch
                if (esFavorita) quitarFavoritoUseCase(userId, palabraId)
                else marcarFavoritoUseCase(userId, palabraId)
                esFavorita = !esFavorita
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo actualizar el favorito"
            }
        }
    }
}
