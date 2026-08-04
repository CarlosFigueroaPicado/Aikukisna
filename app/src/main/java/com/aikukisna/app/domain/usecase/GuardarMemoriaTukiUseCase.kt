package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.repository.UsuarioRepository
import javax.inject.Inject

class GuardarMemoriaTukiUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(memoria: MemoriaTuki) {
        usuarioRepository.guardarMemoriaTuki(memoria)
    }
}