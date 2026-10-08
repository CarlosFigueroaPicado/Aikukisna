package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Logro
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

data class LogroConEstado(
    val logro: Logro,
    val desbloqueado: Boolean,
    val fecha: Instant?
)

class ObtenerLogrosConEstadoUseCase @Inject constructor(
    private val obtenerLogrosUseCase: ObtenerLogrosUseCase,
    private val obtenerLogrosDesbloqueadosUseCase: ObtenerLogrosDesbloqueadosUseCase
) {
    suspend operator fun invoke(usuarioId: UUID): List<LogroConEstado> {
        val todos = obtenerLogrosUseCase()
        val desbloqueados = obtenerLogrosDesbloqueadosUseCase(usuarioId).associateBy { it.logro.id }
        return todos.map { logro ->
            val d = desbloqueados[logro.id]
            LogroConEstado(logro = logro, desbloqueado = d != null, fecha = d?.fecha)
        }
    }
}