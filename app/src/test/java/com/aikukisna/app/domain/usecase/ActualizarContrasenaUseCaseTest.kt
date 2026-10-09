package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.repository.AuthRepository
import java.util.UUID
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ActualizarContrasenaUseCaseTest {
    @Test fun actualizaCuandoCumpleLaPoliticaYCoincide() {
        val repo = AuthFalso()
        ejecutarActualizacion { ActualizarContrasenaUseCase(repo)("Segura1!", "Segura1!") }
        assertEquals("Segura1!", repo.recibida)
    }

    @Test fun rechazaConfirmacionDistinta() {
        assertThrows(IllegalArgumentException::class.java) {
            ejecutarActualizacion { ActualizarContrasenaUseCase(AuthFalso())("Segura1!", "Distinta1!") }
        }
    }

    private class AuthFalso : AuthRepository {
        var recibida: String? = null
        override suspend fun actualizarContrasena(nuevaContrasena: String) { recibida = nuevaContrasena }
        override suspend fun solicitarRestablecimientoContrasena(correo: String) = Unit
        override suspend fun registrarse(correo: String, contrasena: String, metadatos: JsonObject) = error("No usado")
        override suspend fun iniciarSesionConCorreo(correo: String, contrasena: String) = error("No usado")
        override suspend fun iniciarSesionConNombreUsuario(nombreUsuario: String, contrasena: String) = error("No usado")
        override suspend fun cerrarSesion() = Unit
        override suspend fun usuarioActualId(): UUID? = null
        override suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String?) = error("No usado")
    }
}

private fun <T> ejecutarActualizacion(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) { resultado = result }
    })
    return resultado!!.getOrThrow()
}
