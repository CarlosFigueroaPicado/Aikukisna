package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.repository.LogroRepository
import java.util.UUID

class ObtenerLogrosDesbloqueadosUseCase(
    private val logroRepository: LogroRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<LogroDesbloqueado> {
        return logroRepository.obtenerLogrosDesbloqueados(usuarioId)
    }
}