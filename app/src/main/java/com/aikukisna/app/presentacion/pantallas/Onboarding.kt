package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import kotlinx.coroutines.launch


private data class PaginaOnboarding(
    val fondo: Int,
    val tuki: Int,
    val mensaje: Int? = null,
    val titulo: String,
    val subtitulo: String,
    val cuerpo: String
)

private val paginas = listOf(
    PaginaOnboarding(
        fondo = R.drawable.morning,
        tuki = R.drawable.wave,
        mensaje = R.drawable.message,
        titulo = "Descubre el Miskito",
        subtitulo = "Lengua viva del caribe",
        cuerpo = "Aprende el idioma miskito, hablado por más de 120,000 personas en la Costa Caribe de Nicaragua."
    ),

    PaginaOnboarding(
        fondo = R.drawable.afternoon,
        tuki = R.drawable.learn,
        titulo = "Aprende de verdad",
        subtitulo = "Método probado y divertido",
        cuerpo = "Lecciones breves, quizzes interactivos y un diccionario completo para que domines el Miskito paso a paso."
    ),
    PaginaOnboarding(
        fondo = R.drawable.evening,
        tuki = R.drawable.celebrate,
        titulo = "¡Gana logros!",
        subtitulo = "Gamificación que motiva",
        cuerpo = "Acumula XP, mantén tu racha diaria y desbloquea logros únicos mientras dominas el idioma Miskito."
    ),
    PaginaOnboarding(
        fondo = R.drawable.night,
        tuki = R.drawable.music,
        titulo = "Conecta con la cultura",
        subtitulo = "Más que un idioma",
        cuerpo = "Explora la rica cultura, tradiciones, música y gastronomía del pueblo Miskito del Caribe nicaragüense."
    )
)

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
            PaginaOnboardingContenido(paginas[indice])
        }

        DotsPagination(
            totalPaginas = paginas.size,
            paginaActual = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp)
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            if (pagerState.currentPage == paginas.lastIndex) {
                AikukisnaButton(
                    text = "Comenzar",
                    onClick = onOnboardingTerminado
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onOnboardingTerminado) {
                        Text("Saltar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }) {
                        Text("Siguiente", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaginaOnboardingContenido(pagina: PaginaOnboarding) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        Box(
            modifier = Modifier.size(width = 230.dp, height = 157.dp)
        ) {
            // Fondo: llena todo el recuadro.
            Image(
                painter = painterResource(pagina.fondo),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            // Tuki: esquina inferior derecha, casi tocando el borde.
            Image(
                painter = painterResource(pagina.tuki),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 70.dp, height = 87.dp)
            )
            // Burbuja de mensaje: solo en la página que la tiene.
            pagina.mensaje?.let { mensaje ->
                Image(
                    painter = painterResource(mensaje),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 33.dp, top = 21.dp)
                        .size(width = 141.dp, height = 52.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = pagina.titulo,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = pagina.subtitulo,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = pagina.cuerpo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DotsPagination(
    totalPaginas: Int,
    paginaActual: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPaginas) { indice ->
            val esActual = indice == paginaActual
            Box(
                modifier = Modifier
                    .size(if (esActual) 10.dp else 7.dp)
                    .background(
                        color = if (esActual) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}