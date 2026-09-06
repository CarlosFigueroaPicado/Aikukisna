package com.aikukisna.app.presentacion.pantallas

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.acentoPara
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BorderStrong
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.DarkNeutral
import com.aikukisna.app.ui.theme.MediumGray
import com.aikukisna.app.ui.theme.YellowPrimary

@Composable
fun SeleccionarIdiomaScreen(
    onContinuar: (Idioma) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var seleccionado by remember { mutableStateOf<Idioma?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 26.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_ave_login),
            contentDescription = null,
            modifier = Modifier.size(width = 89.dp, height = 80.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¿Qué idioma querés aprender?",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Elegí uno para tu lección de muestra",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.widthIn(max = 298.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Idioma.DISPONIBLES.chunked(2).forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    fila.forEach { idioma ->
                        TarjetaIdioma(
                            idioma = idioma,
                            seleccionado = idioma == seleccionado,
                            onClick = { seleccionado = idioma },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(28.dp))

        Box(modifier = Modifier.width(240.dp)) {
            AikukisnaButton(
                text = "Continuar",
                onClick = { seleccionado?.let(onContinuar) },
                enabled = seleccionado != null && !isLoading,
                isLoading = isLoading,
                trailingIcon = R.drawable.arrow_right
            )
        }

        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TarjetaIdioma(
    idioma: Idioma,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val acento = remember(idioma) { acentoPara(idioma) }

    val interactionSource = remember { MutableInteractionSource() }
    val estaPresionado by interactionSource.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (estaPresionado) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "escalaTarjeta"
    )

    Column(
        modifier = modifier
            .height(104.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (seleccionado) 2.dp else 1.dp,
                color = if (seleccionado) acento.color else BorderStrong,
                shape = RoundedCornerShape(12.dp)
            )
            .background(
                color = if (seleccionado) acento.color.copy(alpha = 0.1f) else CardSurface,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(acento.color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = acento.inicial,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,

                color = if (acento.color == YellowPrimary) DarkNeutral else Color.White
            )
        }

        Text(
            text = idioma.nombre,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            color = if (seleccionado) acento.color else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, name = "Sin elegir")
@Composable
private fun SeleccionarIdiomaScreenPreview() {
    AikukisnaTheme {
        SeleccionarIdiomaScreen(onContinuar = {})
    }
}