package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import javax.inject.Inject

class CerrarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.cerrarSesion()
    }
}