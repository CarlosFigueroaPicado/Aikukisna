package com.aikukisna.app.data.gramatica

import android.content.Context
import com.aikukisna.app.domain.gramatica.DecisionReglaGramatical
import com.aikukisna.app.domain.gramatica.EjemploReglaValidada
import com.aikukisna.app.domain.gramatica.ReglaGramaticalValidada
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogoReglasGramaticales @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()
    @Volatile private var cache: List<ReglaGramaticalValidada>? = null

    suspend fun obtenerTodas(): List<ReglaGramaticalValidada> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: cargar().also { reglas ->
                require(reglas.size == TOTAL_REGLAS_VALIDADAS) {
                    "El catálogo gramatical validado debe contener $TOTAL_REGLAS_VALIDADAS reglas"
                }
                require(reglas.map { it.id }.distinct().size == reglas.size) {
                    "El catálogo gramatical validado contiene identificadores duplicados"
                }
                cache = reglas
            }
        }
    }

    private fun cargar(): List<ReglaGramaticalValidada> =
        context.assets.open(NOMBRE_ACTIVO).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter(String::isNotBlank).map { line ->
                json.decodeFromString<ReglaGramaticalDto>(line).toDomain()
            }.toList()
        }

    private fun ReglaGramaticalDto.toDomain(): ReglaGramaticalValidada {
        val decision = DecisionReglaGramatical.entries.firstOrNull { it.valor == decisionValidador }
            ?: error("Decisión gramatical desconocida en la regla $reglaId: $decisionValidador")
        if (decision == DecisionReglaGramatical.GENERACION_CONTROLADA) {
            require(!patronEjecutable.isNullOrBlank()) {
                "La regla controlada $codigo no contiene un patrón ejecutable"
            }
        } else {
            require(patronEjecutable.isNullOrBlank()) {
                "La regla $codigo no puede declarar un patrón ejecutable"
            }
        }
        return ReglaGramaticalValidada(
            id = reglaId,
            idiomaId = idiomaId,
            codigo = codigo,
            categoria = categoria,
            titulo = titulo,
            descripcion = descripcion,
            patronDocumentado = patronDocumentado,
            aplicacionDocumentada = aplicacionDocumentada,
            estadoOriginal = estadoOriginal,
            ejemplos = ejemplos.map {
                EjemploReglaValidada(
                    textoIdioma = it.textoIdioma,
                    traduccionEspanol = it.traduccionEspanol,
                    estadoValidacion = it.estadoValidacion,
                    fuenteId = it.fuenteId
                )
            },
            decision = decision,
            patronEjecutable = patronEjecutable,
            restricciones = restricciones,
            observacionesValidador = observacionesValidador
        )
    }

    private companion object {
        const val NOMBRE_ACTIVO = "plantillas_gramaticales_validadas.jsonl"
        const val TOTAL_REGLAS_VALIDADAS = 40
        val json = Json { ignoreUnknownKeys = false }
    }
}

@Serializable
private data class ReglaGramaticalDto(
    @SerialName("regla_id") val reglaId: Long,
    @SerialName("idioma_id") val idiomaId: Int,
    val codigo: String,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    @SerialName("patron_documentado") val patronDocumentado: String? = null,
    @SerialName("aplicacion_documentada") val aplicacionDocumentada: String? = null,
    @SerialName("estado_original") val estadoOriginal: String,
    val ejemplos: List<EjemploReglaDto>,
    @SerialName("decision_validador") val decisionValidador: String,
    @SerialName("patron_ejecutable") val patronEjecutable: String? = null,
    val restricciones: String,
    @SerialName("observaciones_validador") val observacionesValidador: String
)

@Serializable
private data class EjemploReglaDto(
    @SerialName("texto_idioma") val textoIdioma: String,
    @SerialName("traduccion_espanol") val traduccionEspanol: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String,
    @SerialName("fuente_id") val fuenteId: Int? = null
)
