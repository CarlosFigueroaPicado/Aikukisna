package com.aikukisna.app.presentacion.pantallas

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import com.aikukisna.app.presentacion.viewmodel.TukiViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray

@Composable
fun TukiScreen(
    viewModel: TukiViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    TukiScreenContenido(
        mensajes = viewModel.mensajes,
        textoEntrada = viewModel.textoEntrada,
        onTextoEntradaChange = viewModel::onTextoEntradaChange,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onEnviarClick = viewModel::enviarMensaje,
        onVolver = onVolver
    )
}

@Composable
private fun TukiScreenContenido(
    mensajes: List<MensajeChat>,
    textoEntrada: String,
    onTextoEntradaChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onEnviarClick: () -> Unit,
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
                contentDescription = "Volver",
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = "TUKI AI",
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
                BurbujaMensaje(mensaje)
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
                    .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(24.dp))
                    .background(color = CardSurface, shape = RoundedCornerShape(24.dp))
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
                                text = "Pregunta lo que quieras...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumGray
                            )
                        }
                        campo()
                    }
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = null,
                tint = MediumGray,
                modifier = Modifier.size(22.dp)
            )
            Icon(
                painter = painterResource(id = R.drawable.send),
                contentDescription = "Enviar",
                tint = if (textoEntrada.isNotBlank()) MaterialTheme.colorScheme.primary else MediumGray,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(enabled = textoEntrada.isNotBlank() && !isLoading, onClick = onEnviarClick)
            )
        }
    }
}

@Composable
private fun BurbujaMensaje(mensaje: MensajeChat) {
    val esUsuario = mensaje.rol == RolChat.USUARIO
    val forma = RoundedCornerShape(
        topStart = 14.dp,
        topEnd = 14.dp,
        bottomStart = if (esUsuario) 14.dp else 2.dp,
        bottomEnd = if (esUsuario) 2.dp else 14.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(forma)
                .background(if (esUsuario) MaterialTheme.colorScheme.primary else CardSurface, forma)
                .border(width = if (esUsuario) 0.dp else 1.dp, color = LightGray, shape = forma)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = mensaje.texto,
                style = MaterialTheme.typography.bodyMedium,
                color = if (esUsuario) Color.White else MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun BurbujaEscribiendo() {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp))
                .background(CardSurface)
                .border(
                    width = 1.dp,
                    color = LightGray,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 2.dp, bottomEnd = 14.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Tuki está escribiendo…",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MediumGray
            )
        }
    }
}

private val mensajesDeMuestra = listOf(
    MensajeChat(rol = RolChat.TUKI, texto = "¡Diaki! Soy Tuki. Puedo enseñarte vocabulario, pronunciación y frases del día a día en Miskito, o cualquier otra cosa que quieras."),
    MensajeChat(rol = RolChat.USUARIO, texto = "¿Cómo se dice 'gracias'?"),
    MensajeChat(rol = RolChat.TUKI, texto = "En miskito, 'gracias' se dice 'pura'.\nPor ejemplo:\n• Pura = Gracias.\n• Pura kum = Muchas gracias.")
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