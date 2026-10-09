package com.aikukisna.app.domain.assistant

import java.text.Normalizer
import javax.inject.Inject

enum class TukiIntent {
    TRANSLATE, DEFINE_WORD, PRONUNCIATION, EXAMPLE, LESSON_HELP, GRAMMAR_HELP,
    CULTURE, STUDENT_PROGRESS, APP_HELP, SELECTED_LANGUAGE, AVAILABLE_LANGUAGES, XP_HELP, TUTOR_HELP, PRACTICE_PHRASES,
    CLARIFY, COMPLAINT, VERIFY_ANSWER, FOLLOW_UP, GENERAL_CONVERSATION, UNKNOWN
}

class TukiIntentClassifier @Inject constructor() {
    fun classify(text: String, hasContext: Boolean = false): TukiIntent {
        val value = normalize(text)
        return when {
            value.matches(Regex("(que|cual es mi) idioma (estoy aprendiendo|aprendo|tengo seleccionado|seleccione|seleccionado)")) ||
                value in setOf("cual es mi idioma", "mi idioma seleccionado", "que idioma tengo seleccionado en la app") ||
                (value.contains("idioma") && !value.containsAny("traduce", "traducir", "como se dice", "significa") &&
                    value.containsAny("estoy aprendiendo", "que aprendo", "elegi", "escogi", "seleccione", "cual es mi idioma")
                ) -> TukiIntent.SELECTED_LANGUAGE
            value.containsAny(
                "que idiomas", "cuales idiomas", "cuantos idiomas", "idiomas disponibles", "otros idiomas",
                "otro idioma", "cambiar de idioma", "cambiar el idioma", "cambiar idioma", "cambio de idioma"
            ) -> TukiIntent.AVAILABLE_LANGUAGES
            value.matches(Regex("(para que (son|sirven)|que (son|significa)|cual es el beneficio( que tendre)?) (los |esos |con esos )?(puntos( xp)?|xp)")) -> TukiIntent.XP_HELP
            value in setOf("cual es tu funcion", "como funcionas", "que puedes hacer", "dime que puedes hacer", "para que sirves") ||
                value.containsAny("como funcionas", "que puedes hacer", "para que sirves", "cual es tu funcion", "quien eres") -> TukiIntent.TUTOR_HELP
            value.containsAny(
                "hablame en", "dime algo en", "frases en", "una frase en", "ensename frases", "ensename a decir",
                "saludame en", "practicar frases", "practiquemos frases", "dime frases"
            ) -> TukiIntent.PRACTICE_PHRASES
            // "¿Cómo se pronuncia X?" pide una palabra; "¿cómo practico la pronunciación?" es una pregunta de clase.
            value.contains("como se pronuncia") || Regex("\bpronuncia\b").containsMatchIn(value) -> TukiIntent.PRONUNCIATION
            value.containsAny(
                "dame un ejemplo", "otro ejemplo", "ejemplo de", "en una oracion", "en una frase",
                "frase con", "oracion con", "frase usando", "oracion usando", "usa la palabra"
            ) -> TukiIntent.EXAMPLE
            value.containsAny("que significa", "significado de", "que quiere decir", "define ") ||
                Regex("^what does .+ mean$").matches(value) -> TukiIntent.DEFINE_WORD
            value.containsAny(
                "como se dice", "traduce", "traducir", "traduccion", "como digo", "como le digo",
                "como puedo decir", "como se diria", "how do i say", "how do you say", "how to say"
            ) -> TukiIntent.TRANSLATE
            value.containsAny(
                "mi progreso", "mi avance", "como voy", "cuantas lecciones",
                "cuanto xp", "cuantos xp", "cuantos puntos", "que puntaje",
                "mi racha", "cuanta racha", "cuantos dias de racha",
                "mis logros", "que logros", "cuantos logros"
            ) -> TukiIntent.STUDENT_PROGRESS
            value.containsAny("leccion", "que estudio", "que estudiar", "siguiente leccion", "que debo estudiar", "continuar leccion", "ayuda con la leccion", "no avance", "no se desbloqueo", "no desbloqueo", "por que no avance") -> TukiIntent.LESSON_HELP
            value.containsAny(
                "como funciona la app", "como funciona aikukisna", "como funciona la camara",
                "como funciona el diccionario", "como funciona el traductor", "como funciona el perfil",
                "que hace la camara", "que hace el diccionario", "que hace el traductor",
                "funciona offline", "funciona sin internet", "necesita internet", "sincronizacion"
            ) -> TukiIntent.APP_HELP
            value in setOf(
                "no entiendo", "no te entiendo", "no entendi", "no entiendo nada", "no comprendo",
                "explicame mejor", "como asi", "que dices", "mas despacio", "otra vez"
            ) -> TukiIntent.CLARIFY
            value.containsAny(
                "sin sentido", "no tiene sentido", "incoherente", "no sirves", "respondes mal",
                "hablas raro", "tonterias", "por que siempre", "porque siempre"
            ) -> TukiIntent.COMPLAINT
            value.containsAny(
                "es falsa", "es falso", "es correcto", "es correcta", "es verdad", "estas seguro",
                "es real", "lo inventaste", "inventaste", "eso existe", "es cierto"
            ) -> TukiIntent.VERIFY_ANSWER
            value.containsAny("gramatica", "regla gramatical", "conjuga", "conjugacion") -> TukiIntent.GRAMMAR_HELP
            value.containsAny("cultura", "historia", "tradicion", "costumbre", "gastronomia", "vestuario") -> TukiIntent.CULTURE
            hasContext && value.containsAny("y esa palabra", "y esa frase", "que significa", "explicamelo otra vez", "repite") -> TukiIntent.FOLLOW_UP
            value.isBlank() -> TukiIntent.UNKNOWN
            else -> TukiIntent.GENERAL_CONVERSATION
        }
    }

    private fun String.containsAny(vararg values: String) = values.any(::contains)

    private fun normalize(text: String): String = Normalizer.normalize(
        text.lowercase(), Normalizer.Form.NFD
    ).replace(Regex("\\p{M}+"), "")
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
