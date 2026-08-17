package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

class ObtenerPalabrasDemoUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository
) {
    companion object {

        const val LECCION_DEMO_ID = 2
    }

    suspend operator fun invoke(): List<Palabra> {
        return when (val contenido = leccionRepository.obtenerContenidoLeccion(LECCION_DEMO_ID)) {
            is ContenidoLeccion.Vocabulario -> contenido.palabras
            is ContenidoLeccion.Frases -> emptyList()
        }
    }
}