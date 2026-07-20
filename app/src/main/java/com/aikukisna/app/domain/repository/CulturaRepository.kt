package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.CulturaContenido

interface CulturaRepository {
    suspend fun obtenerContenidoCultural(): List<CulturaContenido>
    suspend fun obtenerContenidoCulturalPorId(id: Int): CulturaContenido?
}