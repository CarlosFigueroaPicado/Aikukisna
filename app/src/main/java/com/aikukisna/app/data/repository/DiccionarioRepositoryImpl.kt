package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.PalabraDto
import com.aikukisna.app.data.remote.dto.TraduccionDto
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class DiccionarioRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : DiccionarioRepository {

    private companion object {
        const val PALABRA_EMBED = "*, idioma(*), categoria(*), fuente_documento(*)"

        const val TRADUCCION_EMBED =
            "*, palabra_origen:palabra!traduccion_palabra_origen_id_fkey($PALABRA_EMBED), " +
                    "palabra_destino:palabra!traduccion_palabra_destino_id_fkey($PALABRA_EMBED)"
    }


    override suspend fun buscarPalabras(
        query: String,
        idiomaId: Int,
        limite: Int,
        offset: Int
    ): List<Palabra> {
        return client.from("palabra")
            .select(Columns.raw(PALABRA_EMBED)) {
                filter {
                    eq("idioma_id", idiomaId)
                    ilike("texto", "%$query%")
                }
                order("texto",Order.ASCENDING)
                order("id",Order.ASCENDING)
                range(offset.toLong(), (offset + limite - 1).toLong())
            }
            .decodeList<PalabraDto>()
            .map { it.toDomain() }
    }

    override suspend fun obtenerPalabraPorId(id: Int): Palabra? {
        return client.from("palabra")
            .select(Columns.raw(PALABRA_EMBED)) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<PalabraDto>()
            ?.toDomain()
    }

    override suspend fun obtenerTraducciones(palabraId: Int): List<Traduccion> {
        return client.from("traduccion")
            .select(columns = Columns.raw(TRADUCCION_EMBED)) {
                filter {
                    or {
                        eq("palabra_origen_id", palabraId)
                        eq("palabra_destino_id", palabraId)
                    }
                }
                order("id", Order.ASCENDING)
            }
            .decodeList<TraduccionDto>()
            .map { it.toDomain().orientarDesde(palabraId) }
            .distinctBy { it.palabraDestino.id }
    }
}

private fun Traduccion.orientarDesde(palabraConsultadaId: Int): Traduccion =
    if (palabraOrigen.id == palabraConsultadaId) {
        this

    } else {
        copy(palabraOrigen = palabraDestino, palabraDestino = palabraOrigen)

    }

