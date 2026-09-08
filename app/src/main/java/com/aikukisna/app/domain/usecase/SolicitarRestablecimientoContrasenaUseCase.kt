package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import javax.inject.Inject

class SolicitarRestablecimientoContrasenaUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(correo: String) {
        require(correo.isNotBlank()) { "Escribe tu correo electrónico" }
        require(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(correo)) {
            "Escribe un correo electrónico válido"
        }
        authRepository.solicitarRestablecimientoContrasena(correo.trim())
    }
}
