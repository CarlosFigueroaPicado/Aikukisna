package com.aikukisna.app.domain.assistant

internal fun functionalAnswer(intent: TukiIntent, languageName: String?): String = when (intent) {
    TukiIntent.SELECTED_LANGUAGE -> languageName?.let {
        "Por el momento estás aprendiendo $it. Si quieres practicar otro idioma, cámbialo desde la pantalla principal (Inicio) tocando el idioma que aparece arriba."
    } ?: "No encuentro un perfil local con el que pueda confirmar tu idioma de aprendizaje."
    TukiIntent.XP_HELP ->
        "Los XP son puntos de experiencia que acumulas al completar lecciones. " +
            "Representan tu avance y determinan el nivel mostrado en Inicio: cada 500 XP subes un nivel. " +
            "Para desbloquear la siguiente lección necesitas aprobar con al menos 85%; acumular XP no sustituye ese requisito."
    TukiIntent.TUTOR_HELP ->
        "Soy Tuki, el tutor de Aikukisna. Puedo consultar vocabulario, traducciones y ejemplos documentados, " +
            "ayudarte con las lecciones y explicar tu progreso y las funciones de la app. " +
            "Si no encuentro información verificada, te lo indico."
    else -> error("La intención no corresponde a una consulta funcional específica")
}
