package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID

class QuitarFavoritoUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID, palabraId: Int) {
        usuarioRepository.quitarFavorito(usuarioId, palabraId)
    }
}