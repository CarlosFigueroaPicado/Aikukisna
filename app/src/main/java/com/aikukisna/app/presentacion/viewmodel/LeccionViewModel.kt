package com.aikukisna.app.presentacion.viewmodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeccionViewModel @Inject constructor(
    private val obtenerVocabularioLeccionUseCase: ObtenerVocabularioLeccionUseCase,
    private val generarQuizLeccionUseCase: GenerarQuizLeccionUseCase,
    private val completarLeccionUseCase: CompletarLeccionUseCase
) : ViewModel() {

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
                vocabulario = obtenerVocabularioLeccionUseCase(leccionId)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Error al cargar la lección"
            } finally {
                isLoadingVocabulario = false
            }
        }
    }

    fun voltearTarjeta() {
        tarjetaVolteada = true
    }

    fun siguienteTarjeta(): Boolean {
        if (indiceActual >= vocabulario.lastIndex) return true
        indiceActual++
        tarjetaVolteada = false
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
                errorMessage = e.message ?: "Error al generar el quiz"
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

    fun completarLeccion() {
        val leccionId = leccionIdCargado ?: return
        viewModelScope.launch {
            try {
                completarLeccionUseCase(leccionId, respuestasCorrectas)
            } catch (e: Exception) {
                errorMessage = e.message ?: "No se pudo guardar tu progreso"
            }
        }
    }
}