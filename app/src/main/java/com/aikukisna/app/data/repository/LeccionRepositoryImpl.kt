package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.CacheEscritor
import com.aikukisna.app.data.local.ConectividadHelper
import com.aikukisna.app.data.remote.dto.LeccionDto
import com.aikukisna.app.data.remote.dto.LeccionPalabraDto
import com.aikukisna.app.data.remote.dto.OracionEjemploDto
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LeccionRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val conectividad: ConectividadHelper,
    private val cache: CacheEscritor
) : LeccionRepository {

    override suspend fun obtenerLecciones(nivel: Int?): List<Leccion> {
        if (conectividad.hayConexion()) {
            try {
                val resultado = client.from("leccion")
                    .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)")) {
                        filter {
                            nivel?.let { eq("nivel", it) }
                        }
                    }
                    .decodeList<LeccionDto>()
                    .map { it.toDomain() }
                cache.cachearLecciones(resultado)
                return resultado
            } catch (e: Exception) {
                // caer a caché
            }
        }
        return cache.leerLecciones(nivel)
    }

    override suspend fun obtenerLeccionPorId(id: Int): Leccion? {
        if (conectividad.hayConexion()) {
            try {
                val resultado = client.from("leccion")
                    .select(Columns.raw("*, categoria(*), idioma_meta:idioma_meta_id(*)")) {
                        filter { eq("id", id) }
                    }
                    .decodeSingleOrNull<LeccionDto>()
                    ?.toDomain()
                if (resultado != null) cache.cachearLecciones(listOf(resultado))
                return resultado
            } catch (e: Exception) {
                // caer a caché
            }
        }
        return cache.leerLeccion(id)
    }

    override suspend fun obtenerContenidoLeccion(leccionId: Int): ContenidoLeccion {
        val leccion = obtenerLeccionPorId(leccionId)
            ?: error("La lección $leccionId no existe")

        // Misma regla de negocio de siempre:
        // capituloNumero != null -> vocabulario (leccion_palabra)
        // capituloNumero == null -> frases (oracion_ejemplo)
        return if (leccion.capituloNumero != null) {
            ContenidoLeccion.Vocabulario(obtenerVocabulario(leccionId))
        } else {
            ContenidoLeccion.Frases(obtenerFrases(leccionId))
        }
    }

    override suspend fun completarLeccion(leccionId: Int, puntaje: Int) {
        if (conectividad.hayConexion()) {
            try {
                client.postgrest.rpc(
                    "completar_leccion",
                    buildJsonObject {
                        put("p_leccion_id", leccionId)
                        put("p_puntaje", puntaje)
                    }
                )
                return
            } catch (e: Exception) {
                // Sin conexión real pese al chequeo previo, o falla
                // transitoria: se encola en vez de perder el progreso.
            }
        }
        cache.encolarLeccionPendiente(leccionId, puntaje)
    }

    override suspend fun sincronizarLeccionesPendientes(): Int {
        if (!conectividad.hayConexion()) return 0

        val pendientes = cache.obtenerLeccionesPendientes()
        var exitosas = 0
        for (pendiente in pendientes) {
            try {
                client.postgrest.rpc(
                    "completar_leccion",
                    buildJsonObject {
                        put("p_leccion_id", pendiente.leccionId)
                        put("p_puntaje", pendiente.puntaje)
                    }
                )
                cache.borrarLeccionPendiente(pendiente.id)
                exitosas++
            } catch (e: Exception) {
                // Sigue sin señal, o falló puntualmente esta — se queda en
                // la cola, se reintenta la próxima vez que se llame esto.
            }
        }
        return exitosas
    }

    private suspend fun obtenerVocabulario(leccionId: Int): List<com.aikukisna.app.domain.model.Palabra> {
        if (conectividad.hayConexion()) {
            try {
                val palabras = client.from("leccion_palabra")
                    .select(Columns.raw("palabra(*, idioma(*), categoria(*), fuente_documento(*))")) {
                        filter { eq("leccion_id", leccionId) }
                    }
                    .decodeList<LeccionPalabraDto>()
                    .map { it.palabra.toDomain() }
                cache.cachearPalabras(palabras)
                cache.cachearVinculosLeccionPalabra(palabras.map { leccionId to it.id })
                return palabras
            } catch (e: Exception) {
                // caer a caché
            }
        }
        return cache.leerVocabularioLeccion(leccionId)
    }

    private suspend fun obtenerFrases(leccionId: Int): List<OracionEjemplo> {
        if (conectividad.hayConexion()) {
            try {
                val oraciones = client.from("oracion_ejemplo")
                    .select(Columns.raw("*, fuente_documento(*)")) {
                        filter { eq("leccion_id", leccionId) }
                    }
                    .decodeList<OracionEjemploDto>()
                    .map { it.toDomain() }
                cache.cachearOraciones(leccionId, oraciones)
                return oraciones
            } catch (e: Exception) {
                // caer a caché
            }
        }
        return cache.leerOracionesLeccion(leccionId)
    }
}

private fun OracionEjemploDto.toDomain() = OracionEjemplo(
    id = id,
    textoOrigen = textoOrigen,
    textoDestino = textoDestino,
    leccion = null, // ya se conoce el contexto (leccionId) desde donde se llamó; no se re-embebe
    fuente = FuenteDocumento(fuente.id, fuente.titulo, fuente.autor, fuente.anio, fuente.institucion)
)