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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.TraductorViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray

@Composable
fun TraductorScreen(
    viewModel: TraductorViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    TraductorScreenContenido(
        idiomaOrigen = viewModel.idiomaOrigen,
        idiomaDestino = viewModel.idiomaDestino,
        onSeleccionarOrigen = viewModel::onSeleccionarOrigen,
        onSeleccionarDestino = viewModel::onSeleccionarDestino,
        onIntercambiar = viewModel::intercambiarIdiomas,
        texto = viewModel.texto,
        onTextoChange = viewModel::onTextoChange,
        resultado = viewModel.resultado,
        isLoading = viewModel.isLoading,
        errorMessage = viewModel.errorMessage,
        onTraducirClick = viewModel::traducir,
        onVolver = onVolver
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
    errorMessage: String?,
    onTraducirClick: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                contentDescription = "Volver",
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = "TRADUCTOR",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectorIdiomaCompacto(
                idiomaSeleccionado = idiomaOrigen,
                onSeleccionar = onSeleccionarOrigen,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CardSurface)
                    .border(width = 1.dp, color = LightGray, shape = CircleShape)
                    .clickable(onClick = onIntercambiar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.bidirectional),
                    contentDescription = "Intercambiar idiomas",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            SelectorIdiomaCompacto(
                idiomaSeleccionado = idiomaDestino,
                onSeleccionar = onSeleccionarDestino,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CardSurface)
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            BasicTextField(
                value = texto,
                onValueChange = onTextoChange,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { campo ->
                    if (texto.isEmpty()) {
                        Text(
                            text = "Escribe o habla en ${idiomaOrigen.nombre}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray
                        )
                    }
                    campo()
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                painter = painterResource(id = R.drawable.microphone),
                contentDescription = null,
                tint = MediumGray,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AikukisnaButton(
            text = "Traducir",
            onClick = onTraducirClick,
            isLoading = isLoading,
            enabled = texto.isNotBlank(),
            trailingIcon = R.drawable.arrow_right
        )

        Spacer(modifier = Modifier.height(20.dp))

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        resultado?.let { TarjetaResultado(it) }

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
                .clip(RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(12.dp))
                .background(CardSurface, RoundedCornerShape(12.dp))
                .clickable { expandido = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = MediumGray,
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
            .background(CardSurface)
            .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = resultado.texto,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (resultado.fuente) {
                FuenteTraduccion.DICCIONARIO -> "Verificado en el diccionario"
                FuenteTraduccion.IA -> "Traducción generada por IA"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray
        )
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