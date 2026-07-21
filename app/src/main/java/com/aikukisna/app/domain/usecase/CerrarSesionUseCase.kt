package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository

class CerrarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        authRepository.cerrarSesion()
    }
}