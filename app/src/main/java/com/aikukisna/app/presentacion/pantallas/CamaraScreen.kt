package com.aikukisna.app.presentacion.pantallas

import android.Manifest
import android.content.pm.PackageManager
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikukisna.app.R
import com.aikukisna.app.domain.model.ResultadoReconocimiento
import com.aikukisna.app.presentacion.componentes.AikukisnaButton
import com.aikukisna.app.presentacion.viewmodel.CamaraViewModel
import com.aikukisna.app.ui.theme.AikukisnaTheme
import com.aikukisna.app.ui.theme.BrandSubtle
import com.aikukisna.app.ui.theme.CardSurface
import com.aikukisna.app.ui.theme.LightGray
import com.aikukisna.app.ui.theme.MediumGray

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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when {
            !tienePermiso -> {
                PermisoCamaraContenido(
                    onSolicitarPermiso = { lanzadorPermiso.launch(Manifest.permission.CAMERA) },
                    onVolver = onVolver
                )
            }
            viewModel.resultado != null -> {
                ResultadoReconocimientoContenido(
                    resultado = viewModel.resultado!!,
                    onEscanearOtraVez = viewModel::escanearOtraVez,
                    onVolver = onVolver
                )
            }
            else -> {
                VistaCamaraEnVivo(
                    isLoading = viewModel.isLoading,
                    errorMessage = viewModel.errorMessage,
                    onImagenCapturada = viewModel::analizarImagen,
                    onVolver = onVolver
                )
            }
        }
    }
}

@Composable
private fun VistaCamaraEnVivo(
    isLoading: Boolean,
    errorMessage: String?,
    onImagenCapturada: (String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var modoObjetos by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            val preview = CameraPreview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val captura = ImageCapture.Builder().build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    captura
                )
                cameraProvider = provider
                imageCapture = captura
            } catch (e: Exception) {
                // Sin cámara disponible en este dispositivo/emulador — se queda
                // sin vista previa, pero no crashea.
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Volver",
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onVolver)
            )
            Text(
                text = "CÁMARA INTELIGENTE",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PildoraModo(texto = "Objetos", seleccionado = modoObjetos, onClick = { modoObjetos = true })
            PildoraModo(texto = "Texto", seleccionado = false, onClick = {})
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(12.dp))
            }
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(if (isLoading) MediumGray else MaterialTheme.colorScheme.primary)
                    .border(width = 3.dp, color = Color.White, shape = CircleShape)
                    .clickable(enabled = !isLoading && imageCapture != null) {
                        val captura = imageCapture ?: return@clickable
                        captura.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    val buffer = image.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    image.close()
                                    onImagenCapturada(Base64.encodeToString(bytes, Base64.NO_WRAP))
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    // Se refleja como errorMessage vía el ViewModel en el próximo intento.
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {}
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Toca para escanear",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PildoraModo(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = seleccionado, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            color = if (seleccionado) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun PermisoCamaraContenido(
    onSolicitarPermiso: () -> Unit,
    onVolver: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Volver",
                modifier = Modifier.size(20.dp).clickable(onClick = onVolver)
            )
            Text(
                text = "CÁMARA INTELIGENTE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BrandSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Permiso de cámara",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Aikukisna necesita acceso a la cámara para reconocer objetos y encontrar su nombre en Miskito.",
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.width(220.dp)) {
                AikukisnaButton(
                    text = "Otorgar permiso",
                    onClick = onSolicitarPermiso,
                    trailingIcon = R.drawable.refresh
                )
            }
        }
    }
}

@Composable
private fun ResultadoReconocimientoContenido(
    resultado: ResultadoReconocimiento,
    onEscanearOtraVez: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardSurface)
                .border(width = 1.dp, color = LightGray, shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = resultado.objetoDetectado,
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resultado.traduccion ?: "Sin traducción verificada todavía",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.width(220.dp)) {
            AikukisnaButton(
                text = "Escanear otra vez",
                onClick = onEscanearOtraVez,
                trailingIcon = R.drawable.camera
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Volver",
            style = MaterialTheme.typography.labelLarge,
            color = MediumGray,
            modifier = Modifier.clickable(onClick = onVolver)
        )
    }
}

@Preview(showBackground = true, name = "Sin permiso")
@Composable
private fun PermisoCamaraContenidoPreview() {
    AikukisnaTheme {
        PermisoCamaraContenido(onSolicitarPermiso = {}, onVolver = {})
    }
}

@Preview(showBackground = true, name = "Resultado")
@Composable
private fun ResultadoReconocimientoContenidoPreview() {
    AikukisnaTheme {
        ResultadoReconocimientoContenido(
            resultado = ResultadoReconocimiento(objetoDetectado = "silla", traduccion = "Sitka"),
            onEscanearOtraVez = {},
            onVolver = {}
        )
    }
}

@Preview(showBackground = true, name = "Resultado sin traducción")
@Composable
private fun ResultadoReconocimientoContenidoSinTraduccionPreview() {
    AikukisnaTheme {
        ResultadoReconocimientoContenido(
            resultado = ResultadoReconocimiento(objetoDetectado = "lámpara", traduccion = null),
            onEscanearOtraVez = {},
            onVolver = {}
        )
    }
}