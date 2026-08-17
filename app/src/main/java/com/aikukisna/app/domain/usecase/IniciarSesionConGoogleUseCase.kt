package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import java.util.UUID
import javax.inject.Inject

class IniciarSesionConGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(idTokenGoogle: String, nonce: String? = null): UUID =
        authRepository.iniciarSesionConGoogle(idTokenGoogle, nonce)
}