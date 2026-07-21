package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID

class ObtenerUsuarioUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(id: UUID): Usuario? {
        return usuarioRepository.obtenerUsuario(id)
    }
}