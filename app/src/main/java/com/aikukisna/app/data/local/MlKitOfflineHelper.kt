package com.aikukisna.app.data.local

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.aikukisna.app.domain.repository.RegionObjeto
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object MlKitOfflineHelper {
    // Se crean una sola vez: crearlos en cada toque agregaba retraso a la cámara.
    private val etiquetador by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.Builder().setConfidenceThreshold(0.4f).build())
    }

    /** EfficientNet-Lite2 (ImageNet, 1000 objetos): mucho más específico que el etiquetador base. */
    private val clasificador by lazy {
        val modelo = LocalModel.Builder().setAssetFilePath(MODELO_CLASIFICADOR).build()
        ImageLabeling.getClient(
            CustomImageLabelerOptions.Builder(modelo)
                .setConfidenceThreshold(0.15f)
                .setMaxResultCount(5)
                .build()
        )
    }
    private val detector by lazy {
        ObjectDetection.getClient(
            ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableMultipleObjects()
                .build()
        )
    }

    fun imagenDesdeBase64(imagenBase64: String): InputImage {
        val limpia = imagenBase64.substringAfter(",")
        val bytes = Base64.decode(limpia, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("No se pudo leer la imagen")
        return InputImage.fromBitmap(bitmap, 0)
    }

    suspend fun reconocerTexto(recognizer: TextRecognizer, image: InputImage): String =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(it.text) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }

    /**
     * Etiquetas en inglés de la más confiable a la menos. Primero las del clasificador específico
     * ("golden retriever", "coffee mug"); las genéricas del etiquetador base ("Furniture") quedan
     * como respaldo.
     */
    suspend fun reconocerObjetos(image: InputImage): List<String> {
        val especificas = runCatching { etiquetar(clasificador, image) }.getOrDefault(emptyList())
            .sortedByDescending { it.confidence }
        val generales = etiquetar(etiquetador, image).sortedByDescending { it.confidence }
        // ImageNet siempre elige alguna de sus 1000 clases aunque el objeto no esté entre ellas
        // (no tiene "persona", "mesa" ni "cuaderno"): una etiqueta específica dudosa no debe
        // ganarle a una general segura. Orden: específicas fiables, generales seguras, el resto.
        android.util.Log.d(
            "AIK_CAMARA",
            "especificas=${especificas.map { "${it.text}:%.2f".format(it.confidence) }} " +
                "generales=${generales.map { "${it.text}:%.2f".format(it.confidence) }}"
        )
        val (especificasFiables, especificasDudosas) = especificas.partition { it.confidence >= CONFIANZA_ESPECIFICA }
        val (generalesSeguras, generalesDudosas) = generales.partition { it.confidence >= CONFIANZA_GENERAL }
        // Fondo, escena o categoría ("Rock", "Food", "Product") no es el objeto que el estudiante señala.
        val (escena, objetos) = (especificasFiables + generalesSeguras + especificasDudosas + generalesDudosas)
            .partition { it.text.lowercase() in ETIQUETAS_ESCENA }
        return (objetos + escena)
            .map { it.text }
            .distinctBy { it.lowercase() }
    }

    private suspend fun etiquetar(cliente: ImageLabeler, image: InputImage): List<ImageLabel> =
        suspendCancellableCoroutine { continuation ->
            cliente.process(image)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }

    suspend fun detectarRegiones(image: InputImage): List<RegionObjeto> =
        suspendCancellableCoroutine { continuation ->
            detector.process(image)
                .addOnSuccessListener { objetos ->
                    if (continuation.isActive) {
                        val ancho = image.width.toFloat()
                        val alto = image.height.toFloat()
                        continuation.resume(objetos.mapNotNull { objeto ->
                            val caja = objeto.boundingBox
                            if (caja.width() <= 0 || caja.height() <= 0) return@mapNotNull null
                            RegionObjeto(
                                izquierda = (caja.left / ancho).coerceIn(0f, 1f),
                                arriba = (caja.top / alto).coerceIn(0f, 1f),
                                derecha = (caja.right / ancho).coerceIn(0f, 1f),
                                abajo = (caja.bottom / alto).coerceIn(0f, 1f)
                            )
                        })
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resumeWithException(it)
                }
        }

    private const val MODELO_CLASIFICADOR = "modelos/clasificador_objetos.tflite"
    private const val CONFIANZA_ESPECIFICA = 0.35f
    private const val CONFIANZA_GENERAL = 0.6f
    private val ETIQUETAS_ESCENA = setOf(
        "food", "cuisine", "tableware", "product", "fun", "leisure", "vacation", "event", "monochrome",
        "pattern", "metal", "soil", "sky", "rock", "beach", "mountain", "field", "smile", "sitting", "flesh",
        "crowd", "race", "building", "sand", "cliff", "porcelain", "plant", "fruit", "vegetable", "pet", "fur"
    )
}
