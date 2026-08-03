package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.PalabraDto
import com.aikukisna.app.data.remote.dto.TraduccionDto
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

class DiccionarioRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : DiccionarioRepository {

    private val palabraEmbed = "*, idioma(*), categoria(*), fuente_documento(*)"

    override suspend fun buscarPalabras(
        query: String,
        idiomaId: Int,
        limite: Int,
        offset: Int
    ): List<Palabra> {
        return client.from("palabra")
            .select(Columns.raw(palabraEmbed)) {
                filter {
                    eq("idioma_id", idiomaId)
                    ilike("texto", "%$query%")
                }
                range(offset.toLong(), (offset + limite - 1).toLong())
            }
            .decodeList<PalabraDto>()
            .map { it.toDomain() }
    }

    override suspend fun obtenerPalabraPorId(id: Int): Palabra? {
        return client.from("palabra")
            .select(Columns.raw(palabraEmbed)) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<PalabraDto>()
            ?.toDomain()
    }

    override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> {
        return client.from("traduccion")
            .select(
                Columns.raw(
                    "*, palabra_origen:palabra_origen_id($palabraEmbed), " +
                            "palabra_destino:palabra_destino_id($palabraEmbed)"
                )
            ) {
                filter { eq("palabra_origen_id", palabraId) }
            }
            .decodeList<TraduccionDto>()
            .map { it.toDomain() }
    }
}