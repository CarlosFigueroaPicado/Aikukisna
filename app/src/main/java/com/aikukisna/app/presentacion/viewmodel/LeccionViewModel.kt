package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.usecase.CompletarLeccionUseCase
import com.aikukisna.app.domain.usecase.GenerarQuizLeccionUseCase
import com.aikukisna.app.domain.usecase.ItemVocabularioLeccion
import com.aikukisna.app.domain.usecase.ObtenerVocabularioLeccionUseCase
import com.aikukisna.app.domain.usecase.RegistrarIntentoLeccionUseCase
import com.aikukisna.app.domain.repository.LeccionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeccionViewModel @Inject constructor(
    private val obtenerVocabularioLeccionUseCase: ObtenerVocabularioLeccionUseCase,
    private val generarQuizLeccionUseCase: GenerarQuizLeccionUseCase,
    private val completarLeccionUseCase: CompletarLeccionUseCase,
    private val registrarIntentoLeccionUseCase: RegistrarIntentoLeccionUseCase,
    private val leccionRepository: LeccionRepository,
    private val obtenerAudioPronunciacion: com.aikukisna.app.domain.usecase.ObtenerAudioPronunciacionUseCase
) : ViewModel() {

    private var idiomaLeccion: com.aikukisna.app.domain.model.Idioma? = null

    /** Audio listo para reproducir; la pantalla lo consume con [consumirAudioTarjeta]. */
    var audioTarjeta by mutableStateOf<ByteArray?>(null)
        private set
    /** Mensaje cuando no hay pronunciación verificada (p. ej. Miskitu sin grabación humana). */
    var avisoAudio by mutableStateOf<String?>(null)
        private set
    var cargandoAudio by mutableStateOf(false)
        private set

    fun escucharTarjeta() {
        val item = vocabulario.getOrNull(indiceActual) ?: return
        val idioma = idiomaLeccion ?: return
        if (cargandoAudio) return
        viewModelScope.launch {
            cargandoAudio = true
            avisoAudio = null
            try {
                val resultado = obtenerAudioPronunciacion(
                    com.aikukisna.app.domain.model.SolicitudPronunciacion(
                        texto = com.aikukisna.app.domain.usecase.limpiarEntradaDiccionario(item.textoOrigen),
                        idioma = idioma,
                        palabraId = item.palabraId
                    )
                )
                audioTarjeta = resultado.audio
                if (resultado.audio == null) avisoAudio = t(R.string.traductor_no_hay_una_pronunciacion_verificada)
            } catch (e: Exception) {
                avisoAudio = t(R.string.traductor_no_se_pudo_reproducir_la)
            } finally {
                cargandoAudio = false
            }
        }
    }

    fun consumirAudioTarjeta() {
        audioTarjeta = null
    }

    var tituloLeccion by mutableStateOf("")
        private set

    var vocabulario by mutableStateOf<List<ItemVocabularioLeccion>>(emptyList())
        private set
    var indiceActual by mutableStateOf(0)
        private set
    var tarjetaVolteada by mutableStateOf(false)
        private set
    var isLoadingVocabulario by mutableStateOf(true)
        private set

    var preguntas by mutableStateOf<List<PreguntaQuiz>>(emptyList())
        private set
    var indicePregunta by mutableStateOf(0)
        private set
    var opcionSeleccionada by mutableStateOf<String?>(null)
        private set
    var respuestasCorrectas by mutableStateOf(0)
        private set
    var isLoadingQuiz by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var leccionIdCargado: Int? = null

    fun cargar(leccionId: Int) {
        if (leccionIdCargado == leccionId) return
        leccionIdCargado = leccionId
        viewModelScope.launch {
            isLoadingVocabulario = true
            errorMessage = null
            try {
                val leccion = leccionRepository.obtenerLeccionPorId(leccionId)
                tituloLeccion = leccion?.titulo.orEmpty()
                idiomaLeccion = leccion?.idiomaMeta
                vocabulario = obtenerVocabularioLeccionUseCase(leccionId)
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.leccion_error_al_cargar_la_leccion)
            } finally {
                isLoadingVocabulario = false
            }
        }
    }

    /** true en cuanto el estudiante vio la traducción: habilita "Siguiente". */
    var tarjetaRevelada by mutableStateOf(false)
        private set

    /** La tarjeta gira cada vez que se toca, para repasar la palabra cuantas veces haga falta. */
    fun voltearTarjeta() {
        tarjetaVolteada = !tarjetaVolteada
        tarjetaRevelada = true
    }

    fun siguienteTarjeta(): Boolean {
        if (indiceActual >= vocabulario.lastIndex) return true
        indiceActual++
        tarjetaVolteada = false
        tarjetaRevelada = false
        avisoAudio = null
        return false
    }

    fun cargarQuiz() {
        val leccionId = leccionIdCargado ?: return
        viewModelScope.launch {
            isLoadingQuiz = true
            errorMessage = null
            try {
                preguntas = generarQuizLeccionUseCase(leccionId)
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.leccion_error_al_generar_el_quiz)
            } finally {
                isLoadingQuiz = false
            }
        }
    }

    fun seleccionarOpcion(opcion: String) {
        opcionSeleccionada = opcion
    }

    fun siguientePregunta(): Boolean {
        if (opcionSeleccionada == preguntas[indicePregunta].respuestaCorrecta) {
            respuestasCorrectas++
        }
        if (indicePregunta >= preguntas.lastIndex) return true
        indicePregunta++
        opcionSeleccionada = null
        return false
    }

    fun completarLeccion(puntajePorcentaje: Int, onCompletada: () -> Unit) {
        val leccionId = leccionIdCargado ?: return
        viewModelScope.launch {
            try {
                completarLeccionUseCase(leccionId, puntajePorcentaje)
                onCompletada()
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.leccion_no_se_pudo_guardar_tu)
            }
        }
    }

    fun registrarIntento(puntajePorcentaje: Int, onRegistrado: () -> Unit) {
        val leccionId = leccionIdCargado ?: return
        viewModelScope.launch {
            try {
                registrarIntentoLeccionUseCase(leccionId, puntajePorcentaje)
                onRegistrado()
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.leccion_no_se_pudo_guardar_el)
            }
        }
    }
}
