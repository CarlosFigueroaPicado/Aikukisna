package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.LogroDesbloqueado
import java.util.UUID

interface LogroRepository {
    suspend fun obtenerLogros(): List<Logro>
    suspend fun obtenerLogrosDesbloqueados(usuarioId: UUID): List<LogroDesbloqueado>
    suspend fun desbloquearLogro(usuarioId: UUID, logroId: Int)
}