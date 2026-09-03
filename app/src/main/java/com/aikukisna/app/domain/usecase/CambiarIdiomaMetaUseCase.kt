package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.UsuarioRepository
import javax.inject.Inject

class CambiarIdiomaMetaUseCase @Inject constructor(
    private val usuarioRepository: UsuarioRepository
) {
    suspend operator fun invoke(usuario: Usuario, nuevoIdioma: Idioma) {
        usuarioRepository.actualizarUsuario(usuario.copy(idiomaMeta = nuevoIdioma))
    }
}