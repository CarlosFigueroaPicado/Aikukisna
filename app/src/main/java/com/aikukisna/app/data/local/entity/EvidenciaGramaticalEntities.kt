package com.aikukisna.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categoria_linguistica_cache",
    indices = [Index(value = ["codigo"], unique = true), Index("fuenteId")]
)
data class CategoriaLinguisticaEntity(
    @PrimaryKey val id: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "acepcion_categoria_linguistica_cache",
    primaryKeys = ["acepcionId", "categoriaId"],
    indices = [Index("categoriaId"), Index("fuenteId")]
)
data class AcepcionCategoriaLinguisticaEntity(
    val acepcionId: Long,
    val categoriaId: Long,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "raiz_verbal_cache",
    indices = [
        Index(value = ["acepcionId", "textoNormalizado"], unique = true),
        Index(value = ["evidenciaId"], unique = true),
        Index("idiomaId"),
        Index("fuenteId")
    ]
)
data class RaizVerbalEntity(
    @PrimaryKey val id: Long,
    val acepcionId: Long?,
    val idiomaId: Int,
    val texto: String,
    val textoNormalizado: String,
    val regularidad: String?,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val tipoRaiz: String? = null,
    val evidenciaId: String? = null,
    val segmentacionOriginal: String? = null,
    val segmentacionNormalizada: String? = null,
    val grafiaOriginal: String? = null,
    val pagina: Int? = null,
    val motivoRevision: String? = null
)

@Entity(
    tableName = "forma_verbal_documentada_cache",
    indices = [
        Index("raizId"),
        Index("palabraId"),
        Index("reglaId"),
        Index(value = ["raizId", "codigoForma", "textoNormalizado"], unique = true),
        Index(value = ["evidenciaId"], unique = true),
        Index("fuenteId")
    ]
)
data class FormaVerbalDocumentadaEntity(
    @PrimaryKey val id: Long,
    val raizId: Long?,
    val palabraId: Int?,
    val reglaId: Long?,
    val codigoForma: String,
    val texto: String,
    val textoNormalizado: String,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val tipoEvidenciaRaiz: String? = null,
    val evidenciaId: String? = null,
    val segmentacionOriginal: String? = null,
    val segmentacionNormalizada: String? = null,
    val grafiaOriginal: String? = null,
    val pagina: Int? = null,
    val motivoRevision: String? = null,
    val regularidadDocumentada: String? = null
)

@Entity(
    tableName = "excepcion_regla_gramatical_cache",
    primaryKeys = ["reglaId", "tipoEntidad", "entidadId"],
    indices = [
        Index("formaDocumentadaId"),
        Index("fuenteId")
    ]
)
data class ExcepcionReglaGramaticalEntity(
    val reglaId: Long,
    val tipoEntidad: String,
    val entidadId: Long,
    val motivo: String,
    val formaDocumentadaId: Long?,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "marca_gramatical_documentada_cache",
    primaryKeys = ["reglaId", "tipoEntidad", "entidadId", "codigoMarca"],
    indices = [
        Index(value = ["tipoEntidad", "entidadId"]),
        Index("reglaId"),
        Index("codigoMarca"),
        Index("fuenteId")
    ]
)
data class MarcaGramaticalDocumentadaEntity(
    val reglaId: Long,
    val tipoEntidad: String,
    val entidadId: Long,
    val codigoMarca: String,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)

@Entity(
    tableName = "componente_regla_gramatical_cache",
    indices = [
        Index(value = ["reglaId", "tipoContexto", "contextoId", "nombreComponente"], unique = true),
        Index(value = ["tipoEntidadOrigen", "entidadOrigenId"]),
        Index("fuenteId")
    ]
)
data class ComponenteReglaGramaticalEntity(
    @PrimaryKey val id: Long,
    val reglaId: Long,
    val tipoContexto: String,
    val contextoId: Long,
    val nombreComponente: String,
    val tipoEntidadOrigen: String,
    val entidadOrigenId: Long,
    val campoOrigen: String,
    val valorDocumentado: String,
    val estadoValidacion: String,
    val fuenteId: Int?,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
