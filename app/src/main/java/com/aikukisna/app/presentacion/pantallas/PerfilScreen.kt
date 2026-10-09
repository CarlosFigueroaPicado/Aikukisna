package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.aikukisna.app.data.local.PerfilFotoStorage
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.usecase.LogroConEstado
import com.aikukisna.app.presentacion.viewmodel.PalabraConTraduccion
import com.aikukisna.app.presentacion.viewmodel.PerfilViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel(),
    onVerLogros: () -> Unit = {},
    onVerFavoritos: () -> Unit = {},
    onConfiguracion: () -> Unit = {},
    onCultura: () -> Unit = {},
    onEditarPerfil: () -> Unit = {}
) {
    LaunchedEffect(Unit) { viewModel.cargar() }
    PerfilScreenContenido(
        usuario = viewModel.usuario,
        leccionesCompletadas = viewModel.leccionesCompletadas,
        nivelActual = viewModel.nivelActual,
        xpEnNivelActual = viewModel.xpEnNivelActual,
        logros = viewModel.logros,
        logrosDesbloqueados = viewModel.logrosDesbloqueados,
        favoritos = viewModel.favoritos,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onVerLogros = onVerLogros,
        onVerFavoritos = onVerFavoritos,
        onConfiguracion = onConfiguracion,
        onCultura = onCultura,
        onEditarPerfil = onEditarPerfil
    )
}

@Composable
private fun PerfilScreenContenido(
    usuario: Usuario?,
    leccionesCompletadas: Int,
    nivelActual: Int,
    xpEnNivelActual: Int,
    logros: List<LogroConEstado>,
    logrosDesbloqueados: Int,
    favoritos: List<PalabraConTraduccion>,
    isLoading: Boolean,
    errorMessage: String?,
    onVerLogros: () -> Unit,
    onVerFavoritos: () -> Unit,
    onConfiguracion: () -> Unit,
    onCultura: () -> Unit = {},
    onEditarPerfil: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = t(R.string.perfil_perfil),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = t(R.string.perfil_configuracion),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onConfiguracion)
                )
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
            usuario != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable(onClick = onEditarPerfil),
                        contentAlignment = Alignment.Center
                    ) {
                        val context = LocalContext.current
                        val bitmap by produceState<android.graphics.Bitmap?>(null, usuario.fotoPerfilUri) {
                            value = usuario.fotoPerfilUri?.let { PerfilFotoStorage.leerReducida(context, it) }
                        }
                        if (bitmap != null) {
                            androidx.compose.foundation.Image(bitmap!!.asImageBitmap(), null, Modifier.fillMaxSize())
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = usuario.nombreUsuario ?: usuario.nombre ?: t(R.string.perfil_usuario),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    usuario.correo?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = t(R.string.perfil_nivel, nivelActual),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimaryContainer))
                        usuario.idiomaMeta?.let { idioma ->
                            Text(
                                text = t(R.string.perfil_aprendiz, idioma.nombre),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EstadisticaPerfil(R.drawable.ic_flame, t(R.string.perfil_texto, usuario.rachaActual), t(R.string.perfil_racha))
                        DivisorVertical()
                        EstadisticaPerfil(R.drawable.xp, "${usuario.xp}", t(R.string.perfil_xp_total))
                        DivisorVertical()
                        EstadisticaPerfil(R.drawable.book_bookmark, t(R.string.perfil_texto, leccionesCompletadas), t(R.string.perfil_lecciones))
                        DivisorVertical()
                        EstadisticaPerfilVector(Icons.Default.EmojiEvents, t(R.string.perfil_texto, logrosDesbloqueados), t(R.string.perfil_logros))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SeccionProgresoDeNivel(nivelActual = nivelActual, xpEnNivel = xpEnNivelActual)

                    Spacer(modifier = Modifier.height(24.dp))

                    SeccionLogrosPreview(
                        logros = logros,
                        logrosDesbloqueados = logrosDesbloqueados,
                        onVerTodos = onVerLogros
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    SeccionFavoritosPreview(
                        favoritos = favoritos,
                        onVerTodos = onVerFavoritos
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        t(R.string.perfil_explorar_cultura),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onCultura).padding(vertical = 10.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TarjetaMotivacionalPerfil(idioma = usuario.idiomaMeta?.nombre ?: t(R.string.perfil_tu_idioma))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DivisorVertical() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun EstadisticaPerfil(icono: Int, valor: String, etiqueta: String) {
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icono),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 19.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun EstadisticaPerfilVector(icono: ImageVector, valor: String, etiqueta: String) {
    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 19.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SeccionProgresoDeNivel(nivelActual: Int, xpEnNivel: Int) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = t(R.string.perfil_progreso_de_nivel), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = t(R.string.perfil_nivel, nivelActual), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = t(R.string.perfil_500_xp, xpEnNivel), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            }
            val progreso = (xpEnNivel / 500f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progreso)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun SeccionLogrosPreview(
    logros: List<LogroConEstado>,
    logrosDesbloqueados: Int,
    onVerTodos: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = t(R.string.perfil_logros_2, logrosDesbloqueados, logros.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.clickable(onClick = onVerTodos),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = t(R.string.perfil_ver_todos), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(painter = painterResource(id = R.drawable.arrow_right), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        val destacado = logros.firstOrNull { !it.desbloqueado } ?: logros.firstOrNull()
        if (destacado != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (destacado.desbloqueado) {
                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                } else {
                    Icon(painter = painterResource(id = R.drawable.lock), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                }
                Column {
                    Text(text = destacado.logro.nombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    Text(text = destacado.logro.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SeccionFavoritosPreview(
    favoritos: List<PalabraConTraduccion>,
    onVerTodos: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = t(R.string.perfil_favoritos), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.clickable(onClick = onVerTodos),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = t(R.string.perfil_ver_todos), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(painter = painterResource(id = R.drawable.arrow_right), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        if (favoritos.isEmpty()) {
            Text(text = t(R.string.perfil_todavia_no_tienes_palabras_favoritas), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            favoritos.forEach { fav ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = fav.texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    fav.traduccion?.let {
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun TarjetaMotivacionalPerfil(idioma: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.tuki_celebrating),
            contentDescription = null,
            modifier = Modifier.size(82.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = t(R.string.perfil_sigue_adelante), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t(R.string.perfil_cada_dia_que_aprendes_te, idioma),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val usuarioDeMuestraPerfil = Usuario(
    id = java.util.UUID.randomUUID(),
    nombre = "David",
    apellido = null,
    nombreUsuario = "davidf",
    correo = "dvd@gmail.com",
    edad = null,
    pais = null,
    ciudad = null,
    idiomaMeta = Idioma.DISPONIBLES.first { it.codigo == "mi" },
    xp = 250,
    rachaActual = 1,
    rachaMaxima = 5,
    ultimaActividad = null
)

private val logroDeMuestraPerfil = Logro(id = 1, nombre = t(R.string.perfil_primera_leccion), descripcion = t(R.string.perfil_completa_tu_primera_leccion), condicionTipo = "lecciones_completadas", condicionValor = 1, categoria = null)

@Preview(showBackground = true, name = "Con datos")
@Composable
private fun PerfilScreenContenidoPreview() {
    AikukisnaTheme {
        PerfilScreenContenido(
            usuario = usuarioDeMuestraPerfil,
            leccionesCompletadas = 0,
            nivelActual = 1,
            xpEnNivelActual = 250,
            logros = listOf(LogroConEstado(logroDeMuestraPerfil, desbloqueado = false, fecha = null)),
            logrosDesbloqueados = 0,
            favoritos = listOf(PalabraConTraduccion(texto = "Tingki", traduccion = "Gracias")),
            isLoading = false,
            errorMessage = null,
            onVerLogros = {},
            onVerFavoritos = {},
            onConfiguracion = {}
        )
    }
}

@Preview(showBackground = true, name = "Sin favoritos")
@Composable
private fun PerfilScreenContenidoSinFavoritosPreview() {
    AikukisnaTheme {
        PerfilScreenContenido(
            usuario = usuarioDeMuestraPerfil,
            leccionesCompletadas = 0,
            nivelActual = 1,
            xpEnNivelActual = 250,
            logros = listOf(LogroConEstado(logroDeMuestraPerfil, desbloqueado = false, fecha = null)),
            logrosDesbloqueados = 0,
            favoritos = emptyList(),
            isLoading = false,
            errorMessage = null,
            onVerLogros = {},
            onVerFavoritos = {},
            onConfiguracion = {}
        )
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
private fun PerfilScreenContenidoCargandoPreview() {
    AikukisnaTheme {
        PerfilScreenContenido(
            usuario = null,
            leccionesCompletadas = 0,
            nivelActual = 1,
            xpEnNivelActual = 0,
            logros = emptyList(),
            logrosDesbloqueados = 0,
            favoritos = emptyList(),
            isLoading = true,
            errorMessage = null,
            onVerLogros = {},
            onVerFavoritos = {},
            onConfiguracion = {}
        )
    }
}
