package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.usecase.BuscarPalabrasUseCase
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

data class PalabraConTraduccion(
    val id: Int = 0,
    val texto: String,
    val traduccion: String?
)

private const val TAMANO_PAGINA = 50

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val buscarPalabrasUseCase: BuscarPalabrasUseCase,
    private val diccionarioRepository: DiccionarioRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    var query by mutableStateOf("")
        private set
    var resultados by mutableStateOf<List<PalabraConTraduccion>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var idiomaNombre by mutableStateOf("tu idioma")
        private set
    var cargandoMas by mutableStateOf(false)
        private set
    var hayMasResultados by mutableStateOf(true)
        private set

    private var idiomaMetaId: Int? = null
    private var busquedaJob: Job? = null

    init {
        viewModelScope.launch {
            val usuarioId = authRepository.usuarioActualId()
            val usuario = usuarioId?.let { usuarioRepository.obtenerUsuario(it) }
            idiomaMetaId = usuario?.idiomaMeta?.id
            idiomaNombre = usuario?.idiomaMeta?.nombre ?: "tu idioma"
            buscar(query)
        }
    }

    fun onQueryChange(valor: String) {
        query = valor
        buscar(valor)
    }

    private fun buscar(texto: String) {
        val idiomaId = idiomaMetaId ?: return
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            hayMasResultados = true
            try {
                val palabras = buscarPalabrasUseCase(
                    query = texto,
                    idiomaId = idiomaId,
                    limite = TAMANO_PAGINA,
                    offset = 0
                )
                resultados = convertirResultados(palabras)
                hayMasResultados = palabras.size == TAMANO_PAGINA
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al buscar"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarMas() {
        val idiomaId = idiomaMetaId ?: return
        if (isLoading || cargandoMas || !hayMasResultados) return
        cargandoMas = true
        viewModelScope.launch {
            try {
                val palabras = buscarPalabrasUseCase(
                    query = query,
                    idiomaId = idiomaId,
                    limite = TAMANO_PAGINA,
                    offset = resultados.size
                )
                resultados = resultados + convertirResultados(palabras)
                hayMasResultados = palabras.size == TAMANO_PAGINA
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudieron cargar más palabras"
            } finally {
                cargandoMas = false
            }
        }
    }

    private suspend fun convertirResultados(palabras: List<com.aikukisna.app.domain.model.Palabra>) =
        palabras.map { palabra ->
            val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                .firstOrNull()
                ?.palabraDestino
                ?.texto
            PalabraConTraduccion(id = palabra.id, texto = palabra.texto, traduccion = traduccion)
        }
}
