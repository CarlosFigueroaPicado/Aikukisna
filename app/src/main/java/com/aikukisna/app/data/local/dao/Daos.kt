package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aikukisna.app.data.local.entity.CategoriaEntity
import com.aikukisna.app.data.local.entity.CompletarLeccionPendienteEntity
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.LeccionEntity
import com.aikukisna.app.data.local.entity.LeccionPalabraEntity
import com.aikukisna.app.data.local.entity.OracionEjemploEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.TraduccionEntity

@Dao
interface IdiomaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(idiomas: List<IdiomaEntity>)

    @Query("SELECT * FROM idioma_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): IdiomaEntity?
}

@Dao
interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(categorias: List<CategoriaEntity>)

    @Query("SELECT * FROM categoria_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): CategoriaEntity?
}

@Dao
interface FuenteDocumentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(fuentes: List<FuenteDocumentoEntity>)

    @Query("SELECT * FROM fuente_documento_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): FuenteDocumentoEntity?
}

@Dao
interface PalabraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(palabras: List<PalabraEntity>)

    // %:query% en vez de LIKE con comodines armados afuera — evita que un
    // símbolo % suelto en la búsqueda del usuario rompa el patrón.
    @Query("""
        SELECT * FROM palabra_cache
        WHERE idiomaId = :idiomaId AND texto LIKE '%' || :query || '%'
        ORDER BY texto ASC
        LIMIT :limite OFFSET :offset
    """)
    suspend fun buscar(query: String, idiomaId: Int, limite: Int, offset: Int): List<PalabraEntity>

    @Query("SELECT * FROM palabra_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PalabraEntity?

    @Query("SELECT * FROM palabra_cache WHERE id IN (:ids)")
    suspend fun obtenerPorIds(ids: List<Int>): List<PalabraEntity>

    @Query("SELECT COUNT(*) FROM palabra_cache")
    suspend fun contarTodas(): Int
}

@Dao
interface TraduccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(traducciones: List<TraduccionEntity>)

    @Query("""
        SELECT * FROM traduccion_cache
        WHERE palabraOrigenId = :palabraId OR palabraDestinoId = :palabraId
        ORDER BY id ASC
    """)
    suspend fun obtenerPorPalabra(palabraId: Int): List<TraduccionEntity>
}

@Dao
interface LeccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(lecciones: List<LeccionEntity>)

    @Query("SELECT * FROM leccion_cache WHERE (:nivel IS NULL OR nivel = :nivel) ORDER BY id ASC")
    suspend fun obtenerTodas(nivel: Int?): List<LeccionEntity>

    @Query("SELECT * FROM leccion_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): LeccionEntity?
}

@Dao
interface OracionEjemploDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(oraciones: List<OracionEjemploEntity>)

    @Query("SELECT * FROM oracion_ejemplo_cache WHERE leccionId = :leccionId")
    suspend fun obtenerPorLeccion(leccionId: Int): List<OracionEjemploEntity>
}

@Dao
interface LeccionPalabraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(vinculos: List<LeccionPalabraEntity>)

    @Query("SELECT palabraId FROM leccion_palabra_cache WHERE leccionId = :leccionId")
    suspend fun obtenerPalabraIdsPorLeccion(leccionId: Int): List<Int>
}

@Dao
interface CompletarLeccionPendienteDao {
    @Insert
    suspend fun encolar(pendiente: CompletarLeccionPendienteEntity)

    @Query("SELECT * FROM completar_leccion_pendiente ORDER BY fechaCreadoEpochMs ASC")
    suspend fun obtenerTodas(): List<CompletarLeccionPendienteEntity>

    @Query("DELETE FROM completar_leccion_pendiente WHERE id = :id")
    suspend fun borrar(id: Int)

    @Query("SELECT COUNT(*) FROM completar_leccion_pendiente")
    suspend fun contarPendientes(): Int
}