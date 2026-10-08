package com.aikukisna.app.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pasos que el usuario hace una sola vez tras iniciar sesión: aceptar los términos
 * (por versión, para volver a pedirlos si cambian) y ver la pantalla de descarga del modelo.
 */
@Singleton
class PrimerUsoPreferencias @Inject constructor(
    @ApplicationContext context: Context
) {
    private val preferencias = context.getSharedPreferences("primer_uso", Context.MODE_PRIVATE)

    fun terminosAceptados(): Boolean =
        preferencias.getInt(CLAVE_TERMINOS, 0) >= VERSION_TERMINOS

    fun aceptarTerminos() {
        preferencias.edit()
            .putInt(CLAVE_TERMINOS, VERSION_TERMINOS)
            .putLong(CLAVE_FECHA_TERMINOS, System.currentTimeMillis())
            .apply()
    }

    /** La presentación se muestra una vez; después la app abre directo en el inicio de sesión. */
    fun onboardingVisto(): Boolean = preferencias.getBoolean(CLAVE_ONBOARDING, false)

    fun marcarOnboardingVisto() {
        preferencias.edit().putBoolean(CLAVE_ONBOARDING, true).apply()
    }

    fun descargaModeloMostrada(): Boolean = preferencias.getBoolean(CLAVE_DESCARGA, false)

    fun marcarDescargaModeloMostrada() {
        preferencias.edit().putBoolean(CLAVE_DESCARGA, true).apply()
    }

    companion object {
        /** Subir este número cuando cambie el texto de TerminosScreen. */
        const val VERSION_TERMINOS = 1
        private const val CLAVE_TERMINOS = "version_terminos_aceptada"
        private const val CLAVE_ONBOARDING = "onboarding_visto"
        private const val CLAVE_FECHA_TERMINOS = "fecha_terminos_aceptados"
        private const val CLAVE_DESCARGA = "descarga_modelo_mostrada"
    }
}
