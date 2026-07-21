package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.LogroRepository
import java.util.UUID

class DesbloquearLogroUseCase(
    private val logroRepository: LogroRepository
) {
    suspend operator fun invoke(usuarioId: UUID, logroId: Int) {
        logroRepository.desbloquearLogro(usuarioId, logroId)
    }
}