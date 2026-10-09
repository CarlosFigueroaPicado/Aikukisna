package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.model.Idioma
import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface UsuarioRepository {
    /** Idioma meta del perfil activo; emite de nuevo cada vez que el usuario lo cambia. */
    fun observarIdiomaMeta(): Flow<Idioma?>
    suspend fun obtenerUsuario(id: UUID): Usuario?
    /** Sube el perfil modificado sin conexión. Devuelve true si no queda nada pendiente. */
    suspend fun sincronizarPerfilPendiente(id: UUID): Boolean
    suspend fun actualizarUsuario(usuario: Usuario)
    suspend fun guardarUsuarioLocal(usuario: Usuario)
    suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion>
    suspend fun obtenerProgresoLocal(usuarioId: UUID): List<ProgresoLeccion>
    suspend fun actualizarProgreso(progreso: ProgresoLeccion)
    suspend fun guardarProgresoLocal(progreso: ProgresoLeccion)
    suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita>
    suspend fun marcarFavorito(usuarioId: UUID, palabraId: Int)
    suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int)
    suspend fun sincronizarFavoritosPendientes(): Int
    suspend fun obtenerMemoriaTuki(usuarioId: UUID): List<MemoriaTuki>
    suspend fun guardarMemoriaTuki(memoria: MemoriaTuki)
}
