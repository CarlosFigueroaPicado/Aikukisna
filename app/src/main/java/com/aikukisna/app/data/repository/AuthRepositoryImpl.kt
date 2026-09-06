package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID
import javax.inject.Inject
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
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
        return usuarioCreado?.id?.let(UUID::fromString)
            ?: error("No se creó el usuario tras registrarse")
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): UUID {
        client.auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
        return obtenerIdUsuarioActual()
            ?: error("No se pudo obtener el usuario tras iniciar sesión")
    }

    override suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String?): UUID {
        client.auth.signInWith(IDToken) {
            idToken = idTokenGoogle
            provider = Google
            this.nonce = nonce
        }
        return usuarioActualId()
            ?: error("No se pudo obtener el usuario tras iniciar sesión con Google")
    }

    override suspend fun cerrarSesion() {
        client.auth.signOut()
    }

    override suspend fun usuarioActualId(): UUID? {
        return obtenerIdUsuarioActual()
    }

    override suspend fun obtenerCorreoPorNombreUsuario(nombreUsuario: String): String? {
        return client.postgrest.rpc(
            "obtener_correo_por_usuario",
            buildJsonObject { put("p_nombre_usuario", nombreUsuario) }
        ).decodeAs<String?>()
    }

    private fun obtenerIdUsuarioActual(): UUID? {
        return client.auth.currentUserOrNull()?.id?.let { UUID.fromString(it) }
    }
}