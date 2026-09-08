package com.aikukisna.app.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object PerfilFotoStorage {
    suspend fun guardar(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            val options = BitmapFactory.Options().apply {
                inSampleSize = calcularMuestra(bounds.outWidth, bounds.outHeight)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return@runCatching null
            val directory = File(context.filesDir, "profile_photos").apply { mkdirs() }
            val output = File(directory, "${UUID.randomUUID()}.jpg")
            FileOutputStream(output).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)) {
                    "No se pudo comprimir la foto de perfil"
                }
            }
            bitmap.recycle()
            output.absolutePath
        }.getOrNull()
    }

    suspend fun leerReducida(context: Context, location: String, maxDimension: Int = 512): Bitmap? =
        withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                abrir(context, location)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null
                val options = BitmapFactory.Options().apply {
                    inSampleSize = calcularMuestra(bounds.outWidth, bounds.outHeight, maxDimension)
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                abrir(context, location)?.use { BitmapFactory.decodeStream(it, null, options) }
            }
        }.getOrNull()

    private fun calcularMuestra(width: Int, height: Int, maxDimension: Int = 1600): Int {
        var sample = 1
        while (width / sample > maxDimension || height / sample > maxDimension) sample *= 2
        return sample
    }

    private fun abrir(context: Context, location: String) =
        if (location.startsWith("content://") || location.startsWith("file://")) {
            context.contentResolver.openInputStream(Uri.parse(location))
        } else {
            File(location).takeIf { it.isFile }?.inputStream()
        }
}
