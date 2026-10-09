package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.SesionLocalCache
import com.aikukisna.app.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.SignOutScope
import kotlinx.coroutines.CancellationException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.functions.functions
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val sesionLocalCache: SesionLocalCache
) : AuthRepository {

    override suspend fun registrarse(
        correo: String,
        contrasena: String,
        metadatos: JsonObject
    ): UUID {
        val usuarioCreado = client.auth.signUpWith(Email) {
            email = correo
            password = contrasena
            data = metadatos
        }
        val usuarioId = usuarioCreado?.id?.let(UUID::fromString)
            ?: error("No se creó el usuario tras registrarse")
        sesionLocalCache.guardarUsuario(usuarioId)
        return usuarioId
    }

    override suspend fun iniciarSesionConCorreo(correo: String, contrasena: String): UUID {
        client.auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
        val usuarioId = obtenerIdUsuarioActual()
            ?: error("No se pudo obtener el usuario tras iniciar sesión")
        sesionLocalCache.guardarUsuario(usuarioId)
        return usuarioId
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    override suspend fun iniciarSesionConNombreUsuario(nombreUsuario: String, contrasena: String): UUID {
        val respuesta = client.functions.invoke(
            function = "login-usuario",
            body = SolicitudLoginUsuario(nombreUsuario, contrasena),
            headers = Headers.build {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }
        ).body<RespuestaLoginUsuario>()
        client.auth.importSession(
            UserSession(
                accessToken = respuesta.accessToken,
                refreshToken = respuesta.refreshToken,
                expiresIn = respuesta.expiresIn,
                tokenType = respuesta.tokenType,
                user = null
            )
        )
        client.auth.retrieveUserForCurrentSession(updateSession = true)
        val usuarioId = obtenerIdUsuarioActual()
            ?: UUID.fromString(respuesta.usuarioId)
        sesionLocalCache.guardarUsuario(usuarioId)
        return usuarioId
    }

    override suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String?): UUID {
        client.auth.signInWith(IDToken) {
            idToken = idTokenGoogle
            provider = Google
            this.nonce = nonce
        }
        val usuarioId = obtenerIdUsuarioActual()
            ?: error("No se pudo obtener el usuario tras iniciar sesión con Google")
        sesionLocalCache.guardarUsuario(usuarioId)
        return usuarioId
    }

    override suspend fun solicitarRestablecimientoContrasena(correo: String) {
        client.auth.resetPasswordForEmail(
            email = correo,
            redirectUrl = "aikukisna://auth-callback/recovery"
        )
    }

    override suspend fun actualizarContrasena(nuevaContrasena: String) {
        client.auth.updateUser {
            password = nuevaContrasena
        }
    }

    override suspend fun cerrarSesion() {
        // Sin red (o con la sesión vencida) el cierre en el servidor falla; aun así la sesión se
        // cierra en el teléfono. Antes el error impedía salir y el botón parecía no funcionar.
        try {
            client.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            runCatching { client.auth.signOut(SignOutScope.LOCAL) }
        } finally {
            sesionLocalCache.limpiar()
        }
    }

    override suspend fun usuarioActualId(): UUID? {
        return obtenerIdUsuarioActual() ?: sesionLocalCache.obtenerUsuario()
    }

    private fun obtenerIdUsuarioActual(): UUID? {
        return client.auth.currentUserOrNull()?.id?.let { UUID.fromString(it) }
    }
}

@Serializable
private data class SolicitudLoginUsuario(
    val identificador: String,
    @SerialName("contrasena") val contrasena: String
)

@Serializable
private data class RespuestaLoginUsuario(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String,
    @SerialName("usuario_id") val usuarioId: String
)
