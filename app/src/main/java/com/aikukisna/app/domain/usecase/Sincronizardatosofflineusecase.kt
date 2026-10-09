package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.repository.SincronizacionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SincronizarDatosOfflineUseCase @Inject constructor(
    private val sincronizacionRepository: SincronizacionRepository
) {
    suspend fun yaHayDatos(idiomaId: Int? = null): Boolean =
        sincronizacionRepository.hayDatosDescargados(idiomaId)

    fun invoke(idiomaId: Int? = null): Flow<EstadoSincronizacion> =
        sincronizacionRepository.sincronizarTodo(idiomaId)
}
