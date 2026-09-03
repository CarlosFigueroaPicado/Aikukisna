package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.usecase.ProximaLeccion
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BlueBorder
import com.aikukisna.app.ui.theme.BlueDark
import com.aikukisna.app.ui.theme.BlueDarkest
import com.aikukisna.app.ui.theme.BluePrimary
import com.aikukisna.app.ui.theme.BlueSubtle
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.OrangeBorder


@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCerrarSesion: () -> Unit,
    onContinuarLeccion: (Int) -> Unit = {},
    onChatIA: () -> Unit = {},
    onCamara: () -> Unit = {},
    onTraductor: () -> Unit = {},
    onLogros: () -> Unit = {},
    onRepaso: () -> Unit = {},
    onCambiarIdioma: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        HomeUiState.Cargando -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        is HomeUiState.Exito -> {
            HomeScreenContenido(
                usuario = s.usuario,
                proximaLeccion = s.proximaLeccion,
                onContinuarLeccion = onContinuarLeccion,
                onChatIA = onChatIA,
                onCamara = onCamara,
                onTraductor = onTraductor,
                onLogros = onLogros,
                onRepaso = onRepaso,
                onCambiarIdioma = onCambiarIdioma
            )
        }
        is HomeUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = s.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HomeScreenContenido(
    usuario: Usuario,
    proximaLeccion: ProximaLeccion?,
    onContinuarLeccion: (Int) -> Unit,
    onChatIA: () -> Unit,
    onCamara: () -> Unit,
    onTraductor: () -> Unit,
    onLogros: () -> Unit,
    onRepaso: () -> Unit,
    onCambiarIdioma: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ak_mark),
                contentDescription = null,
                modifier = Modifier.size(width = 28.dp, height = 25.6.dp)
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(16.dp))
                    .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp))
                    .clickable(onClick = onCambiarIdioma)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usuario.idiomaMeta?.nombre ?: "Sin idioma",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = BluePrimary
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion("Continúa aprendiendo")
                if (proximaLeccion != null) {
                    TarjetaContinuarLeccion(
                        leccion = proximaLeccion.leccion,
                        numPalabras = proximaLeccion.numPalabras,
                        onClick = { onContinuarLeccion(proximaLeccion.leccion.id) }
                    )
                } else {
                    Text(
                        text = "Todavía no hay lecciones cargadas para ${usuario.idiomaMeta?.nombre ?: "tu idioma"}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion("Explorar")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaExplorar(
                        icono = R.drawable.ic_chat,
                        etiqueta = "Chat IA",
                        onClick = onChatIA,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaExplorar(
                        icono = R.drawable.camera,
                        etiqueta = "Cámara",
                        onClick = onCamara,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaExplorar(
                        icono = R.drawable.ic_globe,
                        etiqueta = "Traductor",
                        onClick = onTraductor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion("Tu progreso")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlueSubtle)
                        .border(width = 1.dp, color = BlueBorder, shape = RoundedCornerShape(16.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Racha actual",
                            style = MaterialTheme.typography.bodySmall,
                            color = BlueDark
                        )
                        Text(
                            text = "${usuario.rachaActual} días",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = BlueDarkest
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flame),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotonSecundarioHome(texto = "Logros", onClick = onLogros, modifier = Modifier.weight(1f))
                    BotonSecundarioHome(texto = "Repaso", onClick = onRepaso, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
        color = BluePrimary
    )
}

@Composable
private fun TarjetaContinuarLeccion(
    leccion: Leccion,
    numPalabras: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leccion.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "$numPalabras palabras · Desde 20 XP",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = OrangeBorder
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TarjetaExplorar(
    icono: Int,
    etiqueta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(14.dp))
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray
        )
    }
}

@Composable
private fun BotonSecundarioHome(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary,
            textAlign = TextAlign.Center
        )
    }
}

private val usuarioDeMuestra = Usuario(
    id = java.util.UUID.randomUUID(),
    nombre = "Carlos",
    apellido = null,
    nombreUsuario = "carlosf",
    correo = null,
    edad = null,
    pais = null,
    ciudad = null,
    idiomaMeta = Idioma.DISPONIBLES.first { it.codigo == "mi" },
    xp = 180,
    rachaActual = 3,
    rachaMaxima = 5,
    ultimaActividad = null
)

private val leccionDeMuestra = Leccion(
    id = 1,
    titulo = "Saludos y Despedidas",
    capituloNumero = 1,
    nivel = 1,
    categoria = null,
    idiomaMeta = Idioma.DISPONIBLES.first { it.codigo == "mi" }
)

@Preview(showBackground = true, name = "Con lección pendiente")
@Composable
private fun HomeScreenContenidoPreview() {
    AikukisnaTheme {
        HomeScreenContenido(
            usuario = usuarioDeMuestra,
            proximaLeccion = ProximaLeccion(leccion = leccionDeMuestra, numPalabras = 8),
            onContinuarLeccion = {},
            onChatIA = {},
            onCamara = {},
            onTraductor = {},
            onLogros = {},
            onRepaso = {},
            onCambiarIdioma = {}
        )
    }
}

@Preview(showBackground = true, name = "Sin lecciones")
@Composable
private fun HomeScreenContenidoVacioPreview() {
    AikukisnaTheme {
        HomeScreenContenido(
            usuario = usuarioDeMuestra,
            proximaLeccion = null,
            onContinuarLeccion = {},
            onChatIA = {},
            onCamara = {},
            onTraductor = {},
            onLogros = {},
            onRepaso = {},
            onCambiarIdioma = {}
        )
    }
}