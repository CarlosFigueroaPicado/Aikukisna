package com.aikukisna.app.presentacion.pantallas

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.DotsPagination
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue


private data class PaginaOnboarding(
    val titulo: String,
    val subtitulo: String,
    val cuerpo: String,
)

private val paginas = listOf(
    PaginaOnboarding(
        titulo = "Descubre las lenguas",
        subtitulo = "Lenguas vivas de la costa caribe de Nicaragua",
        cuerpo = "Aprende estas lenguas originarias y afrodescendientes, habladas entre 200,000 y 230,000 personas en la Costa Caribe de Nicaragua: Miskito (Miskitu), Mayangna / Sumu, Rama, Creole / Kriol (Inglés Criollo Nicaragüense) y Garífuna."
    ),
    PaginaOnboarding(
        titulo = "Aprende de verdad",
        subtitulo = "Método probado y divertido",
        cuerpo = "Lecciones breves, quizzes interactivos y un diccionario completo para que domines estas lenguas paso a paso."
    ),
    PaginaOnboarding(
        titulo = "¡Gana logros!",
        subtitulo = "Gamificación que motiva",
        cuerpo = "Acumula XP, mantén tu racha diaria y desbloquea logros únicos mientras dominas estas lenguas."
    ),
    PaginaOnboarding(
        titulo = "Conecta con la cultura",
        subtitulo = "Más que un idioma",
        cuerpo = "Explora la rica cultura, tradiciones, música y gastronomía de los pueblos originarios y afrodescendientes del Caribe nicaragüense."
    ),
)

private val ILUSTRACION_TOP_OFFSET = listOf(120.dp, 132.dp, 130.dp, 132.dp)

private val CONTENIDO_ANCHO_CUERPO = 254.dp

@Composable
fun OnboardingScreen(
    onOnboardingTerminado: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { paginas.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { indice ->

            val distanciaAlCentro = (
                    (pagerState.currentPage - indice) + pagerState.currentPageOffsetFraction
                    ).absoluteValue.coerceIn(0f, 1f)

            PaginaOnboardingContenido(
                indice = indice,
                pagina = paginas[indice],
                paginaActual = pagerState.currentPage,
                totalPaginas = paginas.size,
                modifier = Modifier.graphicsLayer {
                    val escala = lerp(0.88f, 1f, 1f - distanciaAlCentro)
                    scaleX = escala
                    scaleY = escala
                    alpha = lerp(0.3f, 1f, 1f - distanciaAlCentro)
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (pagerState.currentPage == paginas.lastIndex) {

                Box(modifier = Modifier.width(240.dp)) {
                    AikukisnaButton(
                        text = "¡Empieza a aprender!",
                        onClick = onOnboardingTerminado,
                        fontSize = 16.sp
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 51.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onOnboardingTerminado,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Saltar".uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(modifier = Modifier.width(IntrinsicSize.Min)) {
                        AikukisnaButton(
                            text = "Siguiente",
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            trailingIcon = R.drawable.arrow_right,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginaOnboardingContenido(
    indice: Int,
    pagina: PaginaOnboarding,
    paginaActual: Int,
    totalPaginas: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(ILUSTRACION_TOP_OFFSET[indice]))

        IlustracionPagina(indice)

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pagina.titulo,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = pagina.subtitulo,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = pagina.cuerpo,
                // bodySmall (14sp) + 2sp = 16sp, que ya es exactamente bodyMedium
                // en Type.kt — uso ese escalón existente en vez de un tamaño suelto.
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(CONTENIDO_ANCHO_CUERPO)
            )
            DotsPagination(
                totalPageCount = totalPaginas,
                currentPage = paginaActual
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IlustracionPagina(indice: Int) {
    when (indice) {
        0 -> IlustracionDescubre()
        1 -> IlustracionAprende()
        2 -> IlustracionLogros()
        else -> IlustracionCultura()
    }
}

@Composable
private fun IlustracionDescubre() {
    Box(modifier = Modifier.size(width = 210.dp, height = 143.dp)) {
        Image(
            painter = painterResource(R.drawable.morning),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.wave),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 133.dp, y = 63.dp)
                .size(width = 64.dp, height = 79.dp)
        )

        BurbujaTuki(modifier = Modifier.offset(x = 30.dp, y = (-10).dp))
    }
}

@Composable
private fun IlustracionAprende() {
    Box(modifier = Modifier.size(width = 207.dp, height = 131.dp)) {
        Image(
            painter = painterResource(R.drawable.afternoon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.learn),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 63.dp, y = 54.dp)
                .size(width = 80.dp, height = 77.dp)
        )
    }
}

@Composable
private fun IlustracionLogros() {
    Box(modifier = Modifier.size(width = 213.dp, height = 133.dp)) {
        Image(
            painter = painterResource(R.drawable.evening),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.celebrate),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 93.dp, y = 32.dp)
                .size(80.dp)
        )
    }
}

@Composable
private fun IlustracionCultura() {
    Box(modifier = Modifier.size(width = 207.dp, height = 131.dp)) {
        Image(
            painter = painterResource(R.drawable.night),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(R.drawable.music),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 63.dp, y = 54.dp)
                .size(width = 80.dp, height = 77.dp)
        )
    }
}


private val MENSAJES_TUKI: List<AnnotatedString> = listOf(
    buildAnnotatedString {
        append("¡Hola! Soy ")
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Tuki") }
    },
    AnnotatedString("¡Juntos aprenderemos el fabuloso mundo de los idiomas!"),
)

private const val MENSAJE_INTERVALO_MS = 3500L

@Composable
private fun BurbujaTuki(modifier: Modifier = Modifier) {
    var indiceMensaje by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MENSAJE_INTERVALO_MS)
            indiceMensaje = (indiceMensaje + 1) % MENSAJES_TUKI.size
        }
    }

    Box(

        modifier = modifier
            .widthIn(max = 220.dp)
            .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(12.dp))
            .background(color = CardSurface, shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Crossfade(targetState = indiceMensaje, label = "mensajeTuki") { i ->
            Text(
                text = MENSAJES_TUKI[i],
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}