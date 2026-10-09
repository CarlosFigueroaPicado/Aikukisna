package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

class IniciarSesionUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(identificador: String, contrasena: String): UUID {
        val identificadorNormalizado = identificador.trim()
        require(identificadorNormalizado.isNotBlank()) { "El identificador no puede estar vacío" }
        require(contrasena.isNotBlank()) { "La contraseña no puede estar vacía" }
        return try {
            if (PATRON_CORREO.matches(identificadorNormalizado)) {
                authRepository.iniciarSesionConCorreo(identificadorNormalizado, contrasena)
            } else {
                authRepository.iniciarSesionConNombreUsuario(identificadorNormalizado, contrasena)
            }
        } catch (_: Exception) {
            throw CredencialesInvalidasException()
        }
    }

    companion object {
        private val PATRON_CORREO = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val MENSAJE_CREDENCIALES_INVALIDAS = "Usuario/correo o contraseña incorrectos."
    }
}

class CredencialesInvalidasException : Exception(IniciarSesionUseCase.MENSAJE_CREDENCIALES_INVALIDAS)
