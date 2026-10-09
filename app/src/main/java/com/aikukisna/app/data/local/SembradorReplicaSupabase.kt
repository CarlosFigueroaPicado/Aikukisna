package com.aikukisna.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.zip.GZIPInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock

@Singleton
class SembradorReplicaSupabase @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val database: AikukisnaDatabase,
    private val pobladorEvidenciaGramatical: PobladorEvidenciaGramatical,
    private val pobladorCorpusCamara: PobladorCorpusCamara
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
    private val esquemas = mutableMapOf<String, EsquemaTabla>()
    private val evidenciaKriolAprobada by lazy(::cargarEvidenciaKriolAprobada)
    private val mutexSembrado = Mutex()

    // Diccionario, Traductor y Tuki llaman a esto al abrirse. Antes cada apertura reescribía toda
    // la evidencia gramatical dentro de una transacción y bloqueaba las demás consultas.
    @Volatile
    private var preparadoEnProceso = false

    @Volatile
    private var actualizandoVersion = false
    private var vaciarVinculosAlRecargar = false
    private val tablasVinculoVaciadas = mutableSetOf<String>()

    suspend fun sembrarSiExiste(): Int {
        if (preparadoEnProceso) return 0
        // Una actualización de versión tarda minutos; mientras tanto el contenido anterior sirve
        // y las pantallas no deben quedarse esperando.
        if (actualizandoVersion) return 0
        return withContext(Dispatchers.IO) {
            mutexSembrado.withLock {
                if (preparadoEnProceso) return@withLock 0
                sembrarSiHaceFalta().also { preparadoEnProceso = true }
            }
        }
    }

    private suspend fun sembrarSiHaceFalta(): Int {
        if (
            preferencias.getInt(CLAVE_VERSION, 0) >= VERSION_REPLICA &&
            hayContenidoDisponible()
        ) {
            prepararEvidenciaUnaVezPorInstalacion()
            return 0
        }
        if (!context.assets.list("").orEmpty().contains(ARCHIVO)) {
            if (hayContenidoDisponible()) prepararEvidenciaUnaVezPorInstalacion()
            return 0
        }

        // Al pasar de una versión anterior también se corrigen filas que ya estaban en el teléfono.
        val actualizar = preferencias.getInt(CLAVE_VERSION, 0) > 0 && hayContenidoDisponible()
        actualizandoVersion = actualizar
        // La recarga solo inserta y actualiza: los vínculos de lecciones que se quitaron en Supabase
        // quedarían en el teléfono. Esas tablas se vacían al llegar su primer bloque y se recargan completas.
        tablasVinculoVaciadas.clear()
        vaciarVinculosAlRecargar = actualizar
        var total = 0
        val lote = ArrayList<FilaReplica>(TAMANO_LOTE)
        context.assets.open(ARCHIVO).use { raw ->
            GZIPInputStream(raw).bufferedReader(Charsets.UTF_8).use { reader ->
                while (true) {
                    val linea = reader.readLine() ?: break
                    if (linea.isBlank()) continue
                    val envoltorio = json.parseToJsonElement(linea).jsonObject
                    lote += FilaReplica(
                        tablaRemota = envoltorio.getValue("tabla").jsonPrimitive.content,
                        datos = envoltorio.getValue("json").jsonObject
                    )
                    if (lote.size == TAMANO_LOTE) {
                        total += guardarLote(lote, actualizarExistentes = actualizar)
                        lote.clear()
                    }
                }
            }
        }
        if (lote.isNotEmpty()) total += guardarLote(lote, actualizarExistentes = actualizar)

        check(hayContenidoDisponible()) {
            "La réplica no produjo el contenido local esencial"
        }
        preferencias.edit().putInt(CLAVE_VERSION, VERSION_REPLICA).commit()
        prepararEvidenciaUnaVezPorInstalacion()
        vaciarVinculosAlRecargar = false
        actualizandoVersion = false
        return total
    }

    // La evidencia sale de archivos del APK y de la réplica: solo cambia al reinstalar la app o
    // cuando la sincronización trae filas nuevas (actualizarDesdeSupabase borra la marca).
    private suspend fun prepararEvidenciaUnaVezPorInstalacion() {
        val instalacion = context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        if (preferencias.getLong(CLAVE_EVIDENCIA, 0L) == instalacion) return
        val inicio = System.currentTimeMillis()
        if (sembrarEvidenciaGramatical()) {
            preferencias.edit().putLong(CLAVE_EVIDENCIA, instalacion).apply()
            Log.i(ETIQUETA, "Evidencia gramatical preparada en ${System.currentTimeMillis() - inicio} ms")
        }
    }

    private suspend fun sembrarEvidenciaGramatical(): Boolean = try {
        aplicarPoliticaKriolAprobada()
        pobladorEvidenciaGramatical.sembrar()
        pobladorCorpusCamara.sembrar()
        true
    } catch (error: Exception) {
        Log.w(
            ETIQUETA,
            "No se pudo preparar la evidencia gramatical local; tipo=${error.javaClass.simpleName}"
        )
        false
    }

    suspend fun hayContenidoDisponible(): Boolean = withContext(Dispatchers.IO) {
        val sqlite = database.openHelper.readableDatabase
        TABLAS_ESENCIALES.all { tabla ->
            // EXISTS se detiene en la primera fila; COUNT(*) recorría tablas de decenas de miles.
            sqlite.query("SELECT EXISTS(SELECT 1 FROM `$tabla` LIMIT 1)")
                .use { cursor -> cursor.moveToFirst() && cursor.getLong(0) > 0L }
        }
    }

    suspend fun actualizarDesdeSupabase(tabla: String, filas: List<JsonObject>): Int =
        guardarLote(
            filas.map { FilaReplica(tablaRemota = tabla, datos = it) },
            actualizarExistentes = true
        ).also { if (it > 0) preferencias.edit().remove(CLAVE_EVIDENCIA).apply() }

    private suspend fun guardarLote(
        filas: List<FilaReplica>,
        actualizarExistentes: Boolean
    ): Int {
        var guardadas = 0
        database.withTransaction {
            val sqlite = database.openHelper.writableDatabase
            filas.forEach filaLoop@ { fila ->
                if (!evidenciaKriolAprobada.permite(fila)) return@filaLoop
                val tablaLocal = TABLAS[fila.tablaRemota] ?: return@filaLoop
                if (vaciarVinculosAlRecargar && tablaLocal in TABLAS_VINCULO_LECCION && tablasVinculoVaciadas.add(tablaLocal)) {
                    sqlite.delete(tablaLocal, null, null)
                }
                val esquema = esquemaDe(sqlite, tablaLocal)
                val valores = ContentValues()
                fila.datos.forEach campoLoop@ { (campoRemoto, valor) ->
                    val columna = resolverColumna(campoRemoto, esquema.columnas) ?: return@campoLoop
                    guardarValor(valores, columna, campoRemoto, valor)
                }
                if (tablaLocal == "oracion_ejemplo_cache" && valores.get("leccionId") == null) {
                    valores.put("leccionId", 0)
                }
                if (valores.size() > 0) {
                    val resultado = sqlite.insert(
                        tablaLocal,
                        SQLiteDatabase.CONFLICT_IGNORE,
                        valores
                    )
                    if (resultado != -1L) {
                        guardadas++
                    } else if (actualizarExistentes) {
                        val argumentos = esquema.clavesPrimarias.map { valores.get(it) }.toTypedArray()
                        if (argumentos.none { it == null }) {
                            guardadas += sqlite.update(
                                tablaLocal,
                                SQLiteDatabase.CONFLICT_ABORT,
                                valores,
                                esquema.clavesPrimarias.joinToString(" AND ") { "`$it` = ?" },
                                argumentos
                            )
                        }
                    }
                }
            }
        }
        return guardadas
    }

    private suspend fun aplicarPoliticaKriolAprobada() = database.withTransaction {
        val sqlite = database.openHelper.writableDatabase
        val reglas = evidenciaKriolAprobada.reglas.joinToString(",")
        val ejemplos = evidenciaKriolAprobada.ejemplos.joinToString(",")
        sqlite.execSQL(
            "DELETE FROM ejemplo_regla_gramatical_cache " +
                "WHERE reglaId IN ($reglas) AND id NOT IN ($ejemplos)"
        )
        sqlite.execSQL(
            "DELETE FROM regla_gramatical_cache " +
                "WHERE idiomaId = 3 AND id NOT IN ($reglas)"
        )
    }

    private fun cargarEvidenciaKriolAprobada(): EvidenciaKriolAprobada {
        val reglas = mutableSetOf<Long>()
        val ejemplos = mutableSetOf<Long>()
        context.assets.open(ARCHIVO_EVIDENCIA_KRIOL).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter(String::isNotBlank).forEach { linea ->
                val fila = json.parseToJsonElement(linea).jsonObject
                check(fila.getValue("decision_revision").jsonPrimitive.content == "aprobada")
                check(fila.getValue("apta_para_sembrado").jsonPrimitive.booleanOrNull == true)
                val id = fila.getValue("registro_origen_id").jsonPrimitive.longOrNull
                    ?: error("Evidencia Kriol sin registro_origen_id")
                when (fila.getValue("tabla_origen").jsonPrimitive.content) {
                    "regla_gramatical" -> reglas += id
                    "ejemplo_regla_gramatical" -> ejemplos += id
                    else -> error("Tabla de evidencia Kriol no admitida")
                }
            }
        }
        check(reglas.size == TOTAL_REGLAS_KRIOL_APROBADAS)
        check(ejemplos.size == TOTAL_EJEMPLOS_KRIOL_APROBADOS)
        return EvidenciaKriolAprobada(reglas, ejemplos)
    }

    private fun esquemaDe(sqlite: SupportSQLiteDatabase, tabla: String): EsquemaTabla =
        esquemas.getOrPut(tabla) {
            sqlite.query("PRAGMA table_info(`$tabla`)").use { cursor ->
                val indiceNombre = cursor.getColumnIndexOrThrow("name")
                val indicePk = cursor.getColumnIndexOrThrow("pk")
                val columnas = mutableSetOf<String>()
                val claves = mutableListOf<Pair<Int, String>>()
                while (cursor.moveToNext()) {
                    val nombre = cursor.getString(indiceNombre)
                    columnas += nombre
                    val ordenPk = cursor.getInt(indicePk)
                    if (ordenPk > 0) claves += ordenPk to nombre
                }
                EsquemaTabla(
                    columnas = columnas,
                    clavesPrimarias = claves.sortedBy { it.first }.map { it.second }
                )
            }
        }

    private fun resolverColumna(campo: String, columnas: Set<String>): String? {
        val candidatos = when (campo) {
            "created_at" -> listOf("createdAtEpochMs", "created_at_epoch_ms")
            "updated_at" -> listOf("updatedAtEpochMs", "updated_at_epoch_ms")
            else -> listOf(campo, campo.snakeACamel())
        }
        return candidatos.firstOrNull(columnas::contains)
    }

    private fun guardarValor(
        valores: ContentValues,
        columna: String,
        campoRemoto: String,
        valor: JsonElement
    ) {
        if (valor is JsonNull) {
            valores.putNull(columna)
            return
        }
        val primitivo = valor as? JsonPrimitive
        if (primitivo == null) {
            valores.put(columna, valor.toString())
            return
        }
        if (campoRemoto == "created_at" || campoRemoto == "updated_at") {
            valores.put(columna, primitivo.contentOrNull.aEpochMillis())
            return
        }
        primitivo.booleanOrNull?.let { booleano ->
            valores.put(columna, if (booleano) 1 else 0)
            return
        }
        primitivo.longOrNull?.let { entero ->
            valores.put(columna, entero)
            return
        }
        primitivo.doubleOrNull?.let { decimal ->
            valores.put(columna, decimal)
            return
        }
        valores.put(columna, primitivo.content)
    }

    private fun String.snakeACamel(): String {
        val partes = split('_')
        return partes.first() + partes.drop(1).joinToString("") {
            it.replaceFirstChar(Char::uppercaseChar)
        }
    }

    private fun String?.aEpochMillis(): Long =
        this?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrDefault(0L) } ?: 0L

    private data class FilaReplica(val tablaRemota: String, val datos: JsonObject)

    private data class EvidenciaKriolAprobada(
        val reglas: Set<Long>,
        val ejemplos: Set<Long>
    ) {
        fun permite(fila: FilaReplica): Boolean = when (fila.tablaRemota) {
            "regla_gramatical" -> {
                val idiomaId = fila.datos["idioma_id"]?.jsonPrimitive?.longOrNull
                val id = fila.datos["id"]?.jsonPrimitive?.longOrNull
                idiomaId != IDIOMA_KRIOL || (id != null && id in reglas)
            }
            "ejemplo_regla_gramatical" -> {
                val reglaId = fila.datos["regla_id"]?.jsonPrimitive?.longOrNull
                val id = fila.datos["id"]?.jsonPrimitive?.longOrNull
                reglaId == null || reglaId !in reglas || (id != null && id in ejemplos)
            }
            else -> true
        }
    }

    private data class EsquemaTabla(
        val columnas: Set<String>,
        val clavesPrimarias: List<String>
    )

    companion object {
        const val ARCHIVO = "supabase_completa.ndjson.gzip"
        const val ARCHIVO_EVIDENCIA_KRIOL = "evidencia_gramatical_kriol_aprobada.jsonl"
        private const val ETIQUETA = "EvidenciaGramatical"
        private const val PREFERENCIAS = "replica_supabase_inicial"
        private const val CLAVE_VERSION = "version"
        private const val CLAVE_EVIDENCIA = "evidencia_instalacion"
        // 2: añade Wiktionary (palabras Español–Inglés) y frases de Tatoeba; Kriol pasa a "bzk".
        private const val VERSION_REPLICA = 19
        private const val TAMANO_LOTE = 500
        private const val IDIOMA_KRIOL = 3L
        private const val TOTAL_REGLAS_KRIOL_APROBADAS = 32
        private const val TOTAL_EJEMPLOS_KRIOL_APROBADOS = 68
        private val TABLAS_VINCULO_LECCION = setOf("leccion_palabra_cache", "leccion_oracion_cache", "leccion_expresion_cache")
        private val TABLAS_ESENCIALES = setOf(
            "idioma_cache",
            "palabra_cache",
            "traduccion_cache",
            "acepcion_cache",
            "expresion_cache",
            "oracion_ejemplo_cache",
            "regla_gramatical_cache",
            "regla_pronunciacion_cache",
            "cultura_contenido_cache",
            "tuki_respuesta_sistema_cache",
            "leccion_cache"
        )

        private val TABLAS = mapOf(
            "idioma" to "idioma_cache",
            "categoria" to "categoria_cache",
            "fuente_documento" to "fuente_documento_cache",
            "palabra" to "palabra_cache",
            "palabra_canonica" to "palabra_canonica_cache",
            "palabra_fuente" to "palabra_fuente_cache",
            "acepcion" to "acepcion_cache",
            "traduccion" to "traduccion_cache",
            "traduccion_acepcion" to "traduccion_acepcion_cache",
            "traduccion_fuente" to "traduccion_fuente_cache",
            "variante_palabra" to "variante_palabra_cache",
            "expresion" to "expresion_cache",
            "traduccion_expresion" to "traduccion_expresion_cache",
            "expresion_contexto_cultural" to "expresion_contexto_cultural_cache",
            "oracion_ejemplo" to "oracion_ejemplo_cache",
            "regla_gramatical" to "regla_gramatical_cache",
            "regla_pronunciacion" to "regla_pronunciacion_cache",
            "ejemplo_regla_gramatical" to "ejemplo_regla_gramatical_cache",
            "cultura_contenido" to "cultura_contenido_cache",
            "leccion" to "leccion_cache",
            "leccion_palabra" to "leccion_palabra_cache",
            "leccion_expresion" to "leccion_expresion_cache",
            "leccion_oracion" to "leccion_oracion_cache",
            "leccion_fuente" to "leccion_fuente_cache",
            "leccion_cultura" to "leccion_cultura_cache",
            "leccion_regla_gramatical" to "leccion_regla_gramatical_cache",
            "leccion_regla_pronunciacion" to "leccion_regla_pronunciacion_cache",
            "alineacion_curricular" to "alineacion_curricular_cache",
            "mundo_gamificado" to "mundo_gamificado_cache",
            "leccion_experiencia_gamificada" to "leccion_experiencia_gamificada_cache",
            "actividad_leccion" to "actividad_leccion_cache",
            "actividad_recurso" to "actividad_recurso_cache",
            "etapa_ruta_curricular" to "etapa_ruta_curricular_cache",
            "leccion_ruta_curricular" to "leccion_ruta_curricular_cache",
            "evidencia_curricular_leccion" to "evidencia_curricular_leccion_cache",
            "logro" to "logro_cache",
            "tuki_respuesta_sistema" to "tuki_respuesta_sistema_cache"
        )
    }
}
