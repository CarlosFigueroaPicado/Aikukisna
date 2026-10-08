package com.aikukisna.app.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "idioma_cache")
data class IdiomaEntity(
    @PrimaryKey val id: Int,
    val codigo: String,
    val nombre: String
)

@Entity(tableName = "categoria_cache")
data class CategoriaEntity(
    @PrimaryKey val id: Int,
    val nombre: String
)

@Entity(tableName = "fuente_documento_cache")
data class FuenteDocumentoEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val autor: String?,
    val anio: Int?,
    val institucion: String?
)

@Entity(tableName = "palabra_cache", indices = [Index(value = ["idiomaId", "texto_normalizado"])])
data class PalabraEntity(
    @PrimaryKey val id: Int,
    val idiomaId: Int,
    val texto: String,
    val categoriaId: Int?,
    val fuenteId: Int,
    @ColumnInfo(name = "pronunciacion") val pronunciacion: String? = null,
    @ColumnInfo(name = "pronunciacion_fonetica") val pronunciacionFonetica: String? = null,
    @ColumnInfo(name = "pronunciacion_verificada", defaultValue = "0")
    val pronunciacionVerificada: Boolean = false,
    @ColumnInfo(name = "texto_normalizado", defaultValue = "''")
    val textoNormalizado: String = "",
    @ColumnInfo(name = "estado_validacion", defaultValue = "'importada'")
    val estadoValidacion: String = "importada",
    @ColumnInfo(name = "created_at_epoch_ms", defaultValue = "0")
    val createdAtEpochMs: Long = 0,
    @ColumnInfo(name = "updated_at_epoch_ms", defaultValue = "0")
    val updatedAtEpochMs: Long = 0
)

@Entity(tableName = "palabra_fuente_cache", primaryKeys = ["palabraId", "fuenteId"])
data class PalabraFuenteEntity(
    val palabraId: Int,
    val fuenteId: Int,
    val paginaInicio: Int? = null,
    val paginaFin: Int? = null,
    val nota: String? = null,
    val updatedAtEpochMs: Long = 0
)

@Entity(
    tableName = "acepcion_cache",
    indices = [Index(value = ["palabraId", "numeroAcepcion"], unique = true), Index("updatedAtEpochMs")]
)
data class AcepcionEntity(
    @PrimaryKey val id: Long,
    val palabraId: Int,
    val numeroAcepcion: Int,
    val definicion: String?,
    val contexto: String?,
    val categoriaGramatical: String?,
    val estadoValidacion: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "traduccion_acepcion_cache",
    indices = [Index("acepcionOrigenId"), Index("acepcionDestinoId"), Index("updatedAtEpochMs")]
)
data class TraduccionAcepcionEntity(
    @PrimaryKey val id: Long,
    val acepcionOrigenId: Long,
    val acepcionDestinoId: Long,
    val tipo: String,
    val estadoValidacion: String,
    val nivelConfianza: Double?,
    val nota: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "variante_palabra_cache",
    indices = [Index(value = ["textoNormalizado", "palabraId"]), Index("updatedAtEpochMs")]
)
data class VariantePalabraEntity(
    @PrimaryKey val id: Long,
    val palabraId: Int,
    val texto: String,
    val textoNormalizado: String,
    val tipo: String,
    val estadoValidacion: String,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "expresion_cache",
    indices = [Index(value = ["idiomaId", "textoNormalizado"]), Index("updatedAtEpochMs")]
)
data class ExpresionEntity(
    @PrimaryKey val id: Long,
    val idiomaId: Int,
    val texto: String,
    val textoNormalizado: String,
    val tipo: String,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "traduccion_expresion_cache",
    indices = [Index("expresionOrigenId"), Index("expresionDestinoId"), Index("updatedAtEpochMs")]
)
data class TraduccionExpresionEntity(
    @PrimaryKey val id: Long,
    val expresionOrigenId: Long,
    val expresionDestinoId: Long,
    val estadoValidacion: String,
    val nota: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "sincronizacion_linguistica")
data class SincronizacionLinguisticaEntity(
    @PrimaryKey val recurso: String,
    val ultimoUpdatedAtEpochMs: Long,
    val ultimoId: Long
)

@Entity(
    tableName = "audio_pronunciacion_cache",
    primaryKeys = ["palabraId", "idiomaCodigo", "tipoReferencia", "referencia"],
    foreignKeys = [
        ForeignKey(
            entity = PalabraEntity::class,
            parentColumns = ["id"],
            childColumns = ["palabraId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["palabraId", "idiomaCodigo", "verificado", "origen"]),
        Index(value = ["idiomaCodigo", "textoNormalizado"])
    ]
)
data class AudioPronunciacionEntity(
    val palabraId: Int,
    val idiomaCodigo: String,
    val tipoReferencia: String,
    val referencia: String,
    val origen: String,
    val verificado: Boolean,
    // Forma escrita sin signos, para encontrar el audio aunque la pantalla no tenga el id de la palabra.
    @ColumnInfo(defaultValue = "") val textoNormalizado: String = "",
    // Crédito del audio (fuente_documento.id y nombre del hablante o canal), solo como dato, nunca dentro del audio.
    val fuenteId: Int? = null,
    val hablante: String? = null
)

@Entity(tableName = "traduccion_cache")
data class TraduccionEntity(
    @PrimaryKey val id: Int,
    val palabraOrigenId: Int,
    val palabraDestinoId: Int,
    val nota: String?,
    @ColumnInfo(defaultValue = "'importada'") val estadoValidacion: String = "importada",
    val fuenteId: Int? = null,
    val nivelConfianza: Double? = null,
    @ColumnInfo(defaultValue = "0") val esPreferida: Boolean = false,
    @ColumnInfo(defaultValue = "0") val updatedAtEpochMs: Long = 0
)

@Entity(tableName = "leccion_cache")
data class LeccionEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val capituloNumero: Int?,
    val nivel: Int,
    val categoriaId: Int?,
    val idiomaMetaId: Int
)

@Entity(tableName = "oracion_ejemplo_cache")
data class OracionEjemploEntity(
    @PrimaryKey val id: Int,
    val textoOrigen: String,
    val textoDestino: String,
    val idiomaOrigenId: Int,
    val idiomaDestinoId: Int,
    val fuenteId: Int,
    val leccionId: Int,
    @ColumnInfo(defaultValue = "'importada'") val estadoValidacion: String = "importada",
    @ColumnInfo(defaultValue = "0") val updatedAtEpochMs: Long = 0
)

@Entity(tableName = "leccion_palabra_cache", primaryKeys = ["leccionId", "palabraId"])
data class LeccionPalabraEntity(
    val leccionId: Int,
    val palabraId: Int
)


@Entity(tableName = "completar_leccion_pendiente")
data class CompletarLeccionPendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val leccionId: Int,
    val puntaje: Int,
    val fechaCreadoEpochMs: Long
)

@Entity(
    tableName = "memoria_tuki_local",
    indices = [
        Index(value = ["usuarioId", "fechaEpochMs"]),
        Index(value = ["sincronizada"])
    ]
)
data class MemoriaTukiLocalEntity(
    @PrimaryKey val id: String,
    val usuarioId: String,
    val tipo: String,
    val resumen: String,
    val palabrasClave: String,
    val fechaEpochMs: Long,
    val usos: Int = 0,
    val sincronizada: Boolean = false,
    val tema: String? = null,
    val ultimaPalabraOFrase: String? = null,
    val ultimaLeccionId: Int? = null,
    val ultimaIntencion: String? = null
)

@Entity(tableName = "mundo_gamificado_cache")
data class MundoGamificadoEntity(
    @PrimaryKey val codigo: String,
    val nombreVisible: String,
    val descripcionVisible: String,
    val orden: Int,
    val iconoClave: String?,
    val xpDesbloqueo: Int,
    val recompensaFinal: String?,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "leccion_experiencia_gamificada_cache", indices = [Index("mundoCodigo")])
data class LeccionExperienciaGamificadaEntity(
    @PrimaryKey val leccionId: Int,
    val tituloVisible: String,
    val subtituloVisible: String?,
    val formatoPrincipal: String,
    val xpBase: Int,
    val mundoCodigo: String?,
    val ordenEnMundo: Int?,
    val recompensaVisible: String?,
    val mensajeInicioTuki: String?,
    val mensajeFinTuki: String?,
    val usaTuki: Boolean,
    val usaAudio: Boolean,
    val usaLectura: Boolean,
    val usaEscritura: Boolean,
    val usaOralidad: Boolean,
    val tieneRetoFinal: Boolean,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "actividad_leccion_cache",
    indices = [Index(value = ["leccionId", "orden"], unique = true), Index(value = ["leccionId", "codigo"], unique = true)]
)
data class ActividadLeccionEntity(
    @PrimaryKey val id: Long,
    val leccionId: Int,
    val orden: Int,
    val codigo: String,
    val tipo: String,
    val habilidadCurricular: String,
    val mecanicaGamificada: String,
    val descripcionEstudiante: String,
    val evidenciaAprendizaje: String?,
    val xp: Int,
    val obligatoria: Boolean,
    val activa: Boolean,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "actividad_recurso_cache",
    primaryKeys = ["actividadId", "tipoRecurso", "recursoId"],
    indices = [Index(value = ["actividadId", "orden"])]
)
data class ActividadRecursoEntity(
    val actividadId: Long,
    val tipoRecurso: String,
    val recursoId: Long,
    val orden: Int,
    val rol: String,
    val textoPrincipal: String?,
    val textoApoyo: String?,
    val estadoValidacion: String?,
    val fuenteId: Int?
)

@Entity(tableName = "etapa_ruta_curricular_cache")
data class EtapaRutaCurricularEntity(
    @PrimaryKey val codigo: String,
    val nombre: String,
    val orden: Int,
    val modalidad: String,
    val gradoOrigen: Int?,
    val gradoDestino: Int?,
    val unidadPedagogicaCiclo: String?,
    val descripcion: String,
    val activa: Boolean
)

@Entity(tableName = "leccion_ruta_curricular_cache", indices = [Index("etapaCodigo")])
data class LeccionRutaCurricularEntity(
    @PrimaryKey val leccionId: Int,
    val etapaCodigo: String?,
    val estadoMapeo: String,
    val esRefuerzo: Boolean,
    val esTransicion: Boolean,
    val prerrequisitoDescripcion: String?,
    val propositoTransicion: String?,
    val justificacion: String?,
    val fuentePrimariaId: Int?,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "evidencia_curricular_leccion_cache",
    indices = [Index("leccionId"), Index("etapaCodigo"), Index("fuenteId")]
)
data class EvidenciaCurricularLeccionEntity(
    @PrimaryKey val id: Long,
    val leccionId: Int,
    val etapaCodigo: String,
    val fuenteId: Int,
    val grado: Int,
    val asignaturaArea: String,
    val unidadOficial: String?,
    val competenciaEjeTransversal: String?,
    val competenciaGrado: String?,
    val indicadorLogro: String?,
    val contenidoOficial: String?,
    val criterioEvaluacion: String?,
    val actividadAikukisna: String?,
    val evidenciaAprendizaje: String?,
    val paginaSeccion: String?,
    val tipoCorrespondencia: String,
    val aplicaSear: Boolean,
    val ejeSear: String?,
    val adecuacionIntercultural: String?,
    val estadoValidacion: String,
    val observacion: String?,
    val lenguaAplicacionId: Int?,
    val naturalezaAplicacion: String?,
    val esTextoOficialLiteral: Boolean,
    val updatedAtEpochMs: Long
)
