package com.aikukisna.app.data.sync

import com.aikukisna.app.data.local.dao.ConocimientoLinguisticoDao
import com.aikukisna.app.data.local.entity.AcepcionEntity
import com.aikukisna.app.data.local.entity.ExpresionEntity
import com.aikukisna.app.data.local.entity.PalabraFuenteEntity
import com.aikukisna.app.data.local.entity.SincronizacionLinguisticaEntity
import com.aikukisna.app.data.local.entity.TraduccionAcepcionEntity
import com.aikukisna.app.data.local.entity.TraduccionExpresionEntity
import com.aikukisna.app.data.local.entity.VariantePalabraEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SincronizadorConocimientoLinguistico @Inject constructor(
    private val client: SupabaseClient,
    private val dao: ConocimientoLinguisticoDao
) {
    suspend fun estaInicializado(): Boolean = RECURSOS.all { dao.obtenerCursor(it) != null }

    suspend fun sincronizar(): Int {
        var total = 0
        total += sincronizarRecurso("acepcion") { sincronizarAcepciones() }
        total += sincronizarRecurso("traduccion_acepcion") { sincronizarTraduccionesAcepcion() }
        total += sincronizarRecurso("variante_palabra") { sincronizarVariantes() }
        total += sincronizarRecurso("expresion") { sincronizarExpresiones() }
        total += sincronizarRecurso("traduccion_expresion") { sincronizarTraduccionesExpresion() }
        total += sincronizarRecurso("palabra_fuente") { sincronizarFuentes() }
        return total
    }

    private suspend fun sincronizarRecurso(recurso: String, bloque: suspend () -> Int): Int =
        try {
            bloque()
        } catch (e: Exception) {
            throw FalloSincronizacionLinguistica(recurso, e)
        }

    private suspend fun sincronizarAcepciones() = paginar<AcepcionDto>("acepcion") { items ->
        dao.guardarAcepciones(items.map { it.toEntity() })
    }

    private suspend fun sincronizarTraduccionesAcepcion() =
        paginar<TraduccionAcepcionDto>("traduccion_acepcion") { items ->
            dao.guardarTraduccionesAcepcion(items.map { it.toEntity() })
        }

    private suspend fun sincronizarVariantes() =
        paginar<VarianteDto>("variante_palabra") { items ->
            dao.guardarVariantes(items.map { it.toEntity() })
        }

    private suspend fun sincronizarExpresiones() =
        paginar<ExpresionDto>("expresion") { items ->
            dao.guardarExpresiones(items.map { it.toEntity() })
        }

    private suspend fun sincronizarTraduccionesExpresion() =
        paginar<TraduccionExpresionDto>("traduccion_expresion") { items ->
            dao.guardarTraduccionesExpresion(items.map { it.toEntity() })
        }

    private suspend fun sincronizarFuentes() =
        paginar<PalabraFuenteDto>(
            recurso = "palabra_fuente",
            idColumn = "palabra_id",
            secondaryIdColumn = "fuente_id"
        ) { items ->
            dao.guardarFuentes(items.map { it.toEntity() })
        }

    private suspend inline fun <reified T : ActualizableDto> paginar(
        recurso: String,
        idColumn: String = "id",
        secondaryIdColumn: String? = null,
        crossinline guardar: suspend (List<T>) -> Unit
    ): Int {
        val cursor = dao.obtenerCursor(recurso)
        val desde = Instant.ofEpochMilli(cursor?.ultimoUpdatedAtEpochMs ?: 0).toString()
        var offset = 0L
        var total = 0
        var maxEpoch = cursor?.ultimoUpdatedAtEpochMs ?: 0
        var maxId = cursor?.ultimoId ?: 0
        while (true) {
            val page = client.from(recurso).select {
                filter { gte("updated_at", desde) }
                order("updated_at", Order.ASCENDING)
                order(idColumn, Order.ASCENDING)
                secondaryIdColumn?.let { order(it, Order.ASCENDING) }
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<T>()
            if (page.isEmpty()) break
            val pendientes = page.filter {
                val epoch = Instant.parse(it.updatedAt).toEpochMilli()
                epoch > maxEpoch || (epoch == maxEpoch && it.cursorId > maxId)
            }
            if (pendientes.isNotEmpty()) guardar(pendientes)
            total += pendientes.size
            pendientes.forEach {
                val epoch = Instant.parse(it.updatedAt).toEpochMilli()
                if (epoch > maxEpoch || (epoch == maxEpoch && it.cursorId > maxId)) {
                    maxEpoch = epoch
                    maxId = it.cursorId
                }
            }
            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        dao.guardarCursor(SincronizacionLinguisticaEntity(recurso, maxEpoch, maxId))
        return total
    }

    private companion object {
        const val PAGE_SIZE = 250L
        val RECURSOS = listOf(
            "acepcion",
            "traduccion_acepcion",
            "variante_palabra",
            "expresion",
            "traduccion_expresion",
            "palabra_fuente"
        )
    }
}

class FalloSincronizacionLinguistica(
    val recurso: String,
    causa: Throwable
) : Exception("No se pudo sincronizar $recurso", causa)

private interface ActualizableDto {
    val cursorId: Long
    val updatedAt: String
}

@Serializable
private data class AcepcionDto(
    val id: Long,
    @SerialName("palabra_id") val palabraId: Int,
    @SerialName("numero_acepcion") val numero: Int,
    val definicion: String? = null,
    val contexto: String? = null,
    @SerialName("categoria_gramatical") val categoriaGramatical: String? = null,
    @SerialName("estado_validacion") val estado: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = id
    fun toEntity() = AcepcionEntity(id, palabraId, numero, definicion, contexto, categoriaGramatical, estado, epoch(createdAt), epoch(updatedAt))
}

@Serializable
private data class TraduccionAcepcionDto(
    val id: Long,
    @SerialName("acepcion_origen_id") val origenId: Long,
    @SerialName("acepcion_destino_id") val destinoId: Long,
    val tipo: String,
    @SerialName("estado_validacion") val estado: String,
    @SerialName("nivel_confianza") val confianza: Double? = null,
    val nota: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = id
    fun toEntity() = TraduccionAcepcionEntity(id, origenId, destinoId, tipo, estado, confianza, nota, epoch(createdAt), epoch(updatedAt))
}

@Serializable
private data class VarianteDto(
    val id: Long,
    @SerialName("palabra_id") val palabraId: Int,
    val texto: String,
    @SerialName("texto_normalizado") val textoNormalizado: String,
    val tipo: String,
    @SerialName("estado_validacion") val estado: String,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = id
    fun toEntity() = VariantePalabraEntity(id, palabraId, texto, textoNormalizado, tipo, estado, epoch(updatedAt))
}

@Serializable
private data class ExpresionDto(
    val id: Long,
    @SerialName("idioma_id") val idiomaId: Int,
    val texto: String,
    @SerialName("texto_normalizado") val textoNormalizado: String,
    val tipo: String,
    @SerialName("estado_validacion") val estado: String,
    @SerialName("fuente_id") val fuenteId: Int? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = id
    fun toEntity() = ExpresionEntity(id, idiomaId, texto, textoNormalizado, tipo, estado, fuenteId, epoch(createdAt), epoch(updatedAt))
}

@Serializable
private data class TraduccionExpresionDto(
    val id: Long,
    @SerialName("expresion_origen_id") val origenId: Long,
    @SerialName("expresion_destino_id") val destinoId: Long,
    @SerialName("estado_validacion") val estado: String,
    val nota: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = id
    fun toEntity() = TraduccionExpresionEntity(id, origenId, destinoId, estado, nota, epoch(createdAt), epoch(updatedAt))
}

@Serializable
private data class PalabraFuenteDto(
    @SerialName("palabra_id") val palabraId: Int,
    @SerialName("fuente_id") val fuenteId: Int,
    @SerialName("pagina_inicio") val paginaInicio: Int? = null,
    @SerialName("pagina_fin") val paginaFin: Int? = null,
    val nota: String? = null,
    @SerialName("updated_at") override val updatedAt: String
) : ActualizableDto {
    override val cursorId get() = (palabraId.toLong() shl 32) + fuenteId
    fun toEntity() = PalabraFuenteEntity(palabraId, fuenteId, paginaInicio, paginaFin, nota, epoch(updatedAt))
}

private fun epoch(value: String): Long = Instant.parse(value).toEpochMilli()
