package com.aikukisna.app.data.local

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object MlKitOfflineHelper {
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

    suspend fun etiquetar(labeler: ImageLabeler, image: InputImage): List<ImageLabel> =
        suspendCancellableCoroutine { continuation ->
            labeler.process(image)
                .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        }
}
