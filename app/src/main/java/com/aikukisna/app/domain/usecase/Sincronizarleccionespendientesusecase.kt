package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.LeccionRepository
import javax.inject.Inject

class SincronizarLeccionesPendientesUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository
) {
    suspend operator fun invoke(): Int = leccionRepository.sincronizarLeccionesPendientes()
}