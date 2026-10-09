package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.R
import com.aikukisna.app.presentacion.idioma.t

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.ui.theme.AikukisnaTheme

private val SECCIONES_TERMINOS = listOf(
    t(R.string.terminos_1_que_es_aikukisna) to
        t(R.string.terminos_aikukisna_es_una_app_educativa) +
        t(R.string.terminos_de_la_costa_caribe_de),
    t(R.string.terminos_2_tu_cuenta) to
        t(R.string.terminos_eres_responsable_de_los_datos) +
        t(R.string.terminos_si_eres_menor_de_edad),
    t(R.string.terminos_3_datos_que_guardamos) to
        t(R.string.terminos_guardamos_tu_nombre_correo_idioma) +
        t(R.string.terminos_que_no_lo_pierdas_al) +
        t(R.string.terminos_tienes_internet_no_vendemos_tus),
    t(R.string.terminos_4_microfono_y_camara) to
        t(R.string.terminos_el_microfono_se_usa_solo) +
        t(R.string.terminos_solo_cuando_abres_la_funcion),
    t(R.string.terminos_5_tuki_y_el_contenido) to
        t(R.string.terminos_tuki_y_el_traductor_funcionan) +
        t(R.string.terminos_unos_550_mb_las_traducciones) +
        t(R.string.terminos_todo_en_miskito_y_kriol) +
        t(R.string.terminos_consultas_pueden_enviarse_a_un),
    t(R.string.terminos_6_fuentes_y_respeto_cultural) to
        t(R.string.terminos_el_vocabulario_proviene_de_diccionarios) +
        t(R.string.terminos_tatoeba_citadas_en_la_app) +
        t(R.string.terminos_sus_comunidades_te_pedimos_usarlas),
    t(R.string.terminos_7_uso_adecuado) to
        t(R.string.terminos_no_uses_la_app_para) +
        "funcionamiento.",
    t(R.string.terminos_8_cambios) to
        t(R.string.terminos_si_estos_terminos_cambian_te)
)

@Composable
fun TerminosScreen(
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    var aceptado by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            t(R.string.terminos_terminos_y_condiciones),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 24.dp, bottom = 4.dp)
        )
        Text(
            t(R.string.terminos_leelos_antes_de_empezar_a),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SECCIONES_TERMINOS.forEach { (titulo, texto) ->
                Column {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { aceptado = !aceptado }
        ) {
            Checkbox(checked = aceptado, onCheckedChange = { aceptado = it })
            Text(
                t(R.string.terminos_he_leido_y_acepto_los),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(8.dp))
        AikukisnaButton(
            text = t(R.string.terminos_aceptar_y_continuar),
            onClick = onAceptar,
            enabled = aceptado,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        AikukisnaButton(
            text = t(R.string.terminos_no_acepto_cerrar_sesion),
            onClick = onRechazar,
            style = ButtonStyle.PrimaryGhost,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TerminosScreenPreview() {
    AikukisnaTheme { TerminosScreen(onAceptar = {}, onRechazar = {}) }
}
