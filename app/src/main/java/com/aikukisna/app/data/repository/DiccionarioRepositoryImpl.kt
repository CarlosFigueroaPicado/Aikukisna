package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.CacheEscritor
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import javax.inject.Inject

class DiccionarioRepositoryImpl @Inject constructor(
    private val cache: CacheEscritor
) : DiccionarioRepository {

    override suspend fun buscarPalabras(
        query: String,
        idiomaId: Int,
        limite: Int,
        offset: Int,
        categoriaId: Int?
    ): List<Palabra> {
        return cache.buscarPalabrasCacheadas(query, idiomaId, limite, offset)
            .let { palabras ->
                if (categoriaId == null) palabras else palabras.filter { it.categoria?.id == categoriaId }
            }
    }

    override suspend fun obtenerPalabraPorId(id: Int): Palabra? = cache.leerPalabra(id)

    override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> {
        return cache.leerTraducciones(palabraId)
            .map { it.orientarDesde(palabraId) }
            .distinctBy { it.palabraDestino.id }
    }

    override suspend fun buscarOracionExacta(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): String? = cache.buscarOracionExacta(texto, idiomaOrigenId, idiomaDestinoId)

    override suspend fun obtenerOracionesPorIdiomas(
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): List<OracionEjemplo> = cache.obtenerOracionesPorIdiomas(idiomaOrigenId, idiomaDestinoId)
}

private fun Traduccion.orientarDesde(palabraConsultadaId: Int): Traduccion =
    if (palabraOrigen.id == palabraConsultadaId) {
        this
    } else {
        copy(palabraOrigen = palabraDestino, palabraDestino = palabraOrigen)
    }
