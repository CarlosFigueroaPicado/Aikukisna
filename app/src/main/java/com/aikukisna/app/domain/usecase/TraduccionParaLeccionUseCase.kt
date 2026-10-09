package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import javax.inject.Inject

/**
 * Elige la traducción que se muestra en tarjetas y quizzes.
 *
 * Las palabras de Miskito, Kriol e Inglés se explican en español. Las palabras en Español se
 * explican en la lengua de apoyo del estudiante (Miskito por defecto), con respaldo en las
 * demás: antes se buscaba siempre el español y las lecciones de Español quedaban vacías.
 */
class TraduccionParaLeccionUseCase @Inject constructor(
    private val diccionarioRepository: DiccionarioRepository,
    private val preferencias: PreferenciasAprendizaje
) {
    suspend operator fun invoke(palabra: Palabra): String? {
        val traducciones = diccionarioRepository.obtenerTraducciones(palabra.id)
        return idiomasDestino(palabra.idioma.id).firstNotNullOfOrNull { idiomaId ->
            mejorTraduccion(traducciones.filter { it.palabraDestino.idioma.id == idiomaId })
        }
    }

    /** Idioma en que se explican las oraciones de una lección del idioma [idiomaMetaId]. */
    fun idiomasDestino(idiomaMetaId: Int): List<Int> =
        if (idiomaMetaId == ESPANOL) {
            val apoyo = preferencias.lenguaApoyoId()
            listOf(apoyo) + listOf(MISKITO, KRIOL, INGLES).filter { it != apoyo }
        } else {
            listOf(ESPANOL)
        }

    private companion object {
        const val MISKITO = 1
        const val ESPANOL = 2
        const val KRIOL = 3
        const val INGLES = 4
    }
}

private val RANGO_VALIDACION = mapOf("validada" to 0, "documentada" to 1, "importada" to 2, "pendiente_revision" to 3)

/**
 * La traducción que se muestra cuando el diccionario trae varias: la preferida, luego la mejor
 * validada, la de mayor confianza y la más corta (sin notas entre paréntesis ni punto final).
 */
fun mejorTraduccion(opciones: List<Traduccion>): String? = opciones
    .sortedWith(
        compareByDescending<Traduccion> { it.esPreferida }
            .thenBy { RANGO_VALIDACION[it.estadoValidacion] ?: RANGO_VALIDACION.size }
            .thenByDescending { it.nivelConfianza ?: 0.0 }
            .thenBy { limpiarEntradaDiccionario(it.palabraDestino.texto).length }
    )
    .map { limpiarEntradaDiccionario(it.palabraDestino.texto) }
    .firstOrNull { it.isNotBlank() }

/** Quita el punto final y las notas entre paréntesis que traen algunos diccionarios. */
fun limpiarEntradaDiccionario(texto: String): String = texto
    .replace(Regex("\\s*\\([^)]*\\)"), "")
    .trim()
    .trimEnd('.', ';', ',')
    .trim()
