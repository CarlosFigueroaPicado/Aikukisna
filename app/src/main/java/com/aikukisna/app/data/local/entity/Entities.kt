package com.aikukisna.app.data.local.entity

import androidx.room.Entity
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

@Entity(tableName = "palabra_cache")
data class PalabraEntity(
    @PrimaryKey val id: Int,
    val idiomaId: Int,
    val texto: String,
    val categoriaId: Int?,
    val fuenteId: Int
)

@Entity(tableName = "traduccion_cache")
data class TraduccionEntity(
    @PrimaryKey val id: Int,
    val palabraOrigenId: Int,
    val palabraDestinoId: Int,
    val nota: String?
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
    val fuenteId: Int,
    val leccionId: Int
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