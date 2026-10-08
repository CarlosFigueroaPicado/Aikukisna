package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.domain.repository.EtiquetasObjetoEspanol
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

/** Nombre común en español de las etiquetas de ML Kit (447) y del clasificador ImageNet (1000), incluidos en assets. */
@Singleton
class TablaEtiquetasMlKit @Inject constructor(
    @ApplicationContext private val context: Context
) : EtiquetasObjetoEspanol {

    // Etiquetas del etiquetador base (447) y del clasificador EfficientNet (1000 de ImageNet).
    private val tabla: Map<String, String> by lazy { leer(ARCHIVO_IMAGENET) + leer(ARCHIVO) }

    private fun leer(archivo: String): Map<String, String> {
        val json = JSONObject(context.assets.open(archivo).bufferedReader().use { it.readText() })
        return json.keys().asSequence()
            .filterNot { it.startsWith("_") }
            .associate { it.lowercase() to json.getString(it) }
    }

    override fun espanolPara(etiquetaIngles: String): String? = tabla[etiquetaIngles.trim().lowercase()]

    private companion object {
        const val ARCHIVO = "etiquetas_mlkit_es.json"
        const val ARCHIVO_IMAGENET = "etiquetas_imagenet_es.json"
    }
}
