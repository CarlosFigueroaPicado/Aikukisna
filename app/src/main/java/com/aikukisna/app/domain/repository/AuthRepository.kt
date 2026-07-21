package com.aikukisna.app.domain.repository

import java.util.UUID

interface AuthRepository {
    suspend fun registrarse(correo: String, contrasena: String): UUID
    suspend fun iniciarSesion(correo: String, contrasena: String): UUID
    suspend fun cerrarSesion()
    suspend fun usuarioActualId(): UUID?
}