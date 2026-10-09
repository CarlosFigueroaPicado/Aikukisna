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
    /** Devuelve el XP ganado con este intento (lo muestra la pantalla de resultados). */
    suspend operator fun invoke(leccionId: Int, puntaje: Int = 0): Int {
        val usuarioId = authRepository.usuarioActualId()
        val usuarioAnterior = usuarioId?.let { usuarioRepository.obtenerUsuario(it) }
        // Puntaje con que ya estaba aprobada (si lo estaba): repetir una lección no vuelve a dar el XP base.
        val puntajeAnterior: Int? = if (usuarioId == null) null else {
            val progreso: List<ProgresoLeccion> =
                runCatching { usuarioRepository.obtenerProgresoLocal(usuarioId) }.getOrDefault(emptyList())
            progreso.filter { it.leccion.id == leccionId && it.estado == "completada" }
                .mapNotNull { it.puntaje }.maxOrNull()
        }
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
                        xp = usuarioAnterior.xp + xpGanado(puntajeAnterior, puntaje),
                        rachaActual = racha,
                        rachaMaxima = maxOf(usuarioAnterior.rachaMaxima, racha),
                        ultimaActividad = hoy
                    )
                usuarioRepository.guardarUsuarioLocal(usuarioActualizado)
            }
            val progresoLocal = usuarioRepository.obtenerProgresoLocal(usuarioId)
            runCatching { evaluarLogrosUseCase(usuarioId, progresoLocal, usuarioActualizado) }
        }
        return xpGanado(puntajeAnterior, puntaje)
    }

    companion object {
        /** El mismo valor que promete la tarjeta de la lección ("Desde 20 XP"). */
        const val XP_BASE = 20

        /**
         * XP de una lección aprobada; debe coincidir con la función completar_leccion de Supabase.
         * Primera vez: XP_BASE + bono (+5 con 90 % o más, +10 con 100 %). Al repetirla solo cuenta la
         * mejora del bono. Antes se sumaba 20 + porcentaje × 5 (hasta 520 XP) en cada intento.
         */
        fun xpGanado(puntajeAnterior: Int?, puntajeNuevo: Int): Int =
            if (puntajeAnterior == null) XP_BASE + bono(puntajeNuevo)
            else (bono(puntajeNuevo) - bono(puntajeAnterior)).coerceAtLeast(0)

        private fun bono(porcentaje: Int): Int = when {
            porcentaje >= 100 -> 10
            porcentaje >= 90 -> 5
            else -> 0
        }
    }
}
