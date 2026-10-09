package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.MemoriaTukiDto
import com.aikukisna.app.data.remote.dto.PalabraFavoritaDto
import com.aikukisna.app.data.remote.dto.ProgresoLeccionDto
import com.aikukisna.app.data.remote.dto.UsuarioDto
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.UsuarioRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.auth.auth
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import com.aikukisna.app.data.remote.dto.MemoriaTukiInsertDto
import com.aikukisna.app.data.remote.dto.PalabraFavoritaInsertDto
import com.aikukisna.app.data.remote.dto.ProgresoLeccionUpsertDto
import com.aikukisna.app.data.remote.dto.UsuarioUpdateDto
import com.aikukisna.app.data.local.PerfilLocalCache
import com.aikukisna.app.data.local.OfflineUserDataCache




class UsuarioRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val perfilLocalCache: PerfilLocalCache,
    private val offlineUserDataCache: OfflineUserDataCache,
    private val conectividad: com.aikukisna.app.data.local.ConectividadHelper
) : UsuarioRepository {

    override fun observarIdiomaMeta(): Flow<Idioma?> =
        perfilLocalCache.perfil.map { it?.idiomaMeta }.distinctUntilChanged()

    private fun esReciente(descargadoEn: Long?): Boolean =
        descargadoEn != null && System.currentTimeMillis() - descargadoEn < VIGENCIA_COPIA_MS

    private companion object {
        const val VIGENCIA_COPIA_MS = 2 * 60 * 1000L
        // El repositorio no es singleton; las marcas viven mientras viva el proceso.
        val perfilDescargado = java.util.concurrent.ConcurrentHashMap<UUID, Long>()
        val progresoDescargado = java.util.concurrent.ConcurrentHashMap<UUID, Long>()
        // Actualizaciones que no deben frenar la pantalla: viven con el proceso, no con un ViewModel.
        val segundoPlano = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    override suspend fun obtenerUsuario(id: UUID): Usuario? {
        val perfilGuardado = perfilLocalCache.leer(id)
        if (!conectividad.hayConexion()) return perfilGuardado
        // Cada pantalla y cada mensaje a Tuki piden el perfil: esperar a la red cada vez hacía
        // sentir lenta toda la app. Los cambios locales se guardan antes, así que la copia es fiable.
        if (perfilGuardado != null && esReciente(perfilDescargado[id])) return perfilGuardado
        // Con copia local se responde al instante y se actualiza detrás: con señal débil la consulta
        // tardaba hasta el tiempo límite (12 s) y la pantalla quedaba "cargando".
        if (perfilGuardado != null) {
            perfilDescargado[id] = System.currentTimeMillis()
            segundoPlano.launch { if (descargarPerfil(id, perfilGuardado) == null) perfilDescargado.remove(id) }
            return perfilGuardado
        }
        return descargarPerfil(id, null)
    }

    /** Baja el perfil de Supabase y lo guarda; null si no se pudo (sin red, error o cambios sin subir). */
    private suspend fun descargarPerfil(id: UUID, perfilGuardado: Usuario?): Usuario? {
        // Un cambio hecho sin conexión manda sobre el perfil remoto hasta subirse.
        if (perfilLocalCache.hayCambiosPendientes(id) && !sincronizarPerfilPendiente(id)) {
            return perfilGuardado
        }
        return try {
            client.from("usuario")
                .select(Columns.raw("*, idioma_meta:idioma_meta_id(*)")) {
                    filter { eq("id", id.toString()) }
                }
                .decodeSingleOrNull<UsuarioDto>()
                ?.toDomain()
                ?.conNombreUsuarioGoogleSiFalta(client.auth.currentUserOrNull()?.userMetadata)
                ?.copy(fotoPerfilUri = perfilGuardado?.fotoPerfilUri)
                ?.also {
                    perfilLocalCache.guardar(it)
                    perfilDescargado[id] = System.currentTimeMillis()
                }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun actualizarUsuario(usuario: Usuario) {
        // Offline-first: el cambio queda vigente localmente al instante y se sube después.
        perfilLocalCache.guardar(usuario, pendienteSincronizar = true)
        if (conectividad.hayConexion()) sincronizarPerfilPendiente(usuario.id)
    }

    override suspend fun sincronizarPerfilPendiente(id: UUID): Boolean {
        if (!perfilLocalCache.hayCambiosPendientes(id)) return true
        val local = perfilLocalCache.leer(id) ?: return true
        return try {
            client.from("usuario")
                .update(local.toUpdateDto()) {
                    filter { eq("id", id.toString()) }
                }
            perfilLocalCache.marcarSincronizado(id)
            true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun guardarUsuarioLocal(usuario: Usuario) {
        perfilLocalCache.guardar(usuario)
    }

    override suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion> {
        if (!conectividad.hayConexion()) return offlineUserDataCache.leerProgreso(usuarioId)
        if (esReciente(progresoDescargado[usuarioId])) return offlineUserDataCache.leerProgreso(usuarioId)
        val local = offlineUserDataCache.leerProgreso(usuarioId)
        if (local.isNotEmpty()) {
            progresoDescargado[usuarioId] = System.currentTimeMillis()
            segundoPlano.launch { if (descargarProgreso(usuarioId) == null) progresoDescargado.remove(usuarioId) }
            return local
        }
        return descargarProgreso(usuarioId) ?: local
    }

    private suspend fun descargarProgreso(usuarioId: UUID): List<ProgresoLeccion>? {
        return try {
            client.from("progreso_leccion")
                .select(Columns.raw("*, leccion(*, categoria(*), idioma_meta:idioma_meta_id(*))")) {
                    filter { eq("usuario_id", usuarioId.toString()) }
                }
                .decodeList<ProgresoLeccionDto>()
                .map { it.toDomain() }
                .also {
                    offlineUserDataCache.guardarProgreso(it)
                    progresoDescargado[usuarioId] = System.currentTimeMillis()
                }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun obtenerProgresoLocal(usuarioId: UUID): List<ProgresoLeccion> =
        offlineUserDataCache.leerProgreso(usuarioId)

    override suspend fun actualizarProgreso(progreso: ProgresoLeccion) {
        offlineUserDataCache.guardarProgresoLocal(progreso)
        if (!conectividad.hayConexion()) return
        runCatching {
            client.from("progreso_leccion")
                .upsert(progreso.toUpsertDto()) {
                    onConflict = "usuario_id,leccion_id"
                }
        }
    }

    override suspend fun guardarProgresoLocal(progreso: ProgresoLeccion) {
        offlineUserDataCache.guardarProgresoLocal(progreso)
    }

    override suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita> {
        val locales = offlineUserDataCache.leerFavoritos(usuarioId)
        if (!conectividad.hayConexion()) return locales
        if (locales.isNotEmpty()) {
            segundoPlano.launch { descargarFavoritos(usuarioId) }
            return locales
        }
        return descargarFavoritos(usuarioId)
    }

    private suspend fun descargarFavoritos(usuarioId: UUID): List<PalabraFavorita> {
        return try {
            client.from("palabra_favorita")
                .select(Columns.raw("*, palabra(*, idioma(*), categoria(*), fuente_documento(*))")) {
                    filter { eq("usuario_id", usuarioId.toString()) }
                }
                .decodeList<PalabraFavoritaDto>()
                .map { it.toDomain() }
                .also { offlineUserDataCache.guardarFavoritos(it) }
        } catch (_: Exception) {
            offlineUserDataCache.leerFavoritos(usuarioId)
        }
    }

    override suspend fun marcarFavorito(usuarioId: UUID, palabraId: Int) {
        try {
            client.from("palabra_favorita").insert(
                PalabraFavoritaInsertDto(usuarioId = usuarioId.toString(), palabraId = palabraId)
            )
        } catch (_: Exception) {
            offlineUserDataCache.encolarFavorito(usuarioId, palabraId, quitar = false)
        } finally {
            offlineUserDataCache.agregarFavorito(usuarioId, palabraId)
        }
    }

    override suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int) {
        try {
            client.from("palabra_favorita").delete {
                filter {
                    eq("usuario_id", usuarioId.toString())
                    eq("palabra_id", palabraId)
                }
            }
        } catch (_: Exception) {
            offlineUserDataCache.encolarFavorito(usuarioId, palabraId, quitar = true)
        } finally {
            offlineUserDataCache.quitarFavorito(usuarioId, palabraId)
        }
    }

    override suspend fun sincronizarFavoritosPendientes(): Int {
        var sincronizados = 0
        for ((usuarioId, palabraId, quitar) in offlineUserDataCache.leerFavoritosPendientes()) {
            try {
                if (quitar) {
                    client.from("palabra_favorita").delete {
                        filter {
                            eq("usuario_id", usuarioId.toString())
                            eq("palabra_id", palabraId)
                        }
                    }
                } else {
                    client.from("palabra_favorita").insert(
                        PalabraFavoritaInsertDto(usuarioId.toString(), palabraId)
                    )
                }
                offlineUserDataCache.borrarFavoritoPendiente(usuarioId, palabraId)
                sincronizados++
            } catch (_: Exception) {
                // Se conserva para el siguiente intento.
            }
        }
        return sincronizados
    }

    override suspend fun obtenerMemoriaTuki(usuarioId: UUID): List<MemoriaTuki> {
        return try {
            client.from("memoria_tuki")
                .select {
                    filter { eq("usuario_id", usuarioId.toString()) }
                }
                .decodeList<MemoriaTukiDto>()
                .map { it.toDomain() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun guardarMemoriaTuki(memoria: MemoriaTuki) {
        client.from("memoria_tuki").insert(
            MemoriaTukiInsertDto(
                usuarioId = memoria.usuarioId.toString(),
                tipo = memoria.tipo,
                resumen = memoria.resumen,
                fecha = memoria.fecha.toString()
            )
        )
    }
}

private fun UsuarioDto.toDomain() = Usuario(
    id = UUID.fromString(id),
    nombre = nombre,
    apellido = apellido,
    nombreUsuario = nombreUsuario,
    correo = correo,
    edad = edad,
    pais = pais,
    ciudad = ciudad,
    idiomaMeta = idiomaMeta?.let { Idioma(it.id, it.codigo, it.nombre) },
    xp = xp,
    rachaActual = rachaActual,
    rachaMaxima = rachaMaxima,
    ultimaActividad = ultimaActividad?.let { LocalDate.parse(it) }
)

private fun Usuario.conNombreUsuarioGoogleSiFalta(metadata: JsonObject?): Usuario {
    if (!nombreUsuario.isNullOrBlank()) return this
    // Google suele enviar preferred_username, name o full_name; el correo es el último respaldo.
    val valor = listOf("preferred_username", "user_name", "name", "full_name")
        .asSequence()
        .mapNotNull { metadata?.get(it)?.jsonPrimitive?.contentOrNull }
        .firstOrNull { it.isNotBlank() }
        ?: correo?.substringBefore('@')
    val generado = valor
        ?.lowercase()
        ?.replace(Regex("[^a-z0-9_]+"), "")
        ?.take(24)
        ?.takeIf { it.isNotBlank() }
    return copy(nombreUsuario = generado)
}

private fun ProgresoLeccionDto.toDomain() = ProgresoLeccion(
    usuarioId = UUID.fromString(usuarioId),
    leccion = leccion.toDomain(),
    estado = estado,
    puntaje = puntaje,
    fechaCompletado = fechaCompletado?.let { Instant.parse(it) }
)

private fun PalabraFavoritaDto.toDomain() = PalabraFavorita(
    usuarioId = UUID.fromString(usuarioId),
    palabra = palabra.toDomain()
)

private fun MemoriaTukiDto.toDomain() = MemoriaTuki(
    id = id,
    usuarioId = UUID.fromString(usuarioId),
    tipo = tipo,
    resumen = resumen,
    fecha = Instant.parse(fecha)
)

private fun Usuario.toUpdateDto() = UsuarioUpdateDto(
    nombre = nombre,
    apellido = apellido,
    nombreUsuario = nombreUsuario,

    edad = edad,
    pais = pais,
    ciudad = ciudad,
    idiomaMetaId = idiomaMeta?.id,
    xp = xp,
    rachaActual = rachaActual,
    rachaMaxima = rachaMaxima,
    ultimaActividad = ultimaActividad?.toString()
)

private fun ProgresoLeccion.toUpsertDto() = ProgresoLeccionUpsertDto(
    usuarioId = usuarioId.toString(),
    leccionId = leccion.id,
    estado = estado,
    puntaje = puntaje,
    fechaCompletado = fechaCompletado?.toString()
)
