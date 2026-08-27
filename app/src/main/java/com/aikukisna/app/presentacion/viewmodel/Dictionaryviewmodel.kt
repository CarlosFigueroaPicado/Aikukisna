package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.BuscarPalabrasUseCase
import com.aikukisna.app.domain.repository.DiccionarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PalabraConTraduccion(
    val texto: String,
    val traduccion: String?
)

// Idiomas fijos por id, según la tabla `idioma` ya verificada en producción.
private const val IDIOMA_MISKITO = 1
private const val IDIOMA_ESPANOL = 2

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val buscarPalabrasUseCase: BuscarPalabrasUseCase,
    private val diccionarioRepository: DiccionarioRepository
) : ViewModel() {

    var query by mutableStateOf("")
        private set
    var resultados by mutableStateOf<List<PalabraConTraduccion>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        buscar("a") // primera carga con algo de contenido visible
    }

    fun onQueryChange(valor: String) {
        query = valor
        buscar(valor)
    }

    private fun buscar(texto: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val palabras = buscarPalabrasUseCase(
                    query = texto,
                    idiomaId = IDIOMA_MISKITO,
                    limite = 20
                )
                resultados = palabras.map { palabra ->
                    val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                        .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                        ?.palabraDestino
                        ?.texto
                    PalabraConTraduccion(texto = palabra.texto, traduccion = traduccion)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al buscar"
            } finally {
                isLoading = false
            }
        }
    }
}