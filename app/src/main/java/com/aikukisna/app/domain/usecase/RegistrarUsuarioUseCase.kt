package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.AuthRepository
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class RegistrarUsuarioUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    private companion object {
        // Chequeo pragmático de "tiene forma de correo", no RFC5322 completo.
        // La verificación real de que el correo existe y es del usuario ya
        // la hace el deep link de confirmación — esto solo evita mandar
        // basura evidente al servidor.
        val PATRON_CORREO = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val LONGITUD_MINIMA_CONTRASENA = 6
    }

    suspend operator fun invoke(
        correo: String,
        contrasena: String,
        nombre: String,
        apellido: String,
        nombreUsuario: String,
        edad: Int,
        pais: String,
        ciudad: String,
        idiomaMeta: Idioma
    ): Usuario {
        require(correo.isNotBlank() && PATRON_CORREO.matches(correo)) {
            "El correo no tiene un formato válido"
        }
        require(contrasena.length >= LONGITUD_MINIMA_CONTRASENA) {
            "La contraseña debe tener al menos $LONGITUD_MINIMA_CONTRASENA caracteres"
        }
        require(nombre.isNotBlank()) {
            "El nombre no puede estar vacío"
        }
        require(nombreUsuario.isNotBlank()) {
            "El nombre de usuario no puede estar vacío"
        }
        // 0 es válido a propósito: el registro simplificado no pide edad
        // todavía (ver RegisterViewModel) y manda 0 como marcador. Se
        // rechaza solo lo evidentemente inválido (negativo o absurdo).
        require(edad in 0..120) {
            "La edad no es válida"
        }

        val metadatos = buildJsonObject {
            put("nombre", nombre)
            put("apellido", apellido)
            put("nombre_usuario", nombreUsuario)
            put("edad", edad)
            put("pais", pais)
            put("ciudad", ciudad)
        }

        val id = authRepository.registrarse(correo, contrasena, metadatos)

        return Usuario(
            id = id,
            nombre = nombre,
            apellido = apellido,
            nombreUsuario = nombreUsuario,
            correo = correo,
            edad = edad,
            pais = pais,
            ciudad = ciudad,
            idiomaMeta = idiomaMeta,
            xp = 0,
            rachaActual = 0,
            rachaMaxima = 0,
            ultimaActividad = null
        )
    }
}