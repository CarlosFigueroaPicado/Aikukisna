package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.model.ProgresoLeccion
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class CompletarLeccionUseCase @Inject constructor(
    private val leccionRepository: LeccionRepository,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val evaluarLogrosUseCase: EvaluarLogrosUseCase
) {
    suspend operator fun invoke(leccionId: Int, puntaje: Int = 0) {
        val usuarioId = authRepository.usuarioActualId()
        val usuarioAnterior = usuarioId?.let { usuarioRepository.obtenerUsuario(it) }
        leccionRepository.completarLeccion(leccionId, puntaje)

        if (usuarioId != null) {
            var usuarioActualizado = usuarioAnterior
            val leccion = leccionRepository.obtenerLeccionPorId(leccionId)
            if (leccion != null) {
                usuarioRepository.guardarProgresoLocal(
                    ProgresoLeccion(
                        usuarioId = usuarioId,
                        leccion = leccion,
                        estado = "completada",
                        puntaje = puntaje,
                        fechaCompletado = Instant.now()
                    )
                )
            }
            if (usuarioAnterior != null) {
                val hoy = LocalDate.now()
                val racha = when (usuarioAnterior.ultimaActividad) {
                    hoy -> usuarioAnterior.rachaActual
                    hoy.minusDays(1) -> usuarioAnterior.rachaActual + 1
                    else -> 1
                }
                usuarioActualizado = usuarioAnterior.copy(
                        xp = usuarioAnterior.xp + XP_BASE + puntaje.coerceAtLeast(0) * XP_POR_PUNTO,
                        rachaActual = racha,
                        rachaMaxima = maxOf(usuarioAnterior.rachaMaxima, racha),
                        ultimaActividad = hoy
                    )
                usuarioRepository.guardarUsuarioLocal(usuarioActualizado)
            }
            val progresoLocal = usuarioRepository.obtenerProgresoLocal(usuarioId)
            runCatching { evaluarLogrosUseCase(usuarioId, progresoLocal, usuarioActualizado) }
        }
    }

    private companion object {
        const val XP_BASE = 20
        const val XP_POR_PUNTO = 5
    }
}
