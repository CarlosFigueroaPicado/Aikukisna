package com.aikukisna.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "cultura_contenido_cache")
data class CulturaContenidoEntity(
    @androidx.room.PrimaryKey val id: Int,
    val titulo: String,
    val contenido: String,
    val rangoPaginaInicio: Int?,
    val rangoPaginaFin: Int?,
    val fuenteId: Int,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "regla_gramatical_cache", indices = [Index("idiomaId"), Index("fuenteId"), Index("updatedAtEpochMs")])
data class ReglaGramaticalEntity(
    @androidx.room.PrimaryKey val id: Long,
    val idiomaId: Int,
    val codigo: String,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val patron: String?,
    val aplicacion: String?,
    val productiva: Boolean,
    val prioridad: Int,
    val fuenteId: Int?,
    val estadoValidacion: String,
    val notas: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "ejemplo_regla_gramatical_cache", indices = [Index("reglaId"), Index("fuenteId")])
data class EjemploReglaGramaticalEntity(
    @androidx.room.PrimaryKey val id: Long,
    val reglaId: Long,
    val textoIdioma: String,
    val traduccionEspanol: String?,
    val fuenteId: Int?,
    val estadoValidacion: String,
    val nota: String?,
    val createdAtEpochMs: Long,
    val idiomaTraduccionId: Int?
)

@Entity(tableName = "regla_pronunciacion_cache", indices = [Index("idiomaId"), Index("fuenteId"), Index("updatedAtEpochMs")])
data class ReglaPronunciacionEntity(
    @androidx.room.PrimaryKey val id: Long,
    val idiomaId: Int,
    val patron: String,
    val tipo: String,
    val descripcion: String,
    val reemplazoFonetico: String?,
    val ejemplo: String?,
    val fuenteId: Int?,
    val prioridad: Int,
    val activa: Boolean,
    val createdAtEpochMs: Long,
    val estadoValidacion: String,
    val notas: String?,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "leccion_expresion_cache", primaryKeys = ["leccionId", "expresionId", "tipoVinculo"])
data class LeccionExpresionEntity(val leccionId: Int, val expresionId: Long, val tipoVinculo: String, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "leccion_oracion_cache", primaryKeys = ["leccionId", "oracionId", "tipoVinculo"])
data class LeccionOracionEntity(val leccionId: Int, val oracionId: Int, val tipoVinculo: String, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "leccion_fuente_cache", primaryKeys = ["leccionId", "fuenteId", "tipoVinculo"])
data class LeccionFuenteEntity(val leccionId: Int, val fuenteId: Int, val tipoVinculo: String, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "leccion_cultura_cache", primaryKeys = ["leccionId", "culturaId", "tipoVinculo"])
data class LeccionCulturaEntity(val leccionId: Int, val culturaId: Int, val tipoVinculo: String, val createdAtEpochMs: Long, val nota: String?)

@Entity(tableName = "leccion_regla_gramatical_cache", primaryKeys = ["leccionId", "reglaId"])
data class LeccionReglaGramaticalEntity(val leccionId: Int, val reglaId: Long, val orden: Int, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "leccion_regla_pronunciacion_cache", primaryKeys = ["leccionId", "reglaId"])
data class LeccionReglaPronunciacionEntity(val leccionId: Int, val reglaId: Long, val orden: Int, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "expresion_contexto_cultural_cache", primaryKeys = ["expresionId", "culturaId", "tipoRelacion"])
data class ExpresionContextoCulturalEntity(val expresionId: Long, val culturaId: Int, val tipoRelacion: String, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "traduccion_fuente_cache", primaryKeys = ["traduccionId", "fuenteId", "tipoVinculo"])
data class TraduccionFuenteEntity(val traduccionId: Int, val fuenteId: Int, val tipoVinculo: String, val nota: String?, val createdAtEpochMs: Long)

@Entity(tableName = "palabra_canonica_cache")
data class PalabraCanonicaEntity(
    @androidx.room.PrimaryKey val palabraId: Int,
    val palabraCanonicaId: Int,
    val motivo: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "alineacion_curricular_cache", indices = [Index("leccionId"), Index("fuenteId")])
data class AlineacionCurricularEntity(
    @androidx.room.PrimaryKey val id: Long,
    val leccionId: Int,
    val fuenteId: Int,
    val unidad: String?,
    val tema: String,
    val nivelReferencia: String?,
    val evidencia: String?,
    val estadoValidacion: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val tipoAlineacion: String?,
    val gradoReferencia: String?,
    val areaCurricular: String?,
    val competencia: String?,
    val indicadorLogro: String?,
    val contenidoCurricular: String?,
    val ejeSear: String?,
    val referenciaDocumental: String?,
    val paginaReferencia: String?,
    val aptaRevisionFormal: Boolean
)

@Entity(tableName = "tuki_respuesta_sistema_cache", indices = [Index("codigo", unique = true), Index("idiomaRespuestaId")])
data class TukiRespuestaSistemaEntity(
    @androidx.room.PrimaryKey val id: Long,
    val codigo: String,
    val tipo: String,
    val idiomaRespuestaId: Int?,
    val texto: String,
    val prioridad: Int,
    val activa: Boolean,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(tableName = "logro_cache")
data class LogroEntity(
    @androidx.room.PrimaryKey val id: Int,
    val nombre: String,
    val descripcion: String,
    val condicionTipo: String,
    val condicionValor: Int,
    val categoriaId: Int?
)

@Entity(tableName = "revision_linguistica_cache", indices = [Index("tipoEntidad", "entidadId"), Index("estado")])
data class RevisionLinguisticaEntity(
    @androidx.room.PrimaryKey val id: Long,
    val tipoEntidad: String,
    val entidadId: Long,
    val clasificacion: String?,
    val estado: String,
    val observacion: String?,
    val revisadoPor: String?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

// Datos privados: solo se sincroniza el usuario autenticado, nunca se incluyen globalmente en la semilla.
@Entity(tableName = "usuario_actual_cache")
data class UsuarioActualEntity(
    @androidx.room.PrimaryKey val id: String,
    val nombreUsuario: String?, val correo: String?, val edad: Int?, val pais: String?, val ciudad: String?,
    val idiomaMetaId: Int?, val xp: Int, val rachaActual: Int, val rachaMaxima: Int,
    val ultimaActividad: String?, val nombre: String?, val apellido: String?
)

@Entity(tableName = "progreso_leccion_usuario_cache", primaryKeys = ["usuarioId", "leccionId"])
data class ProgresoLeccionUsuarioEntity(val usuarioId: String, val leccionId: Int, val estado: String, val puntaje: Int?, val fechaCompletadoEpochMs: Long?)

@Entity(tableName = "palabra_favorita_usuario_cache", primaryKeys = ["usuarioId", "palabraId"])
data class PalabraFavoritaUsuarioEntity(val usuarioId: String, val palabraId: Int)

@Entity(tableName = "logro_desbloqueado_usuario_cache", primaryKeys = ["usuarioId", "logroId"])
data class LogroDesbloqueadoUsuarioEntity(val usuarioId: String, val logroId: Int, val fechaEpochMs: Long)

@Entity(tableName = "replica_supabase_cache", primaryKeys = ["tabla", "clave"], indices = [Index("tabla"), Index("updatedAtEpochMs")])
data class ReplicaSupabaseEntity(
    val tabla: String,
    val clave: String,
    val json: String,
    val updatedAtEpochMs: Long
)
