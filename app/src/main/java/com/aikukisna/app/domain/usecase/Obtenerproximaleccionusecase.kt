package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import java.util.UUID
import javax.inject.Inject


data class ProximaLeccion(
    val leccion: Leccion,
    val numPalabras: Int
)
class ObtenerProximaLeccionUseCase @Inject constructor(
    private val obtenerLeccionesUseCase: ObtenerLeccionesUseCase,
    private val obtenerProgresoUseCase: ObtenerProgresoUseCase,
    private val leccionRepository: LeccionRepository
) {
    suspend operator fun invoke(usuarioId: UUID, idiomaMetaId: Int): ProximaLeccion? {
        val leccionesDelIdioma = obtenerLeccionesUseCase()
            .filter { it.idiomaMeta.id == idiomaMetaId }
            .sortedWith(compareBy({ it.nivel }, { it.capituloNumero ?: 0 }, { it.id }))

        if (leccionesDelIdioma.isEmpty()) return null

        val completadasIds = obtenerProgresoUseCase(usuarioId)
            .filter { it.estado == "completada" }
            .map { it.leccion.id }
            .toSet()

        val siguiente = leccionesDelIdioma.firstOrNull { it.id !in completadasIds }
            ?: leccionesDelIdioma.first() // si ya completó todas, repasa la primera

        val numPalabras = when (val contenido = leccionRepository.obtenerContenidoLeccion(siguiente.id)) {
            is ContenidoLeccion.Vocabulario -> contenido.palabras.size
            is ContenidoLeccion.Frases -> contenido.oraciones.size
        }

        return ProximaLeccion(leccion = siguiente, numPalabras = numPalabras)
    }
}