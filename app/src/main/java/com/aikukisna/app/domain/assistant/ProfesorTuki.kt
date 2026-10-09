package com.aikukisna.app.domain.assistant

import com.aikukisna.app.domain.repository.ConocimientoDocumentado
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import com.aikukisna.app.domain.repository.FragmentoConocimiento
import com.aikukisna.app.domain.repository.TipoFragmento
import java.text.Normalizer
import javax.inject.Inject

/**
 * Tuki como profesor sin conexión: responde preguntas abiertas sobre el idioma con el material
 * documentado (reglas gramaticales, notas de pronunciación y cultura) en lugar de dejar que el
 * modelo pequeño invente. Si no encuentra respaldo, devuelve null y el asistente lo dice.
 */
class ProfesorTuki @Inject constructor(
    private val conocimiento: ConocimientoDocumentado,
    private val diccionario: DiccionarioRepository,
    private val preferencias: PreferenciasAprendizaje
) {
    /** ¿El estudiante pide una frase, expresión o conversación? */
    fun pideFrases(mensaje: String): Boolean =
        normalizar(mensaje).containsAny("frase", "expresion", "oracion", "conversacion", "dialogo", "como le digo", "como digo")

    /**
     * Frases y expresiones de las lecciones (oraciones del corpus) cuyo significado en español
     * trata del tema preguntado: "frase para pedir permiso", "expresiones para el mercado"...
     */
    suspend fun frasesDeLecciones(pregunta: String, idiomaId: Int, limite: Int = 4): String? {
        // Cada palabra de la pregunta es un grupo con sus sinónimos ("presentarme" → llamo, nombre...).
        val grupos = normalizar(pregunta).split(' ')
            .filter { it.length >= 4 && it !in VACIAS && it !in VACIAS_FRASES && PREFIJOS_IDIOMA.none(it::startsWith) }
            .map { (listOf(raizFrase(it)) + SINONIMOS_FRASES[raizFrase(it)].orEmpty()).toSet() }
            .distinct()
        if (grupos.isEmpty()) return null
        // Triples (texto en el idioma que aprende, explicación para el estudiante, texto en español para buscar).
        val apoyo = preferencias.lenguaApoyoId()
        // Puntaje: cuántos grupos cubre la frase; a igual número, gana la que cubre el primer tema pedido
        // ("presentarme en clase" debe dar presentaciones, no cualquier frase con la palabra "clase").
        val puntuadas = frasesIndexadas(idiomaId, apoyo).map { (par, raices) ->
            val cubiertos = grupos.indices.filter { i -> grupos[i].any { it in raices } }
            Triple(par, cubiertos.size, cubiertos.minOrNull() ?: Int.MAX_VALUE)
        }.filter { it.second > 0 }
        val mejor = puntuadas.maxOfOrNull { it.second } ?: return null
        val primerTema = puntuadas.filter { it.second == mejor }.minOf { it.third }
        // Solo las que comparten el máximo de términos: evita frases que coinciden en una palabra suelta.
        val elegidas = puntuadas.filter { it.second == mejor && it.third == primerTema }
            .map { it.first to it.second }
            .sortedBy { it.first.first.length }
            .map { it.first }
            .distinctBy { normalizar(it.first) }
            .take(limite)
        val idioma = IdiomasTuki.nombre(idiomaId)
        return buildString {
            append("Aquí tienes frases de las lecciones de $idioma sobre eso:")
            elegidas.forEach { (texto, explicacion, _) -> append("\n• ").append(texto).append(" — ").append(explicacion) }
            append("\nLéelas en voz alta y luego intenta cambiar una palabra. ¿Practicamos alguna?")
        }
    }

    /**
     * Frases del corpus con sus raíces ya calculadas. Cargarlas y normalizarlas en cada pregunta
     * hacía tardar a Tuki 12–19 s cuando no tenía una respuesta directa; se guardan unos minutos.
     */
    private suspend fun frasesIndexadas(idiomaId: Int, apoyo: Int): List<Pair<Triple<String, String, String>, Set<String>>> {
        val clave = "$idiomaId-$apoyo"
        cacheFrases[clave]?.takeIf { System.currentTimeMillis() - it.first < VIGENCIA_CACHE_MS }?.let { return it.second }
        val pares: List<Triple<String, String, String>> = if (idiomaId == ESPANOL) {
            // Quien aprende Español ve la frase en español con su traducción a la lengua de apoyo.
            (listOf(apoyo) + listOf(MISKITO, KRIOL, INGLES).filter { it != apoyo }).flatMap { otro ->
                diccionario.obtenerOracionesPorIdiomas(ESPANOL, otro).map { Triple(it.textoOrigen, it.textoDestino, it.textoOrigen) } +
                    diccionario.obtenerOracionesPorIdiomas(otro, ESPANOL).map { Triple(it.textoDestino, it.textoOrigen, it.textoDestino) }
            }
        } else {
            diccionario.obtenerOracionesPorIdiomas(idiomaId, ESPANOL).map { Triple(it.textoOrigen, it.textoDestino, it.textoDestino) } +
                diccionario.obtenerOracionesPorIdiomas(ESPANOL, idiomaId).map { Triple(it.textoDestino, it.textoOrigen, it.textoOrigen) }
        }
        return pares.map { par -> par to normalizar(par.third).split(' ').map(::raizFrase).toSet() }
            .also { cacheFrases[clave] = System.currentTimeMillis() to it }
    }

    /** Saludo del material verificado (oraciones 138–140 del corpus, lección 17); solo Miskitu lo tiene registrado. */
    private fun saludoEnIdioma(idiomaId: Int): String =
        if (idiomaId == 1) "En miskito se dice: ¡Naksa! ¿Nahki sma? (¡Hola! ¿Cómo estás?). " else ""

    private fun raizFrase(palabra: String): String = if (palabra.length > 5) palabra.take(5) else palabra

    /** Respuestas breves de cortesía y ánimo, como las de un profesor en clase. */
    /** [nombre] solo se consulta si el mensaje es de cortesía. */
    suspend fun cortesia(mensaje: String, idiomaId: Int, nombre: suspend () -> String?): String? {
        val plantilla = plantillaCortesia(mensaje, idiomaId) ?: return null
        val trato = nombre()?.takeIf(String::isNotBlank)?.let { ", $it" }.orEmpty()
        return plantilla.replace(TRATO, trato)
    }

    private fun plantillaCortesia(mensaje: String, idiomaId: Int): String? {
        val texto = normalizar(mensaje)
        val idioma = IdiomasTuki.nombre(idiomaId)
        val trato = TRATO
        val palabras = texto.split(' ').filter(String::isNotBlank)
        val corto = palabras.size <= 6
        return when {
            corto && palabras.any { it in SALUDOS } && texto.containsAny("como estas", "que tal", "como te va", "how are you") ->
                "¡Hola$trato! Estoy muy bien, gracias por preguntar. " + saludoEnIdioma(idiomaId) +
                    "¿Qué quieres aprender hoy de $idioma? Puedo enseñarte palabras, explicarte la gramática o contarte de la cultura."
            corto && (palabras.firstOrNull() in SALUDOS || texto in setOf("buenos dias", "buenas tardes", "buenas noches")) ->
                "¡Hola$trato! Qué bueno verte. ¿Practicamos $idioma? Pregúntame cómo se dice algo, " +
                    "pídeme que te explique una regla o que te cuente de la cultura."
            corto && texto.containsAny("como estas", "que tal estas", "como te va", "how are you") ->
                "Estoy muy bien$trato, gracias. ¿Y tú? Cuando quieras seguimos aprendiendo $idioma."
            corto && texto.containsAny("gracias", "muchas gracias", "te lo agradezco", "thank you", "thanks", "tingki") ->
                "¡Con mucho gusto$trato! Para eso estoy. ¿Seguimos con otra pregunta?"
            corto && texto.containsAny("adios", "hasta luego", "nos vemos", "hasta manana", "chao", "bye", "goodbye") ->
                "¡Hasta pronto$trato! Recuerda practicar un poquito cada día. Aquí te espero."
            texto.containsAny("cansad", "aburrid", "dificil", "no puedo", "no me sale", "me cuesta", "frustrad", "no entiendo nada") ->
                "Te entiendo$trato, aprender otro idioma cuesta y es normal sentirse así. Te propongo algo corto: " +
                    "repasemos 5 palabras de tu lección y una frase para usarlas. Pequeños pasos todos los días funcionan mejor que mucho de una vez. " +
                    "¿Empezamos? Pídeme \"háblame en $idioma\" o pregúntame cómo se dice una palabra."
            // Comentarios sobre el propio Tuki: no son preguntas de idioma ni deben buscarse en el material.
            corto && texto.containsAny("tardaste", "tardas", "tarda mucho", "demoraste", "demoras", "eres lento", "muy lento",
                "por que tardaste", "tanto tiempo") ->
                "Perdona la espera$trato. Sin internet preparo las respuestas con mi material guardado en el teléfono, " +
                    "y a veces tardo unos segundos. ¿Qué quieres aprender de $idioma?"
            corto && texto.containsAny("no entendi", "no te entiendo", "que dijiste", "no tiene sentido", "eso no te pregunte",
                "no te pregunte eso", "eso no es lo que") ->
                "Perdona$trato, creo que no te entendí bien. ¿Me lo preguntas de otra forma? Por ejemplo: " +
                    "\"¿cómo se dice casa en $idioma?\" o \"explícame el plural\"."
            corto && texto.containsAny("eres genial", "te quiero", "eres el mejor", "me caes bien", "buen trabajo") ->
                "¡Gracias$trato! A mí también me gusta aprender contigo. ¿Qué practicamos ahora?"
            else -> null
        }
    }

    /** Explicación respaldada por el material documentado, o null si no hay nada pertinente. */
    suspend fun explicar(pregunta: String, idiomaId: Int): String? {
        val terminos = terminos(pregunta)
        if (terminos.isEmpty()) return null
        val (fragmentos, indices) = fragmentosIndexados(idiomaId)
        val aliasIdioma = ALIAS_IDIOMA[idiomaId].orEmpty()
        // Un término que aparece en pocos fragmentos (p. ej. "sikro") identifica el tema aunque no esté en el título.
        val frecuencia = terminos.associateWith { t -> indices.count { t in it.titulo || t in it.cuerpo } }
        val puntuados = fragmentos.mapIndexed { i, fragmento ->
            // La cultura es común a los cuatro idiomas; un texto sobre los verbos del miskito no
            // responde "¿qué es un verbo?" a quien aprende inglés.
            if (fragmento.tipo == TipoFragmento.CULTURA && hablaDeOtroIdioma(fragmento, idiomaId)) {
                return@mapIndexed fragmento to 0
            }
            var puntaje = puntuar(indices[i], terminos, frecuencia)
            // La cultura se comparte entre idiomas: se prefiere la que habla del pueblo del idioma.
            if (fragmento.tipo == TipoFragmento.CULTURA && puntaje > 0 &&
                aliasIdioma.any { normalizar("${fragmento.titulo} ${fragmento.texto}").contains(it) }
            ) puntaje += 1
            fragmento to puntaje
        }.filter { it.second >= PUNTAJE_MINIMO }
            // A igual puntaje, una regla del idioma explica mejor que un texto cultural general.
            .sortedWith(compareByDescending<Pair<FragmentoConocimiento, Int>> { it.second }.thenBy { it.first.tipo.ordinal })
        val mejor = puntuados.firstOrNull() ?: return null
        val relacionados = puntuados.drop(1)
            .filter { it.first.tipo == mejor.first.tipo && it.second >= mejor.second - 1 }
            .take(2).map { it.first.titulo }
        return componer(mejor.first, idiomaId, relacionados)
    }

    private fun hablaDeOtroIdioma(fragmento: FragmentoConocimiento, idiomaId: Int): Boolean {
        // "Inglés Kriol" no es inglés estándar.
        val texto = normalizar("${fragmento.titulo} ${fragmento.texto}").replace("ingles kriol", "kriol")
        val propio = ALIAS_IDIOMA[idiomaId].orEmpty().any { texto.contains(it) }
        val ajeno = ALIAS_IDIOMA.filterKeys { it != idiomaId }.values.flatten().any { texto.contains(it) }
        return ajeno && !propio
    }

    /** Temas documentados que Tuki puede enseñar, para sugerir cuando no hay respaldo. */
    suspend fun temasSugeridos(idiomaId: Int, cantidad: Int = 3): List<String> =
        conocimiento.fragmentos(idiomaId)
            .filter { it.tipo == TipoFragmento.GRAMATICA }
            .map { it.titulo }
            .shuffled()
            .take(cantidad)

    private fun componer(fragmento: FragmentoConocimiento, idiomaId: Int, relacionados: List<String>): String {
        val idioma = IdiomasTuki.nombre(idiomaId)
        return buildString {
            when (fragmento.tipo) {
                TipoFragmento.GRAMATICA -> {
                    append("Te explico «").append(fragmento.titulo).append("» en ").append(idioma).append(": ")
                    append(fragmento.texto.trim().let { if (it.endsWith('.')) it else "$it." })
                    if (fragmento.ejemplos.isNotEmpty()) {
                        append("\nEjemplos:")
                        fragmento.ejemplos.forEach { append("\n• ").append(it) }
                    }
                    append("\n¿Quieres que practiquemos con otra palabra?")
                }
                TipoFragmento.PRONUNCIACION -> {
                    append("Sobre la pronunciación y la escritura en ").append(idioma).append(": ")
                    append(fragmento.texto.trim().let { if (it.endsWith('.')) it else "$it." })
                    if (fragmento.ejemplos.isNotEmpty()) append("\nEjemplo: ").append(fragmento.ejemplos.first())
                    append("\nUn buen ejercicio: escucha la palabra en el diccionario, repítela despacio y luego a ritmo normal.")
                }
                TipoFragmento.CULTURA -> {
                    append(fragmento.texto.trim())
                    fragmento.fuente?.let { append("\nFuente: ").append(it) }
                    append("\n¿Quieres que te cuente algo más?")
                }
            }
            if (relacionados.isNotEmpty()) {
                append("\nTambién puedo explicarte: ").append(relacionados.joinToString(" y ") { "«$it»" }).append('.')
            }
        }
    }

    private data class Indice(val titulo: Set<String>, val cuerpo: Set<String>, val inicio: Set<String>)

    /**
     * Título: 3 puntos. Cuerpo: 1 punto, o 3 si el término es raro y aparece en la primera oración
     * (el tema del texto, como "sikro"); así "capital" en una nota al margen no basta.
     */
    private fun puntuar(indice: Indice, terminos: Set<String>, frecuencia: Map<String, Int>): Int =
        terminos.sumOf { termino ->
            val raro = (frecuencia[termino] ?: 0) in 1..TERMINO_RARO
            (if (termino in indice.titulo) 3 else 0) +
                (if (termino in indice.cuerpo) (if (raro && termino in indice.inicio) 3 else 1) else 0)
        }

    private fun primeraOracion(texto: String): String = texto.split(Regex("(?<=[.!?])\\s+")).firstOrNull().orEmpty()

    /** Raíces de los términos de la pregunta, ampliadas con sinónimos del vocabulario gramatical. */
    private fun terminos(pregunta: String): Set<String> {
        val base = normalizar(pregunta).split(' ')
            .filter { it.length >= 3 && it !in VACIAS && PREFIJOS_IDIOMA.none(it::startsWith) }
        return base.flatMap { palabra -> listOf(raiz(palabra)) + SINONIMOS[raiz(palabra)].orEmpty() }.toSet()
    }

    private fun raices(texto: String): Set<String> =
        normalizar(texto).split(' ').filter { it.length >= 2 }.map(::raiz).toSet()

    private fun raiz(palabra: String): String = if (palabra.length > 6) palabra.take(6) else palabra

    private fun String.containsAny(vararg valores: String) = valores.any(::contains)

    private fun normalizar(texto: String): String = Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

    /** Igual que con las frases: el material y sus raíces se calculan una vez cada pocos minutos. */
    private suspend fun fragmentosIndexados(idiomaId: Int): Pair<List<FragmentoConocimiento>, List<Indice>> {
        cacheFragmentos[idiomaId]?.takeIf { System.currentTimeMillis() - it.first < VIGENCIA_CACHE_MS }?.let { return it.second }
        val fragmentos = conocimiento.fragmentos(idiomaId)
        val indices = fragmentos.map {
            Indice(raices(it.titulo), raices("${it.texto} ${it.ejemplos.joinToString(" ")}"), raices(primeraOracion(it.texto)))
        }
        return (fragmentos to indices).also { cacheFragmentos[idiomaId] = System.currentTimeMillis() to it }
    }

    private companion object {
        private const val TRATO = "{{trato}}"
        const val VIGENCIA_CACHE_MS = 10 * 60 * 1000L
        val cacheFrases = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, List<Pair<Triple<String, String, String>, Set<String>>>>>()
        val cacheFragmentos = java.util.concurrent.ConcurrentHashMap<Int, Pair<Long, Pair<List<FragmentoConocimiento>, List<Indice>>>>()
        const val MISKITO = 1
        const val ESPANOL = 2
        const val KRIOL = 3
        const val INGLES = 4

        val VACIAS_FRASES = setOf(
            "frase", "frases", "expresion", "expresiones", "oracion", "oraciones", "conversacion", "dialogo",
            "dame", "algo", "ejemplo", "ejemplos", "mismo", "puedo", "podria", "alguna", "algunas",
            // Verbos genéricos de la petición ("frases para hablar de…"): no indican el tema.
            "hablar", "habla", "tener", "hacer", "estar", "pedir", "quiero", "necesito", "aprender", "practicar"
        )
        val SINONIMOS_FRASES = mapOf(
            "salud" to listOf("buen", "hola", "noche", "tarde", "dias"),
            "despe" to listOf("adios", "hasta", "luego"),
            "agrad" to listOf("graci"),
            "perdo" to listOf("discu", "perdó"),
            "compr" to listOf("cuest", "preci", "merca"),
            "prese" to listOf("llamo", "nombr", "gusto", "conoc"),
            "permi" to listOf("favor", "pasar", "perdo")
        )

        const val PUNTAJE_MINIMO = 3
        const val TERMINO_RARO = 3

        /** El nombre del idioma aparece en casi todo el material: no sirve para elegir el tema. */
        val PREFIJOS_IDIOMA = listOf("miskit", "kriol", "creol", "ingles", "espano")

        val SALUDOS = setOf("hola", "holi", "buenas", "saludos", "hey", "naksa", "buenos", "hello", "hi")

        val ALIAS_IDIOMA = mapOf(
            1 to listOf("miskit"),
            2 to listOf("espanol"),
            3 to listOf("kriol", "creole"),
            4 to listOf("ingles")
        )

        val VACIAS = setOf(
            "que", "como", "cual", "cuales", "cuando", "donde", "quien", "por", "para", "porque",
            "una", "uno", "unos", "unas", "los", "las", "del", "con", "sin", "sobre", "entre", "hay", "son",
            "esta", "este", "esto", "estos", "estas", "ese", "esa", "eso", "mas", "muy", "pero", "tambien",
            "dime", "explica", "explicame", "ensename", "puedes", "podrias", "quiero", "saber", "tengo", "tiene",
            "hace", "hacer", "usa", "usar", "uso", "forma", "formar", "dice", "decir", "palabra", "palabras",
            "idioma", "lengua", "miskito", "miskitu", "kriol", "creole", "ingles", "espanol", "tuki", "profe",
            "diferencia", "significa", "funciona", "ayuda", "ayudas", "solo", "tres",
            // Palabras comunes que no identifican un tema: "porque tardaste tanto" respondía con el texto del
            // sukia porque contiene "capaz tanto de curar".
            "tanto", "tanta", "tantos", "tantas", "mucho", "mucha", "muchos", "muchas", "poco", "poca", "pocos",
            "siempre", "nunca", "ahora", "aqui", "alli", "entonces", "despues", "antes", "todavia", "bien", "mal",
            "algo", "nada", "todo", "toda", "todos", "todas", "cosa", "cosas", "vez", "veces", "hoy", "ayer",
            "eres", "estas", "soy", "tienes", "tiene", "pueden", "puede", "cuanto",
            "tardaste", "tardas", "tarda", "demoras", "demoraste", "lento", "rapido", "favor", "porfa", "oye", "mira"
        )

        /** Términos con que los estudiantes preguntan y cómo aparecen en las reglas documentadas. */
        val SINONIMOS = mapOf(
            "plural" to listOf("nani", "dem"),
            "conjug" to listOf("verbo", "verbal", "infini", "presen", "pasado", "futuro"),
            "verbos" to listOf("verbo", "verbal", "infini"),
            "verbo" to listOf("verbal", "infini"),
            "pasado" to listOf("past", "pasad"),
            "futuro" to listOf("future"),
            "negar" to listOf("negaci", "negati"),
            "negaci" to listOf("negati"),
            "pregun" to listOf("interr", "questi"),
            "vocale" to listOf("vocal", "fonema", "vocali"),
            "vocal" to listOf("fonema", "vocali"),
            "pronun" to listOf("fonema", "sonido", "vocal", "acento", "circun"),
            "sonido" to listOf("fonema", "pronun"),
            "orden" to listOf("sujeto", "objeto"),
            "oracio" to listOf("sujeto", "orden"),
            "pronom" to listOf("pronou"),
            "articu" to listOf("articl"),
            "posesi" to listOf("geniti"),
            "tilde" to listOf("acento", "acentu"),
            "acento" to listOf("tilde", "acentu"),
            
            "obliga" to listOf("hafu", "mos"),
            "capaci" to listOf("kyan", "kan"),
            "viven" to listOf("ubicac", "geogra", "region", "habita"),
            "vive" to listOf("ubicac", "geogra", "region", "habita"),
            // "¿Quiénes son los miskitos?" pregunta por el pueblo: origen, región, dónde viven. (Una sola entrada:
            // en mapOf una clave repetida reemplaza a la anterior sin aviso.)
            "quiene" to listOf("pueblo", "poblac", "grupos", "viven", "origen", "region", "ubicac", "geogra")
        )
    }
}
