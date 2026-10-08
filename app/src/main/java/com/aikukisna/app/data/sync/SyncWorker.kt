package com.aikukisna.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.aikukisna.app.AikukisnaApplication
import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.repository.EstadoSincronizacion
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SyncWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val aplicacion = applicationContext as? AikukisnaApplication ?: return Result.failure()
        // El trabajo periódico y el inmediato pueden coincidir: solo uno descarga a la vez.
        if (!enCurso.compareAndSet(false, true)) return Result.success()
        return try {
            sincronizar(aplicacion)
        } finally {
            enCurso.set(false)
        }
    }

    private suspend fun sincronizar(aplicacion: AikukisnaApplication): Result {
        return runCatching {
            sincronizarContenido(aplicacion)
            aplicacion.sincronizarLeccionesPendientesUseCase()
            aplicacion.sincronizarFavoritosPendientesUseCase()
            sincronizarMemoria(aplicacion)
            Result.success()
        }.getOrElse {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun sincronizarContenido(aplicacion: AikukisnaApplication) {
        val usuarioId = aplicacion.authRepository.usuarioActualId() ?: return
        aplicacion.usuarioRepository.sincronizarPerfilPendiente(usuarioId)
        val idiomaMetaId = aplicacion.usuarioRepository.obtenerUsuario(usuarioId)?.idiomaMeta?.id
        aplicacion.sincronizarDatosOfflineUseCase.invoke(idiomaMetaId).collect { estado ->
            if (estado is EstadoSincronizacion.Error) {
                throw IllegalStateException("La actualización remota quedó pendiente")
            }
        }
    }

    private suspend fun sincronizarMemoria(aplicacion: AikukisnaApplication) {
        val usuarioActual = aplicacion.authRepository.usuarioActualId() ?: return
        aplicacion.memoriaTukiLocalDao.obtenerPendientes(100)
            .filter { it.usuarioId == usuarioActual.toString() }
            .forEach { memoria ->
                aplicacion.usuarioRepository.guardarMemoriaTuki(
                    MemoriaTuki(
                        id = 0,
                        usuarioId = UUID.fromString(memoria.usuarioId),
                        tipo = memoria.tipo,
                        resumen = memoria.resumen,
                        fecha = Instant.ofEpochMilli(memoria.fechaEpochMs)
                    )
                )
                aplicacion.memoriaTukiLocalDao.marcarSincronizada(memoria.id)
            }
    }

    companion object {
        private const val NOMBRE_TRABAJO = "sincronizacion_aikukisna"
        private val enCurso = AtomicBoolean(false)

        fun programar(context: Context) {
            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            // La primera ejecución la cubre solicitarAhora(); el periódico empieza después.
            val periodico = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setConstraints(restricciones)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOMBRE_TRABAJO,
                ExistingPeriodicWorkPolicy.KEEP,
                periodico
            )
            solicitarAhora(context)
        }

        fun solicitarAhora(context: Context) {
            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "${NOMBRE_TRABAJO}_inmediata",
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(restricciones)
                    .build()
            )
        }
    }
}
