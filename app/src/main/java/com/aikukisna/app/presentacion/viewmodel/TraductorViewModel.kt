package com.aikukisna.app.presentacion.viewmodel

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.usecase.TraducirOracionUseCase
import com.aikukisna.app.domain.usecase.TranscribirAudioUseCase
import com.aikukisna.app.domain.usecase.ObtenerAudioPronunciacionUseCase
import com.aikukisna.app.domain.model.SolicitudPronunciacion
import com.aikukisna.app.data.local.SpeechRecognitionManager
import com.aikukisna.app.data.local.SembradorReplicaSupabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/** Un turno del modo conversación: [deOrigen] indica si habló la persona del idioma de origen. */
data class TurnoConversacion(
    val deOrigen: Boolean,
    val original: String,
    val idiomaOriginal: Idioma,
    val idiomaTraducido: Idioma,
    val resultado: ResultadoTraduccion?,
    val error: String? = null
)

@HiltViewModel
class TraductorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository,
    private val traducirOracionUseCase: TraducirOracionUseCase,
    private val transcribirAudioUseCase: TranscribirAudioUseCase,
    private val obtenerAudioPronunciacionUseCase: ObtenerAudioPronunciacionUseCase,
    private val speechRecognitionManager: SpeechRecognitionManager,
    private val sembradorReplica: SembradorReplicaSupabase,
    private val preferencias: PreferenciasAprendizaje
) : ViewModel() {

    var idiomaOrigen by mutableStateOf(Idioma.DISPONIBLES.first { it.codigo == "es" })
        private set
    var idiomaDestino by mutableStateOf(Idioma.DISPONIBLES.first { it.codigo == "mi" })
        private set
    var texto by mutableStateOf("")
        private set
    var resultado by mutableStateOf<ResultadoTraduccion?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isPreparing by mutableStateOf(true)
        private set
    var isTranscribing by mutableStateOf(false)
        private set
    var isPronouncing by mutableStateOf(false)
        private set
    var reconociendoVoz by mutableStateOf(false)
        private set
    var audioPronunciacion by mutableStateOf<ByteArray?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var modoConversacion by mutableStateOf(false)
        private set
    var conversacion by mutableStateOf<List<TurnoConversacion>>(emptyList())
        private set
    /** En conversación, quién está hablando ahora: true = idioma de origen. */
    var hablaOrigen by mutableStateOf(true)
        private set

    private val idiomaEntrada: Idioma
        get() = if (!modoConversacion || hablaOrigen) idiomaOrigen else idiomaDestino

    init {
        viewModelScope.launch {
            try {
                sembradorReplica.sembrarSiExiste()
                check(sembradorReplica.hayContenidoDisponible()) {
                    t(R.string.traductor_el_contenido_local_todavia_no)
                }
                val userId = authRepository.usuarioActualId()
                val idiomaMeta = userId?.let { usuarioRepository.obtenerUsuario(it)?.idiomaMeta }
                if (idiomaMeta?.codigo == "es") {
                    // Quien aprende español traduce desde su lengua de apoyo (Miskito o Kriol).
                    idiomaOrigen = Idioma.DISPONIBLES.firstOrNull { it.id == preferencias.lenguaApoyoId() }
                        ?: Idioma.DISPONIBLES.first { it.codigo == "mi" }
                    idiomaDestino = idiomaMeta
                } else if (idiomaMeta != null) {
                    idiomaDestino = idiomaMeta
                } else {
                    errorMessage = t(R.string.traductor_selecciona_un_idioma_objetivo_en)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.traductor_no_se_pudo_cargar_el)
            } finally {
                isPreparing = false
            }
        }
    }

    fun onTextoChange(valor: String) {
        texto = valor
        resultado = null
        errorMessage = null
    }

    fun onSeleccionarOrigen(idioma: Idioma) {
        idiomaOrigen = idioma
        resultado = null
        errorMessage = null
    }

    fun onSeleccionarDestino(idioma: Idioma) {
        idiomaDestino = idioma
        resultado = null
        errorMessage = null
    }

    fun intercambiarIdiomas() {
        resultado?.takeIf { it.alternativas.isEmpty() }?.let { texto = it.texto }
        val temp = idiomaOrigen
        idiomaOrigen = idiomaDestino
        idiomaDestino = temp
        resultado = null
        errorMessage = null
    }

    fun traducir() {
        if (texto.isBlank() || isLoading || isPreparing) return
        val textoSolicitud = texto
        val origenSolicitud = idiomaOrigen.id
        val destinoSolicitud = idiomaDestino.id
        fun solicitudVigente() = texto == textoSolicitud &&
            idiomaOrigen.id == origenSolicitud && idiomaDestino.id == destinoSolicitud
        isLoading = true
        resultado = null
        viewModelScope.launch {
            errorMessage = null
            try {
                val traduccion = traducirOracionUseCase(textoSolicitud, origenSolicitud, destinoSolicitud)
                if (solicitudVigente()) resultado = traduccion
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (solicitudVigente()) errorMessage = e.message ?: t(R.string.traductor_no_se_pudo_traducir)
            } finally {
                isLoading = false
            }
        }
    }

    fun alternarModoConversacion() {
        modoConversacion = !modoConversacion
        resultado = null
        errorMessage = null
    }

    fun seleccionarHablante(origen: Boolean) {
        hablaOrigen = origen
    }

    fun limpiarConversacion() {
        conversacion = emptyList()
    }

    /** Traduce lo que dijo el hablante actual hacia el idioma de la otra persona. */
    fun enviarTurno() {
        if (texto.isBlank() || isLoading || isPreparing) return
        val original = texto.trim()
        val deOrigen = hablaOrigen
        val desde = if (deOrigen) idiomaOrigen else idiomaDestino
        val hacia = if (deOrigen) idiomaDestino else idiomaOrigen
        texto = ""
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val turno = try {
                TurnoConversacion(deOrigen, original, desde, hacia, traducirOracionUseCase(original, desde.id, hacia.id))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                TurnoConversacion(deOrigen, original, desde, hacia, null, e.message ?: t(R.string.traductor_no_se_pudo_traducir))
            } finally {
                isLoading = false
            }
            conversacion = conversacion + turno
            // Lo natural es que responda la otra persona.
            hablaOrigen = !deOrigen
        }
    }

    fun escucharTurno(turno: TurnoConversacion) {
        val traducido = turno.resultado?.texto?.trim().orEmpty()
        if (traducido.isBlank() || isPronouncing) return
        viewModelScope.launch {
            isPronouncing = true
            errorMessage = null
            try {
                val pronunciacion = obtenerAudioPronunciacionUseCase(
                    SolicitudPronunciacion(texto = traducido, idioma = turno.idiomaTraducido)
                )
                audioPronunciacion = pronunciacion.audio
                if (pronunciacion.audio == null) {
                    errorMessage = t(R.string.traductor_no_hay_una_pronunciacion_verificada)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.traductor_no_se_pudo_reproducir_la)
            } finally {
                isPronouncing = false
            }
        }
    }

    fun transcribirAudio(audioBase64: String) {
        if (isTranscribing) return
        viewModelScope.launch {
            isTranscribing = true
            errorMessage = null
            try {
                texto = transcribirAudioUseCase(
                    audioBase64 = audioBase64,
                    idiomaCodigo = idiomaEntrada.codigo
                )
                resultado = null
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.traductor_no_se_pudo_reconocer_el)
            } finally {
                isTranscribing = false
            }
        }
    }

    fun alternarReconocimientoVoz() {
        if (reconociendoVoz) {
            speechRecognitionManager.detener { reconociendoVoz = it }
            return
        }
        errorMessage = null
        speechRecognitionManager.iniciar(
            codigoIdioma = idiomaEntrada.codigo,
            alResultado = {
                texto = it
                resultado = null
            },
            alError = { errorMessage = it },
            alCambiarEstado = { reconociendoVoz = it }
        )
    }

    fun escucharPronunciacion() {
        val textoTraducido = resultado?.texto?.trim().orEmpty()
        if (textoTraducido.isBlank() || isPronouncing) return
        viewModelScope.launch {
            isPronouncing = true
            errorMessage = null
            audioPronunciacion = null
            try {
                val pronunciacion = obtenerAudioPronunciacionUseCase(
                    SolicitudPronunciacion(
                        texto = textoTraducido,
                        idioma = idiomaDestino
                    )
                )
                audioPronunciacion = pronunciacion.audio
                if (pronunciacion.audio == null) {
                    errorMessage = t(R.string.traductor_no_hay_una_pronunciacion_verificada)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: t(R.string.traductor_no_se_pudo_reproducir_la)
            } finally {
                isPronouncing = false
            }
        }
    }

    fun consumirAudioPronunciacion() {
        audioPronunciacion = null
    }

    override fun onCleared() {
        speechRecognitionManager.liberar()
        super.onCleared()
    }
}
