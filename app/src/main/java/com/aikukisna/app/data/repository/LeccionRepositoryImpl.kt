package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.LeccionDto
import com.aikukisna.app.data.remote.dto.LeccionPalabraDto
import com.aikukisna.app.data.remote.dto.OracionEjemploDto
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject

class LeccionRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : LeccionRepository {

    override suspend fun obtenerLecciones(nivel: Int?): List<Leccion> {
        return client.from("leccion")
            .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)")) {
                filter {
                    nivel?.let { eq("nivel", it) }
                }
            }
            .decodeList<LeccionDto>()
            .map { it.toDomain() }
    }

    override suspend fun obtenerLeccionPorId(id: Int): Leccion? {
        return client.from("leccion")
            .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)")) {
                filter { eq("id", id) }
            }
            .decodeSingleOrNull<LeccionDto>()
            ?.toDomain()
    }

    override suspend fun obtenerContenidoLeccion(leccionId: Int): ContenidoLeccion {
        val leccion = obtenerLeccionPorId(leccionId)
            ?: error("La lección $leccionId no existe")

        // Regla de negocio central del proyecto:
        // capituloNumero != null -> lección de vocabulario (leccion_palabra)
        // capituloNumero == null -> lección de frases (oracion_ejemplo)
        return if (leccion.capituloNumero != null) {
            val palabras = client.from("leccion_palabra")
                .select(Columns.raw("palabra(*, idioma(*), categoria(*), fuente_documento(*))")) {
                    filter { eq("leccion_id", leccionId) }
                }
                .decodeList<LeccionPalabraDto>()
                .map { it.palabra.toDomain() }
            ContenidoLeccion.Vocabulario(palabras)
        } else {
            val oraciones = client.from("oracion_ejemplo")
                .select(Columns.raw("*, fuente_documento(*)")) {
                    filter { eq("leccion_id", leccionId) }
                }
                .decodeList<OracionEjemploDto>()
                .map { it.toDomain() }
            ContenidoLeccion.Frases(oraciones)
        }
    }
}

private fun OracionEjemploDto.toDomain() = OracionEjemplo(
    id = id,
    textoOrigen = textoOrigen,
    textoDestino = textoDestino,
    leccion = null, // ya se conoce el contexto (leccionId) desde donde se llamó; no se re-embebe
    fuente = FuenteDocumento(fuente.id, fuente.titulo, fuente.autor, fuente.anio, fuente.institucion)
)