package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.DetectorObjetosLocal
import com.aikukisna.app.domain.repository.RecortadorImagen
import com.aikukisna.app.domain.repository.RegionObjeto
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.ReconocerObjetoUseCase
import com.aikukisna.app.domain.usecase.ReconocerTextoEnImagenUseCase
import com.aikukisna.app.presentacion.pantallas.regionAlrededorDelToque
import com.aikukisna.app.presentacion.pantallas.seleccionarRegionPorToque
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ModoCamara {
    OBJETOS,
    TEXTO
}

/** Punto tocado en la vista previa, en coordenadas normalizadas (0..1). */
data class PuntoToque(val x: Float, val y: Float)

/**
 * La cámara queda siempre en vivo: el usuario toca un objeto (o un texto) en la vista previa
 * y se le muestra su nombre y la traducción al idioma que está aprendiendo, sin tomar fotos.
 */
@HiltViewModel
class CamaraViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val reconocerObjetoUseCase: ReconocerObjetoUseCase,
    private val reconocerTextoEnImagenUseCase: ReconocerTextoEnImagenUseCase,
    private val detectorObjetosLocal: DetectorObjetosLocal,
    private val recortadorImagen: RecortadorImagen
) : ViewModel() {

    var resultado by mutableStateOf<ResultadoReconocimiento?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var modo by mutableStateOf(ModoCamara.OBJETOS)
        private set

    var puntoToque by mutableStateOf<PuntoToque?>(null)
        private set

    var regionSeleccionada by mutableStateOf<RegionObjeto?>(null)
        private set

    var idiomaMeta by mutableStateOf<Idioma?>(null)
        private set

    private var analisis: Job? = null
    private var toqueActual = 0

    init {
        viewModelScope.launch {
            idiomaMeta = runCatching { cargarIdiomaMeta() }.getOrNull()
        }
    }

    fun seleccionarModo(nuevoModo: ModoCamara) {
        if (modo == nuevoModo) return
        modo = nuevoModo
        limpiar()
    }

    /** [imagenBase64] es el cuadro que se ve en pantalla en el momento del toque. */
    fun tocar(imagenBase64: String, punto: PuntoToque) {
        // Un toque nuevo reemplaza al anterior: el usuario puede ir tocando objetos seguidos.
        analisis?.cancel()
        puntoToque = punto
        regionSeleccionada = null
        resultado = null
        errorMessage = null
        val toque = ++toqueActual
        isLoading = true
        analisis = viewModelScope.launch {
            try {
                val idioma = cargarIdiomaMeta().also { idiomaMeta = it }
                val inicio = System.currentTimeMillis()
                resultado = when (modo) {
                    ModoCamara.OBJETOS -> identificarObjeto(imagenBase64, punto, idioma.id)
                    // Solo el texto tocado, marcado en pantalla en cuanto se lee.
                    ModoCamara.TEXTO -> reconocerTextoEnImagenUseCase(imagenBase64, idioma.id, punto.x, punto.y) { leido ->
                        if (toque == toqueActual) regionSeleccionada = leido.region
                    }
                }
                Log.i(ETIQUETA, "modo=$modo idioma=${idioma.id} objeto=${resultado?.objetoDetectado} " +
                    "traduccion=${resultado?.traduccion} ms=${System.currentTimeMillis() - inicio}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(ETIQUETA, "modo=$modo fallo=${e.javaClass.simpleName}: ${e.message}")
                errorMessage = e.message ?: t(R.string.camara_no_se_pudo_reconocer_lo)
            } finally {
                // Un toque cancelado no debe apagar el indicador del toque que lo reemplazó.
                if (toque == toqueActual) isLoading = false
            }
        }
    }

    private suspend fun identificarObjeto(imagenBase64: String, punto: PuntoToque, idiomaId: Int): ResultadoReconocimiento {
        val regiones = runCatching { detectorObjetosLocal.detectar(imagenBase64) }.getOrDefault(emptyList())
        // Si el detector no encerró el objeto tocado, se analiza el área alrededor del dedo.
        val region = seleccionarRegionPorToque(regiones, punto.x, punto.y)
            ?: regionAlrededorDelToque(punto.x, punto.y)
        regionSeleccionada = region
        return reconocerObjetoUseCase(recortadorImagen.recortar(imagenBase64, region), idiomaId)
    }

    fun cerrarResultado() {
        analisis?.cancel()
        limpiar()
    }

    fun registrarErrorCaptura() {
        errorMessage = t(R.string.camara_no_se_pudo_leer_la)
    }

    private fun limpiar() {
        toqueActual++
        resultado = null
        errorMessage = null
        puntoToque = null
        regionSeleccionada = null
        isLoading = false
    }

    private suspend fun cargarIdiomaMeta(): Idioma {
        val userId = authRepository.usuarioActualId() ?: error(t(R.string.camara_no_se_pudo_identificar_al))
        return usuarioRepository.obtenerUsuario(userId)?.idiomaMeta
            ?: error(t(R.string.camara_elige_el_idioma_que_estas))
    }

    private companion object {
        const val ETIQUETA = "CamaraAikukisna"
    }
}
