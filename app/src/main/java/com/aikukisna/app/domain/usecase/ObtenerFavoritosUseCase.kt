package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID

class ObtenerFavoritosUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<PalabraFavorita> {
        return usuarioRepository.obtenerFavoritos(usuarioId)
    }
}