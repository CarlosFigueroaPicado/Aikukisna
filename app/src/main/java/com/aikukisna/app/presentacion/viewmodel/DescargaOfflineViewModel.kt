package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.repository.EstadoSincronizacion
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.SincronizarDatosOfflineUseCase
import com.aikukisna.app.domain.usecase.PrecargarPronunciacionesUseCase
import com.aikukisna.app.domain.usecase.ProgresoPronunciaciones
import com.aikukisna.app.data.local.SembradorReplicaSupabase
import com.aikukisna.app.data.sync.SyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DescargaOfflineViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sincronizarDatosOfflineUseCase: SincronizarDatosOfflineUseCase,
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val precargarPronunciacionesUseCase: PrecargarPronunciacionesUseCase,
    private val sembradorReplicaSupabase: SembradorReplicaSupabase
) : ViewModel() {

    var estado by mutableStateOf<EstadoSincronizacion?>(null)
        private set
    var lista by mutableStateOf(false)
        private set
    var progresoAudio by mutableStateOf(ProgresoPronunciaciones(0, 0, 0))
        private set
    var audioActivo by mutableStateOf(false)
        private set
    private var idiomaMetaId: Int? = null

    init {
        comprobarContenido()
    }

    private fun comprobarContenido() {
        viewModelScope.launch {
            try {
                val usuarioId = authRepository.usuarioActualId()

                idiomaMetaId = usuarioId?.let {
                    usuarioRepository.obtenerUsuario(it)?.idiomaMeta?.id
                }

                val contenidoListo =
                    sincronizarDatosOfflineUseCase.yaHayDatos(idiomaMetaId)

                if (!contenidoListo) {
                    sembradorReplicaSupabase.sembrarSiExiste()
                }

                val contenidoLocalDisponible =
                    sembradorReplicaSupabase.hayContenidoDisponible()

                if (contenidoLocalDisponible) {
                    // Ya existe contenido local suficiente.
                    // No esperamos a Supabase para permitir entrar.
                    mostrarHomeYPrepararAudio()

                    // La actualización remota queda en segundo plano.
                    SyncWorker.solicitarAhora(context)
                } else {
                    estado = EstadoSincronizacion.Error(
                        t(R.string.descargaoffline_no_se_pudo_preparar_el)
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                estado = EstadoSincronizacion.Error(
                    t(R.string.descargaoffline_no_se_pudo_preparar_el)
                )
            }
        }
    }

    fun reintentar() {
        if (estado is EstadoSincronizacion.EnProgreso) return
        comprobarContenido()
    }

    private fun descargar() {
        viewModelScope.launch {
            try {
                sincronizarDatosOfflineUseCase.invoke(idiomaMetaId).collect { nuevoEstado ->
                    estado = nuevoEstado
                    if (nuevoEstado is EstadoSincronizacion.Completado) {
                        mostrarHomeYPrepararAudio()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                estado = EstadoSincronizacion.Error(
                    t(R.string.descargaoffline_no_se_pudo_descargar_el)
                )
            }
        }
    }

    private fun precargarAudio() {
        if (audioActivo) return
        audioActivo = true
        viewModelScope.launch {
            try {
                precargarPronunciacionesUseCase(idiomaMetaId) { progreso ->
                    progresoAudio = progreso
                }
            } catch (e: Exception) {
                // El audio es complementario: si ElevenLabs o la red fallan,
                // el contenido de texto sigue disponible desde Home.
            } finally {
                audioActivo = false
            }
        }
    }

    private fun mostrarHomeYPrepararAudio() {
        // La pantalla bloqueante cubre solo la descarga de datos esenciales.
        // Las pronunciaciones continúan en segundo plano para no retrasar Home.
        lista = true
        precargarAudio()
    }
}
