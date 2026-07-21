package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID

class MarcarFavoritoUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID, palabraId: Int) {
        usuarioRepository.marcarFavorito(usuarioId, palabraId)
    }
}