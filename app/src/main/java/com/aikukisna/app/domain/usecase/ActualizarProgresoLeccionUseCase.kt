package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.repository.UsuarioRepository

class ActualizarProgresoLeccionUseCase(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(progreso: ProgresoLeccion) {
        usuarioRepository.actualizarProgreso(progreso)
    }
}