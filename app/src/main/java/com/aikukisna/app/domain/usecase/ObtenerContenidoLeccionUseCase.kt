package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

class ObtenerContenidoLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository
) {
    suspend operator fun invoke(leccionId: Int): ContenidoLeccion {
        return leccionRepository.obtenerContenidoLeccion(leccionId)
    }
}