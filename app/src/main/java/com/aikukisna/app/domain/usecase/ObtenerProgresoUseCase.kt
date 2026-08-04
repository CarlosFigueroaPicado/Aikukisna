package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.util.UUID
import javax.inject.Inject

class ObtenerProgresoUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuarioId: UUID): List<ProgresoLeccion> {
        return usuarioRepository.obtenerProgreso(usuarioId)
    }
}