package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.data.local.dao.AudioPronunciacionDao
import com.aikukisna.app.data.local.entity.AudioPronunciacionEntity
import com.aikukisna.app.domain.model.OrigenAudioPronunciacion
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Registra las grabaciones de hablantes incluidas en assets/pronunciaciones_humanas/ (revisadas a oído antes de
 * empaquetarlas). El crédito de cada fuente queda en la base como fuenteId y hablante, nunca dentro del audio.
 * Corre después de la réplica para que las palabras ya existan (la tabla tiene llave foránea a palabra_cache).
 */
@Singleton
class SembradorAudiosHumanos @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AudioPronunciacionDao
) {
    private val preferencias = context.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)

    // La app y las pruebas pueden llamarlo a la vez: sin candado, un borrado deja la tabla vacía a medias.
    private val candado = Mutex()

    suspend fun sembrarSiCambio(): Int = withContext(Dispatchers.IO) { candado.withLock { sembrar() } }

    /** true cuando todas las grabaciones del índice quedaron registradas en esta instalación. */
    @Volatile var registroCompleto = false
        private set

    private suspend fun sembrar(): Int {
        val indice = runCatching {
            context.assets.open("$DIRECTORIO/$INDICE").bufferedReader().use { JSONObject(it.readText()) }
        }.getOrNull() ?: return 0
        val version = indice.optInt("version")
        val fuentes = indice.getJSONArray("fuentes")
        val esperadas = (0 until fuentes.length()).sumOf { f ->
            val audios = fuentes.getJSONObject(f).getJSONArray("audios")
            (0 until audios.length()).sumOf { audios.getJSONObject(it).getJSONArray("palabra_ids").length() }
        }
        // Versión registrada y con todas sus filas: nada que hacer. Si quedó vacía o a medias (se registró
        // mientras la réplica todavía cargaba las palabras), se vuelve a intentar.
        if (preferencias.getInt(CLAVE_VERSION, 0) == version && dao.contarHumanos() >= esperadas) {
            registroCompleto = true
            return 0
        }

        var total = 0
        var completo = true
        for (f in 0 until fuentes.length()) {
            val fuente = fuentes.getJSONObject(f)
            val fuenteId = fuente.getInt("fuente_id")
            val hablante = fuente.getString("hablante")
            val audios = fuente.getJSONArray("audios")
            val ids = (0 until audios.length()).flatMap { i ->
                val lista = audios.getJSONObject(i).getJSONArray("palabra_ids")
                (0 until lista.length()).map { lista.getInt(it) }
            }
            val existentes = ids.distinct().chunked(LOTE).flatMap { dao.palabrasExistentes(it) }.toSet()
            // Recién instalada, la réplica puede no haber cargado las palabras todavía: sin ellas no se
            // registra nada y no se guarda la versión, para reintentar en la próxima consulta.
            if (ids.isNotEmpty() && existentes.isEmpty()) return 0
            if (existentes.size < ids.distinct().size) completo = false
            val filas = (0 until audios.length()).flatMap { i ->
                val audio = audios.getJSONObject(i)
                val lista = audio.getJSONArray("palabra_ids")
                (0 until lista.length()).map { lista.getInt(it) }.filter { it in existentes }.map { palabraId ->
                    AudioPronunciacionEntity(
                        palabraId = palabraId,
                        idiomaCodigo = audio.getString("idioma"),
                        tipoReferencia = TIPO_ASSET,
                        referencia = "$DIRECTORIO/${audio.getString("archivo")}",
                        origen = OrigenAudioPronunciacion.HUMANO.name,
                        verificado = true,
                        textoNormalizado = audio.getString("forma"),
                        fuenteId = fuenteId,
                        hablante = hablante
                    )
                }
            }
            dao.reemplazarFuente(fuenteId, filas)
            total += filas.size
        }
        // La versión solo se marca cuando estaban todas las palabras; si no, se completa en otra consulta.
        if (completo) preferencias.edit().putInt(CLAVE_VERSION, version).apply()
        registroCompleto = completo
        return total
    }

    private companion object {
        const val DIRECTORIO = "pronunciaciones_humanas"
        const val INDICE = "indice.json"
        const val PREFERENCIAS = "audios_humanos"
        const val CLAVE_VERSION = "version_indice"
        const val TIPO_ASSET = "ASSET"
        const val LOTE = 500
    }
}
