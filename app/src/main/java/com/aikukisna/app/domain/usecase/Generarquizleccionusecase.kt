package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

private const val DISTRACTORES_DESEADOS = 2

class GenerarQuizLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val traduccionParaLeccion: TraduccionParaLeccionUseCase
) {
    suspend operator fun invoke(leccionId: Int): List<PreguntaQuiz> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> generarDesdePalabras(contenido.palabras)
            is ContenidoLeccion.Frases -> generarDeFrases(leccionId, contenido)
        }
    }


    suspend fun generarDesdePalabras(palabras: List<Palabra>): List<PreguntaQuiz> {
        // Cada palabra necesita su traducción real del diccionario (al español, o a la
        // lengua de apoyo si la lección es de Español); no se adivina.
        val traduccionPorPalabraId = palabras.associate { palabra ->
            palabra.id to traduccionParaLeccion(palabra)
        }

        val todasLasRespuestas = traduccionPorPalabraId.values.filterNotNull().distinct()

        return palabras.mapNotNull { palabra ->
            val respuestaCorrecta = traduccionPorPalabraId[palabra.id] ?: return@mapNotNull null
            val opciones = armarOpciones(
                respuestaCorrecta = respuestaCorrecta,
                pool = todasLasRespuestas
            )
            PreguntaQuiz(
                textoPregunta = "¿Qué significa \"${palabra.texto}\"?",
                respuestaCorrecta = respuestaCorrecta,
                opciones = opciones
            )
        }
    }

    private suspend fun generarDeFrases(leccionId: Int, contenido: ContenidoLeccion.Frases): List<PreguntaQuiz> {
        val idiomaMeta = leccionRepository.obtenerLeccionPorId(leccionId)?.idiomaMeta?.id
        val oraciones = idiomaMeta?.let { id ->
            traduccionParaLeccion.idiomasDestino(id).firstNotNullOfOrNull { destino ->
                contenido.oraciones.filter { it.idiomaDestinoId == destino }.takeIf { it.isNotEmpty() }
            }
        } ?: contenido.oraciones
        val todasLasRespuestas = oraciones.map { it.textoDestino }.distinct()

        return oraciones.map { oracion ->
            val opciones = armarOpciones(
                respuestaCorrecta = oracion.textoDestino,
                pool = todasLasRespuestas
            )
            PreguntaQuiz(
                textoPregunta = "¿Qué significa \"${oracion.textoOrigen}\"?",
                respuestaCorrecta = oracion.textoDestino,
                opciones = opciones
            )
        }
    }

    private fun armarOpciones(respuestaCorrecta: String, pool: List<String>): List<String> {
        val distractoresDisponibles = pool.filter { it != respuestaCorrecta }
        val distractores = distractoresDisponibles.shuffled().take(DISTRACTORES_DESEADOS)
        return (distractores + respuestaCorrecta).shuffled()
    }
}