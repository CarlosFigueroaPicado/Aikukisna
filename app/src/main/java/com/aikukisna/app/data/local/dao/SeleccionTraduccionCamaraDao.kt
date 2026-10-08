package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aikukisna.app.data.local.entity.SeleccionTraduccionCamaraEntity

@Dao
interface SeleccionTraduccionCamaraDao {
    @Upsert
    suspend fun guardar(selecciones: List<SeleccionTraduccionCamaraEntity>)

    @Query(
        """
        SELECT * FROM seleccion_traduccion_camara_cache
        WHERE etiquetaInglesNormalizada = :etiqueta
          AND idiomaDestinoId = :idiomaDestinoId
        LIMIT 1
        """
    )
    suspend fun obtenerPorEtiqueta(
        etiqueta: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamaraEntity?

    @Query(
        """
        SELECT * FROM seleccion_traduccion_camara_cache
        WHERE conceptoEspanolNormalizado = :concepto
          AND idiomaDestinoId = :idiomaDestinoId
        LIMIT 1
        """
    )
    suspend fun obtenerPorConcepto(
        concepto: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamaraEntity?

    @Query("SELECT COUNT(*) FROM seleccion_traduccion_camara_cache")
    suspend fun contar(): Int

    @Query(
        """
        SELECT COUNT(*) FROM seleccion_traduccion_camara_cache
        WHERE conceptoEspanolNormalizado = 'cangrejo'
          AND idiomaDestinoId = 3
        """
    )
    suspend fun contarCangrejoKriol(): Int
}
