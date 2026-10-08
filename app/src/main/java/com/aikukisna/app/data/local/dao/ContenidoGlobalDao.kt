package com.aikukisna.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aikukisna.app.data.local.entity.*

@Dao
interface ContenidoGlobalDao {
    @Upsert suspend fun guardarCulturas(v: List<CulturaContenidoEntity>)
    @Upsert suspend fun guardarReglasGramaticales(v: List<ReglaGramaticalEntity>)
    @Upsert suspend fun guardarEjemplosRegla(v: List<EjemploReglaGramaticalEntity>)
    @Upsert suspend fun guardarReglasPronunciacion(v: List<ReglaPronunciacionEntity>)
    @Upsert suspend fun guardarLeccionExpresion(v: List<LeccionExpresionEntity>)
    @Upsert suspend fun guardarLeccionOracion(v: List<LeccionOracionEntity>)
    @Upsert suspend fun guardarLeccionFuente(v: List<LeccionFuenteEntity>)
    @Upsert suspend fun guardarLeccionCultura(v: List<LeccionCulturaEntity>)
    @Upsert suspend fun guardarLeccionReglaGramatical(v: List<LeccionReglaGramaticalEntity>)
    @Upsert suspend fun guardarLeccionReglaPronunciacion(v: List<LeccionReglaPronunciacionEntity>)
    @Upsert suspend fun guardarExpresionContexto(v: List<ExpresionContextoCulturalEntity>)
    @Upsert suspend fun guardarTraduccionFuente(v: List<TraduccionFuenteEntity>)
    @Upsert suspend fun guardarPalabraCanonica(v: List<PalabraCanonicaEntity>)
    @Upsert suspend fun guardarAlineaciones(v: List<AlineacionCurricularEntity>)
    @Upsert suspend fun guardarRespuestasTuki(v: List<TukiRespuestaSistemaEntity>)
    @Upsert suspend fun guardarLogros(v: List<LogroEntity>)
    @Upsert suspend fun guardarRevisiones(v: List<RevisionLinguisticaEntity>)
    @Upsert suspend fun guardarUsuarioActual(v: UsuarioActualEntity)
    @Upsert suspend fun guardarProgreso(v: List<ProgresoLeccionUsuarioEntity>)
    @Upsert suspend fun guardarFavoritos(v: List<PalabraFavoritaUsuarioEntity>)
    @Upsert suspend fun guardarLogrosUsuario(v: List<LogroDesbloqueadoUsuarioEntity>)
    @Upsert suspend fun guardarReplica(v: List<ReplicaSupabaseEntity>)

    @Query("DELETE FROM usuario_actual_cache") suspend fun borrarUsuarioActual()
    @Query("DELETE FROM progreso_leccion_usuario_cache") suspend fun borrarProgresoUsuario()
    @Query("DELETE FROM palabra_favorita_usuario_cache") suspend fun borrarFavoritosUsuario()
    @Query("DELETE FROM logro_desbloqueado_usuario_cache") suspend fun borrarLogrosUsuario()

    @Query("SELECT COUNT(*) FROM cultura_contenido_cache") suspend fun contarCulturas(): Int
    @Query("SELECT * FROM cultura_contenido_cache ORDER BY id") suspend fun obtenerCulturas(): List<CulturaContenidoEntity>
    @Query("SELECT COUNT(*) FROM regla_gramatical_cache") suspend fun contarReglasGramaticales(): Int
    @Query("SELECT COUNT(*) FROM regla_pronunciacion_cache") suspend fun contarReglasPronunciacion(): Int
    @Query("SELECT * FROM regla_pronunciacion_cache WHERE idiomaId = :idiomaId AND activa = 1 AND estadoValidacion != 'rechazada' ORDER BY prioridad DESC")
    suspend fun obtenerReglasPronunciacion(idiomaId: Int): List<ReglaPronunciacionEntity>
    @Query("SELECT * FROM regla_gramatical_cache WHERE idiomaId = :idiomaId AND estadoValidacion IN ('documentada', 'validada') ORDER BY prioridad DESC")
    suspend fun obtenerReglasGramaticalesDocumentadas(idiomaId: Int): List<ReglaGramaticalEntity>
    @Query("SELECT * FROM ejemplo_regla_gramatical_cache WHERE reglaId = :reglaId AND estadoValidacion != 'rechazada' ORDER BY id")
    suspend fun obtenerEjemplosRegla(reglaId: Long): List<EjemploReglaGramaticalEntity>
    @Query("SELECT COUNT(*) FROM alineacion_curricular_cache") suspend fun contarAlineaciones(): Int
    @Query("SELECT COUNT(*) FROM replica_supabase_cache WHERE tabla = :tabla") suspend fun contarReplica(tabla: String): Int
    @Query("SELECT * FROM logro_cache ORDER BY id") suspend fun obtenerLogros(): List<LogroEntity>
    @Query("SELECT * FROM logro_desbloqueado_usuario_cache WHERE usuarioId = :usuarioId ORDER BY fechaEpochMs")
    suspend fun obtenerLogrosUsuario(usuarioId: String): List<LogroDesbloqueadoUsuarioEntity>
    @Query("DELETE FROM replica_supabase_cache WHERE tabla = :tabla") suspend fun borrarReplicaTabla(tabla: String)
}
