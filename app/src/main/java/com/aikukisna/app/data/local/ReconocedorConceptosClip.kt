package com.aikukisna.app.data.local

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.aikukisna.app.domain.repository.ConceptoReconocido
import com.aikukisna.app.domain.repository.ReconocedorConceptosLocal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.max

/**
 * TinyCLIP (MIT) sin conexión: compara la foto con un vocabulario propio escrito como texto
 * (scripts/datos/camara_vocabulario.json). A diferencia del clasificador de ImageNet, reconoce cosas
 * como coco, mango, machete o panga. Los vectores del vocabulario se calculan en la PC con
 * scripts/exportar_camara_clip.py.
 */
@Singleton
class ReconocedorConceptosClip @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ReconocedorConceptosLocal {

    private class Modelo(val sesion: OrtSession, val nombres: List<String>, val vectores: Array<FloatArray>, val escala: Float)

    private val entorno by lazy { OrtEnvironment.getEnvironment() }
    private val modelo by lazy {
        val sesion = entorno.createSession(context.assets.open(MODELO).use { it.readBytes() }, OrtSession.SessionOptions())
        val raiz = Json.parseToJsonElement(context.assets.open(CONCEPTOS).bufferedReader().use { it.readText() }).jsonObject
        val conceptos = raiz.getValue("conceptos").jsonArray.map { it.jsonObject }
        Modelo(
            sesion = sesion,
            nombres = conceptos.map { it.getValue("es").jsonPrimitive.content },
            vectores = conceptos.map { c -> c.getValue("vector").jsonArray.map { it.jsonPrimitive.float }.toFloatArray() }.toTypedArray(),
            escala = raiz.getValue("escala").jsonPrimitive.float
        )
    }

    override suspend fun reconocer(imagenBase64: String): List<ConceptoReconocido> =
        runCatching { clasificar(imagenBase64) }
            .onFailure { Log.w("AIK_CAMARA", "clip falló: ${it.javaClass.simpleName}: ${it.message}") }
            .getOrThrow()

    private suspend fun clasificar(imagenBase64: String): List<ConceptoReconocido> = withContext(Dispatchers.Default) {
        val bytes = Base64.decode(imagenBase64.substringAfter(","), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext emptyList()
        val m = modelo
        val entrada = OnnxTensor.createTensor(entorno, FloatBuffer.wrap(pixeles(bitmap)), longArrayOf(1, 3, LADO.toLong(), LADO.toLong()))
        val vector = entrada.use { t -> m.sesion.run(mapOf("pixel_values" to t)).use { r -> (r[0].value as Array<FloatArray>)[0] } }
        val logits = m.vectores.map { texto -> m.escala * texto.indices.sumOf { (texto[it] * vector[it]).toDouble() }.toFloat() }
        val maximo = logits.max()
        val exps = logits.map { exp((it - maximo).toDouble()) }
        val total = exps.sum()
        m.nombres.indices.sortedByDescending { logits[it] }.take(3)
            .map { ConceptoReconocido(m.nombres[it], (exps[it] / total).toFloat()) }
            .also { Log.d("AIK_CAMARA", "clip=" + it.joinToString { c -> "${c.espanol}:%.2f".format(c.confianza) }) }
    }

    /** Igual que el procesador de CLIP: lado corto a 224, recorte central y normalización por canal. */
    private fun pixeles(original: Bitmap): FloatArray {
        val escala = LADO.toFloat() / minOf(original.width, original.height)
        val ancho = max(LADO, (original.width * escala).toInt())
        val alto = max(LADO, (original.height * escala).toInt())
        val redimensionada = Bitmap.createScaledBitmap(original, ancho, alto, true)
        val recorte = Bitmap.createBitmap(redimensionada, (ancho - LADO) / 2, (alto - LADO) / 2, LADO, LADO)
        val colores = IntArray(LADO * LADO).also { recorte.getPixels(it, 0, LADO, 0, 0, LADO, LADO) }
        val salida = FloatArray(3 * LADO * LADO)
        val plano = LADO * LADO
        colores.forEachIndexed { i, c ->
            salida[i] = (((c shr 16) and 0xFF) / 255f - MEDIA[0]) / DESVIO[0]
            salida[plano + i] = (((c shr 8) and 0xFF) / 255f - MEDIA[1]) / DESVIO[1]
            salida[2 * plano + i] = ((c and 0xFF) / 255f - MEDIA[2]) / DESVIO[2]
        }
        return salida
    }

    private companion object {
        const val MODELO = "modelos/camara_clip_vision.onnx"
        const val CONCEPTOS = "modelos/camara_clip_conceptos.json"
        const val LADO = 224
        val MEDIA = floatArrayOf(0.48145466f, 0.4578275f, 0.40821073f)
        val DESVIO = floatArrayOf(0.26862954f, 0.26130258f, 0.27577711f)
    }
}
