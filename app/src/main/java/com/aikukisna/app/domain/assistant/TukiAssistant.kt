package com.aikukisna.app.domain.assistant

import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.repository.ContextoConversacionTuki
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.FraseVerificada
import com.aikukisna.app.domain.repository.MemoriaTukiStore
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.usecase.ConsultarCulturaTukiUseCase
import com.aikukisna.app.domain.usecase.TraducirOracionUseCase
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.domain.gramatica.MotorGramaticalControlado
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import java.text.Normalizer
import java.util.UUID
import javax.inject.Inject

class TukiAssistant @Inject constructor(
    private val classifier: TukiIntentClassifier,
    private val memory: MemoriaTukiStore,
    private val studentContextProvider: StudentContextProvider,
    private val conocimiento: RepositorioConocimiento,
    private val culture: ConsultarCulturaTukiUseCase,
    private val localEngine: LocalAssistantEngine,
    private val onDeviceEngine: OnDeviceAssistantEngine,
    private val remoteEngine: RemoteAssistantEngine,
    private val network: NetworkAvailability,
    private val diccionario: DiccionarioRepository,
    private val traducirOracion: TraducirOracionUseCase,
    private val gramatica: MotorGramaticalControlado,
    private val lecciones: LeccionRepository,
    private val profesor: ProfesorTuki,
    private val preferencias: PreferenciasAprendizaje
) {
    suspend fun recentHistory(userId: UUID?, languageId: Int?): List<MensajeChat> =
        userId?.let { memory.obtenerMensajesRecientes(it, languageId) }.orEmpty()

    suspend fun respond(
        history: List<MensajeChat>,
        languageId: Int,
        userId: UUID?
    ): String {
        val message = history.last().texto.trim()
        val previous = userId?.let { memory.obtenerContexto(it) }
        // "¿y gato?" después de "¿cómo se dice perro?" repite la consulta anterior con otra palabra.
        val ellipsis = ellipticalFollowUp(message, previous?.ultimaIntencion)
        val intent = ellipsis?.first ?: classifier.classify(message, previous?.ultimaPalabraOFrase != null)
        val subject = ellipsis?.second ?: extractSubject(message, intent) ?: previous?.ultimaPalabraOFrase
        // Perfil, progreso, logros y próxima lección solo se consultan si la pregunta los necesita;
        // antes se calculaban en cada mensaje, incluso para un simple "hola".
        var studentCache: StudentContext? = null
        var studentLoaded = false
        val student: suspend () -> StudentContext? = {
            if (!studentLoaded) {
                studentCache = studentContextProvider.get(userId, languageId)
                studentLoaded = true
            }
            studentCache
        }

        val otherLanguages = if (intent in LANGUAGE_NEUTRAL_INTENTS) emptySet()
        // Quien aprende español pregunta por su lengua materna ("¿cómo se dice agua en miskito?"):
        // el Miskito y el Kriol son su apoyo, no otro curso.
        else IdiomasTuki.otrosIdiomasPedidos(message, languageId)
            .let { if (languageId == SPANISH_ID) it - IdiomasTuki.MISKITO - IdiomasTuki.KRIOL else it }
        val nativeAsked = if (languageId == SPANISH_ID) {
            IdiomasTuki.mencionados(message).firstOrNull { it == IdiomasTuki.MISKITO || it == IdiomasTuki.KRIOL }
        } else null

        val direct = if (otherLanguages.isNotEmpty()) {
            IdiomasTuki.redirigirAOtroIdioma(languageId, otherLanguages)
        } else if (asksForGreetings(message)) {
            greetingsAnswer(languageId)
        } else if (comparedWords(message) != null) {
            val (primera, segunda) = comparedWords(message)!!
            differenceAnswer(primera, segunda, languageId)
        } else when (intent) {
            TukiIntent.SELECTED_LANGUAGE, TukiIntent.XP_HELP, TukiIntent.TUTOR_HELP ->
                functionalAnswer(intent, student()?.languageName)
            TukiIntent.AVAILABLE_LANGUAGES -> IdiomasTuki.idiomasDisponibles(languageId)
            TukiIntent.CLARIFY -> guidance(languageId, "Perdón si no fui claro. Te lo pongo fácil:")
            TukiIntent.COMPLAINT -> guidance(languageId, "Tienes razón, perdón por la respuesta confusa. Así sí puedo ayudarte bien:")
            TukiIntent.VERIFY_ANSWER -> verifyPrevious(history)
            TukiIntent.DEFINE_WORD -> subject?.let {
                // En español, "¿qué significa escuela?" se explica en la lengua de apoyo, no "escuela = escuela".
                if (languageId == SPANISH_ID) translationAnswer(it, SPANISH_ID, nativeAsked ?: preferencias.lenguaApoyoId(), true)
                else translationAnswer(it, languageId, SPANISH_ID, true)
            }
            TukiIntent.TRANSLATE -> if (languageId == SPANISH_ID) subject?.let {
                if (nativeAsked != null) translationAnswer(it, SPANISH_ID, nativeAsked, false) else spanishLearnerTranslation(it)
            } else subject?.let {
                val haciaEspanol = languageId != SPANISH_ID && IdiomasTuki.ESPANOL in IdiomasTuki.mencionados(message)
                if (haciaEspanol) translationAnswer(it, languageId, SPANISH_ID, false)
                else translationAnswer(it, SPANISH_ID, languageId, false)
            }
            TukiIntent.PRONUNCIATION -> subject?.let { pronunciation(it, languageId) }
            TukiIntent.EXAMPLE -> subject?.let { example(it, languageId) }
            TukiIntent.STUDENT_PROGRESS -> student()?.let(::progressAnswer)
                ?: "No encuentro un perfil local con el que pueda consultar tu progreso."
            TukiIntent.LESSON_HELP -> student()?.let(::lessonAnswer)
                ?: "No encuentro un perfil local con el que pueda revisar tus lecciones."
            TukiIntent.APP_HELP -> student()?.let { appHelpAnswer(message, it) }
                ?: appHelpAnswer(message, null)
            TukiIntent.CULTURE -> culture(message, languageId)
            TukiIntent.PRACTICE_PHRASES -> practicePhrases(languageId, student()?.languageName)
            else -> null
        }

        val localRequest = AssistantRequest(history, explicitFollowUp(message, intent, subject), languageId)
        val answer = direct ?: if (intent in OPEN_INTENTS || intent in TEACHING_INTENTS) {
            openAnswer(message, languageId, intent, userId, student, previous, localRequest)
        } else {
            localEngine.answer(localRequest).text
        }

        if (userId != null) {
            memory.aprender(
                userId, message, answer,
                ContextoConversacionTuki(
                    tema = topic(intent),
                    ultimaPalabraOFrase = subject ?: previous?.ultimaPalabraOFrase,
                    ultimaLeccionId = studentCache?.currentLessonId ?: previous?.ultimaLeccionId,
                    ultimaIntencion = intent.name,
                    idiomaId = languageId
                )
            )
        }
        return answer
    }

    /**
     * Pregunta abierta, como en clase con un profesor:
     * 1. cortesía y ánimo; 2. explicación con el material documentado (gramática, pronunciación,
     * cultura); 3. un modelo que sí sabe conversar (Gemma base en el teléfono o Gemini con
     * Internet); 4. si nada lo respalda, lo dice con honestidad y propone temas que sí puede enseñar.
     */
    private suspend fun openAnswer(
        message: String,
        languageId: Int,
        intent: TukiIntent,
        userId: UUID?,
        studentLoader: suspend () -> StudentContext?,
        previous: ContextoConversacionTuki?,
        localRequest: AssistantRequest
    ): String {
        profesor.cortesia(message, languageId) { studentContextProvider.name(userId) }?.let { return it }
        // Las expresiones y frases de conversación están en las lecciones.
        if (profesor.pideFrases(message)) profesor.frasesDeLecciones(message, languageId)?.let { return it }
        profesor.explicar(message, languageId)?.let { return it }
        profesor.frasesDeLecciones(message, languageId)?.let { return it }

        val local = localEngine.answer(localRequest)
        if (local.conclusive && !local.conversational) return local.text

        // El modelo ajustado del teléfono solo traduce; para charlar se usa el base o Gemini.
        val generative: AssistantEngine? = when {
            onDeviceEngine.available() && onDeviceEngine.conversational() -> onDeviceEngine
            network.hayConexion() -> remoteEngine
            else -> null
        }
        if (generative != null) {
            val student = studentLoader()
            val verified = buildVerifiedContext(message, languageId)
            val teaching = buildTeachingContext(message, languageId, intent, student)
            val context = buildString {
                appendLine(TukiIdentity.personaje)
                appendLine()
                appendLine(TukiIdentity.reglasVeracidad)
                appendLine()
                appendLine(IdiomasTuki.contextoModelo(languageId))
                student?.let {
                    it.name?.takeIf(String::isNotBlank)?.let { name -> appendLine("El estudiante se llama $name.") }
                    appendLine("Contexto del estudiante: ${it.minimalSummary()}")
                }
                previous?.tema?.let { appendLine("Tema activo de la conversación: $it") }
                if (verified.isNotBlank()) appendLine("Datos verificados:\n$verified")
                else appendLine("Datos verificados: ninguno para esta consulta.")
                if (teaching.isNotBlank()) appendLine(teaching)
            }
            // Una red lenta no debe dejar al estudiante esperando: pasado el límite, Tuki responde con lo que tiene.
            runCatching {
                kotlinx.coroutines.withTimeoutOrNull(TIEMPO_MAXIMO_GENERATIVO_MS) {
                    generative.answer(localRequest.copy(verifiedContext = context)).text
                }
            }
                .getOrNull()
                ?.takeIf { isCoherent(it) && !isEcho(it, message) }
                ?.let { return it }
        }
        if (local.conclusive) return local.text

        val idioma = IdiomasTuki.nombre(languageId)
        val temas = profesor.temasSugeridos(languageId)
        return buildString {
            append("Esa pregunta todavía no la tengo en mi material sobre $idioma, y prefiero no inventarte una respuesta.")
            if (temas.isNotEmpty()) {
                append(" Sí puedo explicarte, por ejemplo: ").append(temas.joinToString(", ") { "«$it»" }).append('.')
            }
            append(" También puedo decirte cómo se dice una palabra, darte frases para practicar o contarte de la cultura.")
            if (generative == null && !network.hayConexion()) {
                append(" Cuando tengas Internet podré responderte más temas.")
            }
        }
    }

    private fun asksForGreetings(message: String): Boolean {
        val texto = normalize(message)
        return texto.containsAny("saludar", "como saludo", "saludos en", "frase para saludar", "frases para saludar", "como se saluda")
    }

    /** Saludos registrados en el diccionario del idioma, en lugar de inventar una frase. */
    private suspend fun greetingsAnswer(languageId: Int): String {
        val idioma = IdiomasTuki.nombre(languageId)
        if (languageId == SPANISH_ID) {
            return "Para saludar en español puedes decir: “Hola”, “Buenos días”, “Buenas tardes”, “Buenas noches” y “¿Cómo está usted?”. " +
                "Con una persona mayor, como tu abuela, es más respetuoso decir “usted”."
        }
        val encontrados = SALUDOS_BASE.mapNotNull { espanol ->
            val resolucion = conocimiento.resolverTraduccion(espanol, SPANISH_ID, languageId)
            val texto = when (resolucion) {
                is ResolucionTraduccion.Expresion -> resolucion.texto
                is ResolucionTraduccion.Unica -> resolucion.opcion.palabra.texto
                is ResolucionTraduccion.Ambigua -> resolucion.opciones.first().palabra.texto
                else -> null
            }
            texto?.let { "• ${it.trimEnd('.')} — $espanol" }
        }.distinct()
        if (encontrados.isEmpty()) return "Todavía no tengo saludos registrados en $idioma. Prefiero no inventarlos."
        return buildString {
            append("Así puedes saludar en $idioma (del diccionario de Aikukisna):\n")
            append(encontrados.joinToString("\n"))
            append("\nRepite cada saludo en voz alta. ¿Quieres que practiquemos cómo responder?")
        }
    }

    /** "¿Qué diferencia hay entre yang y man?" → las dos palabras, cada una con su significado. */
    private fun comparedWords(message: String): Pair<String, String>? {
        val match = Regex("diferencia (?:hay )?entre [\"“']?([\\p{L}\\-]+)[\"”']? y [\"“']?([\\p{L}\\-]+)", RegexOption.IGNORE_CASE)
            .find(message) ?: return null
        return match.groupValues[1] to match.groupValues[2]
    }

    private suspend fun differenceAnswer(primera: String, segunda: String, languageId: Int): String {
        val idioma = IdiomasTuki.nombre(languageId)
        suspend fun significado(palabra: String): String {
            val resolucion = conocimiento.resolverTraduccion(palabra, languageId, SPANISH_ID)
                .takeIf { it.isFound() } ?: conocimiento.resolverTraduccion(palabra, SPANISH_ID, languageId)
            return when (resolucion) {
                is ResolucionTraduccion.Expresion -> resolucion.texto
                is ResolucionTraduccion.Unica -> resolucion.opcion.palabra.texto
                is ResolucionTraduccion.Ambigua -> resolucion.opciones.take(3).joinToString(" / ") { it.palabra.texto }
                else -> "todavía no la tengo registrada"
            }.trimEnd('.')
        }
        return "En $idioma, “$primera” significa: ${significado(primera)}; y “$segunda” significa: ${significado(segunda)}. " +
            "¿Quieres una frase de ejemplo con alguna de las dos?"
    }

    private fun String.containsAny(vararg values: String) = values.any(::contains)

    private fun conPunto(texto: String): String = texto.trim().trimEnd('.').let { if (it.endsWith('!') || it.endsWith('?')) it else "$it." }

    /** El modelo a veces solo repite la pregunta del estudiante: eso no es una respuesta. */
    private fun isEcho(answer: String, message: String): Boolean {
        val a = normalize(answer)
        val m = normalize(message)
        return a.isBlank() || a == m || (a.length < 60 && (m.contains(a) || a.contains(m)))
    }

    /** "¿Cómo se dice agua en español?" cuando el estudiante aprende Español desde su lengua de apoyo. */
    private suspend fun spanishLearnerTranslation(subject: String): String {
        val apoyo = preferencias.lenguaApoyoId()
        val nombreApoyo = IdiomasTuki.nombre(apoyo)
        if (findExact(subject, SPANISH_ID) != null) {
            val enApoyo = conocimiento.resolverTraduccion(subject, SPANISH_ID, apoyo)
            return "“$subject” ya es una palabra en español. " +
                if (enApoyo.isFound()) "En $nombreApoyo: " + describe(subject, enApoyo, false)
                else "Todavía no tengo su equivalencia en $nombreApoyo."
        }
        return translationAnswer(subject, apoyo, SPANISH_ID, false)
    }

    /** Opciones concretas cuando Tuki no entendió o el estudiante se confundió. */
    private fun guidance(languageId: Int, intro: String): String {
        val idioma = IdiomasTuki.nombre(languageId)
        return "$intro Puedo ayudarte con $idioma así:\n" +
            "• “¿Cómo se dice casa?” — te doy la palabra en $idioma\n" +
            "• “¿Qué significa …?” — escribe una palabra en $idioma\n" +
            "• “Hazme una frase con agua” — frases reales que la usan\n" +
            "• “Háblame en $idioma” — frases para practicar\n" +
            "• “¿Cómo voy?” — tu progreso y lecciones"
    }

    /** Responde "¿es correcto?" según cómo se obtuvo la respuesta anterior, sin inventar. */
    private fun verifyPrevious(history: List<MensajeChat>): String {
        val last = history.dropLast(1).lastOrNull { it.rol == com.aikukisna.app.domain.model.RolChat.TUKI }?.texto
            ?: return "Todavía no te he dado una respuesta que pueda revisar."
        val normalized = normalize(last)
        return when {
            normalized.contains("palabra por palabra") ->
                "No está validada. La armé palabra por palabra con el diccionario, así que el orden y el sentido pueden no ser correctos en una frase real. Úsala solo como pista."
            normalized.contains("automatica") ->
                "No está validada. Es una traducción automática; sirve para entender la idea, pero el equipo lingüístico todavía no la ha revisado."
            normalized.contains("registrad") || normalized.contains("verificad") ->
                "Sí. Esa respuesta viene del diccionario y el corpus de Aikukisna, no la inventé."
            else ->
                "No puedo confirmarlo: esa respuesta no viene de un dato verificado. Prefiero que la tomes con cuidado."
        }
    }

    /** "¿y gato?" tras una consulta lingüística reutiliza la intención anterior con la nueva palabra. */
    private fun ellipticalFollowUp(message: String, previousIntent: String?): Pair<TukiIntent, String>? {
        val intent = previousIntent?.let { name -> TukiIntent.entries.firstOrNull { it.name == name } }
            ?.takeIf { it in FOLLOW_UP_INTENTS } ?: return null
        val match = Regex("^\\s*[¿¡]?\\s*(y\\s+c[oó]mo\\s+se\\s+dice|y\\s+qu[eé]\\s+tal|y|e)\\s+(.+?)[?!.]*\\s*$", RegexOption.IGNORE_CASE)
            .find(message) ?: return null
        val subject = match.groupValues[2].trim(' ', '"', '\'', '“', '”')
        if (subject.isBlank() || subject.split(Regex("\\s+")).size > 3) return null
        if (normalize(subject).split(' ').first() in QUESTION_WORDS) return null
        return intent to subject
    }

    /** Descarta respuestas generadas que divagan: demasiado largas o con palabras muy repetidas. */
    private fun isCoherent(text: String): Boolean {
        if (text.isBlank() || text.length > 700) return false
        val words = normalize(text).split(' ').filter { it.length > 2 }
        if (words.size < 12) return true
        val repeated = words.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
        return repeated <= 4 && words.toSet().size >= words.size / 2
    }

    private suspend fun pronunciation(subject: String, languageId: Int): String {
        val word = findExact(subject, languageId) ?: findExactAnyLanguage(subject)
            ?: return "Aún no tengo una pronunciación verificada para “$subject”. Prefiero no inventarla."
        val pronunciation = word.pronunciacionFonetica ?: word.pronunciacion
        return if (word.pronunciacionVerificada && !pronunciation.isNullOrBlank()) {
            "La pronunciación verificada de “${word.texto}” es: $pronunciation. Puedes repetirla despacio y luego a ritmo natural."
        } else {
            "Encontré “${word.texto}”, pero todavía no tiene una pronunciación verificada. Prefiero esperar la validación antes de enseñártela."
        }
    }

    /** Frases reales del corpus en el idioma que aprende el estudiante, con su traducción. */
    private suspend fun practicePhrases(languageId: Int, languageName: String?): String {
        val idioma = languageName ?: IdiomasTuki.nombre(languageId)
        val frases = verifiedPhrases(languageId, null)
        if (frases.isEmpty()) {
            return "Todavía no tengo frases verificadas en $idioma para practicar. Prueba preguntándome por una palabra."
        }
        return buildString {
            append("¡Claro! Aquí tienes unas frases en $idioma:")
            appendPhrases(frases)
            append("\nRepítelas en voz alta. ¿Quieres otras o armamos una frase con alguna palabra?")
        }
    }

    /** Frases del corpus en [languageId]; se glosan en español y, si no hay, en inglés. */
    private suspend fun verifiedPhrases(languageId: Int, word: String?): List<FraseVerificada> {
        for (gloss in listOf(SPANISH_ID, IdiomasTuki.INGLES)) {
            if (gloss == languageId) continue
            val frases = conocimiento.buscarFrases(languageId, gloss, word, 3)
            if (frases.isNotEmpty()) return frases
        }
        return emptyList()
    }

    private fun StringBuilder.appendPhrases(frases: List<FraseVerificada>) {
        frases.forEach { frase ->
            append("\n• ").append(frase.texto).append(" — ").append(frase.traduccion)
            if (frase.idiomaTraduccionId != SPANISH_ID) {
                append(" (").append(IdiomasTuki.nombre(frase.idiomaTraduccionId)).append(")")
            }
        }
    }

    private fun progressAnswer(student: StudentContext): String = buildString {
        student.name?.takeIf { it.isNotBlank() }?.let { append("$it, ") }
        append("tienes ${student.xp} XP, una racha actual de ${student.streak} días")
        append(" y ${student.completedLessons} lecciones completadas.")
        if (student.maximumStreak > student.streak) {
            append(" Tu mejor racha es de ${student.maximumStreak} días.")
        }
        if (student.unlockedAchievements.isEmpty()) {
            append(" Todavía no tienes logros desbloqueados registrados.")
        } else {
            append(" Logros desbloqueados: ${student.unlockedAchievements.joinToString()}.")
        }
        student.currentLessonTitle?.let { append(" Tu siguiente lección es “$it”.") }
        appendSyncStatus(student)
    }

    private fun lessonAnswer(student: StudentContext): String = buildString {
        when {
            student.latestScore == null -> append("No encuentro todavía un intento de lección guardado en este dispositivo.")
            student.latestLessonCompleted == true -> {
                append("La lección")
                student.latestLessonTitle?.let { append(" “$it”") }
                append(" quedó guardada como completada con ${student.latestScore}%. ")
                when {
                    student.currentLessonId == null ->
                        append("No encuentro otra lección disponible en el contenido local.")
                    student.currentLessonId != student.latestLessonId ->
                        append("La siguiente disponible es “${student.currentLessonTitle}”.")
                    else ->
                        append("Sin embargo, el siguiente nodo local no avanzó. El puntaje sí cumple el 85%, así que esto indica una inconsistencia al actualizar el mapa de lecciones, no una falta de puntuación.")
                }
            }
            else -> {
                append("Tu último intento")
                student.latestLessonTitle?.let { append(" en “$it”") }
                append(" fue de ${student.latestScore}%. Se necesita 85% para completar la lección y desbloquear la siguiente, por eso todavía permanece bloqueada.")
            }
        }
        appendSyncStatus(student)
    }

    private fun appHelpAnswer(message: String, student: StudentContext?): String {
        val normalized = normalize(message)
        return when {
            normalized.contains("camara") -> buildString {
                append("La cámara reconoce texto u objetos y luego busca la etiqueta detectada en el diccionario local para el idioma que elegiste aprender. ")
                append("Solo muestra traducción o pronunciación cuando existe contenido documentado; si no hay coincidencia confiable, debe indicarlo sin sustituirla por otra palabra. ")
                append("Para capturar necesita permiso de cámara.")
            }
            normalized.contains("diccionario") ->
                "El diccionario busca en ambos sentidos entre español y el idioma que elegiste aprender. Consulta primero el contenido local y solo muestra entradas, acepciones, ejemplos y pronunciaciones que estén registradas."
            normalized.contains("traductor") || normalized.contains("traduccion") ->
                "El traductor usa el idioma objetivo de tu perfil. Prioriza una oración completa verificada, después una expresión y luego una traducción léxica directa. Si no existe evidencia completa, indica que no hay una traducción verificada y no arma una frase por su cuenta."
            normalized.contains("perfil") || normalized.contains("idioma objetivo") || normalized.contains("idioma seleccionado") ->
                "El perfil conserva tu idioma de aprendizaje, XP, racha, progreso, logros y favoritos. Tu idioma actual de aprendizaje es ${student?.languageName ?: "el que seleccionaste en el perfil"}."
            normalized.contains("logro") || normalized.contains("xp") || normalized.contains("racha") ->
                student?.let(::progressAnswer)
                    ?: "Necesito un perfil local disponible para consultar XP, racha y logros reales."
            normalized.contains("leccion") || normalized.contains("mapa") || normalized.contains("desbloque") ->
                student?.let(::lessonAnswer)
                    ?: "Necesito un perfil local disponible para consultar el estado real de las lecciones."
            normalized.contains("sincron") -> buildString {
                append(if (student?.online == true) "Ahora hay conexión. " else "Ahora no hay conexión. ")
                val pending = student?.pendingLessonSyncs ?: 0
                if (pending > 0) append("Hay $pending cambios de lecciones pendientes de sincronizar; el progreso local puede seguir usándose mientras tanto.")
                else append("No hay cambios de lecciones pendientes registrados.")
            }
            normalized.contains("offline") || normalized.contains("sin internet") || normalized.contains("necesita internet") ->
                "Después del primer inicio de sesión o registro en línea, Aikukisna conserva la sesión y usa el contenido local para lecciones, diccionario y progreso sin conexión. Cuando vuelve Internet, intenta sincronizar los cambios pendientes sin bloquear el uso local."
            else ->
                "Puedo explicarte el estado real de tu progreso, las lecciones, el diccionario, el traductor, la cámara, el perfil y qué funciones usan datos locales o conexión. Dime cuál quieres revisar."
        }
    }

    private fun StringBuilder.appendSyncStatus(student: StudentContext) {
        if (student.pendingLessonSyncs > 0) {
            append(" Tu avance está guardado localmente y hay ${student.pendingLessonSyncs} cambios pendientes de sincronizar.")
        }
    }

    /**
     * Busca en ambos sentidos entre los dos idiomas; si es una frase sin equivalencia registrada,
     * la arma con el traductor de oraciones y la etiqueta como no validada.
     */
    private suspend fun translationAnswer(
        subject: String,
        sourceLanguageId: Int,
        targetLanguageId: Int,
        definition: Boolean
    ): String {
        val forward = conocimiento.resolverTraduccion(subject, sourceLanguageId, targetLanguageId)
        if (forward.isFound()) return describe(subject, forward, definition)
        if (sourceLanguageId != targetLanguageId) {
            val backward = conocimiento.resolverTraduccion(subject, targetLanguageId, sourceLanguageId)
            if (backward.isFound()) return describe(subject, backward, definition)
        }
        if (subject.trim().contains(' ')) {
            composedSentence(subject, sourceLanguageId, targetLanguageId)?.let { return it }
        }
        return describe(subject, forward, definition)
    }

    private fun ResolucionTraduccion.isFound() = this is ResolucionTraduccion.Expresion ||
        this is ResolucionTraduccion.Unica || this is ResolucionTraduccion.Ambigua ||
        this is ResolucionTraduccion.ComposicionDocumentada

    private suspend fun composedSentence(subject: String, sourceLanguageId: Int, targetLanguageId: Int): String? {
        val result = runCatching { traducirOracion(subject, sourceLanguageId, targetLanguageId) }.getOrNull()
            ?: return null
        val idioma = IdiomasTuki.nombre(targetLanguageId)
        return when (result.tipo) {
            TipoTraduccion.VERIFICADA -> "La traducción registrada de “$subject” en $idioma es: ${result.texto}."
            TipoTraduccion.AUTOMATICA ->
                "Así se diría en $idioma: “${result.texto}”.\n" +
                    "(Traducción automática sin conexión; úsala para entender, todavía no está validada por el equipo lingüístico.)"
            TipoTraduccion.LITERAL -> buildString {
                append("No tengo esa frase completa registrada, así que la armé palabra por palabra en $idioma: “${result.texto}”.")
                result.nota?.let { append("\n").append(it) }
            }
        }
    }

    private fun describe(
        subject: String,
        result: ResolucionTraduccion,
        definition: Boolean
    ): String = when (result) {
        is ResolucionTraduccion.Expresion -> "La equivalencia registrada de “$subject” es: ${conPunto(result.texto)}"
        is ResolucionTraduccion.Unica -> "La equivalencia registrada de “$subject” es: ${conPunto(result.opcion.palabra.texto)}"
        is ResolucionTraduccion.Ambigua -> buildString {
            append(if (definition) "“$subject” tiene varias acepciones registradas:" else "Encontré varias traducciones posibles para “$subject”:")
            result.opciones.forEach { option ->
                append("\n• ").append(option.palabra.texto)
                option.contexto?.let { context -> append(" — ").append(context) }
            }
            append("\nElige la opción que corresponda al contexto.")
        }
        is ResolucionTraduccion.Sugerencia -> "No encontré una coincidencia exacta. ¿Quisiste decir “${result.palabra.texto}”?"
        is ResolucionTraduccion.ComposicionDocumentada -> buildString {
            append("No encontré la oración completa, pero sí pude formar esta composición literal con equivalencias documentadas: “")
            append(result.texto).append("”.")
            append("\nComponentes verificados:")
            result.segmentos.forEach { append("\n• ${it.textoOrigen} = ${it.textoDestino}") }
            append("\nEl orden de la oración completa aún no está validado como traducción nativa.")
        }
        is ResolucionTraduccion.Parcial -> buildString {
            append("No tengo una expresión completa respaldada para “$subject”.")
            if (result.conocidas.isNotEmpty()) {
                append("\nSí encontré estos componentes verificados:")
                result.conocidas.forEach { append("\n• $it") }
            }
            if (result.desconocidas.isNotEmpty()) {
                append("\nFalta validar: ${result.desconocidas.joinToString()}.")
            }
        }
        ResolucionTraduccion.NoEncontrada -> "Aún no tengo una equivalencia respaldada para “$subject”. Prefiero no inventarla."
    }

    /** Frases reales del corpus que usan la palabra en el idioma que aprende el estudiante. */
    private suspend fun example(subject: String, languageId: Int): String {
        val idioma = IdiomasTuki.nombre(languageId)
        val words = buildList {
            findExact(subject, languageId)?.let { add(it.texto) }
            if (isEmpty() && languageId != SPANISH_ID) {
                findExact(subject, SPANISH_ID)?.let { spanish ->
                    conocimiento.obtenerEntrada(spanish.id, languageId)?.traducciones.orEmpty()
                        .take(3).forEach { add(it.palabra.texto) }
                }
            }
            if (isEmpty()) add(subject)
        }.distinct()
        for (word in words) {
            val frases = verifiedPhrases(languageId, word)
            if (frases.isNotEmpty()) return buildString {
                append(
                    if (word.equals(subject, ignoreCase = true)) "Frases reales con “$word” en $idioma:"
                    else "“$subject” en $idioma es “$word”. Así se usa en frases reales:"
                )
                appendPhrases(frases)
                append("\n¿Quieres que practiquemos otra palabra?")
            }
        }
        val translated = words.first().takeIf { !it.equals(subject, ignoreCase = true) }
        return "Todavía no tengo una frase verificada con “$subject” en $idioma. " +
            (translated?.let { "Sí sé que se dice “$it”. ¿Probamos con otra palabra?" }
                ?: "Prueba con otra palabra o pregúntame cómo se dice una frase completa.")
    }

    private suspend fun buildVerifiedContext(message: String, languageId: Int): String {
        // Las palabras vacías ("como", "dime", "que"...) existen en el diccionario y desviaban
        // al modelo local hacia su traducción en vez de responder la pregunta.
        val terms = normalize(message).split(' ')
            .filter { it.length >= 4 && it !in PALABRAS_VACIAS }
            .distinct()
            .take(4)
        return terms.flatMap { term ->
            listOf(languageId, SPANISH_ID).distinct().flatMap { source ->
                val word = findExact(term, source) ?: return@flatMap emptyList()
                conocimiento.obtenerEntrada(word.id)?.traducciones.orEmpty()
                    .map { "${word.texto} = ${it.palabra.texto}" }
            }
        }.distinct().take(10).joinToString("\n")
    }

    /**
     * Material de clase para que el modelo responda como profesor sin inventar:
     * reglas gramaticales documentadas del idioma y el vocabulario de la lección actual.
     */
    private suspend fun buildTeachingContext(
        message: String,
        languageId: Int,
        intent: TukiIntent,
        student: StudentContext?
    ): String {
        val terms = normalize(message).split(' ').filter { it.length >= 4 && it !in PALABRAS_VACIAS }.toSet()
        val reglas = runCatching { gramatica.obtenerReglas() }.getOrDefault(emptyList())
            .filter { it.idiomaId == languageId }
        val relevantes = reglas
            .map { regla ->
                val texto = normalize("${regla.titulo} ${regla.categoria} ${regla.descripcion}")
                regla to terms.count { texto.contains(it) }
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
            .ifEmpty { if (intent == TukiIntent.GRAMMAR_HELP) reglas.take(2) else emptyList() }
            .take(2)

        val vocabulario = student?.currentLessonId?.let { leccionId ->
            runCatching {
                when (val contenido = lecciones.obtenerContenidoLeccion(leccionId)) {
                    is ContenidoLeccion.Frases -> contenido.oraciones.take(6)
                        .map { "${it.textoOrigen} = ${it.textoDestino}" }
                    is ContenidoLeccion.Vocabulario -> contenido.palabras.take(6).mapNotNull { palabra ->
                        conocimiento.obtenerEntrada(palabra.id, SPANISH_ID)?.traducciones?.firstOrNull()
                            ?.let { "${palabra.texto} = ${it.palabra.texto}" }
                    }
                }
            }.getOrDefault(emptyList())
        }.orEmpty()

        return buildString {
            if (relevantes.isNotEmpty()) {
                appendLine("Gramática documentada:")
                relevantes.forEach { regla ->
                    append("- ").append(regla.titulo).append(": ").appendLine(regla.descripcion.take(300))
                    regla.ejemplos.firstOrNull { !it.traduccionEspanol.isNullOrBlank() }?.let {
                        appendLine("  Ejemplo: ${it.textoIdioma} = ${it.traduccionEspanol}")
                    }
                }
            }
            if (vocabulario.isNotEmpty()) {
                appendLine("Vocabulario de la lección actual${student?.currentLessonTitle?.let { " ($it)" } ?: ""}:")
                vocabulario.forEach { appendLine("- $it") }
            }
        }.trim()
    }

    private suspend fun findExact(text: String, languageId: Int) =
        (conocimiento.buscarPalabra(text, languageId) as? ResultadoBusquedaConocimiento.Exacta)?.palabra

    private suspend fun findExactAnyLanguage(text: String): com.aikukisna.app.domain.model.Palabra? {
        for (languageId in 1..4) findExact(text, languageId)?.let { return it }
        return null
    }

    private fun explicitFollowUp(message: String, intent: TukiIntent, subject: String?): String = when {
        subject == null -> message
        intent == TukiIntent.DEFINE_WORD && normalize(message).split(' ').size <= 4 -> "¿Qué significa $subject?"
        else -> message
    }

    private fun extractSubject(message: String, intent: TukiIntent): String? {
        val clean = message.trim(' ', '¿', '?', '¡', '!', '.', '"', '\'')
        val prefixes = when (intent) {
            TukiIntent.DEFINE_WORD -> listOf("qué significa", "que significa", "significado de", "qué quiere decir", "que quiere decir", "what does")
            TukiIntent.TRANSLATE -> listOf(
                "cómo se dice", "como se dice", "cómo se diría", "como se diria", "cómo puedo decir", "como puedo decir",
                "cómo le digo", "como le digo", "cómo digo", "como digo", "traduce", "traducir",
                "how do i say", "how do you say", "how to say"
            )
            TukiIntent.PRONUNCIATION -> listOf("cómo se pronuncia", "como se pronuncia", "pronuncia")
            TukiIntent.EXAMPLE -> listOf(
                "dame un ejemplo de", "otro ejemplo de", "ejemplo de",
                "frase con la palabra", "oración con la palabra", "oracion con la palabra",
                "frase con", "oración con", "oracion con", "frase usando", "oración usando", "oracion usando",
                "usa la palabra"
            )
            else -> emptyList()
        }
        val prefix = prefixes.firstOrNull { clean.contains(it, ignoreCase = true) } ?: return null
        return clean.substring(clean.indexOf(prefix, ignoreCase = true) + prefix.length)
            .replace(Regex("\\s+(en|al|del|in)\\s+(miskitu|miskito|español|espanol|ingles|inglés|kriol|criollo|english|spanish|creole).*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+mean$", RegexOption.IGNORE_CASE), "")
            .trim(' ', ':', ',', '"', '\'')
            // "¿Cómo se pronuncia la palabra pus?" pregunta por "pus", no por "la palabra pus".
            .replace(Regex("^(la palabra|el término|el termino|la frase|la expresión|la expresion|the word|the phrase)\\s+", RegexOption.IGNORE_CASE), "")
            .trim(' ', ':', ',', '"', '\'', '“', '”').takeIf { it.isNotBlank() }
    }

    private fun topic(intent: TukiIntent) = when (intent) {
        TukiIntent.CULTURE -> "cultura"
        TukiIntent.LESSON_HELP, TukiIntent.STUDENT_PROGRESS -> "aprendizaje"
        TukiIntent.APP_HELP -> "aplicación"
        TukiIntent.GENERAL_CONVERSATION -> "conversación"
        else -> "idioma"
    }

    private fun normalize(text: String): String = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "").replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim()

    private companion object {
        const val SPANISH_ID = 2

        const val TIEMPO_MAXIMO_GENERATIVO_MS = 8_000L

        val SALUDOS_BASE = listOf("hola", "buenos días", "buenas tardes", "buenas noches", "¿cómo está usted?", "adiós")

        val QUESTION_WORDS = setOf("que", "como", "cual", "cuando", "donde", "quien", "por", "para", "si", "no", "tu")
        val OPEN_INTENTS = setOf(TukiIntent.GENERAL_CONVERSATION, TukiIntent.FOLLOW_UP, TukiIntent.UNKNOWN)

        /** Consultas que, si no tienen una respuesta directa (p. ej. sin palabra concreta), se enseñan como en clase. */
        val TEACHING_INTENTS = setOf(
            TukiIntent.GRAMMAR_HELP, TukiIntent.CULTURE, TukiIntent.PRONUNCIATION,
            TukiIntent.EXAMPLE, TukiIntent.DEFINE_WORD, TukiIntent.TRANSLATE
        )
        val FOLLOW_UP_INTENTS = setOf(
            TukiIntent.TRANSLATE, TukiIntent.DEFINE_WORD, TukiIntent.PRONUNCIATION, TukiIntent.EXAMPLE
        )

        /** Preguntas sobre la app o el progreso: mencionar otro idioma no cambia la respuesta. */
        val LANGUAGE_NEUTRAL_INTENTS = setOf(
            TukiIntent.SELECTED_LANGUAGE, TukiIntent.AVAILABLE_LANGUAGES, TukiIntent.APP_HELP,
            TukiIntent.CLARIFY, TukiIntent.COMPLAINT, TukiIntent.VERIFY_ANSWER,
            TukiIntent.TUTOR_HELP, TukiIntent.XP_HELP, TukiIntent.STUDENT_PROGRESS, TukiIntent.LESSON_HELP
        )
        val PALABRAS_VACIAS = setOf(
            "como", "cual", "cuales", "donde", "cuando", "quien", "quienes", "porque", "para", "pero",
            "dime", "decir", "puedes", "podrias", "quiero", "tengo", "tienes", "esta", "este", "esto",
            "estas", "estos", "eres", "estoy", "hacer", "hablar", "hablame", "sobre", "algo", "mucho",
            "muy", "mas", "menos", "bien", "favor", "ayuda", "ayudame", "entonces", "nada", "todo",
            "entiendo", "explica", "explicame", "significa", "traduce", "miskito", "kriol", "ingles", "espanol"
        )
    }
}
