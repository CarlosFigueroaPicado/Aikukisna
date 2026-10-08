package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.usecase.ObtenerContenidoCulturalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CulturaViewModel @Inject constructor(
    private val obtenerContenidoCulturalUseCase: ObtenerContenidoCulturalUseCase
) : ViewModel() {
    var contenido by mutableStateOf<List<CulturaContenido>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            runCatching { obtenerContenidoCulturalUseCase() }
                .onSuccess { contenido = it }
                .onFailure { errorMessage = it.message ?: "No se pudo cargar la cultura" }
            isLoading = false
        }
    }
}
