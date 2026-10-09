package com.aikukisna.app.presentacion.viewmodel

import android.content.Context
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.data.local.PrimerUsoPreferencias
import com.aikukisna.app.data.local.ia.ModeloTukiLocal
import com.aikukisna.app.data.sync.DescargaModeloTukiWorker
import com.aikukisna.app.data.sync.EstadoDescargaModelo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Progreso de la descarga del modelo con el tiempo restante estimado. */
data class ProgresoDescargaUi(
    val estado: EstadoDescargaModelo,
    val fraccion: Float = 0f,
    val megasDescargados: Long = 0,
    val megasTotales: Long = TAMANO_ESTIMADO_MB,
    val segundosRestantes: Long? = null
) {
    companion object {
        const val TAMANO_ESTIMADO_MB = 546L
    }
}

@HiltViewModel
class PrimerUsoViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencias: PrimerUsoPreferencias,
    private val modelo: ModeloTukiLocal
) : ViewModel() {

    /** Ruta a la que va el usuario autenticado: términos, descarga del modelo o la app. */
    fun pasoPendiente(): PasoPrimerUso = when {
        !preferencias.terminosAceptados() -> PasoPrimerUso.TERMINOS
        // Si el APK trae el modelo no hay nada que descargar: se copia solo al arrancar.
        !modelo.estaDisponible() && !modelo.incluidoEnApp() && !preferencias.descargaModeloMostrada() -> PasoPrimerUso.DESCARGA_MODELO
        else -> PasoPrimerUso.NINGUNO
    }

    fun aceptarTerminos() = preferencias.aceptarTerminos()

    fun onboardingVisto() = preferencias.onboardingVisto()

    fun marcarOnboardingVisto() = preferencias.marcarOnboardingVisto()

    fun terminarPantallaDescarga() = preferencias.marcarDescargaModeloMostrada()

    fun descargarAhora(permitirDatosMoviles: Boolean) =
        DescargaModeloTukiWorker.programar(context, permitirDatosMoviles)

    // Muestras (tiempo, bytes) para estimar la velocidad con una media móvil.
    private var muestraInicial: Pair<Long, Long>? = null

    val progreso: StateFlow<ProgresoDescargaUi> = DescargaModeloTukiWorker.observar(context)
        .map { estado ->
            if (estado !is EstadoDescargaModelo.Descargando || estado.total <= 0) {
                if (estado !is EstadoDescargaModelo.Descargando) muestraInicial = null
                return@map ProgresoDescargaUi(estado)
            }
            val ahora = SystemClock.elapsedRealtime()
            val inicial = muestraInicial ?: (ahora to estado.descargados).also { muestraInicial = it }
            val segundos = (ahora - inicial.first) / 1000.0
            val bytesPorSegundo = if (segundos >= 3) (estado.descargados - inicial.second) / segundos else 0.0
            ProgresoDescargaUi(
                estado = estado,
                fraccion = estado.descargados.toFloat() / estado.total,
                megasDescargados = estado.descargados / MEGA,
                megasTotales = estado.total / MEGA,
                segundosRestantes = if (bytesPorSegundo > 0) {
                    ((estado.total - estado.descargados) / bytesPorSegundo).toLong()
                } else null
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgresoDescargaUi(EstadoDescargaModelo.NoDescargado))

    private companion object {
        const val MEGA = 1024L * 1024
    }
}

enum class PasoPrimerUso { TERMINOS, DESCARGA_MODELO, NINGUNO }
