package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository
import javax.inject.Inject

class ObtenerContenidoCulturalUseCase @Inject constructor(
    private val culturaRepository: CulturaRepository
) {
    suspend operator fun invoke(): List<CulturaContenido> {
        return culturaRepository.obtenerContenidoCultural()
    }
}