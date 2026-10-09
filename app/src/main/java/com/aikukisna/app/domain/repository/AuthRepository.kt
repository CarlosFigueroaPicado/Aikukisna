package com.aikukisna.app.domain.repository

import java.util.UUID
import kotlinx.serialization.json.JsonObject

interface AuthRepository {
    suspend fun registrarse(
        correo: String,
        contrasena: String,
        metadatos: JsonObject
    ): UUID
    suspend fun iniciarSesionConCorreo(correo: String, contrasena: String): UUID
    suspend fun iniciarSesionConNombreUsuario(nombreUsuario: String, contrasena: String): UUID
    suspend fun cerrarSesion()
    suspend fun usuarioActualId(): UUID?
    suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String? = null): UUID
    suspend fun solicitarRestablecimientoContrasena(correo: String)
    suspend fun actualizarContrasena(nuevaContrasena: String)
}
