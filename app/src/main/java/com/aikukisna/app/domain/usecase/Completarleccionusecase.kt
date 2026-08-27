package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

class CompletarLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository
) {
    suspend operator fun invoke(leccionId: Int, puntaje: Int = 0) {
        leccionRepository.completarLeccion(leccionId, puntaje)
    }
}