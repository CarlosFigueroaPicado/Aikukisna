package com.aikukisna.app.presentacion.pantallas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aikukisna.app.R
import com.aikukisna.app.ui.theme.AikukisnaTheme
import kotlinx.coroutines.delay

private const val PEN_DRAW_PATH_DATA =
    "M155 444.5C155 444.5 158.572 428.7 161 410.5C162.819 396.863 162.859 380.783 166.5 375" +
            "C175 361.5 194.42 357.678 204 369C215 382 215 398.191 215 398.191C215 398.191 218.827 427.673 209 437.5" +
            "C199 447.5 188 443 183 436C176.929 427.5 179.549 415.544 184 410.5C191.5 402 220.5 398.5 238.5 390.5" +
            "C249.5 385.611 259 374 260.5 359"

private const val TAIL_PATH_DATA =
    "M218 400C223.167 402 232.838 408.414 240.5 416C247.438 422.869 249.921 429.286 256.5 436.5" +
            "C261.06 441.5 262.818 443.648 268 448"

private const val LOGO_STROKE_WIDTH_DP = 17f

private val TUKI_OFFSET_X_DP = 150.dp
private val TUKI_OFFSET_Y_DP = 287.dp
private val TUKI_SIZE_DP = 80.dp

private const val DOT_RADIUS_DP = 3f
private const val DOT_Y_DP = 485f
private val DOT_X_POSITIONS_DP = listOf(191f, 201f, 211f)



private val EaseTrimReveal = CubicBezierEasing(0.5f, 0f, 0.5f, 1f)
private val EaseBounceOvershoot = CubicBezierEasing(0.4f, 1.75f, 0.3f, 1f)
private val EaseInOutStandard = CubicBezierEasing(0.42f, 0f, 0.58f, 1f) // "ease-in-out" CSS estándar

private data class Keyframe(val fraction: Float, val value: Float, val easingToNext: Easing = LinearEasing)


private fun sampleKeyframes(progress: Float, keyframes: List<Keyframe>): Float {
    if (progress <= keyframes.first().fraction) return keyframes.first().value
    if (progress >= keyframes.last().fraction) return keyframes.last().value
    for (i in 0 until keyframes.size - 1) {
        val start = keyframes[i]
        val end = keyframes[i + 1]
        if (progress in start.fraction..end.fraction) {
            val localT = (progress - start.fraction) / (end.fraction - start.fraction)
            val eased = start.easingToNext.transform(localT)
            return start.value + (end.value - start.value) * eased
        }
    }
    return keyframes.last().value
}


private val penDrawTrimKeyframes = listOf(
    Keyframe(0f, 0f, EaseTrimReveal),
    Keyframe(1f, 1f),
)
private val tailTrimKeyframes = listOf(
    Keyframe(0f, 0f),
    Keyframe(0.45f, 0f, EaseTrimReveal),
    Keyframe(1f, 1f),
)
private val tukiScaleKeyframes = listOf(
    Keyframe(0f, 0f, EaseBounceOvershoot),
    Keyframe(0.9f, 1f),
    Keyframe(1f, 1f),
)

/** Cuánto tarda la fase 1 (AK dibujándose + Tuki asentándose). */
private const val REVEAL_DURATION_MS = 2000

// --- Puntos de carga: indicador continuo en secuencia, fase 2 ---

private const val DOTS_LOADING_CYCLE_MS = 800
private const val DOT_SCALE_PEAK = 1.6f


private const val DOTS_PHASE_DURATION_MS = 7_000L


@Composable
private fun animarEscalaPunto(indiceEnSecuencia: Int): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "puntoCarga$indiceEnSecuencia")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = DOTS_LOADING_CYCLE_MS
                1f at 0
                DOT_SCALE_PEAK at (DOTS_LOADING_CYCLE_MS * 0.35f).toInt() using EaseInOutStandard
                1f at (DOTS_LOADING_CYCLE_MS * 0.7f).toInt() using EaseInOutStandard
                1f at DOTS_LOADING_CYCLE_MS
            },
            initialStartOffset = StartOffset(
                offsetMillis = indiceEnSecuencia * (DOTS_LOADING_CYCLE_MS / 3),
                offsetType = StartOffsetType.FastForward,
            ),
        ),
        label = "escalaPunto$indiceEnSecuencia",
    )
}

private enum class FaseCarga { RevelacionLogo, PuntosCargando }

/** @param onFinished se llama una sola vez, cuando termina la fase 2 (puntos cargando). */
@Composable
fun LoadingScreen(onFinished: () -> Unit = {}) {
    var fase by remember { mutableStateOf(FaseCarga.RevelacionLogo) }

    LaunchedEffect(fase) {
        when (fase) {
            FaseCarga.RevelacionLogo -> {
                delay(REVEAL_DURATION_MS.toLong())
                fase = FaseCarga.PuntosCargando
            }
            FaseCarga.PuntosCargando -> {
                delay(DOTS_PHASE_DURATION_MS)
                onFinished()
            }
        }
    }


    val revelacion = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        revelacion.animateTo(1f, animationSpec = tween(REVEAL_DURATION_MS, easing = LinearEasing))
    }
    val penDrawTrim = sampleKeyframes(revelacion.value, penDrawTrimKeyframes)
    val tailTrim = sampleKeyframes(revelacion.value, tailTrimKeyframes)
    val tukiScale = sampleKeyframes(revelacion.value, tukiScaleKeyframes)

    //
    val density = LocalDensity.current.density
    val penDrawPath = remember(density) { parseAndScalePath(PEN_DRAW_PATH_DATA, density) }
    val tailPath = remember(density) { parseAndScalePath(TAIL_PATH_DATA, density) }

    val logoColor = MaterialTheme.colorScheme.primary
    val dotsColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding(),
    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawTrimmedPath(penDrawPath, penDrawTrim, logoColor, LOGO_STROKE_WIDTH_DP)
            drawTrimmedPath(tailPath, tailTrim, logoColor, LOGO_STROKE_WIDTH_DP)
        }


        Image(
            painter = painterResource(id = R.drawable.tuki_loading),
            contentDescription = null,
            modifier = Modifier
                .offset(x = TUKI_OFFSET_X_DP, y = TUKI_OFFSET_Y_DP)
                .size(TUKI_SIZE_DP)
                .graphicsLayer {
                    scaleX = tukiScale
                    scaleY = tukiScale
                },
        )

         if (fase == FaseCarga.PuntosCargando) {
            val punto1Escala by animarEscalaPunto(0)
            val punto2Escala by animarEscalaPunto(1)
            val punto3Escala by animarEscalaPunto(2)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val yPx = DOT_Y_DP.dp.toPx()
                val baseRadiusPx = DOT_RADIUS_DP.dp.toPx()
                val escalas = listOf(punto1Escala, punto2Escala, punto3Escala)
                DOT_X_POSITIONS_DP.forEachIndexed { indice, xDp ->
                    drawCircle(
                        color = dotsColor,
                        radius = baseRadiusPx * escalas[indice],
                        center = Offset(xDp.dp.toPx(), yPx),
                    )
                }
            }
        }
    }
}


private fun parseAndScalePath(pathData: String, density: Float): Path {
    val androidPath = PathParser().parsePathString(pathData).toPath().asAndroidPath()
    val matrix = android.graphics.Matrix().apply { setScale(density, density) }
    androidPath.transform(matrix)
    return androidPath.asComposePath()
}

/** Dibuja solo el tramo [0, longitudTotal * trim] del [path], con el mismo efecto
 * de "trazo progresivo" (path-trim) que usa el archivo de Figma. */
private fun DrawScope.drawTrimmedPath(path: Path, trim: Float, color: Color, strokeWidthDp: Float) {
    if (trim <= 0f) return
    val androidPath = path.asAndroidPath()
    val measure = android.graphics.PathMeasure(androidPath, false)
    val segment = android.graphics.Path()
    measure.getSegment(0f, measure.length * trim, segment, true)
    drawPath(
        path = segment.asComposePath(),
        color = color,
        style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round),
    )
}

@Preview(showBackground = true, name = "Modo Claro")
@Composable
private fun LoadingScreenPreview() {
    AikukisnaTheme {
        LoadingScreen()
    }
}