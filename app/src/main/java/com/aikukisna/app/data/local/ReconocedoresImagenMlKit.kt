package com.aikukisna.app.data.local

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import com.aikukisna.app.domain.repository.RecortadorImagen

import com.aikukisna.app.domain.repository.ReconocedorObjetosLocal
import com.aikukisna.app.domain.repository.DetectorObjetosLocal
import com.aikukisna.app.domain.repository.RegionObjeto
import com.aikukisna.app.domain.repository.ReconocedorTextoLocal
import com.aikukisna.app.domain.repository.LineaTexto
import com.aikukisna.app.domain.repository.TextoEnImagen
import com.aikukisna.app.domain.repository.elegirTextoTocado
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import javax.inject.Inject

class ReconocedorObjetosMlKit @Inject constructor() : ReconocedorObjetosLocal {
    override suspend fun reconocer(imagenBase64: String): List<String> {
        val imagen = MlKitOfflineHelper.imagenDesdeBase64(imagenBase64)
        return MlKitOfflineHelper.reconocerObjetos(imagen)
    }
}

class DetectorObjetosMlKit @Inject constructor() : DetectorObjetosLocal {
    override suspend fun detectar(imagenBase64: String): List<RegionObjeto> =
        MlKitOfflineHelper.detectarRegiones(MlKitOfflineHelper.imagenDesdeBase64(imagenBase64))
}

class ReconocedorTextoMlKit @Inject constructor() : ReconocedorTextoLocal {
    override suspend fun reconocer(imagenBase64: String): String {
        val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val imagen = MlKitOfflineHelper.imagenDesdeBase64(imagenBase64)
            MlKitOfflineHelper.reconocerTexto(reconocedor, imagen)
        } finally {
            reconocedor.close()
        }
    }

    override suspend fun reconocerEnPunto(imagenBase64: String, x: Float, y: Float): TextoEnImagen {
        val reconocedor = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val imagen = MlKitOfflineHelper.imagenDesdeBase64(imagenBase64)
            val ancho = imagen.width.toFloat()
            val alto = imagen.height.toFloat()
            val lineas = MlKitOfflineHelper.procesarTexto(reconocedor, imagen).textBlocks.flatMapIndexed { indice, bloque ->
                bloque.lines.mapNotNull { linea ->
                    val caja = linea.boundingBox ?: return@mapNotNull null
                    LineaTexto(
                        texto = linea.text,
                        region = RegionObjeto(caja.left / ancho, caja.top / alto, caja.right / ancho, caja.bottom / alto),
                        bloque = indice
                    )
                }
            }
            elegirTextoTocado(lineas, x, y) ?: TextoEnImagen("", null)
        } finally {
            reconocedor.close()
        }
    }
}

class RecortadorImagenAndroid @Inject constructor() : RecortadorImagen {
    override fun recortar(imagenBase64: String, region: RegionObjeto): String {
        val bytes = Base64.decode(imagenBase64.substringAfter(","), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("No se pudo leer la imagen")
        val izquierda = (region.izquierda * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val arriba = (region.arriba * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val derecha = (region.derecha * bitmap.width).toInt().coerceIn(izquierda + 1, bitmap.width)
        val abajo = (region.abajo * bitmap.height).toInt().coerceIn(arriba + 1, bitmap.height)
        val recorte = Bitmap.createBitmap(bitmap, izquierda, arriba, derecha - izquierda, abajo - arriba)
        return ByteArrayOutputStream().use { salida ->
            recorte.compress(Bitmap.CompressFormat.JPEG, 92, salida)
            if (recorte !== bitmap) recorte.recycle()
            bitmap.recycle()
            Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
        }
    }
}
