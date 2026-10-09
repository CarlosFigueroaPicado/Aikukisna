package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.dao.ContenidoGlobalDao
import com.aikukisna.app.domain.gramatica.MotorGramaticalControlado
import com.aikukisna.app.domain.repository.ConocimientoDocumentado
import com.aikukisna.app.domain.repository.CulturaRepository
import com.aikukisna.app.domain.repository.FragmentoConocimiento
import com.aikukisna.app.domain.repository.TipoFragmento
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConocimientoDocumentadoImpl @Inject constructor(
    private val gramatica: MotorGramaticalControlado,
    private val contenidoGlobal: ContenidoGlobalDao,
    private val cultura: CulturaRepository
) : ConocimientoDocumentado {

    override suspend fun fragmentos(idiomaId: Int): List<FragmentoConocimiento> {
        val reglas = runCatching { gramatica.obtenerReglas() }.getOrDefault(emptyList())
            .filter { it.idiomaId == idiomaId && it.estadoOriginal != "rechazada" }
            .map { regla ->
                FragmentoConocimiento(
                    tipo = TipoFragmento.GRAMATICA,
                    idiomaId = regla.idiomaId,
                    titulo = regla.titulo,
                    texto = listOfNotNull(regla.descripcion, regla.aplicacionDocumentada)
                        .map(String::trim).filter(String::isNotBlank).distinct().joinToString(" "),
                    ejemplos = regla.ejemplos.take(3).map { ejemplo ->
                        ejemplo.traduccionEspanol?.takeIf(String::isNotBlank)
                            ?.let { "${ejemplo.textoIdioma} — $it" } ?: ejemplo.textoIdioma
                    }
                )
            }
        // Reglas documentadas en la base local que aún no están en el catálogo validado (p. ej. plural con nani).
        val codigosCatalogo = runCatching { gramatica.obtenerReglas().map { it.codigo }.toSet() }.getOrDefault(emptySet())
        val documentadas = runCatching { contenidoGlobal.obtenerReglasGramaticalesDocumentadas(idiomaId) }
            .getOrDefault(emptyList())
            .filter { it.codigo !in codigosCatalogo }
            .map { regla ->
                FragmentoConocimiento(
                    tipo = TipoFragmento.GRAMATICA,
                    idiomaId = regla.idiomaId,
                    titulo = regla.titulo,
                    texto = listOfNotNull(regla.descripcion, regla.aplicacion)
                        .map(String::trim).filter(String::isNotBlank).distinct().joinToString(" "),
                    ejemplos = contenidoGlobal.obtenerEjemplosRegla(regla.id).take(3).map { ejemplo ->
                        ejemplo.traduccionEspanol?.takeIf(String::isNotBlank)
                            ?.let { "${ejemplo.textoIdioma} — $it" } ?: ejemplo.textoIdioma
                    }
                )
            }
        val pronunciacion = runCatching { contenidoGlobal.obtenerReglasPronunciacion(idiomaId) }
            .getOrDefault(emptyList())
            .map { regla ->
                FragmentoConocimiento(
                    tipo = TipoFragmento.PRONUNCIACION,
                    idiomaId = regla.idiomaId,
                    titulo = "Pronunciación y escritura: ${regla.patron}",
                    texto = regla.descripcion,
                    ejemplos = listOfNotNull(regla.ejemplo?.takeIf(String::isNotBlank))
                )
            }
        val culturales = runCatching { cultura.obtenerContenidoCultural() }.getOrDefault(emptyList())
            .map { item ->
                FragmentoConocimiento(
                    tipo = TipoFragmento.CULTURA,
                    idiomaId = null,
                    titulo = item.titulo,
                    texto = item.contenido,
                    fuente = item.fuente.titulo
                )
            }
        return reglas + documentadas + pronunciacion + culturales
    }
}
