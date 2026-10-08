package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.time.Instant
import javax.inject.Inject

class RegistrarIntentoLeccionUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val leccionRepository: LeccionRepository,
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(leccionId: Int, puntaje: Int) {
        val usuarioId = authRepository.usuarioActualId() ?: return
        val leccion = leccionRepository.obtenerLeccionPorId(leccionId) ?: return
        usuarioRepository.guardarProgresoLocal(
            ProgresoLeccion(
                usuarioId = usuarioId,
                leccion = leccion,
                estado = "en_progreso",
                puntaje = puntaje,
                fechaCompletado = Instant.now()
            )
        )
    }
}
