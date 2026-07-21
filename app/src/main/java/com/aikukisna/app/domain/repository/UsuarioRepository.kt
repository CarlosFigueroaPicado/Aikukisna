package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.model.Usuario
import java.util.UUID

interface UsuarioRepository {
    suspend fun obtenerUsuario(id: UUID): Usuario?
    suspend fun actualizarUsuario(usuario: Usuario)
    suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion>
    suspend fun actualizarProgreso(progreso: ProgresoLeccion)
    suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita>
    suspend fun marcarFavorito(usuarioId: UUID, palabraId: Int)
    suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int)
    suspend fun obtenerMemoriaTuki(usuarioId: UUID): List<MemoriaTuki>
    suspend fun guardarMemoriaTuki(memoria: MemoriaTuki)
}