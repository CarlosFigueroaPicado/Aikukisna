package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

data class ItemVocabularioLeccion(
    val textoOrigen: String,
    val textoDestino: String?,
    /** Para buscar la grabación verificada de la palabra; las frases no tienen. */
    val palabraId: Int? = null
)

class ObtenerVocabularioLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val traduccionParaLeccion: TraduccionParaLeccionUseCase
) {
    suspend operator fun invoke(leccionId: Int): List<ItemVocabularioLeccion> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> contenido.palabras.map { palabra ->
                ItemVocabularioLeccion(textoOrigen = palabra.texto, textoDestino = traduccionParaLeccion(palabra), palabraId = palabra.id)
            }
            is ContenidoLeccion.Frases -> {
                val idiomaMeta = leccionRepository.obtenerLeccionPorId(leccionId)?.idiomaMeta?.id
                val preferidos = idiomaMeta?.let(traduccionParaLeccion::idiomasDestino).orEmpty()
                val oraciones = preferidos.firstNotNullOfOrNull { destino ->
                    // Si la lección trae frases en varias lenguas de apoyo, se muestra la del estudiante.
                    contenido.oraciones.filter { it.idiomaDestinoId == destino }.takeIf { it.isNotEmpty() }
                } ?: contenido.oraciones
                oraciones.map { oracion ->
                    // Sin frases documentadas en su lengua de apoyo, se avisa en qué idioma está la que se muestra
                    // (un hablante de Miskito no debe ver Kriol como si fuera su traducción).
                    val otroIdioma = preferidos.isNotEmpty() && oracion.idiomaDestinoId != preferidos.first()
                    val destino = if (otroIdioma) {
                        Idioma.DISPONIBLES.firstOrNull { it.id == oracion.idiomaDestinoId }
                            ?.let { "En ${it.nombre}: ${oracion.textoDestino}" } ?: oracion.textoDestino
                    } else oracion.textoDestino
                    ItemVocabularioLeccion(textoOrigen = oracion.textoOrigen, textoDestino = destino)
                }
            }
        }
    }
}
