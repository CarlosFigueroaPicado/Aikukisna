package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.LogroDesbloqueadoDto
import com.aikukisna.app.data.local.OfflineUserDataCache
import com.aikukisna.app.data.local.dao.CategoriaDao
import com.aikukisna.app.data.local.dao.ContenidoGlobalDao
import com.aikukisna.app.data.local.entity.LogroDesbloqueadoUsuarioEntity
import com.aikukisna.app.data.remote.dto.LogroDto
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.repository.LogroRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class LogroRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val offlineUserDataCache: OfflineUserDataCache,
    private val conectividad: com.aikukisna.app.data.local.ConectividadHelper,
    private val contenidoGlobalDao: ContenidoGlobalDao,
    private val categoriaDao: CategoriaDao
) : LogroRepository {

    override suspend fun obtenerLogros(): List<Logro> {
        if (!conectividad.hayConexion()) return obtenerLogrosLocales()
        return try {
            client.from("logro")
                .select(Columns.raw("*, categoria(*)"))
                .decodeList<LogroDto>()
                .map { it.toDomain() }
                .also { offlineUserDataCache.guardarLogros(it) }
        } catch (_: Exception) { obtenerLogrosLocales() }
    }

    override suspend fun obtenerLogrosDesbloqueados(usuarioId: UUID): List<LogroDesbloqueado> {
        if (!conectividad.hayConexion()) return obtenerDesbloqueadosLocales(usuarioId)
        return try {
            val remotos = client.from("logro_desbloqueado")
                .select(Columns.raw("*, logro(*, categoria(*))")) {
                    filter { eq("usuario_id", usuarioId.toString()) }
                }
                .decodeList<LogroDesbloqueadoDto>()
                .map { it.toDomain() }

            val locales = obtenerDesbloqueadosLocales(usuarioId)
            val idsRemotos = remotos.map { it.logro.id }.toSet()
            locales.filterNot { it.logro.id in idsRemotos }.forEach { pendiente ->
                runCatching {
                    client.postgrest.rpc(
                        "desbloquear_logro",
                        buildJsonObject { put("p_logro_id", pendiente.logro.id) }
                    )
                }
            }

            val reconciliados = (remotos + locales)
                .distinctBy { it.logro.id }
                .sortedBy { it.logro.id }
            offlineUserDataCache.guardarDesbloqueados(reconciliados)
            contenidoGlobalDao.guardarLogrosUsuario(reconciliados.map {
                LogroDesbloqueadoUsuarioEntity(
                    usuarioId = it.usuarioId.toString(),
                    logroId = it.logro.id,
                    fechaEpochMs = it.fecha.toEpochMilli()
                )
            })
            reconciliados
        } catch (_: Exception) { obtenerDesbloqueadosLocales(usuarioId) }
    }

    override suspend fun desbloquearLogro(usuarioId: UUID, logroId: Int) {
        if (conectividad.hayConexion()) {
            runCatching {
                client.postgrest.rpc(
                    "desbloquear_logro",
                    buildJsonObject { put("p_logro_id", logroId) }
                )
            }
        }
        val fecha = Instant.now()
        offlineUserDataCache.agregarDesbloqueado(usuarioId, logroId, fecha)
        contenidoGlobalDao.guardarLogrosUsuario(
            listOf(LogroDesbloqueadoUsuarioEntity(usuarioId.toString(), logroId, fecha.toEpochMilli()))
        )
    }

    private suspend fun obtenerLogrosLocales(): List<Logro> {
        val cacheados = offlineUserDataCache.leerLogros()
        if (cacheados.isNotEmpty()) return cacheados
        val logros = contenidoGlobalDao.obtenerLogros().map { entity ->
            Logro(
                id = entity.id,
                nombre = entity.nombre,
                descripcion = entity.descripcion,
                condicionTipo = entity.condicionTipo,
                condicionValor = entity.condicionValor,
                categoria = entity.categoriaId?.let { categoriaId ->
                    categoriaDao.obtenerPorId(categoriaId)?.let { Categoria(it.id, it.nombre) }
                }
            )
        }
        if (logros.isNotEmpty()) offlineUserDataCache.guardarLogros(logros)
        return logros
    }

    private suspend fun obtenerDesbloqueadosLocales(usuarioId: UUID): List<LogroDesbloqueado> {
        val logros = obtenerLogrosLocales().associateBy { it.id }
        val room = contenidoGlobalDao.obtenerLogrosUsuario(usuarioId.toString()).mapNotNull { item ->
            logros[item.logroId]?.let { logro ->
                LogroDesbloqueado(usuarioId, logro, Instant.ofEpochMilli(item.fechaEpochMs))
            }
        }
        if (room.isNotEmpty()) return room
        return offlineUserDataCache.leerDesbloqueados(usuarioId)
    }
}

private fun LogroDto.toDomain() = Logro(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    condicionTipo = condicionTipo,
    condicionValor = condicionValor,
    categoria = categoria?.let { Categoria(it.id, it.nombre) }
)

private fun LogroDesbloqueadoDto.toDomain() = LogroDesbloqueado(
    usuarioId = UUID.fromString(usuarioId),
    logro = logro.toDomain(),
    fecha = Instant.parse(fecha)
)
