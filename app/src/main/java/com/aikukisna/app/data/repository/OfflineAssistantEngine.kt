package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.assistant.AssistantRequest
import com.aikukisna.app.domain.assistant.AssistantResult
import com.aikukisna.app.domain.assistant.LocalAssistantEngine
import com.aikukisna.app.domain.usecase.TukiOfflineResponder
import javax.inject.Inject

class OfflineAssistantEngine @Inject constructor(
    private val responder: TukiOfflineResponder
) : LocalAssistantEngine {
    override suspend fun answer(request: AssistantRequest): AssistantResult {
        val result = responder.responder(request.message, request.languageId, request.history)
        return AssistantResult(result.texto, result.concluyente, result.conversacional)
    }
}
