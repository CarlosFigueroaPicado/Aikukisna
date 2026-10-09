package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.dao.ConocimientoLinguisticoDao
import com.aikukisna.app.data.local.dao.PalabraDao
import com.aikukisna.app.domain.conocimiento.Acepcion
import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.EstadoValidacion
import com.aikukisna.app.domain.conocimiento.FraseVerificada
import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.conocimiento.OpcionTraduccion
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import javax.inject.Inject

class RepositorioConocimientoImpl @Inject constructor(
    private val dictionary: DiccionarioRepository,
    private val words: PalabraDao,
    private val conocimiento: ConocimientoLinguisticoDao
) : RepositorioConocimiento {

    override suspend fun buscarPalabra(texto: String, idiomaId: Int): ResultadoBusquedaConocimiento {
        val normalized = NormalizadorLinguistico.normalizar(texto)
        if (normalized.isBlank()) return ResultadoBusquedaConocimiento.NoEncontrada

        words.buscarExactas(normalized, idiomaId).firstNotNullOfOrNull { dictionary.obtenerPalabraPorId(it.id) }
            ?.let { return ResultadoBusquedaConocimiento.Exacta(it) }

        for (variant in conocimiento.buscarVariantesExactas(normalized)) {
            val canonical = dictionary.obtenerPalabraPorId(variant.palabraId) ?: continue
            if (canonical.idioma.id == idiomaId && variant.estadoValidacion != EstadoValidacion.RECHAZADA.value) {
                return ResultadoBusquedaConocimiento.Exacta(canonical, variant.texto)
            }
        }

        val iniciales = variantesInicial(normalized.first())
        // El diccionario guarda las tildes: "pajaro" escrito sin tilde es la misma palabra que "pájaro".
        val sinTildes = quitarTildes(normalized)
        iniciales.flatMap { words.candidatasPorLongitud(it.toString(), idiomaId, normalized.length) }
            .firstOrNull { quitarTildes(it.textoNormalizado) == sinTildes }
            ?.let { dictionary.obtenerPalabraPorId(it.id) }
            ?.let { return ResultadoBusquedaConocimiento.Exacta(it) }
        val suggestions = iniciales.flatMap { words.candidatas(it.toString(), idiomaId, 200) }
            .mapNotNull { dictionary.obtenerPalabraPorId(it.id) }
            .map { it to levenshtein(normalized, NormalizadorLinguistico.normalizar(it.texto)) }
            .filter { (_, distance) -> distance <= maxOf(2, normalized.length / 3) }
            .sortedWith(compareBy<Pair<Palabra, Int>> { it.second }.thenBy { it.first.texto })
            .map { it.first }
            .distinctBy { it.id }
            .take(5)
        return if (suggestions.isEmpty()) ResultadoBusquedaConocimiento.NoEncontrada
        else ResultadoBusquedaConocimiento.Sugerencias(suggestions)
    }

    override suspend fun obtenerEntrada(palabraId: Int, idiomaDestinoId: Int?): EntradaConocimiento? {
        val word = dictionary.obtenerPalabraPorId(palabraId) ?: return null
        val senses = conocimiento.obtenerAcepciones(palabraId).map { it.toDomain() }
        val senseTranslations = senses.flatMap { sense ->
            conocimiento.obtenerRelacionesAcepcion(sense.id).mapNotNull { link ->
                val targetSenseId = when (sense.id) {
                    link.acepcionOrigenId -> link.acepcionDestinoId
                    link.acepcionDestinoId -> link.acepcionOrigenId
                    else -> return@mapNotNull null
                }
                val targetSense = conocimiento.obtenerAcepcion(targetSenseId) ?: return@mapNotNull null
                val targetWord = dictionary.obtenerPalabraPorId(targetSense.palabraId) ?: return@mapNotNull null
                if (idiomaDestinoId != null && targetWord.idioma.id != idiomaDestinoId) return@mapNotNull null
                OpcionTraduccion(
                    palabra = targetWord,
                    acepcionOrigen = sense,
                    acepcionDestino = targetSense.toDomain(),
                    contexto = sense.contexto?.takeUnless(::esNotaTecnica),
                    estado = estado(link.estadoValidacion)
                )
            }
        }
        val traduccionesVigentes = senseTranslations.filter { it.estado != EstadoValidacion.RECHAZADA }
        val traduccionesPreferidas = traduccionesVigentes.maxOfOrNull { prioridad(it.estado) }?.let { prioridadMaxima ->
            traduccionesVigentes.filter { prioridad(it.estado) == prioridadMaxima }
        }.orEmpty()
        val translations = if (traduccionesPreferidas.isNotEmpty()) traduccionesPreferidas else {
            dictionary.obtenerTraducciones(palabraId)
                .map { it.palabraDestino }
                .filter { idiomaDestinoId == null || it.idioma.id == idiomaDestinoId }
                .distinctBy { NormalizadorLinguistico.normalizar(it.texto) }
                .map { OpcionTraduccion(it) }
        }
        val examples = if (idiomaDestinoId == null) emptyList() else {
            dictionary.obtenerOracionesPorIdiomas(word.idioma.id, idiomaDestinoId).filter {
                val needle = NormalizadorLinguistico.normalizar(word.texto)
                NormalizadorLinguistico.normalizar(it.textoOrigen).contains(needle) ||
                    NormalizadorLinguistico.normalizar(it.textoDestino).contains(needle)
            }.take(10)
        }
        return EntradaConocimiento(word, senses, translations, examples)
    }

    override suspend fun resolverTraduccion(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): ResolucionTraduccion {
        val directa = resolverDirecta(texto, idiomaOrigenId, idiomaDestinoId)
        if (directa !is ResolucionTraduccion.NoEncontrada && directa !is ResolucionTraduccion.Sugerencia) return directa
        // El corpus enlaza cada idioma con el español (y el Kriol con el inglés), pero no existen
        // pares directos Miskito ↔ Inglés ni Miskito ↔ Kriol: se pasa por un idioma puente.
        return resolverPorPuente(texto, idiomaOrigenId, idiomaDestinoId) ?: directa
    }

    private suspend fun resolverPorPuente(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): ResolucionTraduccion? {
        for (puenteId in IDIOMAS_PUENTE) {
            if (puenteId == idiomaOrigenId || puenteId == idiomaDestinoId) continue
            val intermedios = textosResueltos(resolverDirecta(texto, idiomaOrigenId, puenteId)).take(MAX_INTERMEDIOS)
            val nombrePuente = NOMBRES_PUENTE.getValue(puenteId)
            var expresion: ResolucionTraduccion.Expresion? = null
            val opciones = intermedios.flatMap { intermedio ->
                val contexto = "vía $nombrePuente: $intermedio"
                when (val final = resolverDirecta(intermedio, puenteId, idiomaDestinoId)) {
                    is ResolucionTraduccion.Expresion -> { expresion = expresion ?: final; emptyList() }
                    is ResolucionTraduccion.Unica -> listOf(final.opcion.copy(contexto = contexto))
                    is ResolucionTraduccion.Ambigua -> final.opciones.map { it.copy(contexto = contexto) }
                    else -> emptyList()
                }
            }.distinctBy { NormalizadorLinguistico.normalizar(it.palabra.texto) }
            if (opciones.size == 1) return ResolucionTraduccion.Unica(opciones.single())
            if (opciones.size > 1) return ResolucionTraduccion.Ambigua(opciones.take(MAX_OPCIONES_PUENTE))
            expresion?.let { return it }
        }
        return null
    }

    override suspend fun buscarFrases(
        idiomaId: Int,
        idiomaTraduccionId: Int,
        palabra: String?,
        limite: Int
    ): List<FraseVerificada> {
        val needle = palabra?.let(NormalizadorLinguistico::normalizar)?.takeIf(String::isNotBlank)
        val candidatas = if (needle != null) conocimiento.buscarFrasesConPalabra(needle, idiomaId, MAX_CANDIDATAS_FRASE)
        else conocimiento.frasesAlAzar(idiomaId, MAX_CANDIDATAS_FRASE)
        val deExpresiones = candidatas.mapNotNull { expresion ->
            val destino = conocimiento.obtenerRelacionesExpresion(expresion.id).firstNotNullOfOrNull { link ->
                val targetId = if (link.expresionOrigenId == expresion.id) link.expresionDestinoId else link.expresionOrigenId
                conocimiento.obtenerExpresion(targetId)?.takeIf { it.idiomaId == idiomaTraduccionId }
            } ?: return@mapNotNull null
            FraseVerificada(expresion.texto, destino.texto, idiomaTraduccionId)
        }
        val deOraciones = dictionary.obtenerOracionesPorIdiomas(idiomaId, idiomaTraduccionId).mapNotNull {
            val (texto, traduccion) = if (it.idiomaOrigenId == idiomaId) it.textoOrigen to it.textoDestino
            else it.textoDestino to it.textoOrigen
            FraseVerificada(texto, traduccion, idiomaTraduccionId).takeIf {
                needle == null || " ${NormalizadorLinguistico.normalizar(texto)} ".contains(" $needle ")
            }
        }.let { if (needle == null) it else it.sortedBy { frase -> frase.texto.length } }
        return (deExpresiones + deOraciones)
            .filter { it.texto.length in 3..120 }
            .distinctBy { NormalizadorLinguistico.normalizar(it.texto) }
            .let { if (needle == null) it.shuffled() else it }
            .take(limite)
    }

    private fun textosResueltos(resolucion: ResolucionTraduccion): List<String> = when (resolucion) {
        is ResolucionTraduccion.Expresion -> listOf(resolucion.texto)
        is ResolucionTraduccion.Unica -> listOf(resolucion.opcion.palabra.texto)
        is ResolucionTraduccion.Ambigua -> resolucion.opciones.map { it.palabra.texto }
        else -> emptyList()
    }

    private suspend fun resolverDirecta(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): ResolucionTraduccion {
        val normalized = NormalizadorLinguistico.normalizar(texto)
        dictionary.buscarOracionExacta(texto, idiomaOrigenId, idiomaDestinoId)?.let {
            return ResolucionTraduccion.Expresion(it, EstadoValidacion.IMPORTADA)
        }
        val expressions = conocimiento.buscarExpresionesExactas(normalized, idiomaOrigenId)
        for (expression in expressions) {
            val pares = conocimiento.obtenerRelacionesExpresion(expression.id).mapNotNull { link ->
                val targetId = when (expression.id) {
                    link.expresionOrigenId -> link.expresionDestinoId
                    link.expresionDestinoId -> link.expresionOrigenId
                    else -> return@mapNotNull null
                }
                conocimiento.obtenerExpresion(targetId)?.takeIf { it.idiomaId == idiomaDestinoId }
                    ?.let { link to it }
            }.distinctBy { (_, target) -> NormalizadorLinguistico.normalizar(target.texto) }
            if (pares.size == 1) {
                val (link, target) = pares.single()
                return ResolucionTraduccion.Expresion(target.texto, estado(link.estadoValidacion))
            }
        }

        return when (val lookup = buscarPalabra(texto, idiomaOrigenId)) {
            is ResultadoBusquedaConocimiento.Exacta -> {
                // Varias entradas pueden compartir el mismo texto ("¡buenos días!" del diccionario Miskito
                // y "Buenos días" de una lección con Kriol): se reúnen las traducciones de todas.
                val candidatos = (listOf(lookup.palabra.id to lookup.palabra.texto) +
                    words.buscarExactas(NormalizadorLinguistico.normalizar(lookup.palabra.texto), idiomaOrigenId)
                        .map { it.id to it.texto })
                    .distinctBy { it.first }
                suspend fun traduccionesDe(ids: List<Int>) = ids.flatMap { obtenerEntrada(it, idiomaDestinoId)?.traducciones.orEmpty() }
                    .filter { it.estado != EstadoValidacion.RECHAZADA }
                // La normalización junta palabras distintas: "man" (tú) y "‘man" (apenas, sin) quedan iguales.
                // Si alguna entrada se escribe exactamente como lo pidió el estudiante, mandan sus traducciones.
                val escrito = formaEscrita(texto)
                val exactos = candidatos.filter { formaEscrita(it.second) == escrito }.map { it.first }
                val todas = traduccionesDe(exactos).ifEmpty { traduccionesDe(candidatos.map { it.first }) }
                // Cada homógrafo se filtra por separado: al juntarlos se vuelve a dejar solo lo mejor
                // validado ("Good morning" de la lección antes que "goodmorning" en revisión).
                val mejorPrioridad = todas.maxOfOrNull { prioridad(it.estado) }
                val options = todas.filter { prioridad(it.estado) == mejorPrioridad }
                    .map { it.copy(palabra = it.palabra.copy(texto = limpiarEntrada(it.palabra.texto))) }
                    .filter { it.palabra.texto.isNotBlank() }
                    // "pus", "pús" y "pusi (pús)" son la misma entrada con variantes de escritura.
                    .distinctBy { claveEntrada(it.palabra.texto) }
                    .take(MAX_OPCIONES)
                when (options.size) {
                    0 -> ResolucionTraduccion.NoEncontrada
                    1 -> ResolucionTraduccion.Unica(options.single())
                    else -> ResolucionTraduccion.Ambigua(options)
                }
            }
            is ResultadoBusquedaConocimiento.Sugerencias -> ResolucionTraduccion.Sugerencia(lookup.palabras.first())
            ResultadoBusquedaConocimiento.NoEncontrada -> ResolucionTraduccion.NoEncontrada
        }
    }

    private fun com.aikukisna.app.data.local.entity.AcepcionEntity.toDomain() = Acepcion(
        id, palabraId, numeroAcepcion, definicion, contexto, categoriaGramatical, estado(estadoValidacion)
    )

    private fun estado(value: String): EstadoValidacion = EstadoValidacion.entries
        .firstOrNull { it.value == value } ?: EstadoValidacion.PENDIENTE_REVISION

    private fun prioridad(estado: EstadoValidacion): Int = when (estado) {
        EstadoValidacion.VALIDADA -> 4
        EstadoValidacion.DOCUMENTADA -> 3
        EstadoValidacion.IMPORTADA -> 2
        EstadoValidacion.PENDIENTE_REVISION -> 1
        EstadoValidacion.RECHAZADA -> 0
    }

    private fun quitarTildes(texto: String): String =
        java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")

    /** "a" y "á" inician palabras distintas para el índice: se buscan ambas. */
    private fun variantesInicial(letra: Char): List<Char> {
        val base = quitarTildes(letra.toString()).firstOrNull() ?: letra
        return (listOf(letra, base) + VARIANTES_TILDE[base].orEmpty()).distinct()
    }

    /** Notas internas del proceso de migración que no deben llegar al estudiante. */
    private fun esNotaTecnica(contexto: String): Boolean {
        val valor = NormalizadorLinguistico.normalizar(contexto)
        return valor.contains("acepcion tecnica") || valor.contains("compatibilidad") ||
            valor.contains("relaciones heredadas") || valor.contains("migrada")
    }

    /** Quita la puntuación sobrante de las entradas importadas ("utla." → "utla"). */
    private fun limpiarEntrada(texto: String): String {
        var valor = texto.trim().trimEnd('.', ',', ';', ':').trim()
        if (valor.count { it == ')' } > valor.count { it == '(' }) valor = valor.replace(")", "").trim()
        return valor
    }

    /** Como se escribió, sin mayúsculas ni puntuación final ("Man." → "man"), pero conservando tildes y apóstrofos. */
    private fun formaEscrita(texto: String): String =
        limpiarEntrada(texto).trim('¿', '?', '¡', '!', ' ').lowercase()

    // "lî" y "Li" son la misma palabra con y sin la marca de vocal larga: se muestra solo la primera.
    private fun claveEntrada(texto: String): String =
        quitarTildes(NormalizadorLinguistico.normalizar(texto.substringBefore('('))).replace(Regex("[^\\p{L}\\p{N} ]"), "").trim()

    private companion object {
        const val MAX_OPCIONES = 4
        val VARIANTES_TILDE = mapOf(
            'a' to listOf('á'), 'e' to listOf('é'), 'i' to listOf('í'),
            'o' to listOf('ó'), 'u' to listOf('ú', 'ü'), 'n' to listOf('ñ')
        )

        /** Español primero: es el idioma con más enlaces; el inglés cubre Kriol ↔ Inglés. */
        val IDIOMAS_PUENTE = listOf(2, 4)
        val NOMBRES_PUENTE = mapOf(2 to "español", 4 to "inglés")
        const val MAX_INTERMEDIOS = 3
        const val MAX_OPCIONES_PUENTE = 6
        const val MAX_CANDIDATAS_FRASE = 30
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        var previous = IntArray(b.length + 1) { it }
        for (i in a.indices) {
            val current = IntArray(b.length + 1)
            current[0] = i + 1
            for (j in b.indices) {
                current[j + 1] = minOf(
                    current[j] + 1,
                    previous[j + 1] + 1,
                    previous[j] + if (a[i] == b[j]) 0 else 1
                )
            }
            previous = current
        }
        return previous[b.length]
    }
}
