package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.usecase.ObtenerPalabrasDemoUseCase
import com.aikukisna.app.domain.usecase.ObtenerQuizDemoUseCase
import com.aikukisna.app.domain.usecase.ObtenerVocabularioDemoUseCase
import com.aikukisna.app.domain.usecase.PalabraDemo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GuestLeccionViewModel @Inject constructor(
    private val obtenerVocabularioDemoUseCase: ObtenerVocabularioDemoUseCase,
    private val obtenerQuizDemoUseCase: ObtenerQuizDemoUseCase,
    private val leccionRepository: LeccionRepository
) : ViewModel() {

    /** Título de la lección de muestra y cuántas lecciones hay del idioma, para no anunciar cifras falsas. */
    var tituloLeccion by mutableStateOf<String?>(null)
        private set
    var totalLecciones by mutableStateOf<Int?>(null)
        private set

    private var idiomaResumenCargado: Int? = null

    fun cargarResumen(idiomaId: Int) {
        if (idiomaResumenCargado == idiomaId) return
        idiomaResumenCargado = idiomaId
        viewModelScope.launch {
            ObtenerPalabrasDemoUseCase.leccionIdParaIdioma(idiomaId)?.let { id ->
                tituloLeccion = runCatching { leccionRepository.obtenerLeccionPorId(id)?.titulo }.getOrNull()
            }
            totalLecciones = runCatching {
                leccionRepository.obtenerLecciones().count { it.idiomaMeta.id == idiomaId }
            }.getOrNull()?.takeIf { it > 0 }
        }
    }



    var vocabulario by mutableStateOf<List<PalabraDemo>>(emptyList())
        private set
    var indiceActual by mutableStateOf(0)
        private set
    var tarjetaVolteada by mutableStateOf(false)
        private set
    var autoevaluacion by mutableStateOf<Boolean?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set

    private var idiomaIdCargado: Int? = null


    fun cargar(idiomaId: Int) {
        if (idiomaIdCargado == idiomaId) return
        idiomaIdCargado = idiomaId
        viewModelScope.launch {
            isLoading = true
            vocabulario = obtenerVocabularioDemoUseCase(idiomaId)
            isLoading = false
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

    fun autoevaluar(acerto: Boolean) {
        autoevaluacion = acerto
    }


    fun siguiente(): Boolean {
        if (indiceActual >= vocabulario.lastIndex) return true
        indiceActual++
        tarjetaVolteada = false
        tarjetaRevelada = false
        autoevaluacion = null
        return false
    }



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

    private var idiomaIdQuizCargado: Int? = null

    fun cargarQuiz(idiomaId: Int) {
        if (idiomaIdQuizCargado == idiomaId) return
        idiomaIdQuizCargado = idiomaId
        viewModelScope.launch {
            isLoadingQuiz = true
            preguntas = obtenerQuizDemoUseCase(idiomaId)
            isLoadingQuiz = false
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
}