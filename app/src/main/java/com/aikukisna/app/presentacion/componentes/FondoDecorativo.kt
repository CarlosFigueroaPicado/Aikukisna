package com.aikukisna.app.presentacion.componentes

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.aikukisna.app.ui.theme.BlueSubtle


private val ANCHO_BANDA = 64.dp
private val ANCHO_SEPARACION = 16.dp


fun Modifier.fondoConBandas(): Modifier = this.drawBehind {
    val anchoBandaPx = ANCHO_BANDA.toPx()
    val periodoPx = anchoBandaPx + ANCHO_SEPARACION.toPx()
    var x = 0f
    while (x < size.width) {
        drawRect(
            color = BlueSubtle,
            topLeft = Offset(x, 0f),
            size = Size(anchoBandaPx, size.height)
        )
        x += periodoPx
    }
}