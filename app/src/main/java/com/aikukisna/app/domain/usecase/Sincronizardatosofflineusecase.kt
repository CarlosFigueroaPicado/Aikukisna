package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.repository.SincronizacionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SincronizarDatosOfflineUseCase @Inject constructor(
    private val sincronizacionRepository: SincronizacionRepository
) {
    suspend fun yaHayDatos(): Boolean = sincronizacionRepository.hayDatosDescargados()

    fun invoke(): Flow<EstadoSincronizacion> = sincronizacionRepository.sincronizarTodo()
}