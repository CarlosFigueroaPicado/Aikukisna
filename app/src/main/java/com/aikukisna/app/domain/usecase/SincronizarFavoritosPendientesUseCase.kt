package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.UsuarioRepository
import javax.inject.Inject

class SincronizarFavoritosPendientesUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(): Int = usuarioRepository.sincronizarFavoritosPendientes()
}
