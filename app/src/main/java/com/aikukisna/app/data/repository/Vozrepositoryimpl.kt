package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.repository.VozRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VozRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : VozRepository {

    override suspend fun sintetizarVoz(texto: String, voiceId: String?): ByteArray {

        val response = client.functions.invoke("sintetizar-voz") {
            contentType(ContentType.Application.Json)
            setBody(
                buildJsonObject {
                    put("texto", texto)
                    if (voiceId != null) put("voiceId", voiceId)
                }
            )
        }

        return response.body<ByteArray>()
    }
}