package com.aikukisna.app.domain.assistant

import org.junit.Assert.assertEquals
import org.junit.Test

class TukiIntentClassifierTest {
    private val classifier = TukiIntentClassifier()

    @Test fun clasificaPreguntasFuncionalesDeLaPruebaFisica() {
        val consultas = mapOf(
            "¿Qué idioma estoy aprendiendo?" to TukiIntent.SELECTED_LANGUAGE,
            "¿Qué idioma tengo seleccionado?" to TukiIntent.SELECTED_LANGUAGE,
            "¿Para qué son los puntos XP?" to TukiIntent.XP_HELP,
            "¿Qué significa XP?" to TukiIntent.XP_HELP,
            "¿Cuál es el beneficio que tendré con esos puntos?" to TukiIntent.XP_HELP,
            "¿Cuál es tu función?" to TukiIntent.TUTOR_HELP,
            "¿Cómo funcionas?" to TukiIntent.TUTOR_HELP,
            "¿Cómo funciona la app?" to TukiIntent.APP_HELP
        )
        consultas.forEach { (consulta, esperada) ->
            assertEquals(consulta, esperada, classifier.classify(consulta))
            assertEquals(consulta, esperada, classifier.classify(consulta, true))
        }
    }

    @Test fun conservaConsultasLinguisticasYSaldoDeXp() {
        assertEquals(TukiIntent.TRANSLATE, classifier.classify("Traduce qué idioma estoy aprendiendo"))
        assertEquals(TukiIntent.DEFINE_WORD, classifier.classify("¿Qué significa función?"))
        assertEquals(TukiIntent.STUDENT_PROGRESS, classifier.classify("¿Cuántos XP tengo?"))
    }

    @Test fun clasificaConsultasPrincipales() {
        assertEquals(TukiIntent.DEFINE_WORD, classifier.classify("¿Qué significa muihni?"))
        assertEquals(TukiIntent.TRANSLATE, classifier.classify("Traduce buenos días al miskito"))
        assertEquals(TukiIntent.PRONUNCIATION, classifier.classify("¿Cómo se pronuncia?", true))
        assertEquals(TukiIntent.EXAMPLE, classifier.classify("Dame otro ejemplo", true))
        assertEquals(TukiIntent.STUDENT_PROGRESS, classifier.classify("¿Cuál es mi progreso?"))
        assertEquals(TukiIntent.STUDENT_PROGRESS, classifier.classify("¿Y cuántos puntos, racha y logros tengo?", true))
        assertEquals(TukiIntent.STUDENT_PROGRESS, classifier.classify("¿Cómo voy?", true))
        assertEquals(TukiIntent.LESSON_HELP, classifier.classify("¿Por qué no avancé a la siguiente lección?"))
        assertEquals(TukiIntent.APP_HELP, classifier.classify("¿Cómo funciona la cámara?"))
        assertEquals(TukiIntent.APP_HELP, classifier.classify("¿Cómo funciona el diccionario?"))
        assertEquals(TukiIntent.APP_HELP, classifier.classify("¿Qué hace el traductor?"))
        assertEquals(TukiIntent.CULTURE, classifier.classify("Cuéntame sobre su cultura"))
    }

    @Test fun conservaLaIntencionDeSeguimientoConContexto() {
        assertEquals(TukiIntent.FOLLOW_UP, classifier.classify("¿Y esa palabra?", true))
    }

    // Frases reales de la primera prueba en el teléfono que terminaban en el modelo generativo.
    @Test fun clasificaFrasesDeLaPruebaConGemma() {
        assertEquals(TukiIntent.LESSON_HELP, classifier.classify("ayúdame entonces con las lecciones"))
        assertEquals(TukiIntent.SELECTED_LANGUAGE, classifier.classify("dime que idioma es el que estoy aprendiendo o el que elegí"))
        assertEquals(TukiIntent.TUTOR_HELP, classifier.classify("dime como funcionas"))
        assertEquals(TukiIntent.PRACTICE_PHRASES, classifier.classify("háblame en miskito"))
        assertEquals(TukiIntent.GENERAL_CONVERSATION, classifier.classify("¿por qué el cielo es azul?"))
    }
}
