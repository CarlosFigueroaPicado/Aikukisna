package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.repository.LogroRepository
import java.util.UUID
import javax.inject.Inject

class ObtenerLogrosDesbloqueadosUseCase @Inject constructor(
    private val logroRepository: LogroRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<LogroDesbloqueado> {
        return logroRepository.obtenerLogrosDesbloqueados(usuarioId)
    }
}