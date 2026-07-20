package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.repository.UsuarioRepository

class GuardarMemoriaTukiUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(memoria: MemoriaTuki) {
        usuarioRepository.guardarMemoriaTuki(memoria)
    }
}