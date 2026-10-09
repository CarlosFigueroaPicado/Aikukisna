package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.aikukisna.app.data.local.entity.CategoriaEntity
import com.aikukisna.app.data.local.entity.AudioPronunciacionEntity
import com.aikukisna.app.data.local.entity.CompletarLeccionPendienteEntity
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.LeccionEntity
import com.aikukisna.app.data.local.entity.LeccionPalabraEntity
import com.aikukisna.app.data.local.entity.OracionEjemploEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.TraduccionEntity
import com.aikukisna.app.data.local.entity.MemoriaTukiLocalEntity
import com.aikukisna.app.data.local.entity.AcepcionEntity
import com.aikukisna.app.data.local.entity.TraduccionAcepcionEntity
import com.aikukisna.app.data.local.entity.VariantePalabraEntity
import com.aikukisna.app.data.local.entity.ExpresionEntity
import com.aikukisna.app.data.local.entity.TraduccionExpresionEntity
import com.aikukisna.app.data.local.entity.PalabraFuenteEntity
import com.aikukisna.app.data.local.entity.SincronizacionLinguisticaEntity

@Dao
interface IdiomaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(idiomas: List<IdiomaEntity>)

    @Query("SELECT * FROM idioma_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): IdiomaEntity?

    @Query("SELECT COUNT(*) FROM idioma_cache")
    suspend fun contarTodos(): Int
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


    @Query("""
        SELECT * FROM palabra_cache
        WHERE idiomaId = :idiomaId
          AND estado_validacion != 'rechazada'
          AND texto_normalizado LIKE '%' || :query || '%'
        ORDER BY
          CASE WHEN texto_normalizado = :query THEN 0 ELSE 1 END,
          LENGTH(texto_normalizado) ASC,
          texto_normalizado ASC,
          id ASC
        LIMIT :limite OFFSET :offset
    """)
    suspend fun buscar(query: String, idiomaId: Int, limite: Int, offset: Int): List<PalabraEntity>

    @Query("SELECT * FROM palabra_cache WHERE id = :id AND estado_validacion != 'rechazada'")
    suspend fun obtenerPorId(id: Int): PalabraEntity?

    @Query("SELECT * FROM palabra_cache WHERE id IN (:ids) AND estado_validacion != 'rechazada'")
    suspend fun obtenerPorIds(ids: List<Int>): List<PalabraEntity>

    @Query("SELECT * FROM palabra_cache WHERE idiomaId = :idiomaId AND texto_normalizado = :textoNormalizado AND estado_validacion != 'rechazada' ORDER BY id")
    suspend fun buscarExactas(textoNormalizado: String, idiomaId: Int): List<PalabraEntity>

    @Query("SELECT * FROM palabra_cache WHERE idiomaId = :idiomaId AND texto_normalizado LIKE :prefijo || '%' AND estado_validacion != 'rechazada' ORDER BY texto_normalizado LIMIT :limite")
    suspend fun candidatas(prefijo: String, idiomaId: Int, limite: Int): List<PalabraEntity>

    @Query("SELECT * FROM palabra_cache WHERE idiomaId = :idiomaId AND texto_normalizado LIKE :prefijo || '%' AND LENGTH(texto_normalizado) = :longitud AND estado_validacion != 'rechazada' ORDER BY id LIMIT 500")
    suspend fun candidatasPorLongitud(prefijo: String, idiomaId: Int, longitud: Int): List<PalabraEntity>

    @Query("SELECT COUNT(*) FROM palabra_cache WHERE estado_validacion != 'rechazada'")
    suspend fun contarTodas(): Int

    @Query("SELECT COUNT(*) FROM palabra_cache WHERE idiomaId = :idiomaId AND estado_validacion != 'rechazada'")
    suspend fun contarPorIdioma(idiomaId: Int): Int
}

@Dao
interface TraduccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(traducciones: List<TraduccionEntity>)

    @Query("""
        SELECT * FROM traduccion_cache
        WHERE (palabraOrigenId = :palabraId OR palabraDestinoId = :palabraId)
          AND estadoValidacion != 'rechazada'
        ORDER BY esPreferida DESC, nivelConfianza DESC, id ASC
    """)
    suspend fun obtenerPorPalabra(palabraId: Int): List<TraduccionEntity>

    @Query("SELECT COUNT(*) FROM traduccion_cache WHERE estadoValidacion != 'rechazada'")
    suspend fun contarTodas(): Int
}

@Dao
interface ConocimientoLinguisticoDao {
    @Query("SELECT * FROM variante_palabra_cache WHERE textoNormalizado = :textoNormalizado ORDER BY id")
    suspend fun buscarVariantesExactas(textoNormalizado: String): List<VariantePalabraEntity>

    @Query("SELECT * FROM acepcion_cache WHERE palabraId = :palabraId AND estadoValidacion != 'rechazada' ORDER BY numeroAcepcion")
    suspend fun obtenerAcepciones(palabraId: Int): List<AcepcionEntity>

    @Query("SELECT * FROM traduccion_acepcion_cache WHERE acepcionOrigenId = :acepcionId AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerTraduccionesAcepcion(acepcionId: Long): List<TraduccionAcepcionEntity>

    @Query("SELECT * FROM traduccion_acepcion_cache WHERE (acepcionOrigenId = :acepcionId OR acepcionDestinoId = :acepcionId) AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerRelacionesAcepcion(acepcionId: Long): List<TraduccionAcepcionEntity>

    @Query("SELECT * FROM acepcion_cache WHERE id = :id")
    suspend fun obtenerAcepcion(id: Long): AcepcionEntity?

    @Query("SELECT * FROM expresion_cache WHERE idiomaId = :idiomaId AND textoNormalizado = :textoNormalizado AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun buscarExpresionesExactas(textoNormalizado: String, idiomaId: Int): List<ExpresionEntity>

    @Query("SELECT te.* FROM traduccion_expresion_cache te JOIN expresion_cache e ON e.id = te.expresionDestinoId WHERE te.expresionOrigenId = :expresionId AND e.idiomaId = :idiomaDestinoId AND te.estadoValidacion != 'rechazada' ORDER BY te.id")
    suspend fun obtenerTraduccionesExpresion(expresionId: Long, idiomaDestinoId: Int): List<TraduccionExpresionEntity>

    @Query("SELECT * FROM traduccion_expresion_cache WHERE (expresionOrigenId = :expresionId OR expresionDestinoId = :expresionId) AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerRelacionesExpresion(expresionId: Long): List<TraduccionExpresionEntity>

    @Query("SELECT * FROM expresion_cache WHERE id = :id")
    suspend fun obtenerExpresion(id: Long): ExpresionEntity?

    @Query("""
        SELECT * FROM expresion_cache
        WHERE idiomaId = :idiomaId AND tipo IN ('oracion', 'frase_ejemplo', 'locucion')
          AND estadoValidacion != 'rechazada'
          AND (' ' || textoNormalizado || ' ') LIKE '% ' || :palabraNormalizada || ' %'
        ORDER BY LENGTH(texto) ASC
        LIMIT :limite
    """)
    suspend fun buscarFrasesConPalabra(palabraNormalizada: String, idiomaId: Int, limite: Int): List<ExpresionEntity>

    @Query("""
        SELECT * FROM expresion_cache
        WHERE idiomaId = :idiomaId AND tipo IN ('oracion', 'frase_ejemplo', 'locucion')
          AND estadoValidacion != 'rechazada' AND LENGTH(texto) BETWEEN 6 AND 80
        ORDER BY RANDOM()
        LIMIT :limite
    """)
    suspend fun frasesAlAzar(idiomaId: Int, limite: Int): List<ExpresionEntity>

    @Query("SELECT * FROM palabra_fuente_cache WHERE palabraId = :palabraId")
    suspend fun obtenerFuentes(palabraId: Int): List<PalabraFuenteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAcepciones(items: List<AcepcionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTraduccionesAcepcion(items: List<TraduccionAcepcionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarVariantes(items: List<VariantePalabraEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarExpresiones(items: List<ExpresionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTraduccionesExpresion(items: List<TraduccionExpresionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarFuentes(items: List<PalabraFuenteEntity>)

    @Query("SELECT * FROM sincronizacion_linguistica WHERE recurso = :recurso")
    suspend fun obtenerCursor(recurso: String): SincronizacionLinguisticaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarCursor(cursor: SincronizacionLinguisticaEntity)
}

@Dao
interface LeccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(lecciones: List<LeccionEntity>)

    @Query("SELECT * FROM leccion_cache WHERE (:nivel IS NULL OR nivel = :nivel) ORDER BY id ASC")
    suspend fun obtenerTodas(nivel: Int?): List<LeccionEntity>

    @Query("SELECT * FROM leccion_cache WHERE id = :id")
    suspend fun obtenerPorId(id: Int): LeccionEntity?

    @Query("SELECT COUNT(*) FROM leccion_cache")
    suspend fun contarTodas(): Int
}

@Dao
interface OracionEjemploDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(oraciones: List<OracionEjemploEntity>)

    @Query("SELECT * FROM oracion_ejemplo_cache WHERE leccionId = :leccionId AND estadoValidacion != 'rechazada'")
    suspend fun obtenerPorLeccion(leccionId: Int): List<OracionEjemploEntity>

    @Query("""
        SELECT * FROM oracion_ejemplo_cache
        WHERE ((idiomaOrigenId = :idiomaOrigenId AND idiomaDestinoId = :idiomaDestinoId)
           OR (idiomaOrigenId = :idiomaDestinoId AND idiomaDestinoId = :idiomaOrigenId))
          AND estadoValidacion != 'rechazada' 
        ORDER BY id ASC
    """)
    suspend fun obtenerPorIdiomas(
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): List<OracionEjemploEntity>

    @Query("SELECT COUNT(*) FROM oracion_ejemplo_cache WHERE estadoValidacion != 'rechazada'")
    suspend fun contarTodas(): Int
}

@Dao
interface AudioPronunciacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(audio: AudioPronunciacionEntity)

    @Query("""
        SELECT * FROM audio_pronunciacion_cache
        WHERE palabraId = :palabraId
          AND idiomaCodigo = :idiomaCodigo
          AND verificado = 1
        ORDER BY CASE origen
            WHEN 'HUMANO' THEN 0
            WHEN 'SINTETICO' THEN 1
            ELSE 2
        END
    """)
    suspend fun buscarVerificados(
        palabraId: Int,
        idiomaCodigo: String
    ): List<AudioPronunciacionEntity>

    @Query("""
        SELECT * FROM audio_pronunciacion_cache
        WHERE textoNormalizado = :textoNormalizado
          AND idiomaCodigo = :idiomaCodigo
          AND verificado = 1
          AND origen = 'HUMANO'
    """)
    suspend fun buscarHumanosPorTexto(
        textoNormalizado: String,
        idiomaCodigo: String
    ): List<AudioPronunciacionEntity>

    @Query("SELECT COUNT(*) FROM audio_pronunciacion_cache WHERE origen = 'HUMANO'")
    suspend fun contarHumanos(): Int

    @Query("SELECT id FROM palabra_cache WHERE id IN (:ids)")
    suspend fun palabrasExistentes(ids: List<Int>): List<Int>

    @Query("DELETE FROM audio_pronunciacion_cache WHERE fuenteId = :fuenteId")
    suspend fun eliminarPorFuente(fuenteId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(audios: List<AudioPronunciacionEntity>)

    @Transaction
    suspend fun reemplazarFuente(fuenteId: Int, audios: List<AudioPronunciacionEntity>) {
        eliminarPorFuente(fuenteId)
        guardarTodos(audios)
    }
}

@Dao
interface LeccionPalabraDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(vinculos: List<LeccionPalabraEntity>)

    @Query("SELECT palabraId FROM leccion_palabra_cache WHERE leccionId = :leccionId")
    suspend fun obtenerPalabraIdsPorLeccion(leccionId: Int): List<Int>

    @Query("SELECT COUNT(*) FROM leccion_palabra_cache")
    suspend fun contarTodos(): Int
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

@Dao
interface MemoriaTukiLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(memoria: MemoriaTukiLocalEntity)

    @Query("SELECT * FROM memoria_tuki_local WHERE usuarioId = :usuarioId ORDER BY fechaEpochMs DESC LIMIT :limite")
    suspend fun obtenerRecientes(usuarioId: String, limite: Int): List<MemoriaTukiLocalEntity>

    @Query("SELECT * FROM memoria_tuki_local WHERE usuarioId = :usuarioId AND tipo = :tipo ORDER BY fechaEpochMs DESC LIMIT :limite")
    suspend fun obtenerRecientesPorTipo(usuarioId: String, tipo: String, limite: Int): List<MemoriaTukiLocalEntity>

    @Query("SELECT * FROM memoria_tuki_local WHERE usuarioId = :usuarioId ORDER BY fechaEpochMs DESC LIMIT 1")
    suspend fun obtenerUltima(usuarioId: String): MemoriaTukiLocalEntity?

    @Query("SELECT * FROM memoria_tuki_local WHERE sincronizada = 0 ORDER BY fechaEpochMs ASC LIMIT :limite")
    suspend fun obtenerPendientes(limite: Int): List<MemoriaTukiLocalEntity>

    @Query("UPDATE memoria_tuki_local SET usos = usos + 1 WHERE id IN (:ids)")
    suspend fun registrarUso(ids: List<String>)

    @Query("UPDATE memoria_tuki_local SET sincronizada = 1 WHERE id = :id")
    suspend fun marcarSincronizada(id: String)

    @Query("DELETE FROM memoria_tuki_local WHERE usuarioId = :usuarioId AND id NOT IN (SELECT id FROM memoria_tuki_local WHERE usuarioId = :usuarioId ORDER BY fechaEpochMs DESC LIMIT :limite)")
    suspend fun conservarRecientes(usuarioId: String, limite: Int)
}
