package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import javax.inject.Inject

class RegistrarUsuarioUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) {
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
        val id = authRepository.registrarse(correo, contrasena)

        val usuario = Usuario(
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

        usuarioRepository.actualizarUsuario(usuario)
        return usuario
    }
}