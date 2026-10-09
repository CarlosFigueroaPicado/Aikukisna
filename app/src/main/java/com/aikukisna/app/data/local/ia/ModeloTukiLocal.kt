package com.aikukisna.app.data.local.ia

import android.content.Context
import com.aikukisna.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Progreso de descarga del modelo; [total] es 0 cuando el servidor no informa el tamaño. */
data class ProgresoModelo(val descargados: Long, val total: Long) {
    val fraccion: Float get() = if (total > 0) descargados.toFloat() / total else 0f
}

/**
 * Archivo del modelo Gemma 3 1B (formato .task de MediaPipe) que permite a Tuki
 * conversar sin conexión. Se descarga una sola vez y queda en el almacenamiento interno.
 */
@Singleton
class ModeloTukiLocal @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val carpeta = File(context.filesDir, "modelos")
    val archivo: File = File(carpeta, NOMBRE_ARCHIVO)
    private val parcial = File(carpeta, "$NOMBRE_ARCHIVO.part")

    fun estaDisponible(): Boolean = archivo.isFile && archivo.length() >= TAMANO_MINIMO_BYTES

    private val rutaIncluida = "$CARPETA_INCLUIDA/$NOMBRE_ARCHIVO"
    private val candadoCopia = Mutex()

    /**
     * true cuando el APK trae el modelo (Gradle lo descarga al compilar, ver app/build.gradle.kts): en zonas
     * con poca señal bajar ~550 MB no es viable, así que la app ya llega con Tuki sin conexión.
     */
    fun incluidoEnApp(): Boolean =
        runCatching { context.assets.list(CARPETA_INCLUIDA).orEmpty().contains(NOMBRE_ARCHIVO) }.getOrDefault(false)

    /**
     * MediaPipe necesita una ruta de archivo, no un asset: la primera vez se copia el modelo incluido al
     * almacenamiento interno (sin red). Devuelve false si el APK no lo trae.
     */
    suspend fun prepararDesdeApp(): Boolean = withContext(Dispatchers.IO) {
        candadoCopia.withLock {
            if (estaDisponible()) return@withLock true
            if (!incluidoEnApp()) return@withLock false
            carpeta.mkdirs()
            context.assets.open(rutaIncluida).use { entrada ->
                FileOutputStream(parcial, false).use { salida -> entrada.copyTo(salida, 1024 * 1024) }
            }
            check(parcial.length() >= TAMANO_MINIMO_BYTES) { "El modelo incluido en la app está incompleto" }
            archivo.delete()
            check(parcial.renameTo(archivo)) { "No se pudo preparar el modelo de Tuki" }
            carpeta.listFiles { f -> f.name.endsWith(".task") && f != archivo }?.forEach { it.delete() }
            true
        }
    }

    // Supabase gratis limita los archivos a 50 MB, por eso el modelo vive en un Release de GitHub.
    fun urlDescarga(): String = BuildConfig.TUKI_MODELO_URL.ifBlank { URL_PREDETERMINADA }

    /** Descarga reanudable: si se corta, el siguiente intento continúa desde el archivo parcial. */
    fun descargar(): Flow<ProgresoModelo> = flow {
        if (estaDisponible() || prepararDesdeApp()) {
            emit(ProgresoModelo(archivo.length(), archivo.length()))
            return@flow
        }
        carpeta.mkdirs()
        val yaDescargado = if (parcial.exists()) parcial.length() else 0L
        val conexion = (URL(urlDescarga()).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 60_000
            if (yaDescargado > 0) setRequestProperty("Range", "bytes=$yaDescargado-")
        }
        try {
            val codigo = conexion.responseCode
            val reanuda = codigo == HttpURLConnection.HTTP_PARTIAL
            check(codigo == HttpURLConnection.HTTP_OK || reanuda) {
                "No se pudo descargar el modelo de Tuki (código $codigo)"
            }
            val inicio = if (reanuda) yaDescargado else 0L
            val restante = conexion.contentLengthLong
            val total = if (restante > 0) inicio + restante else 0L
            var descargados = inicio
            var ultimoEmitido = 0L
            conexion.inputStream.use { entrada ->
                FileOutputStream(parcial, reanuda).use { salida ->
                    val buffer = ByteArray(256 * 1024)
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val leidos = entrada.read(buffer)
                        if (leidos < 0) break
                        salida.write(buffer, 0, leidos)
                        descargados += leidos
                        if (descargados - ultimoEmitido >= 2 * 1024 * 1024) {
                            ultimoEmitido = descargados
                            emit(ProgresoModelo(descargados, total))
                        }
                    }
                }
            }
            check(parcial.length() >= TAMANO_MINIMO_BYTES) {
                "El archivo del modelo descargado está incompleto"
            }
            archivo.delete()
            check(parcial.renameTo(archivo)) { "No se pudo guardar el modelo de Tuki" }
            // Al pasar del modelo base al ajustado, el anterior ocuparía ~550 MB sin usarse.
            carpeta.listFiles { f -> f.name.endsWith(".task") && f != archivo }?.forEach { it.delete() }
            emit(ProgresoModelo(archivo.length(), archivo.length()))
        } finally {
            conexion.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun eliminar() {
        archivo.delete()
        parcial.delete()
    }

    /** true cuando el modelo es el Gemma ajustado con el corpus de Aikukisna (sabe Miskito y Kriol). */
    val ajustado: Boolean get() = BuildConfig.TUKI_MODELO_AJUSTADO

    /** true cuando además se ajustó con diálogos de profesor: puede conversar, no solo traducir. */
    val tutor: Boolean get() = BuildConfig.TUKI_MODELO_AJUSTADO && BuildConfig.TUKI_MODELO_TUTOR

    companion object {
        val NOMBRE_ARCHIVO =
            when {
                BuildConfig.TUKI_MODELO_AJUSTADO && BuildConfig.TUKI_MODELO_TUTOR -> "gemma3-1b-aikukisna-tutor-int4.task"
                BuildConfig.TUKI_MODELO_AJUSTADO -> "gemma3-1b-aikukisna-int4.task"
                else -> "gemma3-1b-it-int4.task"
            }
        private val URL_PREDETERMINADA =
            "https://github.com/CarlosFigueroaPicado/Aikukisna/releases/download/" +
                (when {
                    BuildConfig.TUKI_MODELO_AJUSTADO && BuildConfig.TUKI_MODELO_TUTOR -> "modelo-tuki-v3-tutor"
                    BuildConfig.TUKI_MODELO_AJUSTADO -> "modelo-tuki-v2"
                    else -> "modelo-tuki-v1"
                }) + "/$NOMBRE_ARCHIVO"
        /** Carpeta de assets donde Gradle deja el modelo incluido. */
        const val CARPETA_INCLUIDA = "modelos_llm"
        // El modelo int4 pesa ~550 MB; algo mucho menor indica una descarga rota.
        private const val TAMANO_MINIMO_BYTES = 300L * 1024 * 1024
    }
}
