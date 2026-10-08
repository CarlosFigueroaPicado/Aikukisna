package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.model.TipoTraduccion
import com.aikukisna.app.presentacion.viewmodel.TraductorViewModel
import com.aikukisna.app.presentacion.viewmodel.TurnoConversacion
import com.aikukisna.app.ui.theme.AikukisnaTheme
import java.io.File
import com.aikukisna.app.presentacion.audio.AudioPlayer

@Composable
fun TraductorScreen(
    viewModel: TraductorViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = remember(context) { AudioPlayer(context.applicationContext) }
    val grabador = remember { GrabadorAudio(context) }
    var grabandoArchivo by remember { mutableStateOf(false) }
    val permisoMicrofono = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) grabandoArchivo = grabador.iniciar()
    }

    DisposableEffect(Unit) {
        onDispose {
            grabador.cancelar()
            audioPlayer.liberar()
        }
    }

    LaunchedEffect(viewModel.audioPronunciacion) {
        viewModel.audioPronunciacion?.let { audio ->
            audioPlayer.reproducir(audio)
            viewModel.consumirAudioPronunciacion()
        }
    }

    fun alternarGrabacion() {
        if (grabandoArchivo) {
            grabandoArchivo = false
            grabador.detener()?.let(viewModel::transcribirAudio)
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            grabandoArchivo = grabador.iniciar()
        } else {
            permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    TraductorScreenContenido(
        idiomaOrigen = viewModel.idiomaOrigen,
        idiomaDestino = viewModel.idiomaDestino,
        onSeleccionarOrigen = viewModel::onSeleccionarOrigen,
        onSeleccionarDestino = viewModel::onSeleccionarDestino,
        onIntercambiar = viewModel::intercambiarIdiomas,
        texto = viewModel.texto,
        onTextoChange = viewModel::onTextoChange,
        resultado = viewModel.resultado,
        isLoading = viewModel.isLoading || viewModel.isPreparing,
        isTranscribing = viewModel.isTranscribing,
        isPronouncing = viewModel.isPronouncing,
        grabando = grabandoArchivo,
        errorMessage = viewModel.errorMessage,
        onTraducirClick = viewModel::traducir,
        onMicrofonoClick = ::alternarGrabacion,
        onEscucharClick = viewModel::escucharPronunciacion,
        onVolver = onVolver,
        modoConversacion = viewModel.modoConversacion,
        onAlternarModo = viewModel::alternarModoConversacion,
        conversacion = viewModel.conversacion,
        hablaOrigen = viewModel.hablaOrigen,
        onSeleccionarHablante = viewModel::seleccionarHablante,
        onEnviarTurno = viewModel::enviarTurno,
        onEscucharTurno = viewModel::escucharTurno,
        onLimpiarConversacion = viewModel::limpiarConversacion
    )
}

@Composable
private fun TraductorScreenContenido(
    idiomaOrigen: Idioma,
    idiomaDestino: Idioma,
    onSeleccionarOrigen: (Idioma) -> Unit,
    onSeleccionarDestino: (Idioma) -> Unit,
    onIntercambiar: () -> Unit,
    texto: String,
    onTextoChange: (String) -> Unit,
    resultado: ResultadoTraduccion?,
    isLoading: Boolean,
    isTranscribing: Boolean = false,
    isPronouncing: Boolean = false,
    grabando: Boolean = false,
    errorMessage: String?,
    onTraducirClick: () -> Unit,
    onMicrofonoClick: () -> Unit = {},
    onEscucharClick: () -> Unit = {},
    onVolver: () -> Unit,
    modoConversacion: Boolean = false,
    onAlternarModo: () -> Unit = {},
    conversacion: List<TurnoConversacion> = emptyList(),
    hablaOrigen: Boolean = true,
    onSeleccionarHablante: (Boolean) -> Unit = {},
    onEnviarTurno: () -> Unit = {},
    onEscucharTurno: (TurnoConversacion) -> Unit = {},
    onLimpiarConversacion: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = t(R.string.traductor_volver),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = t(R.string.traductor_traductor),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        SelectorModo(modoConversacion = modoConversacion, onAlternar = onAlternarModo)

        if (modoConversacion) {
            ConversacionContenido(
                idiomaOrigen = idiomaOrigen,
                idiomaDestino = idiomaDestino,
                onSeleccionarOrigen = onSeleccionarOrigen,
                onSeleccionarDestino = onSeleccionarDestino,
                conversacion = conversacion,
                hablaOrigen = hablaOrigen,
                onSeleccionarHablante = onSeleccionarHablante,
                texto = texto,
                onTextoChange = onTextoChange,
                isLoading = isLoading,
                isTranscribing = isTranscribing,
                isPronouncing = isPronouncing,
                grabando = grabando,
                errorMessage = errorMessage,
                onEnviar = onEnviarTurno,
                onMicrofonoClick = onMicrofonoClick,
                onEscucharTurno = onEscucharTurno,
                onLimpiar = onLimpiarConversacion
            )
        } else {
        SelectorIdiomaCompacto(
            idiomaSeleccionado = idiomaOrigen,
            onSeleccionar = onSeleccionarOrigen,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            BasicTextField(
                value = texto,
                onValueChange = onTextoChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth().padding(end = 64.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (texto.isNotBlank()) onTraducirClick() }),
                decorationBox = { campo ->
                    if (texto.isEmpty()) {
                        Text(
                            text = t(R.string.traductor_escribe_o_habla_en, idiomaOrigen.nombre),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    campo()
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.microphone),
                    contentDescription = if (grabando) t(R.string.traductor_detener_dictado) else t(R.string.traductor_iniciar_dictado),
                    tint = if (grabando) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = !isTranscribing, onClick = onMicrofonoClick)
                )
                Icon(
                    painter = painterResource(id = R.drawable.send),
                    contentDescription = t(R.string.traductor_traducir),
                    tint = if (texto.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(enabled = texto.isNotBlank() && !isLoading && !isTranscribing, onClick = onTraducirClick)
                )
            }
        }

        if (grabando || isTranscribing) {
            Text(
                text = if (grabando) t(R.string.traductor_escuchando_toca_el_microfono_para) else t(R.string.traductor_transcribiendo_audio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else if (idiomaOrigen.codigo == "mi" || Idioma.esKriol(idiomaOrigen.codigo)) {
            Text(
                text = t(R.string.traductor_el_dictado_sin_conexion_en, idiomaOrigen.nombre),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onIntercambiar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bidirectional_vertical),
                    contentDescription = t(R.string.traductor_intercambiar_idiomas),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        SelectorIdiomaCompacto(
            idiomaSeleccionado = idiomaDestino,
            onSeleccionar = onSeleccionarDestino,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 13.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                resultado != null -> Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            resultado.texto,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (resultado.alternativas.isEmpty()) Icon(
                            painter = painterResource(id = R.drawable.volume),
                            contentDescription = t(R.string.traductor_escuchar_pronunciacion),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(enabled = !isPronouncing, onClick = onEscucharClick)
                        )
                    }
                    resultado.alternativas.forEach { alternativa ->
                        Text(
                            text = t(R.string.traductor_texto, alternativa),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
                else -> Text(t(R.string.traductor_la_traduccion_aparecera_aqui), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        resultado?.let {
            Spacer(modifier = Modifier.height(6.dp))
            EtiquetaTipoTraduccion(it)
        }

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(t(R.string.traductor_consejos_de_pronunciacion), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(t(R.string.traductor_escucha_y_repite_la_pronunciacion), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SelectorIdiomaCompacto(
    idiomaSeleccionado: Idioma,
    onSeleccionar: (Idioma) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = true }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = idiomaSeleccionado.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = t(R.string.traductor_cambiar),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            Idioma.DISPONIBLES.forEach { idioma ->
                DropdownMenuItem(
                    text = { Text(idioma.nombre) },
                    onClick = {
                        onSeleccionar(idioma)
                        expandido = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TarjetaResultado(resultado: ResultadoTraduccion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = resultado.texto,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = descripcionTipo(resultado),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun descripcionTipo(resultado: ResultadoTraduccion): String = when (resultado.tipo) {
    TipoTraduccion.VERIFICADA -> t(R.string.traductor_verificado_en_el_diccionario_disponible)
    TipoTraduccion.LITERAL -> t(R.string.traductor_traduccion_literal_con_el_diccionario)
    TipoTraduccion.AUTOMATICA -> t(R.string.traductor_traduccion_automatica_sin_conexion)
}

@Composable
private fun EtiquetaTipoTraduccion(resultado: ResultadoTraduccion) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = descripcionTipo(resultado),
            style = MaterialTheme.typography.bodySmall,
            color = if (resultado.tipo == TipoTraduccion.VERIFICADA) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.tertiary
        )
        resultado.nota?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SelectorModo(modoConversacion: Boolean, onAlternar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
    ) {
        listOf(false to t(R.string.traductor_texto_2), true to t(R.string.traductor_conversacion)).forEach { (conversacion, etiqueta) ->
            val activo = conversacion == modoConversacion
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                    .clickable(enabled = !activo, onClick = onAlternar)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConversacionContenido(
    idiomaOrigen: Idioma,
    idiomaDestino: Idioma,
    onSeleccionarOrigen: (Idioma) -> Unit,
    onSeleccionarDestino: (Idioma) -> Unit,
    conversacion: List<TurnoConversacion>,
    hablaOrigen: Boolean,
    onSeleccionarHablante: (Boolean) -> Unit,
    texto: String,
    onTextoChange: (String) -> Unit,
    isLoading: Boolean,
    isTranscribing: Boolean,
    isPronouncing: Boolean,
    grabando: Boolean,
    errorMessage: String?,
    onEnviar: () -> Unit,
    onMicrofonoClick: () -> Unit,
    onEscucharTurno: (TurnoConversacion) -> Unit,
    onLimpiar: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        SelectorIdiomaCompacto(idiomaOrigen, onSeleccionarOrigen, Modifier.weight(1f))
        SelectorIdiomaCompacto(idiomaDestino, onSeleccionarDestino, Modifier.weight(1f))
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (conversacion.isEmpty()) {
            Text(
                t(R.string.traductor_elige_quien_habla_escribe_o),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        conversacion.forEach { turno ->
            BurbujaTurno(turno, isPronouncing, onEscucharTurno)
        }
        if (isLoading) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
    }

    Text(t(R.string.traductor_quien_habla), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp)
    ) {
        listOf(true to idiomaOrigen, false to idiomaDestino).forEach { (origen, idioma) ->
            val activo = origen == hablaOrigen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    .clickable { onSeleccionarHablante(origen) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    idioma.nombre,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    val idiomaHablante = if (hablaOrigen) idiomaOrigen else idiomaDestino
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        BasicTextField(
            value = texto,
            onValueChange = onTextoChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(end = 64.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (texto.isNotBlank()) onEnviar() }),
            decorationBox = { campo ->
                if (texto.isEmpty()) {
                    Text(
                        t(R.string.traductor_frase_en, idiomaHablante.nombre),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                campo()
            }
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = if (grabando) t(R.string.traductor_detener_dictado) else t(R.string.traductor_dictar),
                tint = if (grabando) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).clickable(enabled = !isTranscribing, onClick = onMicrofonoClick)
            )
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = t(R.string.traductor_traducir_turno),
                tint = if (texto.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(enabled = texto.isNotBlank() && !isLoading && !isTranscribing, onClick = onEnviar)
            )
        }
    }
    if (grabando || isTranscribing) {
        Text(
            if (grabando) t(R.string.traductor_escuchando_toca_el_microfono_para) else t(R.string.traductor_transcribiendo_audio),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
    errorMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
    }
    if (conversacion.isNotEmpty()) {
        Text(
            t(R.string.traductor_nueva_conversacion),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 14.dp).clickable(onClick = onLimpiar)
        )
    }
}

@Composable
private fun BurbujaTurno(
    turno: TurnoConversacion,
    isPronouncing: Boolean,
    onEscuchar: (TurnoConversacion) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (turno.deOrigen) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (turno.deOrigen) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.primaryContainer
                )
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                t(R.string.traductor_texto_3, turno.idiomaOriginal.nombre, turno.original),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val resultado = turno.resultado
            if (resultado != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        resultado.texto,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.volume),
                        contentDescription = t(R.string.traductor_escuchar),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp).clickable(enabled = !isPronouncing) { onEscuchar(turno) }
                    )
                }
                resultado.alternativas.forEach {
                    Text(t(R.string.traductor_texto, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    descripcionTipo(resultado),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (resultado.tipo == TipoTraduccion.VERIFICADA) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.tertiary
                )
            } else {
                Text(turno.error.orEmpty(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

internal class GrabadorAudio(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var archivo: File? = null

    @Suppress("DEPRECATION")
    fun iniciar(): Boolean = runCatching {
        cancelar()
        val destino = File.createTempFile("dictado_", ".aac", context.cacheDir)
        val nuevoRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
        nuevoRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(16_000)
            setAudioEncodingBitRate(64_000)
            setOutputFile(destino.absolutePath)
            prepare()
            start()
        }
        archivo = destino
        recorder = nuevoRecorder
        true
    }.getOrDefault(false)

    fun detener(): String? {
        val actual = recorder ?: return null
        recorder = null
        val destino = archivo
        archivo = null
        return try {
            actual.stop()
            actual.release()
            destino?.takeIf { it.isFile && it.length() > 0 }?.let {
                Base64.encodeToString(it.readBytes(), Base64.NO_WRAP)
            }
        } catch (_: RuntimeException) {
            actual.release()
            null
        } finally {
            destino?.delete()
        }
    }

    fun cancelar() {
        recorder?.runCatching { stop() }
        recorder?.release()
        recorder = null
        archivo?.delete()
        archivo = null
    }
}

private val idiomaEsMuestra = Idioma.DISPONIBLES.first { it.codigo == "es" }
private val idiomaMiMuestra = Idioma.DISPONIBLES.first { it.codigo == "mi" }

@Preview(showBackground = true, name = "Con resultado")
@Composable
private fun TraductorScreenContenidoPreview() {
    AikukisnaTheme {
        TraductorScreenContenido(
            idiomaOrigen = idiomaEsMuestra,
            idiomaDestino = idiomaMiMuestra,
            onSeleccionarOrigen = {},
            onSeleccionarDestino = {},
            onIntercambiar = {},
            texto = "Gracias",
            onTextoChange = {},
            resultado = ResultadoTraduccion(texto = "Tingki", fuente = FuenteTraduccion.DICCIONARIO),
            isLoading = false,
            errorMessage = null,
            onTraducirClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Vacío")
@Composable
private fun TraductorScreenContenidoVacioPreview() {
    AikukisnaTheme {
        TraductorScreenContenido(
            idiomaOrigen = idiomaEsMuestra,
            idiomaDestino = idiomaMiMuestra,
            onSeleccionarOrigen = {},
            onSeleccionarDestino = {},
            onIntercambiar = {},
            texto = "",
            onTextoChange = {},
            resultado = null,
            isLoading = false,
            errorMessage = null,
            onTraducirClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Traduciendo")
@Composable
private fun TraductorScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        TraductorScreenContenido(
            idiomaOrigen = idiomaEsMuestra,
            idiomaDestino = idiomaMiMuestra,
            onSeleccionarOrigen = {},
            onSeleccionarDestino = {},
            onIntercambiar = {},
            texto = "Buenos días",
            onTextoChange = {},
            resultado = null,
            isLoading = true,
            errorMessage = null,
            onTraducirClick = {},
            onVolver = {}
        )
    }
}
