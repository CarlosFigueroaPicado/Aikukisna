package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

private const val IDIOMA_MISKITO = 1
// La lección 17 mezcla palabras en Miskito y sus glosas en español; para la muestra se toman
// cinco palabras Miskitas de esa lección (alineada con Segunda Lengua, 3.er grado EIB).
private val PALABRAS_SALUDO_MISKITO = listOf(
    60231, // witin -> él/ella
    48554, // ¿nahki sma? -> ¿cómo está usted?
    851,   // pain -> bien
    22732, // yang -> yo
    53680  // siknis ai daukisa -> estoy enfermo
)

class ObtenerPalabrasDemoUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    companion object {

        const val PALABRAS_DEMO = 5

        private val LECCION_DEMO_POR_IDIOMA = mapOf(
            IDIOMA_MISKITO to 17, // Miskito -> Saludos, bienestar y presentación
            2 to 38, // Español -> Saludos e Integración Escolar (5 palabras)
            3 to 48, // Inglés Kriol -> Saludos y despedidas (5 palabras)
            4 to 60  // Inglés Estándar -> Saludos y expresiones de cortesía (5 palabras)
        )


        fun leccionIdParaIdioma(idiomaId: Int): Int? = LECCION_DEMO_POR_IDIOMA[idiomaId]
    }

    suspend operator fun invoke(idiomaId: Int): List<Palabra> {
        if (idiomaId == IDIOMA_MISKITO) {
            return PALABRAS_SALUDO_MISKITO.mapNotNull { diccionarioRepository.obtenerPalabraPorId(it) }
        }

        val leccionId = leccionIdParaIdioma(idiomaId)
            ?: error("No hay lección de muestra configurada para el idioma $idiomaId")

        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> contenido.palabras.take(PALABRAS_DEMO)
            is ContenidoLeccion.Frases -> emptyList()
        }
    }
}