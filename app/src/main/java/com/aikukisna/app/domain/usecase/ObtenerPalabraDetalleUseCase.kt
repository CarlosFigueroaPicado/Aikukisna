package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository

data class DetallePalabra(
    val palabra: Palabra,
    val traducciones: List<Traduccion>
)

class ObtenerPalabraDetalleUseCase(
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(palabraId: Int): DetallePalabra? {
        val palabra = diccionarioRepository.obtenerPalabraPorId(palabraId) ?: return null
        val traducciones = diccionarioRepository.obtenerTraducciones(palabraId)
        return DetallePalabra(palabra, traducciones)
    }
}