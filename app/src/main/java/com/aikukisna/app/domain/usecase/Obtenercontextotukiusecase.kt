package com.aikukisna.app.domain.usecase

import java.util.UUID
import javax.inject.Inject


private const val MAXIMO_MEMORIAS_A_CARGAR = 8


class ObtenerContextoTukiUseCase @Inject constructor(
    private val obtenerMemoriaTukiUseCase: ObtenerMemoriaTukiUseCase
) {
    suspend operator fun invoke(usuarioId: UUID, idiomaMetaNombre: String): String {
        val base = "Sos Tuki, el asistente de Aikukisna que ayuda a aprender $idiomaMetaNombre. " +
                "Enfocate en ese idioma — si el estudiante pregunta por otro de los idiomas de la " +
                "app, podés mencionarlo brevemente, pero no te desvíes a enseñarlo."

        val memorias = obtenerMemoriaTukiUseCase(usuarioId)
            .sortedByDescending { it.fecha }
            .take(MAXIMO_MEMORIAS_A_CARGAR)

        if (memorias.isEmpty()) return base

        val resumenes = memorias.joinToString(separator = "\n") { "- ${it.resumen}" }
        return "$base\nEsto es lo que ya sabés de conversaciones anteriores con este estudiante " +
                "(usalo si es relevante, no lo repitas literalmente):\n$resumenes"
    }
}