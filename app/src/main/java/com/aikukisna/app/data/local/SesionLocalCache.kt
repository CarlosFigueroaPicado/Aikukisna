package com.aikukisna.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SesionLocalCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferencias = context.getSharedPreferences("sesion_local", Context.MODE_PRIVATE)

    fun guardarUsuario(usuarioId: UUID) {
        preferencias.edit().putString(CLAVE_USUARIO_ID, usuarioId.toString()).apply()
    }

    fun obtenerUsuario(): UUID? {
        val valor = preferencias.getString(CLAVE_USUARIO_ID, null) ?: return null
        return runCatching { UUID.fromString(valor) }.getOrNull()
    }

    fun limpiar() {
        preferencias.edit().remove(CLAVE_USUARIO_ID).apply()
    }

    private companion object {
        const val CLAVE_USUARIO_ID = "usuario_id"
    }
}
