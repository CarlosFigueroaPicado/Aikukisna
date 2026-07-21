package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.UsuarioRepository

class ActualizarPerfilUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuario: Usuario) {
        usuarioRepository.actualizarUsuario(usuario)
    }
}