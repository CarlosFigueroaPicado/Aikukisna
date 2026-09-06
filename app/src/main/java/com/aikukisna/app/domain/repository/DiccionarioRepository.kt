package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion

interface DiccionarioRepository {
    suspend fun buscarPalabras(
        query: String,
        idiomaId: Int,
        limite: Int = 50,
        offset: Int = 0,
        categoriaId: Int? = null
    ): List<Palabra>
    suspend fun obtenerPalabraPorId(id: Int): Palabra?
    suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion>
}