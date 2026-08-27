package com.aikukisna.app.presentacion.componentes

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.AikukisnaTheme

@Composable
fun DotsPagination(
    totalPageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    indicativeColor: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(
        modifier = modifier,
        // Espaciado uniforme de 8dp entre los puntos
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPageCount) { pageIndex ->
            val isActive = pageIndex == currentPage

            // 32dp para la píldora activa, 10dp para que el inactivo sea un círculo perfecto
            val width by animateDpAsState(
                targetValue = if (isActive) 32.dp else 10.dp,
                animationSpec = tween(durationMillis = 300),
                label = "dotWidth"
            )

            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color = if (isActive) activeColor else indicativeColor)
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
fun DotsPaginationPreview() {
    AikukisnaTheme() {
            DotsPagination(totalPageCount = 4, currentPage = 2)
        }
    }

