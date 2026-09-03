package com.aikukisna.app.presentacion.pantallas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aikukisna.app.R
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.componentes.ButtonStyle
import com.aikukisna.app.presentacion.componentes.DotsPagination
import com.aikukisna.app.ui.theme.AikukisnaTheme
import kotlinx.coroutines.launch

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val tagLine: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.ic_descubre,
        title = "Descubre el Miskito",
        tagLine = "Lengua viva del caribe",
        description = "Aprende el idioma miskito, hablado por más de 120.000 personas en la costa caribe de Nicaragua."
    ),
    OnboardingPage(
        imageRes = R.drawable.ic_aprende,
        title = "Aprende de Verdad",
        tagLine = "Método probado y divertido",
        description = "Lecciones breves, quizzes interactivos y un diccionario completo para que domines el Miskito paso a paso."
    ),
    OnboardingPage(
        imageRes = R.drawable.ic_gana_logros,
        title = "¡Gana logros!",
        tagLine = "Gamificación que motiva",
        description = "Acumula XP, mantén tu racha diaria y desbloquea logros únicos mientras dominas el idioma Miskito."
    ),
    OnboardingPage(
        imageRes = R.drawable.ic_conecta,
        title = "Conecta con la cultura",
        tagLine = "Más que un idioma",
        description = "Explora la rica cultura, tradiciones, música y gastronomía del pueblo Miskito del Caribe Nicaragüense."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Pager para deslizar las pantallas
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { pagePosition ->
            PagerCard(page = onboardingPages[pagePosition])
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de puntos (paginación)
        DotsPagination(
            totalPageCount = onboardingPages.size,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Botones de acción inferiores
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLastPage) {
                AikukisnaButton(
                    modifier = Modifier.weight(1f),
                    text = "SALTAR",
                    onClick = onFinished,
                    style = ButtonStyle.Ghost
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.weight(0.2f))

            AikukisnaButton(
                modifier = Modifier.weight(1.5f),
                text = if (isLastPage) "Comenzar" else "Siguiente",
                onClick = {
                    if (isLastPage) {
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                style = ButtonStyle.Primary,
                trailingIcon = if (isLastPage) null else R.drawable.arrow_right
            )
        }
    }
}

@Composable
fun PagerCard(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(260.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.displayLarge,
            fontSize = 32.sp,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.tagLine,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_6", name = "Onboarding Completo")
@Composable
fun OnboardingScreenPreview() {
    AikukisnaTheme {
        OnboardingScreen(onFinished = {})
    }
}