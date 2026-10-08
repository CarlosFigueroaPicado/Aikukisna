package com.aikukisna.app.data.local

import android.content.Context
import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.model.FuenteDocumento
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CulturaCache(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val rangoPaginaInicio: Int? = null,
    val rangoPaginaFin: Int? = null,
    val fuenteId: Int,
    val fuenteTitulo: String,
    val fuenteAutor: String? = null,
    val fuenteAnio: Int? = null,
    val fuenteInstitucion: String? = null
)

@Singleton
class CulturaLocalCache @Inject constructor(
    @ApplicationContext context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val preferencias = context.getSharedPreferences("cultura_offline", Context.MODE_PRIVATE)

    fun guardar(contenidos: List<CulturaContenido>) {
        if (contenidos.isEmpty()) return
        val datos = contenidos.map { item ->
            CulturaCache(
                id = item.id,
                titulo = item.titulo,
                contenido = item.contenido,
                rangoPaginaInicio = item.rangoPaginaInicio,
                rangoPaginaFin = item.rangoPaginaFin,
                fuenteId = item.fuente.id,
                fuenteTitulo = item.fuente.titulo,
                fuenteAutor = item.fuente.autor,
                fuenteAnio = item.fuente.anio,
                fuenteInstitucion = item.fuente.institucion
            )
        }
        preferencias.edit().putString(CLAVE, json.encodeToString(datos)).apply()
    }

    fun leer(): List<CulturaContenido> {
        val texto = preferencias.getString(CLAVE, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<CulturaCache>>(texto) }
            .getOrDefault(emptyList())
            .map { item ->
                CulturaContenido(
                    id = item.id,
                    titulo = item.titulo,
                    contenido = item.contenido,
                    rangoPaginaInicio = item.rangoPaginaInicio,
                    rangoPaginaFin = item.rangoPaginaFin,
                    fuente = FuenteDocumento(
                        id = item.fuenteId,
                        titulo = item.fuenteTitulo,
                        autor = item.fuenteAutor,
                        anio = item.fuenteAnio,
                        institucion = item.fuenteInstitucion
                    )
                )
            }
    }

    fun leer(id: Int): CulturaContenido? = leer().firstOrNull { it.id == id }

    private companion object {
        const val CLAVE = "contenido"
    }
}
