package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.core.content.ContextCompat
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.presentacion.viewmodel.TukiViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun TukiScreen(
    viewModel: TukiViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    val context = LocalContext.current
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

    TukiScreenContenido(
        mensajes = viewModel.mensajes,
        textoEntrada = viewModel.textoEntrada,
        onTextoEntradaChange = viewModel::onTextoEntradaChange,
        isLoading = viewModel.isLoading,
        isTranscribing = viewModel.isTranscribing,
        grabando = grabandoArchivo,
        errorMessage = viewModel.errorMessage,
        onEnviarClick = viewModel::enviarMensaje,
        onMicrofonoClick = ::alternarGrabacion,
        onVolver = onVolver
    )
}

@Composable
private fun TukiScreenContenido(
    mensajes: List<MensajeChat>,
    textoEntrada: String,
    onTextoEntradaChange: (String) -> Unit,
    isLoading: Boolean,
    isTranscribing: Boolean = false,
    grabando: Boolean = false,
    errorMessage: String?,
    onEnviarClick: () -> Unit,
    onMicrofonoClick: () -> Unit = {},
    onVolver: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(mensajes.size, isLoading) {
        val ultimoIndice = mensajes.size - 1 + if (isLoading) 1 else 0
        if (ultimoIndice >= 0) listState.animateScrollToItem(ultimoIndice)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = t(R.string.tuki_volver),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = t(R.string.tuki_tuki_ai),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(mensajes) { _, mensaje ->
                BurbujaMensaje(mensaje = mensaje)
            }
            if (isLoading) {
                item { BurbujaEscribiendo() }
            }
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(24.dp))
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = textoEntrada,
                    onValueChange = onTextoEntradaChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    decorationBox = { campo ->
                        if (textoEntrada.isEmpty()) {
                            Text(
                                text = t(R.string.tuki_pregunta_lo_que_quieras),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        campo()
                    }
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = if (grabando) t(R.string.tuki_detener_dictado) else t(R.string.tuki_iniciar_dictado),
                tint = if (grabando) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(enabled = !isTranscribing, onClick = onMicrofonoClick)
            )
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = t(R.string.tuki_enviar),
                tint = if (textoEntrada.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(enabled = textoEntrada.isNotBlank() && !isLoading && !isTranscribing, onClick = onEnviarClick)
            )
        }
        if (grabando || isTranscribing) {
            Text(
                text = if (grabando) t(R.string.tuki_escuchando_toca_el_microfono_para) else t(R.string.tuki_transcribiendo_audio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun BurbujaMensaje(
    mensaje: MensajeChat
) {
    val esUsuario = mensaje.rol == RolChat.USUARIO
    val forma = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = if (esUsuario) 14.dp else 2.dp,
        bottomEnd = if (esUsuario) 2.dp else 14.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (!esUsuario) {
            Image(
                painter = painterResource(id = R.drawable.tuki_ai_link),
                contentDescription = null,
                modifier = Modifier.size(38.dp)
            )
        }
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(forma)
                .background(if (esUsuario) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, forma)
                .border(width = if (esUsuario) 0.dp else 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = forma)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (esUsuario) AnnotatedString(mensaje.texto) else conFormatoBasico(mensaje.texto),
                style = MaterialTheme.typography.bodyMedium,
                color = if (esUsuario) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Las respuestas usan **negrita** y *cursiva* de Markdown; se muestran con formato, sin asteriscos. */
private fun conFormatoBasico(texto: String): AnnotatedString = buildAnnotatedString {
    var resto = texto
    val marca = Regex("""\*\*(.+?)\*\*|\*([^*\s][^*]*?)\*""")
    while (true) {
        val m = marca.find(resto) ?: break
        append(resto.substring(0, m.range.first))
        val negrita = m.groups[1]?.value
        if (negrita != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(negrita) }
        } else {
            withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.groupValues[2]) }
        }
        resto = resto.substring(m.range.last + 1)
    }
    append(resto)
}

@Composable
private fun BurbujaEscribiendo() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = t(R.string.tuki_tuki_esta_escribiendo),
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val mensajesDeMuestra = listOf(
    MensajeChat(rol = RolChat.TUKI, texto = "¡Hola! Soy Tuki, tu asistente educativo. Te acompañaré mientras aprendes Miskito. Podemos practicar vocabulario, conversar sobre cultura o revisar juntos tu progreso. ¿Qué te gustaría hacer?"),
    MensajeChat(rol = RolChat.USUARIO, texto = "¿Qué significa Naksa?"),
    MensajeChat(rol = RolChat.TUKI, texto = "¡Qué tal!")
)

@Preview(showBackground = true, name = "Conversación")
@Composable
private fun TukiScreenContenidoPreview() {
    AikukisnaTheme {
        TukiScreenContenido(
            mensajes = mensajesDeMuestra,
            textoEntrada = "",
            onTextoEntradaChange = {},
            isLoading = false,
            errorMessage = null,
            onEnviarClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Tuki escribiendo")
@Composable
private fun TukiScreenContenidoEscribiendoPreview() {
    AikukisnaTheme {
        TukiScreenContenido(
            mensajes = mensajesDeMuestra.take(2),
            textoEntrada = "",
            onTextoEntradaChange = {},
            isLoading = true,
            errorMessage = null,
            onEnviarClick = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Recién abierto")
@Composable
private fun TukiScreenContenidoVacioPreview() {
    AikukisnaTheme {
        TukiScreenContenido(
            mensajes = mensajesDeMuestra.take(1),
            textoEntrada = "",
            onTextoEntradaChange = {},
            isLoading = false,
            errorMessage = null,
            onEnviarClick = {},
            onVolver = {}
        )
    }
}
