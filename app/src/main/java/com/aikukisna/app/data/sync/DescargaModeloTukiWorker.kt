package com.aikukisna.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aikukisna.app.data.local.ia.ModeloTukiLocal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Descarga en segundo plano el modelo Gemma de Tuki. La descarga es reanudable:
 * si WorkManager detiene el trabajo, el reintento continúa desde el archivo parcial.
 */
class DescargaModeloTukiWorker(
    context: Context,
    parametros: WorkerParameters
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        val modelo = ModeloTukiLocal(applicationContext)
        if (modelo.estaDisponible()) return Result.success()
        return try {
            modelo.descargar().collect { progreso ->
                setProgress(workDataOf(CLAVE_DESCARGADOS to progreso.descargados, CLAVE_TOTAL to progreso.total))
            }
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            if (runAttemptCount < MAXIMO_REINTENTOS) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val NOMBRE_TRABAJO = "descarga_modelo_tuki"
        private const val CLAVE_DESCARGADOS = "descargados"
        private const val CLAVE_TOTAL = "total"
        private const val MAXIMO_REINTENTOS = 20

        /** Por defecto espera Wi-Fi: el modelo pesa ~550 MB. */
        fun programar(context: Context, permitirDatosMoviles: Boolean = false) {
            if (ModeloTukiLocal(context).estaDisponible()) return
            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(if (permitirDatosMoviles) NetworkType.CONNECTED else NetworkType.UNMETERED)
                .setRequiresStorageNotLow(true)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                NOMBRE_TRABAJO,
                if (permitirDatosMoviles) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<DescargaModeloTukiWorker>()
                    .setConstraints(restricciones)
                    .build()
            )
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(NOMBRE_TRABAJO)
        }

        fun observar(context: Context): Flow<EstadoDescargaModelo> =
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(NOMBRE_TRABAJO).map { trabajos ->
                val modelo = ModeloTukiLocal(context)
                val trabajo = trabajos.firstOrNull()
                when {
                    modelo.estaDisponible() -> EstadoDescargaModelo.Listo
                    trabajo == null -> EstadoDescargaModelo.NoDescargado
                    trabajo.state == WorkInfo.State.RUNNING -> EstadoDescargaModelo.Descargando(
                        trabajo.progress.getLong(CLAVE_DESCARGADOS, 0L),
                        trabajo.progress.getLong(CLAVE_TOTAL, 0L)
                    )
                    trabajo.state == WorkInfo.State.ENQUEUED -> EstadoDescargaModelo.EsperandoRed
                    trabajo.state == WorkInfo.State.FAILED -> EstadoDescargaModelo.Error
                    else -> EstadoDescargaModelo.NoDescargado
                }
            }
    }
}

sealed interface EstadoDescargaModelo {
    data object NoDescargado : EstadoDescargaModelo
    data object EsperandoRed : EstadoDescargaModelo
    data class Descargando(val descargados: Long, val total: Long) : EstadoDescargaModelo
    data object Listo : EstadoDescargaModelo
    data object Error : EstadoDescargaModelo
}
