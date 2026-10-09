package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.OpcionIdiomaInterfaz
import com.aikukisna.app.presentacion.idioma.IdiomaInterfaz
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.outlined.Translate
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.data.local.PreferenciasAprendizajeLocal
import com.aikukisna.app.data.local.ia.ModeloTukiLocal
import com.aikukisna.app.data.sync.DescargaModeloTukiWorker
import com.aikukisna.app.data.sync.EstadoDescargaModelo

@Composable
fun ConfiguracionScreen(
    onCambiarIdioma: () -> Unit,
    onCerrarSesion: () -> Unit,
    onVolver: () -> Unit,
    idiomaActual: String? = null
) {
    val context = LocalContext.current
    val preferencias = context.getSharedPreferences("configuracion", android.content.Context.MODE_PRIVATE)
    var notificacionesActivas by rememberSaveable {
        mutableStateOf(preferencias.getBoolean("notificaciones", true))
    }
    var sonidoActivo by rememberSaveable {
        mutableStateOf(preferencias.getBoolean("sonido", true))
    }
    var metaDiaria by rememberSaveable {
        mutableStateOf(preferencias.getInt("meta_diaria", 3))
    }
    val aprendizaje = remember { PreferenciasAprendizajeLocal(context.applicationContext) }
    var lenguaApoyo by rememberSaveable { mutableStateOf(aprendizaje.lenguaApoyoId()) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var elegirIdiomaApp by rememberSaveable { mutableStateOf(false) }
    val idiomaApp = remember { IdiomaInterfaz.actual(context) }

    // Encabezado fijo; solo el contenido se desplaza y deja espacio sobre la barra del sistema.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t(R.string.configuracion_volver), tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                t(R.string.configuracion_configuracion),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            TituloSeccion(t(R.string.configuracion_idioma))
            Tarjeta {
                OpcionConfiguracion(
                    icono = Icons.Outlined.Translate,
                    titulo = t(R.string.configuracion_idioma_de_la_app),
                    descripcion = idiomaApp.etiqueta +
                        if (idiomaApp.enRevision) " · " + t(R.string.configuracion_traduccion_en_revision) else "",
                    onClick = { elegirIdiomaApp = true }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OpcionConfiguracion(
                    icono = Icons.Outlined.Language,
                    titulo = t(R.string.configuracion_idioma_que_aprendes),
                    descripcion = idiomaActual ?: t(R.string.configuracion_toca_para_elegir),
                    onClick = onCambiarIdioma
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OpcionConfiguracion(
                    icono = Icons.Outlined.SwapHoriz,
                    titulo = t(R.string.configuracion_lengua_de_apoyo_para_espanol),
                    descripcion = t(R.string.configuracion_las_lecciones_de_espanol_se, LENGUAS_APOYO.getValue(lenguaApoyo)),
                    onClick = {
                        val ids = LENGUAS_APOYO.keys.toList()
                        lenguaApoyo = ids[(ids.indexOf(lenguaApoyo) + 1) % ids.size]
                        aprendizaje.cambiarLenguaApoyo(lenguaApoyo)
                    }
                )
            }

            TituloSeccion(t(R.string.configuracion_aprendizaje))
            Tarjeta {
                OpcionInterruptor(
                    icono = Icons.Outlined.Notifications,
                    titulo = t(R.string.configuracion_notificaciones),
                    descripcion = t(R.string.configuracion_recordatorios_diarios_de_practica),
                    marcada = notificacionesActivas,
                    onCambio = {
                        notificacionesActivas = it
                        preferencias.edit().putBoolean("notificaciones", it).apply()
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OpcionInterruptor(
                    icono = Icons.Outlined.VolumeUp,
                    titulo = t(R.string.configuracion_sonido),
                    descripcion = t(R.string.configuracion_efectos_de_sonido),
                    marcada = sonidoActivo,
                    onCambio = {
                        sonidoActivo = it
                        preferencias.edit().putBoolean("sonido", it).apply()
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                OpcionConfiguracion(
                    icono = Icons.Outlined.Flag,
                    titulo = t(R.string.configuracion_meta_diaria),
                    descripcion = if (metaDiaria == 1) t(R.string.configuracion_meta_una_leccion, metaDiaria) else t(R.string.configuracion_meta_varias_lecciones, metaDiaria),
                    onClick = {
                        metaDiaria = when (metaDiaria) { 1 -> 3; 3 -> 5; else -> 1 }
                        preferencias.edit().putInt("meta_diaria", metaDiaria).apply()
                    }
                )
            }

            SeccionTukiOffline()

            Spacer(Modifier.height(28.dp))
            OutlinedButton(
                onClick = { confirmarSalida = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(8.dp))
                Text(t(R.string.configuracion_cerrar_sesion), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    if (elegirIdiomaApp) {
        AlertDialog(
            onDismissRequest = { elegirIdiomaApp = false },
            title = { Text(t(R.string.configuracion_idioma_de_la_app)) },
            text = {
                Column {
                    Text(
                        t(R.string.configuracion_idioma_app_explicacion),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OpcionIdiomaInterfaz.entries.forEach { opcion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    elegirIdiomaApp = false
                                    if (opcion != idiomaApp) (context as? android.app.Activity)?.let { IdiomaInterfaz.cambiar(it, opcion) }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = opcion == idiomaApp, onClick = null)
                            Column(Modifier.padding(start = 12.dp)) {
                                Text(opcion.etiqueta, style = MaterialTheme.typography.bodyLarge)
                                if (opcion.enRevision) {
                                    Text(
                                        t(R.string.configuracion_borrador_revision_nativos),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { elegirIdiomaApp = false }) { Text(t(R.string.configuracion_cancelar)) }
            }
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text(t(R.string.configuracion_cerrar_sesion_2)) },
            text = { Text(t(R.string.configuracion_tu_progreso_queda_guardado_para)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    onCerrarSesion()
                }) { Text(t(R.string.configuracion_cerrar_sesion), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text(t(R.string.configuracion_cancelar)) } }
        )
    }
}

@Composable
private fun SeccionTukiOffline() {
    val context = LocalContext.current
    val estado by remember { DescargaModeloTukiWorker.observar(context.applicationContext) }
        .collectAsState(initial = EstadoDescargaModelo.NoDescargado)

    TituloSeccion(t(R.string.configuracion_tuki_sin_conexion))
    Tarjeta {
        val descripcion = when (val actual = estado) {
            EstadoDescargaModelo.Listo -> t(R.string.configuracion_listo_el_traductor_funciona_sin)
            EstadoDescargaModelo.EsperandoRed -> t(R.string.configuracion_esperando_wi_fi_toca_para)
            is EstadoDescargaModelo.Descargando -> if (actual.total > 0) {
                t(R.string.configuracion_descargando_de_mb, actual.descargados * 100 / actual.total, actual.descargados / MEGABYTE, actual.total / MEGABYTE)
            } else {
                t(R.string.configuracion_descargando_mb, actual.descargados / MEGABYTE)
            }
            EstadoDescargaModelo.Error -> t(R.string.configuracion_la_descarga_fallo_toca_para)
            EstadoDescargaModelo.NoDescargado -> t(R.string.configuracion_toca_para_descargar_el_modelo)
        }
        OpcionConfiguracion(
            icono = Icons.Outlined.CloudDownload,
            titulo = t(R.string.configuracion_modelo_sin_conexion),
            descripcion = descripcion,
            onClick = {
                if (estado != EstadoDescargaModelo.Listo && estado !is EstadoDescargaModelo.Descargando) {
                    DescargaModeloTukiWorker.programar(context.applicationContext, permitirDatosMoviles = true)
                }
            }
        )
        if (estado is EstadoDescargaModelo.Listo) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OpcionConfiguracion(
                icono = Icons.Outlined.DeleteOutline,
                titulo = t(R.string.configuracion_eliminar_modelo),
                descripcion = t(R.string.configuracion_libera_550_mb_el_traductor),
                onClick = {
                    DescargaModeloTukiWorker.cancelar(context.applicationContext)
                    ModeloTukiLocal(context.applicationContext).eliminar()
                }
            )
        }
    }
}

private const val MEGABYTE = 1024L * 1024L

private val LENGUAS_APOYO = linkedMapOf(1 to "Miskito", 3 to "Inglés Kriol", 4 to "Inglés Estándar")

@Composable
private fun TituloSeccion(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 22.dp, bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun Tarjeta(contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
        content = contenido
    )
}

@Composable
private fun IconoOpcion(icono: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun OpcionInterruptor(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    marcada: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onCambio(!marcada) }.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconoOpcion(icono)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = marcada, onCheckedChange = onCambio)
    }
}

@Composable
private fun OpcionConfiguracion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconoOpcion(icono)
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Preview(showBackground = true, name = "Configuración")
@Composable
private fun ConfiguracionScreenPreview() {
    com.aikukisna.app.ui.theme.AikukisnaTheme {
        ConfiguracionScreen(onCambiarIdioma = {}, onCerrarSesion = {}, onVolver = {}, idiomaActual = "Miskito")
    }
}
