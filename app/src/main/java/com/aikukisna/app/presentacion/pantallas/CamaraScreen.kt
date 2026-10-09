package com.aikukisna.app.presentacion.pantallas

import com.aikukisna.app.presentacion.idioma.t

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.domain.repository.RegionObjeto
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.CamaraViewModel
import com.aikukisna.app.presentacion.viewmodel.ModoCamara
import com.aikukisna.app.presentacion.viewmodel.PuntoToque
import com.aikukisna.app.ui.theme.AikukisnaTheme
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CamaraScreen(
    viewModel: CamaraViewModel = hiltViewModel(),
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido -> tienePermiso = concedido }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        if (!tienePermiso) {
            PermisoCamaraContenido(
                onSolicitarPermiso = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                onVolver = onVolver
            )
        } else {
            VistaCamaraEnVivo(
                modo = viewModel.modo,
                isLoading = viewModel.isLoading,
                errorMessage = viewModel.errorMessage,
                resultado = viewModel.resultado,
                idiomaNombre = viewModel.idiomaMeta?.nombre,
                puntoToque = viewModel.puntoToque,
                regionSeleccionada = viewModel.regionSeleccionada,
                onModoSeleccionado = viewModel::seleccionarModo,
                onToque = viewModel::tocar,
                onCerrarResultado = viewModel::cerrarResultado,
                onErrorCaptura = viewModel::registrarErrorCaptura,
                onVolver = onVolver
            )
        }
    }
}

@Composable
private fun VistaCamaraEnVivo(
    modo: ModoCamara,
    isLoading: Boolean,
    errorMessage: String?,
    resultado: ResultadoReconocimiento?,
    idiomaNombre: String?,
    puntoToque: PuntoToque?,
    regionSeleccionada: RegionObjeto?,
    onModoSeleccionado: (ModoCamara) -> Unit,
    onToque: (String, PuntoToque) -> Unit,
    onCerrarResultado: () -> Unit,
    onErrorCaptura: () -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val previewView = remember {
        PreviewView(context).apply {
            // TextureView: permite leer el cuadro visible con getBitmap() en cualquier dispositivo.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val preview = CameraPreview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            try {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
            } catch (_: Exception) {
                // Sin cámara disponible en este dispositivo/emulador: se queda sin vista previa.
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            if (future.isDone) runCatching { future.get().unbindAll() }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

        // Capa táctil sobre la vista previa: el cuadro visible es exactamente lo que el usuario tocó.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(modo) {
                    detectTapGestures { toque ->
                        val cuadro = previewView.bitmap ?: run { onErrorCaptura(); return@detectTapGestures }
                        val punto = PuntoToque(
                            x = (toque.x / size.width).coerceIn(0f, 1f),
                            y = (toque.y / size.height).coerceIn(0f, 1f)
                        )
                        scope.launch {
                            val imagen = withContext(Dispatchers.Default) { codificarCuadro(cuadro) }
                            onToque(imagen, punto)
                        }
                    }
                }
        )

        MarcadorToque(
            punto = puntoToque,
            region = regionSeleccionada,
            modifier = Modifier.fillMaxSize()
        )

        EncabezadoCamara(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            onVolver = onVolver
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary)
                .padding(3.dp)
        ) {
            PildoraModo(t(R.string.camara_objetos), modo == ModoCamara.OBJETOS) { onModoSeleccionado(ModoCamara.OBJETOS) }
            PildoraModo(t(R.string.camara_texto), modo == ModoCamara.TEXTO) { onModoSeleccionado(ModoCamara.TEXTO) }
        }

        TarjetaInferior(
            modo = modo,
            isLoading = isLoading,
            errorMessage = errorMessage,
            resultado = resultado,
            idiomaNombre = idiomaNombre,
            onCerrar = onCerrarResultado,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        )
    }
}

/** Reduce el cuadro a un tamaño razonable para ML Kit y lo codifica como JPEG en base64. */
private fun codificarCuadro(cuadro: Bitmap): String {
    val escala = minOf(1f, MAX_LADO_CUADRO.toFloat() / maxOf(cuadro.width, cuadro.height))
    val reducido = if (escala < 1f) {
        Bitmap.createScaledBitmap(cuadro, (cuadro.width * escala).toInt(), (cuadro.height * escala).toInt(), true)
    } else cuadro
    return ByteArrayOutputStream().use { salida ->
        reducido.compress(Bitmap.CompressFormat.JPEG, 90, salida)
        if (reducido !== cuadro) reducido.recycle()
        cuadro.recycle()
        Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP)
    }
}

private const val MAX_LADO_CUADRO = 1280

/** Lado del área analizada alrededor del dedo cuando el detector no encierra el objeto tocado. */
private const val LADO_AREA_TOQUE = 0.36f

internal fun seleccionarRegionPorToque(
    regiones: List<RegionObjeto>,
    xNormalizada: Float,
    yNormalizada: Float
): RegionObjeto? = regiones
    .filter { it.contiene(xNormalizada, yNormalizada) }
    .minByOrNull { it.area }

internal fun regionAlrededorDelToque(xNormalizada: Float, yNormalizada: Float): RegionObjeto {
    val mitad = LADO_AREA_TOQUE / 2
    val izquierda = (xNormalizada - mitad).coerceIn(0f, 1f - LADO_AREA_TOQUE)
    val arriba = (yNormalizada - mitad).coerceIn(0f, 1f - LADO_AREA_TOQUE)
    return RegionObjeto(izquierda, arriba, izquierda + LADO_AREA_TOQUE, arriba + LADO_AREA_TOQUE)
}

@Composable
private fun MarcadorToque(punto: PuntoToque?, region: RegionObjeto?, modifier: Modifier = Modifier) {
    val naranja = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        region?.let {
            drawRect(
                color = naranja,
                topLeft = Offset(it.izquierda * size.width, it.arriba * size.height),
                size = Size((it.derecha - it.izquierda) * size.width, (it.abajo - it.arriba) * size.height),
                style = Stroke(width = 6f)
            )
        }
        punto?.let {
            val centro = Offset(it.x * size.width, it.y * size.height)
            drawCircle(Color.White, radius = 26f, center = centro, style = Stroke(width = 5f))
            drawCircle(naranja, radius = 10f, center = centro)
        }
    }
}

@Composable
private fun TarjetaInferior(
    modo: ModoCamara,
    isLoading: Boolean,
    errorMessage: String?,
    resultado: ResultadoReconocimiento?,
    idiomaNombre: String?,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val idioma = idiomaNombre ?: t(R.string.camara_el_idioma_que_aprendes)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(if (resultado != null) R.drawable.tuki_pointing else R.drawable.tuki_ask),
            contentDescription = null,
            modifier = Modifier.size(52.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            when {
                isLoading -> Text(
                    text = if (modo == ModoCamara.TEXTO) t(R.string.camara_leyendo_el_texto) else t(R.string.camara_reconociendo_el_objeto),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                resultado != null -> ContenidoResultado(
                    resultado,
                    modo,
                    // Texto que ya estaba en el idioma meta: se muestra su significado en español.
                    resultado.idiomaTraduccionId?.let { id -> com.aikukisna.app.domain.model.Idioma.DISPONIBLES.firstOrNull { it.id == id }?.nombre }
                        ?: idioma
                )
                errorMessage != null -> {
                    Text(errorMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Text(t(R.string.camara_toca_otra_vez_para_intentarlo), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> Text(
                    text = if (modo == ModoCamara.TEXTO) t(R.string.camara_toca_un_texto_para_traducirlo, idioma)
                    else t(R.string.camara_toca_un_objeto_para_saber, idioma),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.size(26.dp), strokeWidth = 3.dp)
            resultado != null || errorMessage != null -> Text(
                text = "✕",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clip(CircleShape).clickable(onClick = onCerrar).padding(8.dp)
            )
        }
    }
}

@Composable
private fun ContenidoResultado(resultado: ResultadoReconocimiento, modo: ModoCamara, idioma: String) {
    Text(
        text = resultado.objetoDetectado.replaceFirstChar { it.uppercase() },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (modo == ModoCamara.TEXTO) 3 else 1
    )
    Text(
        text = resultado.traduccion ?: t(R.string.camara_aun_no_tengo_una_traduccion),
        style = if (resultado.traduccion != null) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyMedium,
        fontWeight = if (resultado.traduccion != null) FontWeight.Bold else FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )
    Text(
        text = t(R.string.camara_en_toca_otro_objeto, idioma),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun PildoraModo(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (seleccionado) MaterialTheme.colorScheme.primary else Color.White
        )
    }
}

@Composable
private fun EncabezadoCamara(modifier: Modifier = Modifier, onVolver: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White).clickable(onClick = onVolver),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = t(R.string.camara_volver),
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = t(R.string.camara_camara_inteligente),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Black,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PermisoCamaraContenido(
    onSolicitarPermiso: () -> Unit,
    onVolver: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Image(
            painter = painterResource(R.drawable.camara_permiso_fondo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        EncabezadoCamara(
            modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 20.dp, vertical = 16.dp),
            onVolver = onVolver
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                .padding(horizontal = 26.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(t(R.string.camara_cam), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Image(painterResource(R.drawable.tuki_ask), null, Modifier.size(96.dp))
            Text(
                text = t(R.string.camara_permiso_de_camara),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = t(R.string.camara_aikukisna_necesita_acceso_a_la),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.width(220.dp)) {
                AikukisnaButton(
                    text = t(R.string.camara_otorgar_permiso),
                    onClick = onSolicitarPermiso,
                    trailingIcon = R.drawable.refresh
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Sin permiso")
@Composable
private fun PermisoCamaraContenidoPreview() {
    AikukisnaTheme {
        PermisoCamaraContenido(onSolicitarPermiso = {}, onVolver = {})
    }
}

@Preview(showBackground = true, name = "Resultado sobre la cámara")
@Composable
private fun TarjetaResultadoPreview() {
    AikukisnaTheme {
        TarjetaInferior(
            modo = ModoCamara.OBJETOS,
            isLoading = false,
            errorMessage = null,
            resultado = ResultadoReconocimiento(objetoDetectado = "silla", traduccion = "Sitka"),
            idiomaNombre = "Miskito",
            onCerrar = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado sin traducción")
@Composable
private fun TarjetaSinTraduccionPreview() {
    AikukisnaTheme {
        TarjetaInferior(
            modo = ModoCamara.OBJETOS,
            isLoading = false,
            errorMessage = null,
            resultado = ResultadoReconocimiento(objetoDetectado = "lámpara", traduccion = null),
            idiomaNombre = "Inglés Kriol",
            onCerrar = {}
        )
    }
}
