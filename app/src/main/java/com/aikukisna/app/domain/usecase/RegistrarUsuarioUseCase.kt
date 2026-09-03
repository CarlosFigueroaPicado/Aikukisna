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

        val PATRON_CORREO = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val LONGITUD_MINIMA_CONTRASENA = 8
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

        val requisitosFaltantes = buildList {
            if (contrasena.length < LONGITUD_MINIMA_CONTRASENA) add("al menos $LONGITUD_MINIMA_CONTRASENA caracteres")
            if (contrasena.none { it.isUpperCase() }) add("una letra mayúscula")
            if (contrasena.none { it.isDigit() }) add("un número")
            if (contrasena.none { !it.isLetterOrDigit() }) add("un símbolo (ej: !@#$%)")
        }
        require(requisitosFaltantes.isEmpty()) {
            "La contraseña debe tener " + requisitosFaltantes.joinToString(", ")
        }
        require(nombre.isNotBlank()) {
            "El nombre no puede estar vacío"
        }
        require(nombreUsuario.isNotBlank()) {
            "El nombre de usuario no puede estar vacío"
        }

        require(edad in 0..120) {
            "La edad no es válida"
        }


        val yaExiste = authRepository.obtenerCorreoPorNombreUsuario(nombreUsuario) != null
        require(!yaExiste) {
            "Ese nombre de usuario ya está en uso"
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