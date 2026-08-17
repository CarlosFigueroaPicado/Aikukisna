package com.aikukisna.app.domain.repository

import java.util.UUID
import kotlinx.serialization.json.JsonObject

interface AuthRepository {
    suspend fun registrarse(
        correo: String,
        contrasena: String,
        metadatos: JsonObject
    ): UUID
    suspend fun iniciarSesion(correo: String, contrasena: String): UUID
    suspend fun cerrarSesion()
    suspend fun usuarioActualId(): UUID?
    suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String? = null): UUID
}