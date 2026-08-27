package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.VozRepository
import javax.inject.Inject

class ObtenerAudioPronunciacionUseCase @Inject constructor(
    private val vozRepository: VozRepository
) {
    suspend operator fun invoke(texto: String, voiceId: String? = null): ByteArray {
        require(texto.isNotBlank()) { "El texto no puede estar vacío" }
        return vozRepository.sintetizarVoz(texto, voiceId)
    }
}