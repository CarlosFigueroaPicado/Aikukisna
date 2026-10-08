package com.aikukisna.app.data.local

import android.content.Context
import androidx.room.withTransaction
import com.aikukisna.app.data.local.dao.EvidenciaGramaticalDao
import com.aikukisna.app.data.local.entity.AcepcionCategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.CategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.ExcepcionReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.FormaVerbalDocumentadaEntity
import com.aikukisna.app.data.local.entity.MarcaGramaticalDocumentadaEntity
import com.aikukisna.app.data.local.entity.RaizVerbalEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

data class ResultadoPoblacionEvidencia(
    val categorias: Int,
    val relacionesCategoria: Int,
    val excepciones: Int,
    val marcas: Int,
    val raicesVerbales: Int,
    val formasVerbales: Int
) {
    val total: Int = categorias + relacionesCategoria + excepciones + marcas + raicesVerbales + formasVerbales
}

@Singleton
class PobladorEvidenciaGramatical @Inject constructor(
    private val database: AikukisnaDatabase,
    private val dao: EvidenciaGramaticalDao,
    @ApplicationContext private val context: Context
) {
    suspend fun sembrar(): ResultadoPoblacionEvidencia = database.withTransaction {
        val acepciones = dao.obtenerAcepcionesConCategoriaDocumentada()
        val categoriasPresentes = acepciones.mapNotNull { acepcion ->
            categoriasPorNombre[acepcion.categoriaGramatical?.trim()?.lowercase()]
        }.distinctBy { it.id }
        dao.sembrarCategorias(categoriasPresentes)
        val idsCategoriasPresentes = categoriasPresentes.mapNotNull { categoria ->
            dao.obtenerCategoriaPorCodigo(categoria.codigo)?.let { categoria.nombre.lowercase() to it }
        }.toMap()
        val relaciones = acepciones.mapNotNull { acepcion ->
            val categoria = idsCategoriasPresentes[acepcion.categoriaGramatical?.trim()?.lowercase()]
                ?: return@mapNotNull null
            AcepcionCategoriaLinguisticaEntity(
                acepcionId = acepcion.id,
                categoriaId = categoria.id,
                estadoValidacion = acepcion.estadoValidacion,
                fuenteId = null,
                createdAtEpochMs = acepcion.createdAtEpochMs,
                updatedAtEpochMs = acepcion.updatedAtEpochMs
            )
        }

        val reglasDisponibles = dao.obtenerIdsReglasDisponibles(
            (excepcionesBase.map { it.reglaId } + marcasBase.map { it.reglaId }).distinct()
        ).toSet()
        val palabrasDisponibles = dao.obtenerIdsPalabrasDisponibles(
            excepcionesBase.map { it.entidadId.toInt() }.distinct()
        ).map(Int::toLong).toSet()
        val expresionesDisponibles = dao.obtenerIdsExpresionesDisponibles(
            marcasBase.map { it.entidadId }.distinct()
        ).toSet()
        val excepciones = excepcionesBase.filter {
            it.reglaId in reglasDisponibles && it.entidadId in palabrasDisponibles
        }
        val marcas = marcasBase.filter {
            it.reglaId in reglasDisponibles && it.entidadId in expresionesDisponibles
        }

        dao.sembrarCategoriasAcepcion(relaciones)
        dao.sembrarExcepciones(excepciones)
        dao.sembrarMarcas(marcas)

        val evidenciaVerbal = cargarEvidenciaVerbalAprobada()
        val raices = evidenciaVerbal.mapIndexedNotNull { indice, evidencia ->
            val raiz = evidencia.raizDeclarada ?: return@mapIndexedNotNull null
            if (evidencia.tipoEvidenciaRaiz !in TIPOS_RAIZ_APROBADOS ||
                DESTINO_RAIZ !in evidencia.destinosAprobados
            ) return@mapIndexedNotNull null
            RaizVerbalEntity(
                id = ID_RAIZ_INICIAL - indice,
                acepcionId = null,
                idiomaId = evidencia.idiomaId,
                texto = raiz,
                textoNormalizado = raiz,
                regularidad = evidencia.regularidadDocumentada,
                estadoValidacion = evidencia.estadoValidacion,
                fuenteId = evidencia.fuenteId,
                createdAtEpochMs = 0,
                updatedAtEpochMs = 0,
                tipoRaiz = evidencia.tipoEvidenciaRaiz,
                evidenciaId = evidencia.evidenciaId,
                segmentacionOriginal = evidencia.segmentacionOriginal,
                segmentacionNormalizada = evidencia.segmentacionNormalizada,
                grafiaOriginal = evidencia.grafiaOriginal,
                pagina = evidencia.pagina,
                motivoRevision = evidencia.motivoRevision
            )
        }
        val raizIdPorEvidencia = raices.associate { it.evidenciaId to it.id }
        val formas = evidenciaVerbal.mapIndexed { indice, evidencia ->
            FormaVerbalDocumentadaEntity(
                id = ID_FORMA_INICIAL - indice,
                raizId = raizIdPorEvidencia[evidencia.evidenciaId],
                palabraId = null,
                reglaId = null,
                codigoForma = evidencia.tipoForma,
                texto = evidencia.formaDocumentada,
                textoNormalizado = evidencia.formaDocumentada,
                estadoValidacion = evidencia.estadoValidacion,
                fuenteId = evidencia.fuenteId,
                createdAtEpochMs = 0,
                updatedAtEpochMs = 0,
                tipoEvidenciaRaiz = evidencia.tipoEvidenciaRaiz,
                evidenciaId = evidencia.evidenciaId,
                segmentacionOriginal = evidencia.segmentacionOriginal,
                segmentacionNormalizada = evidencia.segmentacionNormalizada,
                grafiaOriginal = evidencia.grafiaOriginal,
                pagina = evidencia.pagina,
                motivoRevision = evidencia.motivoRevision,
                regularidadDocumentada = evidencia.regularidadDocumentada
            )
        }
        dao.sembrarRaicesVerbales(raices)
        dao.sembrarFormasVerbales(formas)

        ResultadoPoblacionEvidencia(
            categorias = categoriasPresentes.size,
            relacionesCategoria = relaciones.size,
            excepciones = excepciones.size,
            marcas = marcas.size,
            raicesVerbales = raices.size,
            formasVerbales = formas.size
        )
    }

    private fun cargarEvidenciaVerbalAprobada(): List<EvidenciaVerbalAprobadaDto> =
        context.assets.open(ACTIVO_EVIDENCIA_VERBAL).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.filter(String::isNotBlank).map { json.decodeFromString<EvidenciaVerbalAprobadaDto>(it) }.toList()
        }.also { evidencias ->
            require(evidencias.size == TOTAL_EVIDENCIAS_VERBALES_APROBADAS)
            require(evidencias.all { it.decisionRevision == DECISION_APROBADA })
            require(evidencias.count { DESTINO_RAIZ in it.destinosAprobados } == TOTAL_RAICES_APROBADAS)
            require(evidencias.all { DESTINO_FORMA in it.destinosAprobados })
            require(evidencias.filter { DESTINO_RAIZ in it.destinosAprobados }
                .all { it.raizDeclarada != null && it.tipoEvidenciaRaiz in TIPOS_RAIZ_APROBADOS })
            require(evidencias.map { it.evidenciaId }.distinct().size == evidencias.size)
        }

    private companion object {
        const val ESTADO_DOCUMENTADA = "documentada"
        const val TIPO_PALABRA = "palabra"
        const val TIPO_EXPRESION = "expresion"
        const val TIPO_BASE_FLEXIVA = "base_flexiva"
        const val TIPO_RAIZ_LEXICA = "raiz_lexica"
        const val DESTINO_RAIZ = "raiz_verbal_cache"
        const val DESTINO_FORMA = "forma_verbal_documentada_cache"
        const val DECISION_APROBADA = "aprobada"
        const val ACTIVO_EVIDENCIA_VERBAL = "evidencia_verbal_miskitu_aprobada.jsonl"
        const val TOTAL_EVIDENCIAS_VERBALES_APROBADAS = 51
        const val TOTAL_RAICES_APROBADAS = 48
        const val ID_RAIZ_INICIAL = -2_100_000L
        const val ID_FORMA_INICIAL = -2_200_000L
        val TIPOS_RAIZ_APROBADOS = setOf(TIPO_BASE_FLEXIVA, TIPO_RAIZ_LEXICA)
        val json = Json { ignoreUnknownKeys = true }

        val categoriasPorNombre = listOf(
            CategoriaLinguisticaEntity(1, "auxiliar_modal", "auxiliar modal", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(2, "sustantivo", "sustantivo", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(3, "adverbio_sustantivo", "adverbio/sustantivo", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(4, "adverbio", "adverbio", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(5, "subordinador", "subordinador", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(6, "marcador_aspecto", "marcador de aspecto", null, ESTADO_DOCUMENTADA, null, 0, 0),
            CategoriaLinguisticaEntity(7, "marcador_negacion", "marcador de negación", null, ESTADO_DOCUMENTADA, null, 0, 0)
        ).associateBy { it.nombre }

        val excepcionesBase = listOf(
            ExcepcionReglaGramaticalEntity(1, TIPO_PALABRA, 16359, "Impedir generalización automática; la regla documenta conjugación propia.", null, ESTADO_DOCUMENTADA, 1, 0, 0),
            ExcepcionReglaGramaticalEntity(1, TIPO_PALABRA, 730, "Impedir generalización automática; la regla documenta conjugación propia.", null, ESTADO_DOCUMENTADA, 1, 0, 0),
            ExcepcionReglaGramaticalEntity(1, TIPO_PALABRA, 1207, "Impedir generalización automática; la regla documenta conjugación propia.", null, ESTADO_DOCUMENTADA, 1, 0, 0)
        )

        val marcasBase = listOf(
            marca(44, 1448, "ORACION_NEGATIVA"),
            marca(44, 1448, "REFERENCIA_PASADA"),
            marca(44, 1572, "ORACION_NEGATIVA"),
            marca(44, 1572, "REFERENCIA_PASADA"),
            marca(44, 1344, "ORACION_NEGATIVA"),
            marca(44, 1344, "REFERENCIA_PASADA"),
            marca(36, 8814, "MODALIDAD_OBLIGACION_NECESIDAD"),
            marca(29, 1598, "HABITUALIDAD_PASADA"),
            marca(29, 1502, "HABITUALIDAD_PASADA"),
            marca(29, 1599, "HABITUALIDAD_PASADA"),
            marca(22, 1004, "RELACION_INSTRUMENTAL"),
            marca(22, 1224, "RELACION_INSTRUMENTAL")
        )

        fun marca(reglaId: Long, expresionId: Long, codigo: String) =
            MarcaGramaticalDocumentadaEntity(
                reglaId = reglaId,
                tipoEntidad = TIPO_EXPRESION,
                entidadId = expresionId,
                codigoMarca = codigo,
                estadoValidacion = ESTADO_DOCUMENTADA,
                fuenteId = 13,
                createdAtEpochMs = 0,
                updatedAtEpochMs = 0
            )
    }
}

@Serializable
private data class EvidenciaVerbalAprobadaDto(
    @SerialName("evidencia_id") val evidenciaId: String,
    @SerialName("idioma_id") val idiomaId: Int,
    @SerialName("tipo_evidencia_raiz") val tipoEvidenciaRaiz: String,
    @SerialName("raiz_declarada") val raizDeclarada: String? = null,
    @SerialName("forma_documentada") val formaDocumentada: String,
    @SerialName("tipo_forma") val tipoForma: String,
    @SerialName("segmentacion_original") val segmentacionOriginal: String,
    @SerialName("segmentacion_normalizada") val segmentacionNormalizada: String? = null,
    @SerialName("regularidad_documentada") val regularidadDocumentada: String? = null,
    @SerialName("fuente_id") val fuenteId: Int? = null,
    val pagina: Int,
    @SerialName("grafia_original") val grafiaOriginal: String,
    @SerialName("estado_validacion") val estadoValidacion: String,
    @SerialName("decision_revision") val decisionRevision: String,
    @SerialName("destinos_aprobados") val destinosAprobados: List<String>,
    @SerialName("motivo_revision") val motivoRevision: String
)
