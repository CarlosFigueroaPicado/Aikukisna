package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.PreguntaQuiz
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

private const val IDIOMA_ESPANOL = 2

private const val DISTRACTORES_DESEADOS = 2

class GenerarQuizLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(leccionId: Int): List<PreguntaQuiz> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> generarDesdePalabras(contenido.palabras)
            is ContenidoLeccion.Frases -> generarDeFrases(contenido)
        }
    }


    suspend fun generarDesdePalabras(palabras: List<Palabra>): List<PreguntaQuiz> {
        // Cada palabra necesita su traducción real al español — no se
        // adivina, se busca la misma que usaría el diccionario.
        val traduccionPorPalabraId = palabras.associate { palabra ->
            val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                ?.palabraDestino
                ?.texto
            palabra.id to traduccion
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