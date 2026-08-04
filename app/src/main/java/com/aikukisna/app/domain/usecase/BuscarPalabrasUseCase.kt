package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.DiccionarioRepository
import javax.inject.Inject

class BuscarPalabrasUseCase @Inject constructor(
    private val diccionarioRepository: DiccionarioRepository
) {
    suspend operator fun invoke(query: String, idiomaId: Int, limite: Int = 50, offset: Int = 0): List<Palabra> {
        return diccionarioRepository.buscarPalabras(query, idiomaId, limite, offset)
    }
}