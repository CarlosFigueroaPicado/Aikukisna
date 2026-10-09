package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.ConectividadHelper
import com.aikukisna.app.data.local.CulturaLocalCache
import com.aikukisna.app.data.local.dao.ContenidoGlobalDao
import com.aikukisna.app.data.local.dao.FuenteDocumentoDao
import com.aikukisna.app.data.remote.dto.CulturaContenidoDto
import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.repository.CulturaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class CulturaRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val conectividad: ConectividadHelper,
    private val cache: CulturaLocalCache,
    private val contenidoGlobal: ContenidoGlobalDao,
    private val fuentes: FuenteDocumentoDao
) : CulturaRepository {

    private val contenidoEmbed = "*, fuente_documento(*)"

    override suspend fun obtenerContenidoCultural(): List<CulturaContenido> {
        // Primero lo guardado en el teléfono (la réplica incluida en la app): la red solo si no hay nada.
        locales().takeIf { it.isNotEmpty() }?.let { return it.paraEstudiantes() }
        if (conectividad.hayConexion()) {
            try {
                val resultado = client.from("cultura_contenido")
                    .select(Columns.raw(contenidoEmbed))
                    .decodeList<CulturaContenidoDto>()
                    .map { it.toDomain() }
                cache.guardar(resultado)
                return resultado.paraEstudiantes()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // La red puede dejar de estar disponible después de comprobarla.
            }
        }
        return locales().paraEstudiantes()
    }

    /**
     * Sin conexión manda la réplica incluida en la app (se actualiza con cada versión de la semilla); la copia
     * de la última descarga solo se usa si la réplica no existe. Antes, una instalación que nunca se conectó
     * quedaba sin cultura, y una conectada hace tiempo mostraba textos ya corregidos.
     */
    private suspend fun locales(): List<CulturaContenido> {
        val replica = contenidoGlobal.obtenerCulturas()
        if (replica.isEmpty()) return cache.leer()
        val porFuente = replica.map { it.fuenteId }.distinct().associateWith { fuentes.obtenerPorId(it) }
        return replica.map { item ->
            val fuente = porFuente[item.fuenteId]
            CulturaContenido(
                id = item.id,
                titulo = item.titulo,
                contenido = item.contenido,
                rangoPaginaInicio = item.rangoPaginaInicio,
                rangoPaginaFin = item.rangoPaginaFin,
                fuente = FuenteDocumento(
                    id = item.fuenteId,
                    titulo = fuente?.titulo.orEmpty(),
                    autor = fuente?.autor,
                    anio = fuente?.anio,
                    institucion = fuente?.institucion
                )
            )
        }
    }

    override suspend fun obtenerContenidoCulturalPorId(id: Int): CulturaContenido? {
        if (id in OCULTOS_A_ESTUDIANTES) return null
        locales().firstOrNull { it.id == id }?.let { return it }
        if (conectividad.hayConexion()) {
            try {
                val resultado = client.from("cultura_contenido")
                    .select(Columns.raw(contenidoEmbed)) {
                        filter { eq("id", id) }
                    }
                    .decodeSingleOrNull<CulturaContenidoDto>()
                    ?.toDomain()
                if (resultado != null) {
                    cache.guardar((cache.leer().filterNot { it.id == id } + resultado).sortedBy { it.id })
                }
                return resultado
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Caer a la copia local.
            }
        }
        return locales().firstOrNull { it.id == id } ?: cache.leer(id)
    }
}

/**
 * Textos del estudio etnográfico antiguo que describen rasgos físicos por grupo y estereotipos de carácter
 * ("Antropología física", "Carácter"). Se conservan en la base como referencia, pero no se muestran a los
 * estudiantes (decisión del equipo, 2026-10-07).
 */
private val OCULTOS_A_ESTUDIANTES = setOf(2, 30)

private fun List<CulturaContenido>.paraEstudiantes() = filterNot { it.id in OCULTOS_A_ESTUDIANTES }

private fun CulturaContenidoDto.toDomain() = CulturaContenido(
    id = id,
    titulo = titulo,
    contenido = contenido,
    rangoPaginaInicio = rangoPaginaInicio,
    rangoPaginaFin = rangoPaginaFin,
    fuente = fuente.toDomain()
)
