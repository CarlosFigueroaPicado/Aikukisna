package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.CulturaContenido
import com.aikukisna.app.domain.repository.CulturaRepository
import java.text.Normalizer
import javax.inject.Inject

class ConsultarCulturaTukiUseCase @Inject constructor(
    private val culturaRepository: CulturaRepository
) {
    suspend operator fun invoke(pregunta: String, idiomaMetaId: Int): String? {
        val consulta = normalizar(pregunta)
        val aliases = ALIASES_IDIOMA[idiomaMetaId].orEmpty()
        val mencionaIdioma = aliases.any(consulta::contains)
        val mencionaCultura = PALABRAS_CULTURA.any(consulta::contains)
        val contenido = culturaRepository.obtenerContenidoCultural()
        val mencionaTema = contenido.any { item ->
            val titulo = normalizar(item.titulo)
            titulo.length >= 4 && (consulta == titulo || consulta.contains(titulo))
        }
        if (!mencionaIdioma && !mencionaCultura && !mencionaTema) return null

        val disponibles = contenido.filter { item ->
            aliases.isEmpty() || aliases.any(
                normalizar("${item.titulo} ${item.contenido} ${item.fuente.titulo}")::contains
            )
        }
        if (disponibles.isEmpty()) return MENSAJE_SIN_CONTENIDO

        val terminos = extraerTerminos(consulta, aliases)
        if (terminos.isEmpty()) {
            return "Claro. Tengo contenido cultural verificado sobre estos temas:\n• " +
                disponibles.take(8).joinToString("\n• ") { it.titulo } +
                "\n¿Cuál te gustaría conocer?"
        }

        val mejor = disponibles.map { it to puntuar(it, terminos) }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
            ?.first
            ?: return "No encontré una coincidencia exacta para ese tema. Sí tengo información cultural verificada sobre:\n• " +
                disponibles.take(8).joinToString("\n• ") { it.titulo } +
                "\nEscribe el nombre de uno para revisarlo."

        return buildString {
            append("Encontré información verificada sobre ").append(mejor.titulo).append(":\n")
                .append(mejor.contenido)
            append("\nFuente: ").append(mejor.fuente.titulo)
            if (mejor.rangoPaginaInicio != null) {
                append(", pág. ").append(mejor.rangoPaginaInicio)
                mejor.rangoPaginaFin?.takeIf { it != mejor.rangoPaginaInicio }
                    ?.let { append("–").append(it) }
            }
            append("\n¿Quieres explorar otro aspecto de su cultura?")
        }
    }

    private fun extraerTerminos(consulta: String, aliases: List<String>): Set<String> {
        val ampliados = mutableSetOf<String>()
        consulta.split(Regex("[^a-z0-9]+"))
            .filter { it.length >= 4 && it !in PALABRAS_VACIAS && it !in aliases }
            .forEach { termino ->
                ampliados += termino
                SINONIMOS[termino]?.let(ampliados::add)
            }
        return ampliados
    }

    private fun puntuar(item: CulturaContenido, terminos: Set<String>): Int {
        val titulo = normalizar(item.titulo)
        val contenido = normalizar(item.contenido)
        val fuente = normalizar(item.fuente.titulo)
        return terminos.sumOf { termino ->
            (if (titulo.contains(termino)) 3 else 0) +
                (if (contenido.contains(termino)) 1 else 0) +
                (if (fuente.contains(termino)) 1 else 0)
        }
    }

    private fun normalizar(texto: String): String = Normalizer.normalize(
        texto.lowercase(), Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "")

    private companion object {
        const val MENSAJE_SIN_CONTENIDO =
            "Todavía no tengo contenido cultural verificado para responder esa pregunta."
        val ALIASES_IDIOMA = mapOf(
            1 to listOf("miskito", "miskitu"),
            2 to listOf("espanol"),
            3 to listOf("kriol", "creole", "criollo"),
            4 to listOf("ingles estandar")
        )
        val PALABRAS_CULTURA = listOf(
            "cultura", "historia", "origen", "geografia", "ubicacion", "vestuario",
            "ropa", "comida", "gastronomia", "musica", "danza", "tradicion",
            "costumbre", "religion", "creencia", "vivienda", "casa", "fiesta"
        )
        val PALABRAS_VACIAS = setOf(
            "cual", "como", "donde", "cuando", "quien", "quiero", "saber", "cuentame",
            "sobre", "cultura", "pueblo", "idioma", "lengua", "tienen", "tiene"
        )
        val SINONIMOS = mapOf(
            "visten" to "vestuario", "vestian" to "vestuario", "vestimenta" to "vestuario",
            "casas" to "habitaciones", "viven" to "habitaciones",
            "alimentos" to "comida", "comen" to "comida",
            "nacio" to "origen", "surgio" to "origen"
        )
    }
}
