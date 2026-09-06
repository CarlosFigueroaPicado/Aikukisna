package com.aikukisna.app.presentacion.componentes

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.aikukisna.app.ui.theme.BorderStrong


private val DOT_HEIGHT = 7.dp
private val DOT_WIDTH_INACTIVE = 7.dp
private val DOT_WIDTH_ACTIVE = 23.dp
private val DOT_GAP = 4.dp

@Composable
fun DotsPagination(
    totalPageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,

    indicativeColor: Color = BorderStrong
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DOT_GAP, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPageCount) { pageIndex ->
            val isActive = pageIndex == currentPage

            val width by animateDpAsState(
                targetValue = if (isActive) DOT_WIDTH_ACTIVE else DOT_WIDTH_INACTIVE,
                animationSpec = tween(durationMillis = 300),
                label = "dotWidth"
            )

            Box(
                modifier = Modifier
                    .height(DOT_HEIGHT)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color = if (isActive) activeColor else indicativeColor)
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun DotsPaginationPreview() {
    AikukisnaTheme {
        DotsPagination(totalPageCount = 4, currentPage = 2)
    }
}