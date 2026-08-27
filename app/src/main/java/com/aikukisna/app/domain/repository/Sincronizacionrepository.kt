package com.aikukisna.app.domain.repository

import kotlinx.coroutines.flow.Flow

sealed interface EstadoSincronizacion {
    data class EnProgreso(val etapa: String, val progreso: Float) : EstadoSincronizacion
    data object Completado : EstadoSincronizacion
    data class Error(val mensaje: String) : EstadoSincronizacion
}

interface SincronizacionRepository {

    suspend fun hayDatosDescargados(): Boolean

    fun sincronizarTodo(): Flow<EstadoSincronizacion>
}