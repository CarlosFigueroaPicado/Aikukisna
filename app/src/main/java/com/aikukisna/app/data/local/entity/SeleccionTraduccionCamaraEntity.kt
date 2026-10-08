package com.aikukisna.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "seleccion_traduccion_camara_cache",
    primaryKeys = ["conceptoEspanolNormalizado", "idiomaDestinoId"],
    indices = [
        Index("etiquetaInglesNormalizada", "idiomaDestinoId"),
        Index("palabraEspanolId"),
        Index("palabraDestinoId"),
        Index("relacionId")
    ]
)
data class SeleccionTraduccionCamaraEntity(
    val conceptoEspanolNormalizado: String,
    val etiquetaInglesNormalizada: String,
    val idiomaDestinoId: Int,
    val palabraEspanolId: Int,
    val palabraDestinoId: Int,
    val tipoRelacion: String,
    val relacionId: Int?,
    val acepcionOrigenId: Int?,
    val acepcionDestinoId: Int?,
    val estadoValidacion: String,
    val motivoRevision: String
)
