package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.LogroRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.model.Usuario
import java.util.UUID
import javax.inject.Inject

class EvaluarLogrosUseCase @Inject constructor(
    private val logroRepository: LogroRepository,
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(
        usuarioId: UUID,
        progresoActual: List<ProgresoLeccion>? = null,
        usuarioActual: Usuario? = null
    ) {
        val logros = logroRepository.obtenerLogros()
        if (logros.isEmpty()) return
        val desbloqueados = logroRepository.obtenerLogrosDesbloqueados(usuarioId)
            .map { it.logro.id }
            .toSet()
        val progreso = progresoActual ?: usuarioRepository.obtenerProgreso(usuarioId)
        val usuario = usuarioActual ?: usuarioRepository.obtenerUsuario(usuarioId)
        val leccionesCompletadas = progreso.count { it.estado == "completada" }
        val racha = usuario?.rachaActual ?: 0

        logros.filterNot { it.id in desbloqueados }.forEach { logro ->
            val cumple = when (logro.condicionTipo) {
                "lecciones_completadas" -> leccionesCompletadas >= logro.condicionValor
                "racha_maxima" -> racha >= logro.condicionValor
                else -> false
            }
            if (cumple) runCatching { logroRepository.desbloquearLogro(usuarioId, logro.id) }
        }
    }
}
