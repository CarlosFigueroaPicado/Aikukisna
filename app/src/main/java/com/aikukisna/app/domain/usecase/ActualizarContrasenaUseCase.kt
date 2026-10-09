package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import javax.inject.Inject

class ActualizarContrasenaUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(nuevaContrasena: String, confirmacion: String) {
        require(nuevaContrasena.length >= 8) {
            "La contraseña debe tener al menos 8 caracteres"
        }
        require(nuevaContrasena.any(Char::isUpperCase)) {
            "La contraseña debe tener una letra mayúscula"
        }
        require(nuevaContrasena.any(Char::isDigit)) {
            "La contraseña debe tener un número"
        }
        require(nuevaContrasena.any { !it.isLetterOrDigit() }) {
            "La contraseña debe tener un símbolo"
        }
        require(nuevaContrasena == confirmacion) {
            "Las contraseñas no coinciden"
        }
        authRepository.actualizarContrasena(nuevaContrasena)
    }
}
