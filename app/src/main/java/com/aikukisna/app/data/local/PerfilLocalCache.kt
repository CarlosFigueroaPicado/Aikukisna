package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Usuario
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerfilLocalCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferencias = context.getSharedPreferences("perfil_offline", Context.MODE_PRIVATE)

    fun guardar(usuario: Usuario) {
        preferencias.edit()
            .putString("id", usuario.id.toString())
            .putString("nombre", usuario.nombre)
            .putString("apellido", usuario.apellido)
            .putString("nombreUsuario", usuario.nombreUsuario)
            .putString("correo", usuario.correo)
            .putInt("edad", usuario.edad ?: -1)
            .putString("pais", usuario.pais)
            .putString("ciudad", usuario.ciudad)
            .putInt("idiomaId", usuario.idiomaMeta?.id ?: -1)
            .putString("idiomaCodigo", usuario.idiomaMeta?.codigo)
            .putString("idiomaNombre", usuario.idiomaMeta?.nombre)
            .putInt("xp", usuario.xp)
            .putInt("rachaActual", usuario.rachaActual)
            .putInt("rachaMaxima", usuario.rachaMaxima)
            .putString("ultimaActividad", usuario.ultimaActividad?.toString())
            .putString("fotoPerfilUri", usuario.fotoPerfilUri)
            .apply()
    }

    fun leer(id: UUID): Usuario? {
        if (preferencias.getString("id", null) != id.toString()) return null
        val idiomaId = preferencias.getInt("idiomaId", -1)
        return Usuario(
            id = id,
            nombre = preferencias.getString("nombre", null),
            apellido = preferencias.getString("apellido", null),
            nombreUsuario = preferencias.getString("nombreUsuario", null),
            correo = preferencias.getString("correo", null),
            edad = preferencias.getInt("edad", -1).takeIf { it >= 0 },
            pais = preferencias.getString("pais", null),
            ciudad = preferencias.getString("ciudad", null),
            idiomaMeta = if (idiomaId >= 0) Idioma(
                id = idiomaId,
                codigo = preferencias.getString("idiomaCodigo", "") ?: "",
                nombre = preferencias.getString("idiomaNombre", "") ?: ""
            ) else null,
            xp = preferencias.getInt("xp", 0),
            rachaActual = preferencias.getInt("rachaActual", 0),
            rachaMaxima = preferencias.getInt("rachaMaxima", 0),
            ultimaActividad = preferencias.getString("ultimaActividad", null)?.let(LocalDate::parse),
            fotoPerfilUri = preferencias.getString("fotoPerfilUri", null)
        )
    }
}
