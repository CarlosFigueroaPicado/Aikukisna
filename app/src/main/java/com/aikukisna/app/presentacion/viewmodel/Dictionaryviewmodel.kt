package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.usecase.BuscarDiccionarioBidireccionalUseCase
import com.aikukisna.app.domain.usecase.BuscarPalabrasUseCase
import com.aikukisna.app.domain.usecase.ResultadoDiccionarioBidireccional
import com.aikukisna.app.domain.usecase.limpiarEntradaDiccionario
import com.aikukisna.app.data.local.SembradorReplicaSupabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PalabraConTraduccion(
    val palabraId: Int? = null,
    val texto: String,
    val traduccion: String?,
    val categoriaId: Int? = null,
    val categoriaNombre: String? = null,
    val esSugerencia: Boolean = false
)

private val IDIOMA_ESPANOL = Idioma.DISPONIBLES.first { it.codigo == "es" }

private const val TAMANO_PAGINA = 50

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val buscarDiccionario: BuscarDiccionarioBidireccionalUseCase,
    private val buscarPalabrasUseCase: BuscarPalabrasUseCase,
    private val diccionarioRepository: DiccionarioRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val sembradorReplica: SembradorReplicaSupabase,
    private val preferencias: PreferenciasAprendizaje
) : ViewModel() {

    var query by mutableStateOf("")
        private set
    var resultados by mutableStateOf<List<PalabraConTraduccion>>(emptyList())
        private set
    var idiomaAprendizaje by mutableStateOf<Idioma?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var favoritosIds by mutableStateOf<Set<Int>>(emptySet())
        private set
    /** Con el buscador vacío se exploran las palabras del idioma de 50 en 50 (búsqueda paginada, PR #56). */
    var cargandoMas by mutableStateOf(false)
        private set
    var hayMasResultados by mutableStateOf(false)
        private set

    /** Quien aprende español busca entre el español y su lengua de apoyo, no "Español o Español". */
    val idiomaContraparte: Idioma
        get() = if (idiomaAprendizaje?.id == IDIOMA_ESPANOL.id) {
            Idioma.DISPONIBLES.firstOrNull { it.id == preferencias.lenguaApoyoId() } ?: IDIOMA_ESPANOL
        } else IDIOMA_ESPANOL

    private var busquedaJob: Job? = null
    private var numeroBusqueda = 0
    private var usuarioId: UUID? = null

    init {
        viewModelScope.launch {
            usuarioRepository.observarIdiomaMeta().collect { idioma ->
                if (idioma != null && idioma.id == idiomaAprendizaje?.id) return@collect
                if (idiomaAprendizaje != null) resultados = emptyList()
                cargarIdiomaAprendizaje()
            }
        }
    }

    fun onQueryChange(valor: String) {
        query = valor
        if (idiomaAprendizaje != null) buscar(valor)
    }

    fun buscarAhora() {
        if (idiomaAprendizaje != null) buscar(query)
    }

    fun alternarFavorito(palabraId: Int) {
        val idUsuario = usuarioId ?: return
        viewModelScope.launch {
            try {
                if (palabraId in favoritosIds) {
                    usuarioRepository.quitarFavorito(idUsuario, palabraId)
                    favoritosIds = favoritosIds - palabraId
                } else {
                    usuarioRepository.marcarFavorito(idUsuario, palabraId)
                    favoritosIds = favoritosIds + palabraId
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.dictionary_no_se_pudo_actualizar_favoritos)
            }
        }
    }

    private fun cargarIdiomaAprendizaje() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                sembradorReplica.sembrarSiExiste()
                check(sembradorReplica.hayContenidoDisponible()) {
                    t(R.string.dictionary_el_contenido_local_todavia_no)
                }
                val userId = authRepository.usuarioActualId()
                    ?: throw IllegalStateException(t(R.string.dictionary_sesion_no_iniciada))
                usuarioId = userId
                idiomaAprendizaje = usuarioRepository.obtenerUsuario(userId)?.idiomaMeta
                    ?: throw IllegalStateException(t(R.string.dictionary_todavia_no_elegiste_un_idioma))
                buscar(query)
                // Los favoritos pueden venir de la red: no deben retrasar la búsqueda.
                viewModelScope.launch {
                    runCatching { usuarioRepository.obtenerFavoritos(userId) }
                        .onSuccess { lista -> favoritosIds = lista.map { it.palabra.id }.toSet() }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.dictionary_no_se_pudo_cargar_el)
            } finally {
                isLoading = false
            }
        }
    }

    private fun buscar(texto: String) {
        val idiomaMeta = idiomaAprendizaje ?: return

        busquedaJob?.cancel()
        val busquedaActual = ++numeroBusqueda
        busquedaJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                if (texto.isBlank()) {
                    val pagina = paginaExploracion(idiomaMeta, offset = 0)
                    if (busquedaActual == numeroBusqueda) {
                        offsetExploracion = pagina.leidas
                        resultados = pagina.palabras
                        hayMasResultados = pagina.leidas == TAMANO_PAGINA
                    }
                    return@launch
                }
                if (busquedaActual == numeroBusqueda) hayMasResultados = false
                val nuevosResultados = when (
                    val resultado = buscarDiccionario(texto, idiomaContraparte, idiomaMeta)
                ) {
                    is ResultadoDiccionarioBidireccional.Exactas -> resultado.entradas.flatMap { entrada ->
                        if (entrada.traducciones.isEmpty()) {
                            listOf(entrada.aResultado(null))
                        } else {
                            entrada.traducciones.map { entrada.aResultado(it.palabra) }
                        }
                    }
                    is ResultadoDiccionarioBidireccional.Sugerencias -> resultado.palabras.map { palabra ->
                        PalabraConTraduccion(
                            palabraId = palabra.id,
                            texto = palabra.texto,
                            traduccion = null,
                            categoriaId = palabra.categoria?.takeUnless(::esCategoriaGenerica)?.id,
                            categoriaNombre = palabra.categoria?.takeUnless(::esCategoriaGenerica)?.nombre,
                            esSugerencia = true
                        )
                    }
                    ResultadoDiccionarioBidireccional.NoEncontrada -> emptyList()
                }.map { it.copy(texto = limpiarEntradaDiccionario(it.texto), traduccion = it.traduccion?.let(::limpiarEntradaDiccionario)) }
                    // "lî", "Li" y "li." son la misma traducción escrita de distinta forma.
                    .distinctBy { claveDuplicado(it.texto) to it.traduccion?.let(::claveDuplicado) }
                    .sortedBy { it.texto.lowercase() }
                if (busquedaActual == numeroBusqueda) resultados = nuevosResultados
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (busquedaActual == numeroBusqueda) {
                    errorMessage = e.message ?: t(R.string.dictionary_error_al_buscar)
                }
            } finally {
                if (busquedaActual == numeroBusqueda) isLoading = false
            }
        }
    }

    /** Siguiente página de la exploración (buscador vacío). La pantalla la pide al llegar al final de la lista. */
    fun cargarMas() {
        val idiomaMeta = idiomaAprendizaje ?: return
        if (query.isNotBlank() || isLoading || cargandoMas || !hayMasResultados) return
        val busquedaActual = numeroBusqueda
        cargandoMas = true
        viewModelScope.launch {
            try {
                val pagina = paginaExploracion(idiomaMeta, offset = offsetExploracion)
                if (busquedaActual == numeroBusqueda) {
                    offsetExploracion += pagina.leidas
                    resultados = (resultados + pagina.palabras)
                        .distinctBy { claveDuplicado(it.texto) to it.traduccion?.let(::claveDuplicado) }
                    hayMasResultados = pagina.leidas == TAMANO_PAGINA
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.dictionary_error_al_buscar)
            } finally {
                cargandoMas = false
            }
        }
    }

    private var offsetExploracion = 0

    private class PaginaExploracion(val palabras: List<PalabraConTraduccion>, val leidas: Int)

    private suspend fun paginaExploracion(idiomaMeta: Idioma, offset: Int): PaginaExploracion {
        val palabras = buscarPalabrasUseCase(query = "", idiomaId = idiomaMeta.id, limite = TAMANO_PAGINA, offset = offset)
        val contraparte = idiomaContraparte.id
        val convertidas = palabras.map { palabra ->
            // La traducción al idioma con que se busca (español o la lengua de apoyo), si existe.
            val traducciones = diccionarioRepository.obtenerTraducciones(palabra.id)
            val traduccion = (traducciones.firstOrNull { it.palabraDestino.idioma.id == contraparte }
                ?: traducciones.firstOrNull())?.palabraDestino
            PalabraConTraduccion(
                palabraId = palabra.id,
                texto = limpiarEntradaDiccionario(palabra.texto),
                traduccion = traduccion?.texto?.let(::limpiarEntradaDiccionario),
                categoriaId = palabra.categoria?.takeUnless(::esCategoriaGenerica)?.id,
                categoriaNombre = palabra.categoria?.takeUnless(::esCategoriaGenerica)?.nombre
            )
        }.distinctBy { claveDuplicado(it.texto) to it.traduccion?.let(::claveDuplicado) }
        return PaginaExploracion(convertidas, leidas = palabras.size)
    }

    private fun com.aikukisna.app.domain.conocimiento.EntradaConocimiento.aResultado(
        traduccion: com.aikukisna.app.domain.model.Palabra?
    ) = PalabraConTraduccion(
        palabraId = palabra.id,
        texto = palabra.texto,
        traduccion = traduccion?.texto,
        categoriaId = (palabra.categoria ?: traduccion?.categoria)?.takeUnless(::esCategoriaGenerica)?.id,
        categoriaNombre = (palabra.categoria ?: traduccion?.categoria)?.takeUnless(::esCategoriaGenerica)?.nombre
    )

    // La importación del diccionario marcó como "Gramática" cientos de palabras comunes ("silla", "agua").
    private fun esCategoriaGenerica(categoria: com.aikukisna.app.domain.model.Categoria): Boolean =
        categoria.nombre.equals("Gramática", ignoreCase = true)

    private fun claveDuplicado(texto: String): String =
        java.text.Normalizer.normalize(texto.lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .trim()
}
