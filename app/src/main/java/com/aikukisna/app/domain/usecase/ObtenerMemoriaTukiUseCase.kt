package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID
import javax.inject.Inject

class ObtenerMemoriaTukiUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<MemoriaTuki> {
        return usuarioRepository.obtenerMemoriaTuki(usuarioId)
    }
}