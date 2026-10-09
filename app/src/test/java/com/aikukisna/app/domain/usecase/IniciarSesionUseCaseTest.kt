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

class IniciarSesionUseCaseTest {
    private val usuarioId = UUID.fromString("11111111-1111-1111-1111-111111111111")

    @Test
    fun correoValidoConContrasenaCorrecta() {
        val repositorio = RepositorioAuthPrueba(usuarioId)
        val resultado = ejecutarLogin { IniciarSesionUseCase(repositorio)("carlos@email.com", "correcta") }
        assertEquals(usuarioId, resultado)
        assertEquals("carlos@email.com", repositorio.correoRecibido)
    }

    @Test
    fun usernameValidoConContrasenaCorrecta() {
        val repositorio = RepositorioAuthPrueba(usuarioId)
        val resultado = ejecutarLogin { IniciarSesionUseCase(repositorio)("carlos123", "correcta") }
        assertEquals(usuarioId, resultado)
        assertEquals("carlos123", repositorio.usuarioRecibido)
    }

    @Test
    fun correoValidoConContrasenaIncorrectaUsaMensajeGenerico() = verificarFalloGenerico("carlos@email.com")

    @Test
    fun usernameValidoConContrasenaIncorrectaUsaMensajeGenerico() = verificarFalloGenerico("carlos123")

    @Test
    fun usernameInexistenteUsaMensajeGenerico() = verificarFalloGenerico("usuario_inexistente")

    @Test
    fun correoInexistenteUsaMensajeGenerico() = verificarFalloGenerico("nadie@ejemplo.com")

    @Test
    fun usernameConEspaciosLateralesSeNormaliza() {
        val repositorio = RepositorioAuthPrueba(usuarioId)
        ejecutarLogin { IniciarSesionUseCase(repositorio)("  carlos123  ", "correcta") }
        assertEquals("carlos123", repositorio.usuarioRecibido)
    }

    @Test
    fun usernameConMayusculasSeEnviaSinAlterarloParaComparacionRemotaInsensibleAMayusculas() {
        val repositorio = RepositorioAuthPrueba(usuarioId)
        ejecutarLogin { IniciarSesionUseCase(repositorio)("Carlos123", "correcta") }
        assertEquals("Carlos123", repositorio.usuarioRecibido)
    }

    private fun verificarFalloGenerico(identificador: String) {
        val caso = IniciarSesionUseCase(RepositorioAuthPrueba(error = IllegalStateException("detalle sensible")))
        val error = assertThrows(CredencialesInvalidasException::class.java) {
            ejecutarLogin { caso(identificador, "incorrecta") }
        }
        assertEquals(IniciarSesionUseCase.MENSAJE_CREDENCIALES_INVALIDAS, error.message)
    }

    private class RepositorioAuthPrueba(
        private val resultado: UUID? = null,
        private val error: Exception? = null
    ) : AuthRepository {
        var correoRecibido: String? = null
        var usuarioRecibido: String? = null

        override suspend fun iniciarSesionConCorreo(correo: String, contrasena: String): UUID {
            correoRecibido = correo
            error?.let { throw it }
            return checkNotNull(resultado)
        }

        override suspend fun iniciarSesionConNombreUsuario(nombreUsuario: String, contrasena: String): UUID {
            usuarioRecibido = nombreUsuario
            error?.let { throw it }
            return checkNotNull(resultado)
        }

        override suspend fun registrarse(correo: String, contrasena: String, metadatos: JsonObject) = error("No usado")
        override suspend fun cerrarSesion() = Unit
        override suspend fun usuarioActualId(): UUID? = resultado
        override suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String?) = error("No usado")
        override suspend fun solicitarRestablecimientoContrasena(correo: String) = Unit
        override suspend fun actualizarContrasena(nuevaContrasena: String) = Unit
    }
}

private fun <T> ejecutarLogin(bloque: suspend () -> T): T {
    var resultado: Result<T>? = null
    bloque.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) {
            resultado = result
        }
    })
    return resultado!!.getOrThrow()
}
