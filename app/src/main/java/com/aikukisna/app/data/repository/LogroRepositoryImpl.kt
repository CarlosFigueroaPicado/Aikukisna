package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.LogroDesbloqueadoDto
import com.aikukisna.app.data.remote.dto.LogroDesbloqueadoInsertDto
import com.aikukisna.app.data.remote.dto.LogroDto
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.repository.LogroRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class LogroRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : LogroRepository {

    override suspend fun obtenerLogros(): List<Logro> {
        return client.from("logro")
            .select(Columns.raw("*, categoria(*)"))
            .decodeList<LogroDto>()
            .map { it.toDomain() }
    }

    override suspend fun obtenerLogrosDesbloqueados(usuarioId: UUID): List<LogroDesbloqueado> {
        return client.from("logro_desbloqueado")
            .select(Columns.raw("*, logro(*, categoria(*))")) {
                filter { eq("usuario_id", usuarioId.toString()) }
            }
            .decodeList<LogroDesbloqueadoDto>()
            .map { it.toDomain() }
    }

    override suspend fun desbloquearLogro(usuarioId: UUID, logroId: Int) {
        client.from("logro_desbloqueado").insert(
            LogroDesbloqueadoInsertDto(
                usuarioId = usuarioId.toString(),
                logroId = logroId,
                fecha = Instant.now().toString()
            )
        )
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