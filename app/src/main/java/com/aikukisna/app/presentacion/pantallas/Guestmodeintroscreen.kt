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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.domain.usecase.ObtenerPalabrasDemoUseCase
import com.aikukisna.app.domain.usecase.ObtenerQuizDemoUseCase
import com.aikukisna.app.presentacion.viewmodel.GuestLeccionViewModel
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun GuestModeIntroScreen(
    idioma: Idioma,
    onEmpezarLeccion: () -> Unit,
    onVolver: () -> Unit,
    viewModel: GuestLeccionViewModel = hiltViewModel()
) {
    LaunchedEffect(idioma.id) { viewModel.cargarResumen(idioma.id) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = t(R.string.guestmodeintro_volver),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(onClick = onVolver)
                )
                Text(
                    text = t(R.string.guestmodeintro_modo_invitado),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp))
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = t(R.string.guestmodeintro_vamos_a_aprender_juntos),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_ave_login),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.widthIn(max = 254.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = t(R.string.guestmodeintro_aprende, idioma.nombre),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                viewModel.tituloLeccion?.let {
                    Text(
                        text = t(R.string.guestmodeintro_leccion_de_prueba, it),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = t(R.string.guestmodeintro_esta_es_una_muestra_de),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PasoLeccion(
                    numero = "1",
                    texto = t(R.string.guestmodeintro_aprende_palabras_con_tarjetas, ObtenerPalabrasDemoUseCase.PALABRAS_DEMO) +
                        t(R.string.guestmodeintro_ves_la_palabra_en_piensas, idioma.nombre)
                )
                PasoLeccion(
                    numero = "2",
                    texto = t(R.string.guestmodeintro_prueba_tu_conocimiento_con_un, ObtenerQuizDemoUseCase.PREGUNTAS_DEMO)
                )
                PasoLeccion(
                    numero = "3",
                    texto = t(R.string.guestmodeintro_mira_tu_resultado_y_todo)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.width(240.dp)) {
                AikukisnaButton(
                    text = t(R.string.guestmodeintro_empezar_leccion),
                    onClick = onEmpezarLeccion,
                    trailingIcon = R.drawable.play
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = t(R.string.guestmodeintro_sin_crear_cuenta_gratis),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PasoLeccion(numero: String, texto: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(numero, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DetalleLeccion(emoji: String, valor: String, etiqueta: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Miskito")
@Composable
private fun GuestModeIntroScreenPreview() {
    AikukisnaTheme {
        GuestModeIntroScreen(
            idioma = Idioma.DISPONIBLES.first(),
            onEmpezarLeccion = {},
            onVolver = {}
        )
    }
}
