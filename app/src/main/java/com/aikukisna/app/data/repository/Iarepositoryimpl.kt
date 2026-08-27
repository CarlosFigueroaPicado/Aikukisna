package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.domain.repository.IaRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class RespuestaGeminiDto(
    val respuesta: String? = null,
    val error: String? = null
)

class IaRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : IaRepository {

    override suspend fun preguntar(prompt: String): String {
        val response = client.functions.invoke("gemini-proxy") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject { put("prompt", prompt) })
        }
        return decodificarRespuesta(response.body())
    }

    override suspend fun conversar(historial: List<MensajeChat>, contexto: String?): String {
        require(historial.isNotEmpty()) { "El historial no puede estar vacío" }

        val response = client.functions.invoke("gemini-proxy") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("mensajes", buildJsonArray {
                    historial.forEach { mensaje ->
                        add(buildJsonObject {
                            put("rol", mensaje.rol.aRolGemini())
                            put("texto", mensaje.texto)
                        })
                    }
                })
                if (contexto != null) put("contexto", contexto)
            })
        }
        return decodificarRespuesta(response.body())
    }

    override suspend fun preguntarConImagen(
        prompt: String,
        imagenBase64: String,
        mimeType: String
    ): String {
        val response = client.functions.invoke("gemini-proxy") {
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("prompt", prompt)
                put("imagenBase64", imagenBase64)
                put("mimeType", mimeType)
            })
        }
        return decodificarRespuesta(response.body())
    }

    private fun decodificarRespuesta(resultado: RespuestaGeminiDto): String =
        resultado.respuesta ?: error(resultado.error ?: "La IA no devolvió respuesta")
}


private fun RolChat.aRolGemini(): String = when (this) {
    RolChat.USUARIO -> "user"
    RolChat.TUKI -> "model"
}