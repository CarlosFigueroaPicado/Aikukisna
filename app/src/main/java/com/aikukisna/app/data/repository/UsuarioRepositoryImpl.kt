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
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import com.aikukisna.app.data.remote.dto.MemoriaTukiInsertDto
import com.aikukisna.app.data.remote.dto.PalabraFavoritaInsertDto
import com.aikukisna.app.data.remote.dto.ProgresoLeccionUpsertDto
import com.aikukisna.app.data.remote.dto.UsuarioUpdateDto




class UsuarioRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : UsuarioRepository {

    override suspend fun obtenerUsuario(id: UUID): Usuario? {
        return client.from("usuario")
            .select(Columns.raw("*, idioma_meta(*)")) {
                filter { eq("id", id.toString()) }
            }
            .decodeSingleOrNull<UsuarioDto>()
            ?.toDomain()
    }

    override suspend fun actualizarUsuario(usuario: Usuario) {
        client.from("usuario")
            .update(usuario.toUpdateDto()) {
                filter { eq("id", usuario.id.toString()) }
            }
    }
    override suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion> {
        return client.from("progreso_leccion")
            .select(Columns.raw("*, leccion(*, categoria(*))")) {
                filter { eq("usuario_id", usuarioId.toString()) }
            }
            .decodeList<ProgresoLeccionDto>()
            .map { it.toDomain() }
    }

    override suspend fun actualizarProgreso(progreso: ProgresoLeccion) {
        client.from("progreso_leccion")
            .upsert(progreso.toUpsertDto()) {
                onConflict = "usuario_id,leccion_id"
            }
    }

    override suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita> {
        return client.from("palabra_favorita")
            .select(Columns.raw("*, palabra(*, idioma(*), categoria(*), fuente_documento(*))")) {
                filter { eq("usuario_id", usuarioId.toString()) }
            }
            .decodeList<PalabraFavoritaDto>()
            .map { it.toDomain() }
    }

    override suspend fun marcarFavorito(usuarioId: UUID, palabraId: Int) {
        client.from("palabra_favorita").insert(
            PalabraFavoritaInsertDto(usuarioId = usuarioId.toString(), palabraId = palabraId)
        )
    }

    override suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int) {
        client.from("palabra_favorita").delete {
            filter {
                eq("usuario_id", usuarioId.toString())
                eq("palabra_id", palabraId)
            }
        }
    }

    override suspend fun obtenerMemoriaTuki(usuarioId: UUID): List<MemoriaTuki> {
        return client.from("memoria_tuki")
            .select {
                filter { eq("usuario_id", usuarioId.toString()) }
            }
            .decodeList<MemoriaTukiDto>()
            .map { it.toDomain() }
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
    correo = correo,
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