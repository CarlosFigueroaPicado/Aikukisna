package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenciasAprendizajeLocal @Inject constructor(
    @ApplicationContext context: Context
) : PreferenciasAprendizaje {
    private val preferencias = context.getSharedPreferences("preferencias_aprendizaje", Context.MODE_PRIVATE)

    override fun lenguaApoyoId(): Int = preferencias.getInt(CLAVE_LENGUA_APOYO, MISKITO)

    override fun cambiarLenguaApoyo(idiomaId: Int) {
        preferencias.edit().putInt(CLAVE_LENGUA_APOYO, idiomaId).apply()
    }

    private companion object {
        const val CLAVE_LENGUA_APOYO = "lengua_apoyo_id"
        const val MISKITO = 1
    }
}
