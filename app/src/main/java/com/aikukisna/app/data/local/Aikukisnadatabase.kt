package com.aikukisna.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aikukisna.app.data.local.dao.CategoriaDao
import com.aikukisna.app.data.local.dao.AudioPronunciacionDao
import com.aikukisna.app.data.local.dao.CompletarLeccionPendienteDao
import com.aikukisna.app.data.local.dao.FuenteDocumentoDao
import com.aikukisna.app.data.local.dao.IdiomaDao
import com.aikukisna.app.data.local.dao.LeccionDao
import com.aikukisna.app.data.local.dao.LeccionPalabraDao
import com.aikukisna.app.data.local.dao.OracionEjemploDao
import com.aikukisna.app.data.local.dao.PalabraDao
import com.aikukisna.app.data.local.dao.TraduccionDao
import com.aikukisna.app.data.local.dao.MemoriaTukiLocalDao
import com.aikukisna.app.data.local.dao.ConocimientoLinguisticoDao
import com.aikukisna.app.data.local.dao.ContenidoLeccionDao
import com.aikukisna.app.data.local.dao.ContenidoGlobalDao
import com.aikukisna.app.data.local.dao.EvidenciaGramaticalDao
import com.aikukisna.app.data.local.dao.SeleccionTraduccionCamaraDao
import com.aikukisna.app.data.local.entity.CulturaContenidoEntity
import com.aikukisna.app.data.local.entity.ReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.EjemploReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.ReglaPronunciacionEntity
import com.aikukisna.app.data.local.entity.LeccionExpresionEntity
import com.aikukisna.app.data.local.entity.LeccionOracionEntity
import com.aikukisna.app.data.local.entity.LeccionFuenteEntity
import com.aikukisna.app.data.local.entity.LeccionCulturaEntity
import com.aikukisna.app.data.local.entity.LeccionReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.LeccionReglaPronunciacionEntity
import com.aikukisna.app.data.local.entity.ExpresionContextoCulturalEntity
import com.aikukisna.app.data.local.entity.TraduccionFuenteEntity
import com.aikukisna.app.data.local.entity.PalabraCanonicaEntity
import com.aikukisna.app.data.local.entity.AlineacionCurricularEntity
import com.aikukisna.app.data.local.entity.TukiRespuestaSistemaEntity
import com.aikukisna.app.data.local.entity.LogroEntity
import com.aikukisna.app.data.local.entity.RevisionLinguisticaEntity
import com.aikukisna.app.data.local.entity.UsuarioActualEntity
import com.aikukisna.app.data.local.entity.ProgresoLeccionUsuarioEntity
import com.aikukisna.app.data.local.entity.PalabraFavoritaUsuarioEntity
import com.aikukisna.app.data.local.entity.LogroDesbloqueadoUsuarioEntity
import com.aikukisna.app.data.local.entity.ReplicaSupabaseEntity
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
import com.aikukisna.app.data.local.entity.MundoGamificadoEntity
import com.aikukisna.app.data.local.entity.LeccionExperienciaGamificadaEntity
import com.aikukisna.app.data.local.entity.ActividadLeccionEntity
import com.aikukisna.app.data.local.entity.ActividadRecursoEntity
import com.aikukisna.app.data.local.entity.EtapaRutaCurricularEntity
import com.aikukisna.app.data.local.entity.LeccionRutaCurricularEntity
import com.aikukisna.app.data.local.entity.EvidenciaCurricularLeccionEntity
import com.aikukisna.app.data.local.entity.CategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.AcepcionCategoriaLinguisticaEntity
import com.aikukisna.app.data.local.entity.RaizVerbalEntity
import com.aikukisna.app.data.local.entity.FormaVerbalDocumentadaEntity
import com.aikukisna.app.data.local.entity.ExcepcionReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.MarcaGramaticalDocumentadaEntity
import com.aikukisna.app.data.local.entity.ComponenteReglaGramaticalEntity
import com.aikukisna.app.data.local.entity.SeleccionTraduccionCamaraEntity

@Database(
    entities = [
        IdiomaEntity::class,
        CategoriaEntity::class,
        FuenteDocumentoEntity::class,
        PalabraEntity::class,
        AudioPronunciacionEntity::class,
        TraduccionEntity::class,
        LeccionEntity::class,
        OracionEjemploEntity::class,
        LeccionPalabraEntity::class,
        CompletarLeccionPendienteEntity::class,
        MemoriaTukiLocalEntity::class,
        PalabraFuenteEntity::class,
        AcepcionEntity::class,
        TraduccionAcepcionEntity::class,
        VariantePalabraEntity::class,
        ExpresionEntity::class,
        TraduccionExpresionEntity::class,
        SincronizacionLinguisticaEntity::class,
        MundoGamificadoEntity::class,
        LeccionExperienciaGamificadaEntity::class,
        ActividadLeccionEntity::class,
        ActividadRecursoEntity::class,
        EtapaRutaCurricularEntity::class,
        LeccionRutaCurricularEntity::class,
        EvidenciaCurricularLeccionEntity::class,
        CulturaContenidoEntity::class,
        ReglaGramaticalEntity::class,
        EjemploReglaGramaticalEntity::class,
        ReglaPronunciacionEntity::class,
        LeccionExpresionEntity::class,
        LeccionOracionEntity::class,
        LeccionFuenteEntity::class,
        LeccionCulturaEntity::class,
        LeccionReglaGramaticalEntity::class,
        LeccionReglaPronunciacionEntity::class,
        ExpresionContextoCulturalEntity::class,
        TraduccionFuenteEntity::class,
        PalabraCanonicaEntity::class,
        AlineacionCurricularEntity::class,
        TukiRespuestaSistemaEntity::class,
        LogroEntity::class,
        RevisionLinguisticaEntity::class,
        UsuarioActualEntity::class,
        ProgresoLeccionUsuarioEntity::class,
        PalabraFavoritaUsuarioEntity::class,
        LogroDesbloqueadoUsuarioEntity::class,
        ReplicaSupabaseEntity::class,
        CategoriaLinguisticaEntity::class,
        AcepcionCategoriaLinguisticaEntity::class,
        RaizVerbalEntity::class,
        FormaVerbalDocumentadaEntity::class,
        ExcepcionReglaGramaticalEntity::class,
        MarcaGramaticalDocumentadaEntity::class,
        ComponenteReglaGramaticalEntity::class,
        SeleccionTraduccionCamaraEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class AikukisnaDatabase : RoomDatabase() {
    abstract fun idiomaDao(): IdiomaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun fuenteDocumentoDao(): FuenteDocumentoDao
    abstract fun palabraDao(): PalabraDao
    abstract fun audioPronunciacionDao(): AudioPronunciacionDao
    abstract fun traduccionDao(): TraduccionDao
    abstract fun leccionDao(): LeccionDao
    abstract fun oracionEjemploDao(): OracionEjemploDao
    abstract fun leccionPalabraDao(): LeccionPalabraDao
    abstract fun completarLeccionPendienteDao(): CompletarLeccionPendienteDao
    abstract fun memoriaTukiLocalDao(): MemoriaTukiLocalDao
    abstract fun conocimientoLinguisticoDao(): ConocimientoLinguisticoDao
    abstract fun contenidoLeccionDao(): ContenidoLeccionDao
    abstract fun contenidoGlobalDao(): ContenidoGlobalDao
    abstract fun evidenciaGramaticalDao(): EvidenciaGramaticalDao
    abstract fun seleccionTraduccionCamaraDao(): SeleccionTraduccionCamaraDao
}
