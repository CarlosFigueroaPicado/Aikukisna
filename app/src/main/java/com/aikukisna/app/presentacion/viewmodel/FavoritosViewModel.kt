package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.usecase.ObtenerFavoritosUseCase
import com.aikukisna.app.domain.usecase.QuitarFavoritoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val obtenerFavoritosUseCase: ObtenerFavoritosUseCase,
    private val quitarFavoritoUseCase: QuitarFavoritoUseCase
) : ViewModel() {
    var favoritos by mutableStateOf<List<PalabraFavorita>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepository.usuarioActualId()
                    ?: error(t(R.string.favoritos_sesion_no_iniciada))
                favoritos = obtenerFavoritosUseCase(userId)
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.favoritos_no_se_pudieron_cargar_tus)
            } finally {
                isLoading = false
            }
        }
    }

    fun quitar(palabraId: Int) {
        viewModelScope.launch {
            try {
                val userId = authRepository.usuarioActualId() ?: return@launch
                quitarFavoritoUseCase(userId, palabraId)
                favoritos = favoritos.filterNot { it.palabra.id == palabraId }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.favoritos_no_se_pudo_quitar_el)
            }
        }
    }
}
