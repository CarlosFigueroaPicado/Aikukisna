package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.FrasesIdioma
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.usecase.ProximaLeccion
import com.aikukisna.app.presentacion.viewmodel.HomeViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme


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
    LaunchedEffect(Unit) { viewModel.cargarDatos() }
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
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = FrasesIdioma.saludo(usuario.idiomaMeta?.id),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = usuario.nombreUsuario ?: usuario.nombre ?: t(R.string.home_usuario),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = t(R.string.home_comienza_tu_aprendizaje_hoy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Image(
                painter = painterResource(id = R.drawable.tuki_flying),
                contentDescription = null,
                modifier = Modifier.size(76.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCambiarIdioma)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = usuario.idiomaMeta?.nombre ?: t(R.string.home_sin_idioma),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t(R.string.home_cambiar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val nivelActual = usuario.xp / 500 + 1
        val xpNivel = usuario.xp % 500
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(t(R.string.home_nivel, nivelActual), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(t(R.string.home_500_xp, xpNivel), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((xpNivel / 500f).coerceIn(0f, 1f))
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion(t(R.string.home_continua_aprendiendo))
                if (proximaLeccion != null) {
                    TarjetaContinuarLeccion(
                        leccion = proximaLeccion.leccion,
                        numPalabras = proximaLeccion.numPalabras,
                        onClick = { onContinuarLeccion(proximaLeccion.leccion.id) }
                    )
                } else {
                    Text(
                        text = t(R.string.home_todavia_no_hay_lecciones_cargadas, usuario.idiomaMeta?.nombre ?: t(R.string.home_tu_idioma)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion(t(R.string.home_explorar))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaExplorar(
                        icono = R.drawable.ic_chat,
                        etiqueta = t(R.string.home_chat_ia),
                        onClick = onChatIA,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaExplorar(
                        icono = R.drawable.camera,
                        etiqueta = t(R.string.home_camara),
                        onClick = onCamara,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaExplorar(
                        icono = R.drawable.ic_globe,
                        etiqueta = t(R.string.home_traductor),
                        onClick = onTraductor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TituloSeccion(t(R.string.home_tu_progreso))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TarjetaProgresoHome(
                        titulo = t(R.string.home_racha),
                        valor = if (usuario.rachaActual == 1) t(R.string.home_racha_un_dia, usuario.rachaActual) else t(R.string.home_racha_dias, usuario.rachaActual),
                        icono = R.drawable.ic_flame,
                        onClick = onRepaso,
                        modifier = Modifier.weight(1f)
                    )
                    TarjetaProgresoHome(
                        titulo = t(R.string.home_logros),
                        valor = t(R.string.home_ver_todos),
                        icono = R.drawable.trofeo,
                        onClick = onLogros,
                        modifier = Modifier.weight(1f)
                    )
                }

                BotonSecundarioHome(texto = t(R.string.home_repaso), onClick = onRepaso, modifier = Modifier.fillMaxWidth())
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = t(R.string.home_sigue_adelante),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = t(R.string.home_cada_dia_que_aprendes_te, usuario.idiomaMeta?.nombre ?: t(R.string.home_tu_idioma)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Image(
                painter = painterResource(id = R.drawable.tuki_celebrating),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 19.sp),
        color = MaterialTheme.colorScheme.secondary
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
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = leccion.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 19.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = t(R.string.home_palabras_desde_20_xp, numPalabras),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 16.sp),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Icon(
            painter = painterResource(id = R.drawable.arrow_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(14.dp))
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
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

@Composable
private fun TarjetaProgresoHome(
    titulo: String,
    valor: String,
    icono: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
