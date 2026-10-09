package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import javax.inject.Inject


data class PalabraDemo(
    val palabra: Palabra,
    val traduccionEspanol: String?
)

class ObtenerVocabularioDemoUseCase @Inject constructor(
    private val obtenerPalabrasDemoUseCase: ObtenerPalabrasDemoUseCase,
    private val traduccionParaLeccion: TraduccionParaLeccionUseCase
) {
    suspend operator fun invoke(idiomaId: Int): List<PalabraDemo> {
        val palabras = obtenerPalabrasDemoUseCase(idiomaId)
        return palabras.map { palabra ->
            PalabraDemo(palabra = palabra, traduccionEspanol = traduccionParaLeccion(palabra))
        }
    }
}
