package com.aikukisna.app.presentacion.idioma

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StringRes
import java.util.Locale

/** Idiomas en que puede mostrarse la app (no confundir con el idioma que se aprende). */
enum class OpcionIdiomaInterfaz(
    val etiqueta: String,
    val etiquetaBcp47: String,
    /** Borrador pendiente de revisión por hablantes nativos; la app lo avisa. */
    val enRevision: Boolean
) {
    ESPANOL("Español", "es", enRevision = false),
    INGLES("English", "en", enRevision = false),
    MISKITO("Miskitu", "miq", enRevision = true),
    KRIOL("Kriol", "bzk", enRevision = true)
}

/**
 * Idioma de la interfaz. Un estudiante miskito o kriol puede usar la app en su lengua mientras
 * aprende Español o Inglés. Los textos se leen con [t] desde cualquier capa de presentación.
 */
object IdiomaInterfaz {
    private const val PREFERENCIAS = "idioma_interfaz"
    private const val CLAVE = "opcion"

    @Volatile
    private var recursos: Resources? = null

    fun actual(context: Context): OpcionIdiomaInterfaz {
        val guardada = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).getString(CLAVE, null)
        return OpcionIdiomaInterfaz.entries.firstOrNull { it.name == guardada } ?: OpcionIdiomaInterfaz.ESPANOL
    }

    /** Contexto con el idioma elegido; lo usa la actividad al crearse y la aplicación al iniciar. */
    fun envolver(context: Context): Context {
        val locale = Locale.forLanguageTag(actual(context).etiquetaBcp47)
        val configuracion = Configuration(context.resources.configuration).apply { setLocale(locale) }
        return context.createConfigurationContext(configuracion).also { recursos = it.resources }
    }

    /** Guarda el idioma y reinicia la app para que pantallas y textos ya cargados lo usen. */
    fun cambiar(activity: Activity, opcion: OpcionIdiomaInterfaz) {
        activity.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE).edit().putString(CLAVE, opcion.name).commit()
        envolver(activity.applicationContext)
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        activity.startActivity(intent)
        activity.finish()
    }

    fun texto(@StringRes id: Int, vararg argumentos: Any): String {
        val r = recursos ?: return "…"
        return if (argumentos.isEmpty()) r.getString(id) else r.getString(id, *argumentos)
    }
}

/** Texto de la interfaz en el idioma elegido. */
fun t(@StringRes id: Int, vararg argumentos: Any): String = IdiomaInterfaz.texto(id, *argumentos)
