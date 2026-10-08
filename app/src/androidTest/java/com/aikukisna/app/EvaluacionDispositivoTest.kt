package com.aikukisna.app

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aikukisna.app.di.PruebasDispositivoEntryPoint
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Evaluación manual en el teléfono (no es una prueba de CI). Escribe preguntas y respuestas en
 * files/evaluacion/<nombre>.txt para revisarlas. Se ejecuta con:
 * adb shell am instrument -w -e class com.aikukisna.app.EvaluacionDispositivoTest#tuki \
 *   com.aikukisna.app.test/androidx.test.runner.AndroidJUnitRunner
 */
@RunWith(AndroidJUnit4::class)
class EvaluacionDispositivoTest {

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private val grafo = EntryPointAccessors.fromApplication(
        contexto.applicationContext,
        PruebasDispositivoEntryPoint::class.java
    )

    private fun informe(nombre: String, bloque: (MutableList<String>) -> Unit) {
        val lineas = mutableListOf<String>()
        try {
            bloque(lineas)
        } finally {
            val carpeta = File(contexto.filesDir, "evaluacion").apply { mkdirs() }
            File(carpeta, "$nombre.txt").writeText(lineas.joinToString("\n"))
        }
    }

    @Test
    fun tuki() = informe("tuki") { lineas ->
        val tuki = grafo.tukiAssistant()
        lineas += "modelo_disponible=${grafo.motorGemma().disponible()}"
        val preguntas = argumento("preguntas")?.split("|") ?: PREGUNTAS_TUKI
        val idioma = argumento("idioma")?.toInt() ?: MISKITO
        for (pregunta in preguntas) {
            val (idiomaId, texto) = pregunta.split("::").let { if (it.size == 2) it[0].toInt() to it[1] else idioma to it[0] }
            val inicio = System.currentTimeMillis()
            val respuesta = runCatching {
                runBlocking { tuki.respond(listOf(MensajeChat(RolChat.USUARIO, texto)), idiomaId, null) }
            }.getOrElse { "ERROR: ${it::class.simpleName}: ${it.message}" }
            val linea = "[$idiomaId] P: $texto\n    R (${System.currentTimeMillis() - inicio} ms): ${respuesta.replace("\n", " ⏎ ")}"
            Log.i(TAG, linea)
            lineas += linea
        }
    }

    @Test
    fun traductor() = informe("traductor") { lineas ->
        val traducir = grafo.traducirOracion()
        val casos = argumento("casos")?.split("|") ?: CASOS_TRADUCTOR
        for (caso in casos) {
            val (origen, destino, texto) = caso.split("::")
            val inicio = System.currentTimeMillis()
            val salida = runCatching {
                runBlocking { traducir(texto, origen.toInt(), destino.toInt()) }
                    .let { "${it.texto}  [${it.tipo}/${it.fuente}]" }
            }.getOrElse { "ERROR: ${it.message}" }
            val linea = "$origen→$destino  $texto\n    = $salida (${System.currentTimeMillis() - inicio} ms)"
            Log.i(TAG, linea)
            lineas += linea
        }
    }

    @Test
    fun camara() = informe("camara") { lineas ->
        val carpeta = File(contexto.filesDir, "fotos_prueba")
        val reconocedor = grafo.reconocedorObjetosLocal()
        val reconocer = grafo.reconocerObjeto()
        val fotos = carpeta.listFiles().orEmpty().filter { it.extension.lowercase() in setOf("jpg", "jpeg", "png") }.sortedBy { it.name }
        lineas += "fotos=${fotos.size}"
        for (foto in fotos) {
            val base64 = android.util.Base64.encodeToString(foto.readBytes(), android.util.Base64.NO_WRAP)
            val etiquetas = runCatching { runBlocking { reconocedor.reconocer(base64) } }.getOrElse { listOf("ERROR ${it.message}") }
            val resultado = runCatching { runBlocking { reconocer(base64, MISKITO) }.toString() }.getOrElse { "ERROR ${it.message}" }
            // Como en la app: el detector encierra el objeto y se reconoce el recorte (aquí, el más grande).
            val recorte = runCatching {
                val regiones = runBlocking { grafo.detectorObjetos().detectar(base64) }
                val region = regiones.maxByOrNull { (it.derecha - it.izquierda) * (it.abajo - it.arriba) }
                    ?: return@runCatching "sin región"
                val recortada = grafo.recortador().recortar(base64, region)
                val r = runBlocking { reconocer(recortada, MISKITO) }
                "${r.objetoDetectado} → ${r.traduccion}"
            }.getOrElse { "ERROR ${it.message}" }
            val linea = "${foto.name}\n    etiquetas=$etiquetas\n    resultado=$resultado\n    recorte=$recorte"
            Log.i(TAG, linea)
            lineas += linea
        }
    }

    @Test
    fun lecciones() = informe("lecciones") { lineas ->
        lineas += "conexion=${grafo.conectividad().hayConexion()}"
        val ids = argumento("ids")?.split(",")?.map(String::toInt)
            ?: listOf(17, 38, 40, 43, 62, 94, 95, 96, 97, 99, 105, 48, 60)
        for (id in ids) {
            val linea = runCatching {
                val items = runBlocking { grafo.vocabularioLeccion()(id) }
                val quiz = runBlocking { grafo.quizLeccion()(id) }
                val sinTraduccion = items.count { it.textoDestino.isNullOrBlank() }
                "lección $id: ${items.size} tarjetas ($sinTraduccion sin traducción), ${quiz.size} preguntas\n    " +
                    items.take(4).joinToString(" | ") { "${it.textoOrigen} = ${it.textoDestino}" } +
                    (quiz.firstOrNull()?.let { "\n    quiz: ${it.textoPregunta} → ${it.respuestaCorrecta} ${it.opciones}" } ?: "")
            }.getOrElse { "lección $id: ERROR ${it::class.simpleName}: ${it.message}" }
            Log.i(TAG, linea)
            lineas += linea
        }
    }

    @Test
    fun audios() = informe("audios") { lineas ->
        lineas += "conexion=${grafo.conectividad().hayConexion()}"
        lineas += "registrados_ahora=${runBlocking { grafo.sembradorAudiosHumanos().sembrarSiCambio() }}"
        val motor = grafo.motorPronunciacion()
        val miskito = com.aikukisna.app.domain.model.Idioma(MISKITO, "mi", "Miskito")
        val textos = argumento("textos")?.split("|") ?: listOf("utla", "¿Utla?", "nahwala", "pain", "naika kat", "kiama", "naksa")
        for (texto in textos) {
            val r = runCatching {
                runBlocking { motor.resolver(com.aikukisna.app.domain.model.SolicitudPronunciacion(texto, miskito)) }
            }.getOrNull()
            val encontrados = runBlocking { grafo.audiosPronunciacion().buscarHumanosPorTexto(texto, miskito) }
            val bytes = encontrados.firstOrNull()?.let { runBlocking { grafo.audiosPronunciacion().leerAudio(it) }?.size }
            val linea = "$texto → ${r?.let { it::class.simpleName } ?: "ERROR"} ${r?.audio?.size ?: 0} bytes" +
                " (en base: ${encontrados.size}, lectura: $bytes)"
            Log.i(TAG, linea)
            lineas += linea
        }
    }

    private fun argumento(nombre: String): String? = InstrumentationRegistry.getArguments().getString(nombre)

    private companion object {
        const val TAG = "AIK_EVAL"
        const val MISKITO = 1

        val PREGUNTAS_TUKI = listOf(
            "Hola Tuki, ¿cómo estás?",
            "¿Cómo se dice gracias en miskito?",
            "¿Qué significa naksa?",
            "¿Cómo se forma el plural en miskito?",
            "Explícame cómo se conjugan los verbos en miskito",
            "¿Por qué el miskito tiene solo tres vocales?",
            "¿Cómo puedo practicar la pronunciación?",
            "Dame una frase para saludar a mi abuela",
            "¿Qué diferencia hay entre yang y man?",
            "¿Quiénes son los miskitos y dónde viven?",
            "¿Qué es el sikro?",
            "Estoy cansado de estudiar, ¿me ayudas?",
            "¿Cuál es la capital de Francia?",
            "Dame una frase para pedir permiso",
            "Expresiones para ir al mercado",
            "Frases para hablar de mi familia",
            "3::Dame frases para la escuela",
            "2::Dame frases para presentarme en clase",
            "3::¿Cómo se dice buenos días en kriol?",
            "3::¿Cómo se forma el pasado en kriol?",
            "4::¿Cuándo uso do y does?",
            "4::How do I say good morning?",
            "2::¿Cuándo se usa la tilde en español?",
            "2::¿Cómo se dice agua en español?"
        )

        val CASOS_TRADUCTOR = listOf(
            "2::1::gracias",
            "2::1::buenos días",
            "2::1::¿Cómo estás?",
            "2::1::Mi madre está en la casa",
            "2::1::Voy a pescar en el río mañana",
            "1::2::naksa",
            "1::2::Yang nina Carlos",
            "1::2::Witin ba aras ra wan",
            "2::3::Buenos días, ¿cómo estás?",
            "2::3::Mi casa está cerca del mar",
            "3::2::Mi niem da Karla",
            "3::2::Wi gwain go a skuul tumaro",
            "2::4::Me gusta estudiar inglés",
            "4::2::What time is it, please?",
            "1::4::tingki pali",
            "4::1::Thank you very much"
        )
    }
}
