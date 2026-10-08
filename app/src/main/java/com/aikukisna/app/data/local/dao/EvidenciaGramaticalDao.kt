package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.aikukisna.app.data.local.entity.AcepcionCategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.AcepcionEntity
import com.aikukisna.app.data.local.entity.CategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.ComponenteReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.ExcepcionReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.FormaVerbalDocumentadaEntity
import com.aikukisna.app.data.local.entity.MarcaGramaticalDocumentadaEntity
import com.aikukisna.app.data.local.entity.RaizVerbalEntity

@Dao
interface EvidenciaGramaticalDao {
    @Upsert suspend fun guardarCategorias(categorias: List<CategoriaLinguisticaEntity>)
    @Upsert suspend fun guardarCategoriasAcepcion(relaciones: List<AcepcionCategoriaLinguisticaEntity>)
    @Upsert suspend fun guardarRaicesVerbales(raices: List<RaizVerbalEntity>)
    @Upsert suspend fun guardarFormasVerbales(formas: List<FormaVerbalDocumentadaEntity>)
    @Upsert suspend fun guardarExcepciones(excepciones: List<ExcepcionReglaGramaticalEntity>)
    @Upsert suspend fun guardarMarcas(marcas: List<MarcaGramaticalDocumentadaEntity>)
    @Upsert suspend fun guardarComponentes(componentes: List<ComponenteReglaGramaticalEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarCategorias(categorias: List<CategoriaLinguisticaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarCategoriasAcepcion(relaciones: List<AcepcionCategoriaLinguisticaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarExcepciones(excepciones: List<ExcepcionReglaGramaticalEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarMarcas(marcas: List<MarcaGramaticalDocumentadaEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarRaicesVerbales(raices: List<RaizVerbalEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun sembrarFormasVerbales(formas: List<FormaVerbalDocumentadaEntity>)

    @Query("SELECT * FROM categoria_linguistica_cache WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerCategoriaPorCodigo(codigo: String): CategoriaLinguisticaEntity?

    @Query("SELECT * FROM acepcion_cache WHERE categoriaGramatical IS NOT NULL AND trim(categoriaGramatical) != '' AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerAcepcionesConCategoriaDocumentada(): List<AcepcionEntity>

    @Query("SELECT id FROM palabra_cache WHERE id IN (:ids) AND estado_validacion != 'rechazada'")
    suspend fun obtenerIdsPalabrasDisponibles(ids: List<Int>): List<Int>

    @Query("SELECT id FROM expresion_cache WHERE id IN (:ids) AND estadoValidacion != 'rechazada'")
    suspend fun obtenerIdsExpresionesDisponibles(ids: List<Long>): List<Long>

    @Query("SELECT id FROM regla_gramatical_cache WHERE id IN (:ids) AND estadoValidacion != 'rechazada'")
    suspend fun obtenerIdsReglasDisponibles(ids: List<Long>): List<Long>

    @Query("SELECT * FROM acepcion_categoria_linguistica_cache WHERE acepcionId = :acepcionId ORDER BY categoriaId")
    suspend fun obtenerCategoriasDeAcepcion(acepcionId: Long): List<AcepcionCategoriaLinguisticaEntity>

    @Query("SELECT c.* FROM categoria_linguistica_cache c INNER JOIN acepcion_categoria_linguistica_cache ac ON ac.categoriaId = c.id WHERE ac.acepcionId = :acepcionId AND ac.estadoValidacion != 'rechazada' ORDER BY c.id")
    suspend fun obtenerCategoriasCompletasDeAcepcion(acepcionId: Long): List<CategoriaLinguisticaEntity>

    @Query("SELECT * FROM raiz_verbal_cache WHERE acepcionId = :acepcionId AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerRaicesDeAcepcion(acepcionId: Long): List<RaizVerbalEntity>

    @Query("SELECT * FROM raiz_verbal_cache WHERE id = :id AND estadoValidacion != 'rechazada' LIMIT 1")
    suspend fun obtenerRaiz(id: Long): RaizVerbalEntity?

    @Query("SELECT * FROM raiz_verbal_cache WHERE evidenciaId = :evidenciaId LIMIT 1")
    suspend fun obtenerRaizPorEvidencia(evidenciaId: String): RaizVerbalEntity?

    @Query("SELECT * FROM forma_verbal_documentada_cache WHERE raizId = :raizId AND estadoValidacion != 'rechazada' ORDER BY codigoForma, id")
    suspend fun obtenerFormasDeRaiz(raizId: Long): List<FormaVerbalDocumentadaEntity>

    @Query("SELECT * FROM forma_verbal_documentada_cache WHERE raizId = :raizId AND codigoForma = :codigoForma AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerFormasDeRaizPorCodigo(raizId: Long, codigoForma: String): List<FormaVerbalDocumentadaEntity>

    @Query("SELECT * FROM forma_verbal_documentada_cache WHERE id = :id AND estadoValidacion != 'rechazada' LIMIT 1")
    suspend fun obtenerForma(id: Long): FormaVerbalDocumentadaEntity?

    @Query("SELECT * FROM forma_verbal_documentada_cache WHERE evidenciaId = :evidenciaId LIMIT 1")
    suspend fun obtenerFormaPorEvidencia(evidenciaId: String): FormaVerbalDocumentadaEntity?

    @Query("SELECT * FROM excepcion_regla_gramatical_cache WHERE reglaId = :reglaId AND tipoEntidad = :tipoEntidad AND entidadId = :entidadId AND estadoValidacion != 'rechazada' ORDER BY entidadId")
    suspend fun obtenerExcepciones(reglaId: Long, tipoEntidad: String, entidadId: Long): List<ExcepcionReglaGramaticalEntity>

    @Query("SELECT * FROM marca_gramatical_documentada_cache WHERE tipoEntidad = :tipoEntidad AND entidadId = :entidadId AND estadoValidacion != 'rechazada' ORDER BY reglaId, codigoMarca")
    suspend fun obtenerMarcas(tipoEntidad: String, entidadId: Long): List<MarcaGramaticalDocumentadaEntity>

    @Query("SELECT COUNT(*) FROM categoria_linguistica_cache")
    suspend fun contarCategorias(): Int

    @Query("SELECT COUNT(*) FROM acepcion_categoria_linguistica_cache")
    suspend fun contarRelacionesCategoria(): Int

    @Query("SELECT COUNT(*) FROM excepcion_regla_gramatical_cache")
    suspend fun contarExcepciones(): Int

    @Query("SELECT COUNT(*) FROM marca_gramatical_documentada_cache")
    suspend fun contarMarcas(): Int

    @Query("SELECT COUNT(*) FROM raiz_verbal_cache")
    suspend fun contarRaices(): Int

    @Query("SELECT COUNT(*) FROM forma_verbal_documentada_cache")
    suspend fun contarFormasVerbales(): Int

    @Query("SELECT COUNT(*) FROM componente_regla_gramatical_cache")
    suspend fun contarComponentes(): Int

    @Query("SELECT * FROM componente_regla_gramatical_cache WHERE reglaId = :reglaId AND tipoContexto = :tipoContexto AND contextoId = :contextoId AND estadoValidacion != 'rechazada' ORDER BY nombreComponente")
    suspend fun obtenerComponentes(
        reglaId: Long,
        tipoContexto: String,
        contextoId: Long
    ): List<ComponenteReglaGramaticalEntity>
}
