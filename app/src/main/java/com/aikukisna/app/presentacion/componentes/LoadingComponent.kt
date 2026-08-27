package com.aikukisna.app.presentacion.componentes

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    circleColor: Color = Color.White,
    circleSize: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val activeIndex by infiniteTransition.animateValue(
        initialValue = 0,
        targetValue = 3,
        typeConverter = Int.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "activeIndex"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val size = if (index == activeIndex) circleSize * 1.6f else circleSize
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(circleColor)
            )
        }
    }
}

@Preview
@Composable
fun LoadingDotsPreview() {
    LoadingDots()
}