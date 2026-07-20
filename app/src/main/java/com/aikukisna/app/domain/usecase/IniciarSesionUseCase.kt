package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import java.util.UUID

class IniciarSesionUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(correo: String, contrasena: String): UUID {
        return authRepository.iniciarSesion(correo, contrasena)
    }
}