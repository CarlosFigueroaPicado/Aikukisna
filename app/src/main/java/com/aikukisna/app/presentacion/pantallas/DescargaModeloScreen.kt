package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.data.sync.EstadoDescargaModelo
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.viewmodel.PrimerUsoViewModel
import com.aikukisna.app.presentacion.viewmodel.ProgresoDescargaUi

/**
 * Primera vez tras iniciar sesión: avisa que se descargará el modelo de Tuki en el
 * teléfono, cuánto pesa y cuánto falta. La descarga sigue en segundo plano si el
 * usuario continúa.
 */
@Composable
fun DescargaModeloScreen(
    onContinuar: () -> Unit,
    viewModel: PrimerUsoViewModel = hiltViewModel()
) {
    val progreso by viewModel.progreso.collectAsState()
    val estado = progreso.estado
    val terminar = {
        viewModel.terminarPantallaDescarga()
        onContinuar()
    }

    LaunchedEffect(Unit) { viewModel.descargarAhora(permitirDatosMoviles = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.tuki_lake),
            contentDescription = null,
            modifier = Modifier.size(width = 107.dp, height = 90.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            t(R.string.descargamodelo_actualizacion_de_tuki),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            t(R.string.descargamodelo_vamos_a_descargar_en_tu, progreso.megasTotales) +
                t(R.string.descargamodelo_con_el_tuki_conversa_contigo),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))

        if (estado is EstadoDescargaModelo.Descargando || estado == EstadoDescargaModelo.Listo) {
            LinearProgressIndicator(
                progress = { if (estado == EstadoDescargaModelo.Listo) 1f else progreso.fraccion },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            textoEstado(progreso),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))

        when (estado) {
            EstadoDescargaModelo.Listo ->
                AikukisnaButton(t(R.string.descargamodelo_empezar), onClick = terminar, modifier = Modifier.fillMaxWidth())
            is EstadoDescargaModelo.Descargando ->
                AikukisnaButton(
                    t(R.string.descargamodelo_seguir_en_segundo_plano),
                    onClick = terminar,
                    style = ButtonStyle.PrimaryGhost,
                    modifier = Modifier.fillMaxWidth()
                )
            else -> {
                AikukisnaButton(
                    t(R.string.descargamodelo_descargar_ahora_con_datos_moviles),
                    onClick = { viewModel.descargarAhora(permitirDatosMoviles = true) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                AikukisnaButton(
                    t(R.string.descargamodelo_esperar_wi_fi_y_continuar),
                    onClick = terminar,
                    style = ButtonStyle.PrimaryGhost,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            t(R.string.descargamodelo_mientras_se_descarga_puedes_usar),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun textoEstado(progreso: ProgresoDescargaUi): String = when (val estado = progreso.estado) {
    EstadoDescargaModelo.Listo -> t(R.string.descargamodelo_listo_tuki_ya_esta_en)
    is EstadoDescargaModelo.Descargando -> buildString {
        append(t(R.string.descargamodelo_de_mb, progreso.megasDescargados, progreso.megasTotales))
        append(" · ")
        append(progreso.segundosRestantes?.let { t(R.string.descargamodelo_faltan, formatearTiempo(it)) } ?: t(R.string.descargamodelo_calculando_tiempo))
    }
    EstadoDescargaModelo.EsperandoRed ->
        t(R.string.descargamodelo_esperando_wi_fi_con_wi, tiempoEstimado(progreso.megasTotales))
    EstadoDescargaModelo.Error -> t(R.string.descargamodelo_la_descarga_se_interrumpio_se)
    EstadoDescargaModelo.NoDescargado -> t(R.string.descargamodelo_preparando_la_descarga)
}

/** Rango orientativo con Wi-Fi entre 5 y 20 Mbps, antes de medir la velocidad real. */
private fun tiempoEstimado(megas: Long): String {
    val lento = megas * 8 / 5 / 60
    val rapido = megas * 8 / 20 / 60
    return t(R.string.descargamodelo_entre_y_minutos, rapido, lento)
}

private fun formatearTiempo(segundos: Long): String = when {
    segundos < 60 -> t(R.string.descargamodelo_menos_de_1_minuto)
    segundos < 3600 -> t(R.string.descargamodelo_unos_min, (segundos + 30) / 60)
    else -> t(R.string.descargamodelo_unas_h_min, segundos / 3600, (segundos % 3600) / 60)
}
