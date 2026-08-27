package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject

class PreguntarIaUseCase @Inject constructor(
    private val iaRepository: IaRepository
) {
    suspend operator fun invoke(prompt: String): String {
        require(prompt.isNotBlank()) { "El mensaje no puede estar vacío" }
        return iaRepository.preguntar(prompt)
    }
}