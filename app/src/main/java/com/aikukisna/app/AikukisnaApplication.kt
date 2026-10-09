package com.aikukisna.app

import android.app.Application
import android.util.Log
import com.aikukisna.app.data.local.SembradorAudiosHumanos
import com.aikukisna.app.data.local.SembradorReplicaSupabase
import com.aikukisna.app.data.local.dao.MemoriaTukiLocalDao
import com.aikukisna.app.data.sync.DescargaModeloTukiWorker
import com.aikukisna.app.data.sync.SyncWorker
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.SincronizarFavoritosPendientesUseCase
import com.aikukisna.app.domain.usecase.SincronizarLeccionesPendientesUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface EstadoContenidoInicial {
    data object Preparando : EstadoContenidoInicial
    data object Disponible : EstadoContenidoInicial
    data object NoDisponible : EstadoContenidoInicial
}

@HiltAndroidApp
class AikukisnaApplication : Application() {

    @Inject
    lateinit var sembradorReplicaSupabase: SembradorReplicaSupabase

    @Inject lateinit var sembradorAudiosHumanos: SembradorAudiosHumanos

    @Inject lateinit var modeloTukiLocal: com.aikukisna.app.data.local.ia.ModeloTukiLocal

    @Inject lateinit var sincronizarLeccionesPendientesUseCase: SincronizarLeccionesPendientesUseCase
    @Inject lateinit var sincronizarFavoritosPendientesUseCase: SincronizarFavoritosPendientesUseCase
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var usuarioRepository: UsuarioRepository
    @Inject lateinit var memoriaTukiLocalDao: MemoriaTukiLocalDao

    private val scopeInicializacion = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _estadoContenidoInicial =
        MutableStateFlow<EstadoContenidoInicial>(EstadoContenidoInicial.Preparando)
    val estadoContenidoInicial = _estadoContenidoInicial.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        com.aikukisna.app.presentacion.idioma.IdiomaInterfaz.envolver(this)
        // El modelo incluido se copia en paralelo a la carga de datos (unos segundos, sin red).
        scopeInicializacion.launch {
            try {
                modeloTukiLocal.prepararDesdeApp()
            } catch (e: Exception) {
                Log.e(ETIQUETA, "No se pudo preparar el modelo incluido; tipo=${e.javaClass.simpleName}")
            }
        }
        scopeInicializacion.launch {
            try {
                sembradorReplicaSupabase.sembrarSiExiste()
            } catch (e: Exception) {
                Log.e(ETIQUETA, "No se pudo preparar el contenido offline; tipo=${e.javaClass.simpleName}")
            }
            _estadoContenidoInicial.value = if (sembradorReplicaSupabase.hayContenidoDisponible()) {
                EstadoContenidoInicial.Disponible
            } else {
                EstadoContenidoInicial.NoDisponible
            }
            if (_estadoContenidoInicial.value == EstadoContenidoInicial.Disponible) {
                try {
                    sembradorAudiosHumanos.sembrarSiCambio()
                } catch (e: Exception) {
                    Log.e(ETIQUETA, "No se pudieron registrar los audios de hablantes; tipo=${e.javaClass.simpleName}")
                }
                SyncWorker.programar(applicationContext)
                // Sin modelo incluido en el APK, el de Tuki offline se baja solo cuando hay Wi-Fi.
                if (!modeloTukiLocal.incluidoEnApp()) DescargaModeloTukiWorker.programar(applicationContext)
            }
        }
    }

    private companion object {
        const val ETIQUETA = "AikukisnaInicio"
    }
}
