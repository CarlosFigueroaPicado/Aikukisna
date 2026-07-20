package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID

class ObtenerMemoriaTukiUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<MemoriaTuki> {
        return usuarioRepository.obtenerMemoriaTuki(usuarioId)
    }
}