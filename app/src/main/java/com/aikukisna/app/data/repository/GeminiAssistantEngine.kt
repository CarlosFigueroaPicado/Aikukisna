package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.assistant.RemoteAssistantEngine
import com.aikukisna.app.domain.assistant.AssistantRequest
import com.aikukisna.app.domain.assistant.AssistantResult
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.repository.IaRepository
import javax.inject.Inject

class GeminiAssistantEngine @Inject constructor(
    private val repository: IaRepository
) : RemoteAssistantEngine {
    override suspend fun answer(request: AssistantRequest): AssistantResult = AssistantResult(
        repository.conversar(request.history.takeLast(8), request.verifiedContext),
        conclusive = true
    )
}
