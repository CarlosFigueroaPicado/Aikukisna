package com.aikukisna.app.data.repository

import com.aikukisna.app.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import java.util.UUID
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val client: SupabaseClient
) : AuthRepository {

    override suspend fun registrarse(correo: String, contrasena: String): UUID {
        client.auth.signUpWith(Email) {
            email = correo
            password = contrasena
        }
        return obtenerIdUsuarioActual()
            ?: error("No se pudo obtener el usuario tras el registro")
    }

    override suspend fun iniciarSesion(correo: String, contrasena: String): UUID {
        client.auth.signInWith(Email) {
            email = correo
            password = contrasena
        }
        return obtenerIdUsuarioActual()
            ?: error("No se pudo obtener el usuario tras iniciar sesión")
    }

    override suspend fun cerrarSesion() {
        client.auth.signOut()
    }

    override suspend fun usuarioActualId(): UUID? {
        return obtenerIdUsuarioActual()
    }

    private fun obtenerIdUsuarioActual(): UUID? {
        return client.auth.currentUserOrNull()?.id?.let { UUID.fromString(it) }
    }
}