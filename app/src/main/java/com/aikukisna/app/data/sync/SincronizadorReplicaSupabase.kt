package com.aikukisna.app.data.sync

import com.aikukisna.app.data.local.SembradorReplicaSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.auth.auth
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SincronizadorReplicaSupabase @Inject constructor(
    private val client: SupabaseClient,
    private val importador: SembradorReplicaSupabase
) {
    private data class Tabla(val nombre: String, val pk: List<String>)

    private val tablasPublicas = listOf(
        Tabla("idioma", listOf("id")),
        Tabla("categoria", listOf("id")),
        Tabla("fuente_documento", listOf("id")),
        Tabla("palabra", listOf("id")),
        Tabla("palabra_canonica", listOf("palabra_id")),
        Tabla("palabra_fuente", listOf("palabra_id", "fuente_id")),
        Tabla("acepcion", listOf("id")),
        Tabla("traduccion", listOf("id")),
        Tabla("traduccion_acepcion", listOf("id")),
        Tabla("traduccion_fuente", listOf("traduccion_id", "fuente_id", "tipo_vinculo")),
        Tabla("variante_palabra", listOf("id")),
        Tabla("expresion", listOf("id")),
        Tabla("traduccion_expresion", listOf("id")),
        Tabla("expresion_contexto_cultural", listOf("expresion_id", "cultura_id", "tipo_relacion")),
        Tabla("oracion_ejemplo", listOf("id")),
        Tabla("regla_gramatical", listOf("id")),
        Tabla("regla_pronunciacion", listOf("id")),
        Tabla("ejemplo_regla_gramatical", listOf("id")),
        Tabla("cultura_contenido", listOf("id")),
        Tabla("leccion", listOf("id")),
        Tabla("leccion_palabra", listOf("leccion_id", "palabra_id")),
        Tabla("leccion_expresion", listOf("leccion_id", "expresion_id", "tipo_vinculo")),
        Tabla("leccion_oracion", listOf("leccion_id", "oracion_id", "tipo_vinculo")),
        Tabla("leccion_fuente", listOf("leccion_id", "fuente_id", "tipo_vinculo")),
        Tabla("leccion_cultura", listOf("leccion_id", "cultura_id", "tipo_vinculo")),
        Tabla("leccion_regla_gramatical", listOf("leccion_id", "regla_id")),
        Tabla("leccion_regla_pronunciacion", listOf("leccion_id", "regla_id")),
        Tabla("mundo_gamificado", listOf("codigo")),
        Tabla("leccion_experiencia_gamificada", listOf("leccion_id")),
        Tabla("actividad_leccion", listOf("id")),
        Tabla("actividad_recurso", listOf("actividad_id", "tipo_recurso", "recurso_id")),
        Tabla("etapa_ruta_curricular", listOf("codigo")),
        Tabla("leccion_ruta_curricular", listOf("leccion_id")),
        Tabla("alineacion_curricular", listOf("id")),
        Tabla("logro", listOf("id")),
        Tabla("tuki_respuesta_sistema", listOf("id"))
    )

    private val tablasAutenticadas = listOf(
        Tabla("evidencia_curricular_leccion", listOf("id"))
    )

    suspend fun sincronizar(): Int {
        var total = sincronizarTablas(tablasPublicas)
        if (client.auth.currentUserOrNull() != null) {
            total += sincronizarTablas(tablasAutenticadas)
        }
        return total
    }

    private suspend fun sincronizarTablas(tablas: List<Tabla>): Int {
        var total = 0
        for (tabla in tablas) {
            var desde = 0L
            while (true) {
                val hasta = desde + TAMANO_PAGINA - 1
                val filas = client.from(tabla.nombre)
                    .select { range(desde..hasta) }
                    .decodeList<JsonObject>()
                if (filas.isEmpty()) break
                filas.forEach { fila -> clave(tabla, fila) }
                total += importador.actualizarDesdeSupabase(tabla.nombre, filas)
                if (filas.size < TAMANO_PAGINA) break
                desde += TAMANO_PAGINA
            }
        }
        return total
    }

    private fun clave(tabla: Tabla, fila: JsonObject): String = tabla.pk.joinToString("|") { campo ->
        fila[campo]?.jsonPrimitive?.contentOrNull
            ?: error("${tabla.nombre}: falta clave primaria $campo")
    }

    companion object {
        private const val TAMANO_PAGINA = 500L
    }
}
