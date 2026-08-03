package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.CulturaContenidoDto
import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

class CulturaRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : CulturaRepository {

    private val contenidoEmbed = "*, fuente_documento(*)"

    override suspend fun obtenerContenidoCultural(): List<CulturaContenido> {
        return client.from("cultura_contenido")
            .select(Columns.raw(contenidoEmbed))
            .decodeList<CulturaContenidoDto>()
            .map { it.toDomain() }
    }

    override suspend fun obtenerContenidoCulturalPorId(id: Int): CulturaContenido? {
        return client.from("cultura_contenido")
            .select(Columns.raw(contenidoEmbed)) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<CulturaContenidoDto>()
            ?.toDomain()
    }
}

private fun CulturaContenidoDto.toDomain() = CulturaContenido(
    id = id,
    titulo = titulo,
    contenido = contenido,
    rangoPaginaInicio = rangoPaginaInicio,
    rangoPaginaFin = rangoPaginaFin,
    fuente = fuente.toDomain()
)