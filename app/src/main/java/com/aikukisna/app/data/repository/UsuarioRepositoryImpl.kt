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
    private val offlineUserDataCache: OfflineUserDataCache
) : UsuarioRepository {

    override suspend fun obtenerUsuario(id: UUID): Usuario? {
        return try {
            client.from("usuario")
                .select(Columns.raw("*, idioma_meta:idioma_meta_id(*)")) {
                    filter { eq("id", id.toString()) }
                }
                .decodeSingleOrNull<UsuarioDto>()
                ?.toDomain()
                ?.conNombreUsuarioGoogleSiFalta(client.auth.currentUserOrNull()?.userMetadata)
                ?.also(perfilLocalCache::guardar)
        } catch (_: Exception) {
            perfilLocalCache.leer(id)
        }
    }

    override suspend fun actualizarUsuario(usuario: Usuario) {
        try {
            client.from("usuario")
                .update(usuario.toUpdateDto()) {
                    filter { eq("id", usuario.id.toString()) }
                }
        } finally {
            perfilLocalCache.guardar(usuario)
        }
    }
    override suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion> {
        return try {
            client.from("progreso_leccion")
                .select(Columns.raw("*, leccion(*, categoria(*), idioma_meta:idioma_meta_id(*))")) {
                    filter { eq("usuario_id", usuarioId.toString()) }
                }
                .decodeList<ProgresoLeccionDto>()
                .map { it.toDomain() }
                .also { offlineUserDataCache.guardarProgreso(it) }
        } catch (_: Exception) {
            offlineUserDataCache.leerProgreso(usuarioId)
        }
    }

    override suspend fun actualizarProgreso(progreso: ProgresoLeccion) {
        client.from("progreso_leccion")
            .upsert(progreso.toUpsertDto()) {
                onConflict = "usuario_id,leccion_id"
            }
    }

    override suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita> {
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
