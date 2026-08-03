package com.aikukisna.app.di

import com.aikukisna.app.data.repository.AuthRepositoryImpl
import com.aikukisna.app.data.repository.DiccionarioRepositoryImpl
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.DiccionarioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.aikukisna.app.data.repository.LeccionRepositoryImpl
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.data.repository.CulturaRepositoryImpl
import com.aikukisna.app.domain.repository.CulturaRepository
import com.aikukisna.app.data.repository.LogroRepositoryImpl
import com.aikukisna.app.domain.repository.LogroRepository
import com.aikukisna.app.data.repository.UsuarioRepositoryImpl
import com.aikukisna.app.domain.repository.UsuarioRepository




@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindDiccionarioRepository(impl: DiccionarioRepositoryImpl): DiccionarioRepository

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindLeccionRepository(impl: LeccionRepositoryImpl): LeccionRepository

    @Binds
    fun bindCulturaRepository(impl: CulturaRepositoryImpl): CulturaRepository

    @Binds
    fun bindLogroRepository(impl: LogroRepositoryImpl): LogroRepository

    @Binds
    fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository

}