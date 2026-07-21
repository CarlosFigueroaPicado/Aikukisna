package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository

class ObtenerContenidoCulturalDetalleUseCase(
    private val culturaRepository: CulturaRepository
) {
    suspend operator fun invoke(id: Int): CulturaContenido? {
        return culturaRepository.obtenerContenidoCulturalPorId(id)
    }
}