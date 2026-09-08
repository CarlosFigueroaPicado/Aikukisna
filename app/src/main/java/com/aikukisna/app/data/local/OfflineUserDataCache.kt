package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.model.ProgresoLeccion
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ProgresoCache(
    val usuarioId: String,
    val leccionId: Int,
    val estado: String,
    val puntaje: Int? = null,
    val fecha: String? = null
)

@Serializable
private data class FavoritoCache(val usuarioId: String, val palabraId: Int)

@Serializable
private data class FavoritoPendiente(val usuarioId: String, val palabraId: Int, val quitar: Boolean)

@Serializable
private data class LogroCache(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val condicionTipo: String,
    val condicionValor: Int,
    val categoriaId: Int? = null,
    val categoriaNombre: String? = null
)

@Serializable
private data class DesbloqueadoCache(val usuarioId: String, val logroId: Int, val fecha: String)

@Singleton
class OfflineUserDataCache @Inject constructor(
    @ApplicationContext context: Context,
    private val contenido: CacheEscritor
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val preferencias = context.getSharedPreferences("datos_usuario_offline", Context.MODE_PRIVATE)

    suspend fun guardarProgreso(lista: List<ProgresoLeccion>) {
        val datos = lista.map {
            ProgresoCache(it.usuarioId.toString(), it.leccion.id, it.estado, it.puntaje, it.fechaCompletado?.toString())
        }
        guardar("progreso", datos)
    }

    suspend fun leerProgreso(usuarioId: UUID): List<ProgresoLeccion> {
        val guardado = leer<ProgresoCache>("progreso")
            .filter { it.usuarioId == usuarioId.toString() }
            .mapNotNull { item ->
                contenido.leerLeccion(item.leccionId)?.let { leccion ->
                    ProgresoLeccion(usuarioId, leccion, item.estado, item.puntaje, item.fecha?.let(Instant::parse))
                }
            }
        val idsGuardados = guardado.map { it.leccion.id }.toSet()
        val pendientes = contenido.obtenerLeccionesPendientes()
            .filter { it.leccionId !in idsGuardados }
            .mapNotNull { item -> contenido.leerLeccion(item.leccionId)?.let { leccion ->
                ProgresoLeccion(
                    usuarioId = usuarioId,
                    leccion = leccion,
                    estado = "completada",
                    puntaje = item.puntaje,
                    fechaCompletado = java.time.Instant.ofEpochMilli(item.fechaCreadoEpochMs)
                )
            } }
        return guardado + pendientes
    }

    suspend fun guardarFavoritos(lista: List<PalabraFavorita>) {
        guardar("favoritos", lista.map { FavoritoCache(it.usuarioId.toString(), it.palabra.id) })
    }

    suspend fun agregarFavorito(usuarioId: UUID, palabraId: Int) {
        val datos = leer<FavoritoCache>("favoritos")
            .filterNot { it.usuarioId == usuarioId.toString() && it.palabraId == palabraId }
            .plus(FavoritoCache(usuarioId.toString(), palabraId))
        guardar("favoritos", datos)
    }

    suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int) {
        guardar("favoritos", leer<FavoritoCache>("favoritos").filterNot {
            it.usuarioId == usuarioId.toString() && it.palabraId == palabraId
        })
    }

    fun encolarFavorito(usuarioId: UUID, palabraId: Int, quitar: Boolean) {
        val pendientes = leer<FavoritoPendiente>("favoritos_pendientes")
            .filterNot { it.usuarioId == usuarioId.toString() && it.palabraId == palabraId }
            .plus(FavoritoPendiente(usuarioId.toString(), palabraId, quitar))
        guardar("favoritos_pendientes", pendientes)
    }

    fun leerFavoritosPendientes(): List<Triple<UUID, Int, Boolean>> =
        leer<FavoritoPendiente>("favoritos_pendientes").mapNotNull {
            runCatching { Triple(UUID.fromString(it.usuarioId), it.palabraId, it.quitar) }.getOrNull()
        }

    fun borrarFavoritoPendiente(usuarioId: UUID, palabraId: Int) {
        guardar("favoritos_pendientes", leer<FavoritoPendiente>("favoritos_pendientes").filterNot {
            it.usuarioId == usuarioId.toString() && it.palabraId == palabraId
        })
    }

    suspend fun leerFavoritos(usuarioId: UUID): List<PalabraFavorita> =
        leer<FavoritoCache>("favoritos")
            .filter { it.usuarioId == usuarioId.toString() }
            .mapNotNull { contenido.leerPalabra(it.palabraId)?.let { palabra -> PalabraFavorita(usuarioId, palabra) } }

    suspend fun guardarLogros(lista: List<Logro>) = guardar("logros", lista.map {
        LogroCache(it.id, it.nombre, it.descripcion, it.condicionTipo, it.condicionValor, it.categoria?.id, it.categoria?.nombre)
    })

    fun leerLogros(): List<Logro> = leer<LogroCache>("logros").map {
        Logro(it.id, it.nombre, it.descripcion, it.condicionTipo, it.condicionValor,
            it.categoriaId?.let { id -> Categoria(id, it.categoriaNombre.orEmpty()) })
    }

    suspend fun guardarDesbloqueados(lista: List<LogroDesbloqueado>) = guardar("desbloqueados", lista.map {
        DesbloqueadoCache(it.usuarioId.toString(), it.logro.id, it.fecha.toString())
    })

    fun leerDesbloqueados(usuarioId: UUID): List<LogroDesbloqueado> {
        val logros = leerLogros().associateBy { it.id }
        return leer<DesbloqueadoCache>("desbloqueados")
            .filter { it.usuarioId == usuarioId.toString() }
            .mapNotNull { item -> logros[item.logroId]?.let { logro ->
                LogroDesbloqueado(usuarioId, logro, Instant.parse(item.fecha))
            } }
    }

    private inline fun <reified T> leer(nombre: String): List<T> =
        preferencias.getString(nombre, null)?.let { json.decodeFromString<List<T>>(it) }.orEmpty()

    private inline fun <reified T> guardar(nombre: String, valor: List<T>) {
        preferencias.edit().putString(nombre, json.encodeToString(valor)).apply()
    }
}
