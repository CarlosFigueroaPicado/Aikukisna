package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.ResultadoPronunciacion
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.domain.repository.PronunciationEngine
import javax.inject.Inject

class ObtenerAudioPronunciacionUseCase @Inject constructor(
    private val pronunciationEngine: PronunciationEngine
) {
    suspend operator fun invoke(solicitud: SolicitudPronunciacion): ResultadoPronunciacion =
        pronunciationEngine.resolver(solicitud)
}
