package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository
import javax.inject.Inject

class ObtenerContenidoCulturalDetalleUseCase @Inject constructor(
    private val culturaRepository: CulturaRepository
) {
    suspend operator fun invoke(id: Int): CulturaContenido? {
        return culturaRepository.obtenerContenidoCulturalPorId(id)
    }
}