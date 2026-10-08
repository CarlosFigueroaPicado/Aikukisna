package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aikukisna.app.data.local.entity.ActividadLeccionEntity
import com.aikukisna.app.data.local.entity.ActividadRecursoEntity
import com.aikukisna.app.data.local.entity.EtapaRutaCurricularEntity
import com.aikukisna.app.data.local.entity.EvidenciaCurricularLeccionEntity
import com.aikukisna.app.data.local.entity.LeccionExperienciaGamificadaEntity
import com.aikukisna.app.data.local.entity.LeccionRutaCurricularEntity
import com.aikukisna.app.data.local.entity.MundoGamificadoEntity

@Dao
interface ContenidoLeccionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarMundos(items: List<MundoGamificadoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarExperiencias(items: List<LeccionExperienciaGamificadaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarActividades(items: List<ActividadLeccionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRecursos(items: List<ActividadRecursoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEtapas(items: List<EtapaRutaCurricularEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRutas(items: List<LeccionRutaCurricularEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarEvidencias(items: List<EvidenciaCurricularLeccionEntity>)

    @Query("DELETE FROM actividad_recurso_cache")
    suspend fun borrarRecursos()

    @Query("DELETE FROM actividad_leccion_cache")
    suspend fun borrarActividades()

    @Query("DELETE FROM evidencia_curricular_leccion_cache")
    suspend fun borrarEvidencias()

    @Query("DELETE FROM leccion_ruta_curricular_cache")
    suspend fun borrarRutas()

    @Query("DELETE FROM etapa_ruta_curricular_cache")
    suspend fun borrarEtapas()

    @Query("DELETE FROM leccion_experiencia_gamificada_cache")
    suspend fun borrarExperiencias()

    @Query("DELETE FROM mundo_gamificado_cache")
    suspend fun borrarMundos()

    @Query("SELECT * FROM mundo_gamificado_cache ORDER BY orden")
    suspend fun obtenerMundos(): List<MundoGamificadoEntity>

    @Query("SELECT * FROM leccion_experiencia_gamificada_cache WHERE leccionId = :leccionId")
    suspend fun obtenerExperiencia(leccionId: Int): LeccionExperienciaGamificadaEntity?

    @Query("SELECT * FROM actividad_leccion_cache WHERE leccionId = :leccionId AND activa = 1 ORDER BY orden")
    suspend fun obtenerActividades(leccionId: Int): List<ActividadLeccionEntity>

    @Query("SELECT * FROM actividad_recurso_cache WHERE actividadId IN (:actividadIds) ORDER BY actividadId, orden, tipoRecurso, recursoId")
    suspend fun obtenerRecursos(actividadIds: List<Long>): List<ActividadRecursoEntity>

    @Query("SELECT * FROM leccion_ruta_curricular_cache WHERE leccionId = :leccionId")
    suspend fun obtenerRutaCurricular(leccionId: Int): LeccionRutaCurricularEntity?

    @Query("SELECT * FROM evidencia_curricular_leccion_cache WHERE leccionId = :leccionId AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerEvidenciasCurriculares(leccionId: Int): List<EvidenciaCurricularLeccionEntity>

    @Query("SELECT COUNT(*) FROM actividad_leccion_cache WHERE activa = 1")
    suspend fun contarActividadesActivas(): Int

    @Query("SELECT COUNT(*) FROM leccion_experiencia_gamificada_cache")
    suspend fun contarExperiencias(): Int
}
