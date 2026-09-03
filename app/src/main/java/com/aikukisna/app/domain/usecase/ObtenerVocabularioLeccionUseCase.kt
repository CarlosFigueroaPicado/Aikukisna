package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

private const val IDIOMA_ESPANOL = 2

data class ItemVocabularioLeccion(
    val textoOrigen: String,
    val textoDestino: String?
)

class ObtenerVocabularioLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(leccionId: Int): List<ItemVocabularioLeccion> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(leccionId)) {
            is ContenidoLeccion.Vocabulario -> contenido.palabras.map { palabra ->
                val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                    .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                    ?.palabraDestino
                    ?.texto
                ItemVocabularioLeccion(textoOrigen = palabra.texto, textoDestino = traduccion)
            }
            is ContenidoLeccion.Frases -> contenido.oraciones.map { oracion ->
                ItemVocabularioLeccion(textoOrigen = oracion.textoOrigen, textoDestino = oracion.textoDestino)
            }
        }
    }
}