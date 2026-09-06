package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.DiccionarioRepository
import javax.inject.Inject


private const val IDIOMA_ESPANOL = 2


data class PalabraDemo(
    val palabra: Palabra,
    val traduccionEspanol: String?
)

class ObtenerVocabularioDemoUseCase @Inject constructor(
    private val obtenerPalabrasDemoUseCase: ObtenerPalabrasDemoUseCase,
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(idiomaId: Int): List<PalabraDemo> {
        val palabras = obtenerPalabrasDemoUseCase(idiomaId)
        return palabras.map { palabra ->
            val traduccion = diccionarioRepository.obtenerTraducciones(palabra.id)
                .firstOrNull { it.palabraDestino.idioma.id == IDIOMA_ESPANOL }
                ?.palabraDestino
                ?.texto
            PalabraDemo(palabra = palabra, traduccionEspanol = traduccion)
        }
    }
}