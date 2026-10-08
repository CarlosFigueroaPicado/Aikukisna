package com.aikukisna.app.data.repository

import android.util.Log
import com.aikukisna.app.data.local.CacheEscritor
import com.aikukisna.app.data.local.CulturaLocalCache
import com.aikukisna.app.data.remote.dto.CategoriaDto
import com.aikukisna.app.data.remote.dto.CulturaContenidoDto
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
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.aikukisna.app.data.sync.SincronizadorConocimientoLinguistico
import com.aikukisna.app.data.sync.FalloSincronizacionLinguistica
import com.aikukisna.app.data.sync.SincronizadorContenidoLecciones
import com.aikukisna.app.data.sync.SincronizadorReplicaSupabase

class SincronizacionRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val cache: CacheEscritor,
    private val culturaCache: CulturaLocalCache,
    private val sincronizadorConocimiento: SincronizadorConocimientoLinguistico,
    private val sincronizadorContenidoLecciones: SincronizadorContenidoLecciones,
    private val sincronizadorReplicaSupabase: SincronizadorReplicaSupabase
) : SincronizacionRepository {

    private companion object {
        const val PALABRA_EMBED =
            "*, idioma:idioma!palabra_idioma_id_fkey(*), " +
                "categoria:categoria!palabra_categoria_id_fkey(*), " +
                "fuente_documento:fuente_documento!palabra_fuente_id_fkey(*)"
        const val TRADUCCION_EMBED =
            "*, palabra_origen:palabra!traduccion_palabra_origen_id_fkey($PALABRA_EMBED), " +
                    "palabra_destino:palabra!traduccion_palabra_destino_id_fkey($PALABRA_EMBED)"
        const val TAMANO_PAGINA = 250
        const val INTENTOS_POR_PAGINA = 3

        // Conteos de referencia de la última verificación contra producción
        // (no son exactos para siempre — la base sigue creciendo) — solo
        // sirven para calcular un progreso aproximado, no una cuenta exacta.
        const val TOTAL_ESTIMADO =
            4 /*idiomas*/ + 19 /*categorias*/ + 53 /*fuentes*/ +
                    71_781 /*palabras activas*/ + 132_274 /*traducciones activas*/ +
                    93 /*lecciones*/ + 3_905 /*leccion_palabra*/ + 1_010 /*oraciones activas*/ +
                    4 /*mundos*/ + 93 /*experiencias*/ + 837 /*actividades*/ +
                    2_157 /*recursos de actividad*/ + 4 /*etapas curriculares*/ +
                    93 /*rutas curriculares*/ + 107 /*evidencias curriculares*/
    }

    override suspend fun hayDatosDescargados(idiomaId: Int?): Boolean =
        cache.hayAlgoDescargado()

    override fun sincronizarTodo(idiomaId: Int?): Flow<EstadoSincronizacion> = flow {
        var procesados = 0
        var etapa = "inicio"
        fun progreso() = (procesados.toFloat() / TOTAL_ESTIMADO).coerceIn(0f, 0.99f)

        try {
            cache.marcarDescargaEnProgreso(idiomaId)
            etapa = "idiomas"
            emit(EstadoSincronizacion.EnProgreso("Idiomas y categorías", progreso()))
            val idiomas = client.from("idioma").select().decodeList<IdiomaDto>().map { it.toDomain() }
            cache.cachearIdiomas(idiomas)
            procesados += idiomas.size

            etapa = "categorias"
            val categorias = client.from("categoria").select().decodeList<CategoriaDto>().map { it.toDomain() }
            cache.cachearCategorias(categorias)
            procesados += categorias.size

            etapa = "fuentes"
            val fuentes = client.from("fuente_documento").select().decodeList<FuenteDocumentoDto>().map { it.toDomain() }
            cache.cachearFuentes(fuentes)
            procesados += fuentes.size

            emit(EstadoSincronizacion.EnProgreso("Diccionario", progreso()))
            etapa = "palabras"
            var ultimoPalabraId = 0
            while (true) {
                val pagina = conReintentos {
                    client.from("palabra")
                        .select(Columns.raw(PALABRA_EMBED)) {
                            filter {
                                gt("id", ultimoPalabraId)
                                idiomaId?.let { targetId ->
                                    or {
                                        eq("idioma_id", targetId)
                                        eq("idioma_id", 2)
                                    }
                                }
                            }
                            order("id", Order.ASCENDING)
                            range(0L, (TAMANO_PAGINA - 1).toLong())
                        }
                        .decodeList<PalabraDto>()
                        .map { it.toDomain() }
                }
                if (pagina.isEmpty()) break
                cache.cachearPalabras(pagina)
                procesados += pagina.size
                emit(EstadoSincronizacion.EnProgreso("Diccionario", progreso()))
                if (pagina.size < TAMANO_PAGINA) break
                ultimoPalabraId = pagina.last().id
            }

            emit(EstadoSincronizacion.EnProgreso("Traducciones", progreso()))
            etapa = "traducciones"
            var ultimaTraduccionId = 0
            while (true) {
                val pagina = conReintentos {
                    client.from("traduccion")
                        .select(Columns.raw(TRADUCCION_EMBED)) {
                            filter { gt("id", ultimaTraduccionId) }
                            order("id", Order.ASCENDING)
                            range(0L, (TAMANO_PAGINA - 1).toLong())
                        }
                        .decodeList<TraduccionDto>()
                        .map { it.toDomain() }
                }
                if (pagina.isEmpty()) break
                cache.cachearTraducciones(pagina)
                procesados += pagina.size
                emit(EstadoSincronizacion.EnProgreso("Traducciones", progreso()))
                if (pagina.size < TAMANO_PAGINA) break
                ultimaTraduccionId = pagina.last().id
            }

            emit(EstadoSincronizacion.EnProgreso("Lecciones", progreso()))
            etapa = "lecciones"
            val lecciones = client.from("leccion")
                .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)"))
                {
                    filter { idiomaId?.let { eq("idioma_meta_id", it) } }
                }
                .decodeList<LeccionDto>()
                .map { it.toDomain() }
            cache.cachearLecciones(lecciones)
            procesados += lecciones.size

            // Vínculos lección-palabra (lecciones de vocabulario) — solo los
            // ids, no hace falta re-traer la palabra completa, ya está cacheada.
            etapa = "leccion_palabra"
            val vinculos = client.from("leccion_palabra")
                .select(Columns.raw("leccion_id, palabra_id"))
                .decodeList<VinculoLeccionPalabraDto>()
            cache.cachearVinculosLeccionPalabra(vinculos.map { it.leccionId to it.palabraId })
            procesados += vinculos.size

            emit(EstadoSincronizacion.EnProgreso("Oraciones de ejemplo", progreso()))
            etapa = "oraciones"
            val oraciones = client.from("oracion_ejemplo")
                .select(Columns.raw("*, fuente_documento(*)"))
                .decodeList<OracionEjemploSyncDto>()
            oraciones.groupBy { it.leccionId }.forEach { (leccionId, grupo) ->
                cache.cachearOraciones(leccionId, grupo.map { it.toDomain() })
            }
            procesados += oraciones.size

            emit(EstadoSincronizacion.EnProgreso("Contenido cultural", progreso()))
            etapa = "cultura"
            val cultura = client.from("cultura_contenido")
                .select(Columns.raw("*, fuente_documento(*)"))
                .decodeList<CulturaContenidoDto>()
                .map { dto ->
                    com.aikukisna.app.domain.model.CulturaContenido(
                        id = dto.id,
                        titulo = dto.titulo,
                        contenido = dto.contenido,
                        rangoPaginaInicio = dto.rangoPaginaInicio,
                        rangoPaginaFin = dto.rangoPaginaFin,
                        fuente = dto.fuente.toDomain()
                    )
                }
            culturaCache.guardar(cultura)
            procesados += cultura.size

            emit(EstadoSincronizacion.EnProgreso("Conocimiento lingüístico", progreso()))
            etapa = "conocimiento_linguistico"
            sincronizadorConocimiento.sincronizar()

            emit(EstadoSincronizacion.EnProgreso("Misiones y currículo", progreso()))
            etapa = "contenido_lecciones"
            procesados += sincronizadorContenidoLecciones.sincronizar()

            emit(EstadoSincronizacion.EnProgreso("Réplica completa offline", progreso()))
            etapa = "replica_supabase"
            procesados += sincronizadorReplicaSupabase.sincronizar()

            cache.marcarDescargaCompleta(idiomaId)
            emit(EstadoSincronizacion.Completado)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val detalle = if (e is FalloSincronizacionLinguistica) e.recurso else etapa
            val causa = e.cause ?: e
            val tipo = causa.javaClass.simpleName
            val codigo = (causa as? PostgrestRestException)?.code
            Log.e("Sincronizacion", "Fallo en $detalle; tipo=$tipo; codigo=${codigo ?: "no_disponible"}")
            emit(
                EstadoSincronizacion.Error(
                    "No se pudo descargar el contenido. Comprueba tu conexión y vuelve a intentarlo."
                )
            )
        }
    }

    private suspend fun <T> conReintentos(consulta: suspend () -> T): T {
        var ultimoError: Exception? = null
        repeat(INTENTOS_POR_PAGINA) { intento ->
            try {
                return consulta()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ultimoError = e
                if (intento < INTENTOS_POR_PAGINA - 1) {
                    delay(1_000L * (intento + 1))
                }
            }
        }
        throw ultimoError ?: IllegalStateException("No se pudo completar la consulta")
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
    @SerialName("idioma_origen_id") val idiomaOrigenId: Int,
    @SerialName("idioma_destino_id") val idiomaDestinoId: Int,
    @SerialName("leccion_id") val leccionId: Int? = null,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toDomain() = OracionEjemplo(
        id = id,
        textoOrigen = textoOrigen,
        textoDestino = textoDestino,
        idiomaOrigenId = idiomaOrigenId,
        idiomaDestinoId = idiomaDestinoId,
        leccion = null,
        fuente = fuente.toDomain(),
        estadoValidacion = estadoValidacion,
        updatedAtEpochMs = updatedAt?.let { Instant.parse(it).toEpochMilli() } ?: 0
    )
}
