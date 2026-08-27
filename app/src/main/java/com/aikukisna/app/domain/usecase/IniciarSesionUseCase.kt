package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

class IniciarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(correo: String, contrasena: String): UUID {
        require(correo.isNotBlank()) { "El correo no puede estar vacío" }
        require(contrasena.isNotBlank()) { "La contraseña no puede estar vacía" }
        return authRepository.iniciarSesion(correo, contrasena)
    }
}