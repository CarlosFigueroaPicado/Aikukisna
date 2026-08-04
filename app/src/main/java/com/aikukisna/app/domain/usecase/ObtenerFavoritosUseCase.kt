package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID
import javax.inject.Inject

class ObtenerFavoritosUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<PalabraFavorita> {
        return usuarioRepository.obtenerFavoritos(usuarioId)
    }
}