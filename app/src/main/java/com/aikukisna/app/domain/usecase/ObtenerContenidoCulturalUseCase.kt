package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository

class ObtenerContenidoCulturalUseCase(
    private val culturaRepository: CulturaRepository
) {
    suspend operator fun invoke(): List<CulturaContenido> {
        return culturaRepository.obtenerContenidoCultural()
    }
}