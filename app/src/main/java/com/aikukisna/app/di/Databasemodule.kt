package com.aikukisna.app.di

import android.content.Context
import androidx.room.Room
import com.aikukisna.app.data.local.AikukisnaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAikukisnaDatabase(@ApplicationContext context: Context): AikukisnaDatabase {
        return Room.databaseBuilder(
            context,
            AikukisnaDatabase::class.java,
            "aikukisna.db"
        ).build()
    }

    @Provides
    fun provideIdiomaDao(db: AikukisnaDatabase) = db.idiomaDao()

    @Provides
    fun provideCategoriaDao(db: AikukisnaDatabase) = db.categoriaDao()

    @Provides
    fun provideFuenteDocumentoDao(db: AikukisnaDatabase) = db.fuenteDocumentoDao()

    @Provides
    fun providePalabraDao(db: AikukisnaDatabase) = db.palabraDao()

    @Provides
    fun provideTraduccionDao(db: AikukisnaDatabase) = db.traduccionDao()

    @Provides
    fun provideLeccionDao(db: AikukisnaDatabase) = db.leccionDao()

    @Provides
    fun provideOracionEjemploDao(db: AikukisnaDatabase) = db.oracionEjemploDao()

    @Provides
    fun provideLeccionPalabraDao(db: AikukisnaDatabase) = db.leccionPalabraDao()

    @Provides
    fun provideCompletarLeccionPendienteDao(db: AikukisnaDatabase) = db.completarLeccionPendienteDao()
}