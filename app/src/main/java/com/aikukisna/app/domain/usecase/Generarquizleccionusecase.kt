package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

// Id fijo, según la tabla `idioma` ya verificada en producción.
private const val IDIOMA_ESPANOL = 2

// Cuántas opciones incorrectas se buscan por pregunta. Si la lección no
// tiene suficiente material para llegar a este número, se usan las que
// haya — nunca se inventa una opción falsa que no venga del contenido real.
private const val DISTRACTORES_DESEADOS = 2

class GenerarQuizLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(leccionId: Int): List<PreguntaQuiz> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> generarDeVocabulario(contenido)
            is ContenidoLeccion.Frases -> generarDeFrases(contenido)
        }
    }

    private suspend fun generarDeVocabulario(contenido: ContenidoLeccion.Vocabulario): List<PreguntaQuiz> {
        // Cada palabra necesita su traducción real al español — no se
        // adivina, se busca la misma que usaría el diccionario.
        val traduccionPorPalabraId = contenido.palabras.associate { palabra ->
            val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                ?.palabraDestino
                ?.texto
            palabra.id to traduccion
        }

        val todasLasRespuestas = traduccionPorPalabraId.values.filterNotNull().distinct()

        return contenido.palabras.mapNotNull { palabra ->
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

    private fun generarDeFrases(contenido: ContenidoLeccion.Frases): List<PreguntaQuiz> {
        val todasLasRespuestas = contenido.oraciones.map { it.textoDestino }.distinct()

        return contenido.oraciones.map { oracion ->
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