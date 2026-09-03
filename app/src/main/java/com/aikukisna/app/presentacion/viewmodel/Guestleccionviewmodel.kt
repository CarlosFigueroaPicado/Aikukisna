package com.aikukisna.app.presentacion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.usecase.ObtenerQuizDemoUseCase
import com.aikukisna.app.domain.usecase.ObtenerVocabularioDemoUseCase
import com.aikukisna.app.domain.usecase.PalabraDemo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class GuestLeccionViewModel @Inject constructor(
    private val obtenerVocabularioDemoUseCase: ObtenerVocabularioDemoUseCase,
    private val obtenerQuizDemoUseCase: ObtenerQuizDemoUseCase
) : ViewModel() {



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

    fun voltearTarjeta() {
        tarjetaVolteada = true
    }

    fun autoevaluar(acerto: Boolean) {
        autoevaluacion = acerto
    }


    fun siguiente(): Boolean {
        if (indiceActual >= vocabulario.lastIndex) return true
        indiceActual++
        tarjetaVolteada = false
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