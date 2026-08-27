package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.CacheEscritor
import com.aikukisna.app.data.remote.dto.CategoriaDto
import com.aikukisna.app.data.remote.dto.FuenteDocumentoDto
import com.aikukisna.app.data.remote.dto.IdiomaDto
import com.aikukisna.app.data.remote.dto.LeccionDto
import com.aikukisna.app.data.remote.dto.PalabraDto
import com.aikukisna.app.data.remote.dto.TraduccionDto
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.repository.SincronizacionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SincronizacionRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val cache: CacheEscritor
) : SincronizacionRepository {

    private companion object {
        const val PALABRA_EMBED = "*, idioma(*), categoria(*), fuente_documento(*)"
        const val TRADUCCION_EMBED =
            "*, palabra_origen:palabra!traduccion_palabra_origen_id_fkey($PALABRA_EMBED), " +
                    "palabra_destino:palabra!traduccion_palabra_destino_id_fkey($PALABRA_EMBED)"
        const val TAMANO_PAGINA = 1000

        // Conteos de referencia de la última verificación contra producción
        // (no son exactos para siempre — la base sigue creciendo) — solo
        // sirven para calcular un progreso aproximado, no una cuenta exacta.
        const val TOTAL_ESTIMADO =
            4 /*idiomas*/ + 10 /*categorias*/ + 8 /*fuentes*/ +
                    63_703 /*palabras*/ + 56_879 /*traducciones*/ +
                    76 /*lecciones*/ + 350 /*leccion_palabra*/ + 499 /*oraciones*/
    }

    override suspend fun hayDatosDescargados(): Boolean = cache.hayAlgoDescargado()

    override fun sincronizarTodo(): Flow<EstadoSincronizacion> = flow {
        var procesados = 0
        fun progreso() = (procesados.toFloat() / TOTAL_ESTIMADO).coerceIn(0f, 0.99f)

        try {
            emit(EstadoSincronizacion.EnProgreso("Idiomas y categorías", progreso()))
            val idiomas = client.from("idioma").select().decodeList<IdiomaDto>().map { it.toDomain() }
            cache.cachearIdiomas(idiomas)
            procesados += idiomas.size

            val categorias = client.from("categoria").select().decodeList<CategoriaDto>().map { it.toDomain() }
            cache.cachearCategorias(categorias)
            procesados += categorias.size

            val fuentes = client.from("fuente_documento").select().decodeList<FuenteDocumentoDto>().map { it.toDomain() }
            cache.cachearFuentes(fuentes)
            procesados += fuentes.size

            emit(EstadoSincronizacion.EnProgreso("Diccionario", progreso()))
            var offset = 0
            while (true) {
                val pagina = client.from("palabra")
                    .select(Columns.raw(PALABRA_EMBED)) {
                        range(offset.toLong(), (offset + TAMANO_PAGINA - 1).toLong())
                    }
                    .decodeList<PalabraDto>()
                    .map { it.toDomain() }
                if (pagina.isEmpty()) break
                cache.cachearPalabras(pagina)
                procesados += pagina.size
                emit(EstadoSincronizacion.EnProgreso("Diccionario", progreso()))
                if (pagina.size < TAMANO_PAGINA) break
                offset += TAMANO_PAGINA
            }

            emit(EstadoSincronizacion.EnProgreso("Traducciones", progreso()))
            offset = 0
            while (true) {
                val pagina = client.from("traduccion")
                    .select(Columns.raw(TRADUCCION_EMBED)) {
                        range(offset.toLong(), (offset + TAMANO_PAGINA - 1).toLong())
                    }
                    .decodeList<TraduccionDto>()
                    .map { it.toDomain() }
                if (pagina.isEmpty()) break
                cache.cachearTraducciones(pagina)
                procesados += pagina.size
                emit(EstadoSincronizacion.EnProgreso("Traducciones", progreso()))
                if (pagina.size < TAMANO_PAGINA) break
                offset += TAMANO_PAGINA
            }

            emit(EstadoSincronizacion.EnProgreso("Lecciones", progreso()))
            val lecciones = client.from("leccion")
                .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)"))
                .decodeList<LeccionDto>()
                .map { it.toDomain() }
            cache.cachearLecciones(lecciones)
            procesados += lecciones.size

            // Vínculos lección-palabra (lecciones de vocabulario) — solo los
            // ids, no hace falta re-traer la palabra completa, ya está cacheada.
            val vinculos = client.from("leccion_palabra")
                .select(Columns.raw("leccion_id, palabra_id"))
                .decodeList<VinculoLeccionPalabraDto>()
            cache.cachearVinculosLeccionPalabra(vinculos.map { it.leccionId to it.palabraId })
            procesados += vinculos.size

            emit(EstadoSincronizacion.EnProgreso("Oraciones de ejemplo", progreso()))
            val oraciones = client.from("oracion_ejemplo")
                .select(Columns.raw("*, fuente_documento(*)"))
                .decodeList<OracionEjemploSyncDto>()
            oraciones.groupBy { it.leccionId }.forEach { (leccionId, grupo) ->
                cache.cachearOraciones(leccionId, grupo.map { it.toDomain() })
            }
            procesados += oraciones.size

            emit(EstadoSincronizacion.Completado)
        } catch (e: Exception) {
            emit(EstadoSincronizacion.Error(e.message ?: "Error desconocido al sincronizar"))
        }
    }
}

@Serializable
private data class VinculoLeccionPalabraDto(
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("palabra_id") val palabraId: Int
)

@Serializable
private data class OracionEjemploSyncDto(
    val id: Int,
    @SerialName("texto_origen") val textoOrigen: String,
    @SerialName("texto_destino") val textoDestino: String,
    @SerialName("leccion_id") val leccionId: Int,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto
) {
    fun toDomain() = OracionEjemplo(
        id = id,
        textoOrigen = textoOrigen,
        textoDestino = textoDestino,
        leccion = null,
        fuente = fuente.toDomain()
    )
}