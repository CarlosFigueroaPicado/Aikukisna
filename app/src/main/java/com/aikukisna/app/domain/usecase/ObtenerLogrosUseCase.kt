package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.repository.LogroRepository

class ObtenerLogrosUseCase(
    private val logroRepository: LogroRepository
) {
    suspend operator fun invoke(): List<Logro> {
        return logroRepository.obtenerLogros()
    }
}