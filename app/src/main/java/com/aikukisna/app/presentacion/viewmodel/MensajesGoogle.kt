package com.aikukisna.app.presentacion.viewmodel

import android.util.Log
import com.aikukisna.app.R
import com.aikukisna.app.data.auth.ErrorInicioGoogle
import com.aikukisna.app.presentacion.idioma.t

/**
 * Mensaje para el estudiante cuando falla "Continuar con Google". Nunca muestra el texto técnico de la
 * excepción: antes cualquier error que contuviera "cancel" decía "Inicio de sesión cancelado" y los demás
 * mostraban mensajes de Supabase o de Google en inglés.
 */
internal fun mensajeErrorGoogle(e: Exception, mensajeCancelado: String): String = when {
    e is ErrorInicioGoogle && e.motivo == ErrorInicioGoogle.Motivo.CANCELADO -> mensajeCancelado
    e is ErrorInicioGoogle && e.motivo == ErrorInicioGoogle.Motivo.SIN_CUENTA -> t(R.string.login_google_sin_cuenta)
    else -> {
        Log.w("AikukisnaGoogle", "Inicio con Google falló: ${e.javaClass.simpleName} ${e.message}")
        t(R.string.login_google_no_se_pudo)
    }
}
