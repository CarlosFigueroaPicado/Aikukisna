package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.PreguntaQuiz
import javax.inject.Inject

class ObtenerQuizDemoUseCase @Inject constructor(
    private val obtenerPalabrasDemoUseCase: ObtenerPalabrasDemoUseCase,
    private val generarQuizLeccionUseCase: GenerarQuizLeccionUseCase
) {
    companion object {

        const val PREGUNTAS_DEMO = 2
    }

    suspend operator fun invoke(idiomaId: Int): List<PreguntaQuiz> {
        val palabras = obtenerPalabrasDemoUseCase(idiomaId)
        return generarQuizLeccionUseCase.generarDesdePalabras(palabras).take(PREGUNTAS_DEMO)
    }
}