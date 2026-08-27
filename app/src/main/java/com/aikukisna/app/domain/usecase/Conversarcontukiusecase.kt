package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject


private const val MAXIMO_MENSAJES_HISTORIAL = 30


class ConversarConTukiUseCase @Inject constructor(
    private val iaRepository: IaRepository
) {
    suspend operator fun invoke(historial: List<MensajeChat>, contexto: String? = null): String {
        require(historial.isNotEmpty()) { "El historial no puede estar vacío" }

        val historialAEnviar = if (historial.size > MAXIMO_MENSAJES_HISTORIAL) {
            historial.takeLast(MAXIMO_MENSAJES_HISTORIAL)
        } else {
            historial
        }

        return iaRepository.conversar(historialAEnviar, contexto)
    }
}