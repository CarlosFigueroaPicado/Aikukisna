package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.repository.LeccionRepository

class ObtenerLeccionesUseCase(
    private val leccionRepository: LeccionRepository
) {
    suspend operator fun invoke(nivel: Int? = null): List<Leccion> {
        return leccionRepository.obtenerLecciones(nivel)
    }
}