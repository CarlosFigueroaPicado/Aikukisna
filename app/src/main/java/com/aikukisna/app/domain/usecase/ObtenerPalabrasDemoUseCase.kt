package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

private const val IDIOMA_MISKITO = 1
private val PALABRAS_SALUDO_MISKITO = listOf(
    57006, // tingki (tengki, taiki) -> gracias
    2089,  // lî -> agua
    48554, // ¿nahki sma? -> ¿cómo está usted?
    41570, // kaiki was -> adiós
    555    // aihwa -> bien
)

class ObtenerPalabrasDemoUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    companion object {

        const val PALABRAS_DEMO = 5

        private val LECCION_DEMO_POR_IDIOMA = mapOf(
            2 to 38, // Español -> Saludos e Integración Escolar (5 palabras)
            3 to 48, // Inglés Kriol -> Gradiin an Wiidops / Saludos y Despedidas (5 palabras)
            4 to 60  // Inglés Estándar -> Greetings and Polite Expressions (5 palabras)
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