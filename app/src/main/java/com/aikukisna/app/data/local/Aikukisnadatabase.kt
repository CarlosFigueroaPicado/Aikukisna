package com.aikukisna.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aikukisna.app.data.local.dao.CategoriaDao
import com.aikukisna.app.data.local.dao.CompletarLeccionPendienteDao
import com.aikukisna.app.data.local.dao.FuenteDocumentoDao
import com.aikukisna.app.data.local.dao.IdiomaDao
import com.aikukisna.app.data.local.dao.LeccionDao
import com.aikukisna.app.data.local.dao.LeccionPalabraDao
import com.aikukisna.app.data.local.dao.OracionEjemploDao
import com.aikukisna.app.data.local.dao.PalabraDao
import com.aikukisna.app.data.local.dao.TraduccionDao
import com.aikukisna.app.data.local.entity.CategoriaEntity
import com.aikukisna.app.data.local.entity.CompletarLeccionPendienteEntity
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.LeccionEntity
import com.aikukisna.app.data.local.entity.LeccionPalabraEntity
import com.aikukisna.app.data.local.entity.OracionEjemploEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.TraduccionEntity

@Database(
    entities = [
        IdiomaEntity::class,
        CategoriaEntity::class,
        FuenteDocumentoEntity::class,
        PalabraEntity::class,
        TraduccionEntity::class,
        LeccionEntity::class,
        OracionEjemploEntity::class,
        LeccionPalabraEntity::class,
        CompletarLeccionPendienteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AikukisnaDatabase : RoomDatabase() {
    abstract fun idiomaDao(): IdiomaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun fuenteDocumentoDao(): FuenteDocumentoDao
    abstract fun palabraDao(): PalabraDao
    abstract fun traduccionDao(): TraduccionDao
    abstract fun leccionDao(): LeccionDao
    abstract fun oracionEjemploDao(): OracionEjemploDao
    abstract fun leccionPalabraDao(): LeccionPalabraDao
    abstract fun completarLeccionPendienteDao(): CompletarLeccionPendienteDao
}