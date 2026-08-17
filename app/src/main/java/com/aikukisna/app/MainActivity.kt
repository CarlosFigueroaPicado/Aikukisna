package com.aikukisna.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.aikukisna.app.data.auth.ProveedorTokenGoogle
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.usecase.BuscarPalabrasUseCase
import com.aikukisna.app.domain.usecase.CerrarSesionUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionConGoogleUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import com.aikukisna.app.domain.usecase.ObtenerContenidoCulturalDetalleUseCase
import com.aikukisna.app.domain.usecase.ObtenerContenidoCulturalUseCase
import com.aikukisna.app.domain.usecase.ObtenerContenidoLeccionUseCase
import com.aikukisna.app.domain.usecase.ObtenerLeccionesUseCase
import com.aikukisna.app.domain.usecase.ObtenerLogrosUseCase
import com.aikukisna.app.domain.usecase.ObtenerPalabraDetalleUseCase
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import com.aikukisna.app.domain.usecase.RegistrarUsuarioUseCase
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var obtenerLeccionesUseCase: ObtenerLeccionesUseCase
    @Inject lateinit var registrarUsuarioUseCase: RegistrarUsuarioUseCase
    @Inject lateinit var iniciarSesionUseCase: IniciarSesionUseCase
    @Inject lateinit var cerrarSesionUseCase: CerrarSesionUseCase
    @Inject lateinit var buscarPalabrasUseCase: BuscarPalabrasUseCase
    @Inject lateinit var obtenerContenidoCulturalUseCase: ObtenerContenidoCulturalUseCase
    @Inject lateinit var obtenerContenidoCulturalDetalleUseCase: ObtenerContenidoCulturalDetalleUseCase
    @Inject lateinit var obtenerLogrosUseCase: ObtenerLogrosUseCase
    @Inject lateinit var obtenerPalabraDetalleUseCase: ObtenerPalabraDetalleUseCase
    @Inject lateinit var obtenerUsuarioUseCase: ObtenerUsuarioUseCase
    @Inject lateinit var obtenerContenidoLeccionUseCase: ObtenerContenidoLeccionUseCase
    @Inject lateinit var iniciarSesionConGoogleUseCase: IniciarSesionConGoogleUseCase
    @Inject lateinit var proveedorTokenGoogle: ProveedorTokenGoogle
    @Inject lateinit var supabaseClient: SupabaseClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AikukisnaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val scope = rememberCoroutineScope()

                    var mensajeLecciones by remember { mutableStateOf("Cargando lecciones desde Supabase...") }
                    var mensajeAuth by remember { mutableStateOf("Todavía no probaste nada de Auth.") }
                    var mensajeRepos by remember { mutableStateOf("Todavía no probaste Diccionario/Cultura/Logro/Usuario.") }

                    var correo by remember { mutableStateOf("") }
                    var contrasena by remember { mutableStateOf("") }

                    var idSesionActual by remember { mutableStateOf<UUID?>(null) }

                    LaunchedEffect(Unit) {
                        mensajeLecciones = try {
                            val lecciones = obtenerLeccionesUseCase()
                            "Conexión OK: se cargaron ${lecciones.size} lecciones desde Supabase."
                        } catch (e: Exception) {
                            "Error al conectar: ${e.message}"
                        }
                    }

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(text = mensajeLecciones)

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Prueba de Auth")
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = correo,
                            onValueChange = { correo = it },
                            label = { Text("Correo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = contrasena,
                            onValueChange = { contrasena = it },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {
                                    val idiomaMeta = Idioma(id = 2, codigo = "es", nombre = "Español")
                                    val usuario = registrarUsuarioUseCase(
                                        correo = correo,
                                        contrasena = contrasena,
                                        nombre = "Prueba",
                                        apellido = "Hackathon",
                                        nombreUsuario = correo.substringBefore("@"),
                                        edad = 12,
                                        pais = "Nicaragua",
                                        ciudad = "Managua",
                                        idiomaMeta = idiomaMeta
                                    )
                                    idSesionActual = usuario.id
                                    "Registro OK: usuario creado con id ${usuario.id}"
                                } catch (e: Exception) {
                                    "Error al registrar: ${e.message}"
                                }
                            }
                        }) {
                            Text("Registrar usuario")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {
                                    val id = iniciarSesionUseCase(correo, contrasena)
                                    idSesionActual = id
                                    "Login OK: sesión iniciada con id $id"
                                } catch (e: Exception) {
                                    "Error al iniciar sesión: ${e.message}"
                                }
                            }
                        }) {
                            Text("Iniciar sesión")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {

                                    val credencial = proveedorTokenGoogle.obtenerCredencial(this@MainActivity)
                                    val id = iniciarSesionConGoogleUseCase(
                                        idTokenGoogle = credencial.idToken,
                                        nonce = credencial.nonce
                                    )
                                    idSesionActual = id
                                    "Login con Google OK: id $id"
                                } catch (e: Exception) {
                                    "Error con Google: ${e.message}"
                                }
                            }
                        }) {
                            Text("Iniciar sesión con Google")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {
                                    cerrarSesionUseCase()
                                    idSesionActual = null
                                    "Logout OK: sesión cerrada"
                                } catch (e: Exception) {
                                    "Error al cerrar sesión: ${e.message}"
                                }
                            }
                        }) {
                            Text("Cerrar sesión")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = mensajeAuth)

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Prueba de Diccionario, Cultura, Logro y Usuario")
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                val resultados = StringBuilder()

                                try {
                                    val palabras = buscarPalabrasUseCase(query = "a", idiomaId = 1)
                                    resultados.append("✅ Diccionario.buscarPalabras: ${palabras.size} resultados\n")
                                } catch (e: Exception) {
                                    resultados.append("❌ Diccionario.buscarPalabras: ${e.message}\n")
                                }

                                try {
                                    val detalle = obtenerPalabraDetalleUseCase(palabraId = 1)
                                    resultados.append(
                                        "✅ Diccionario.obtenerPalabraDetalle: '${detalle?.palabra?.texto}' " +
                                                "con ${detalle?.traducciones?.size} traducción(es)\n"
                                    )
                                } catch (e: Exception) {
                                    resultados.append("❌ Diccionario.obtenerPalabraDetalle: ${e.message}\n")
                                }

                                try {
                                    val cultura = obtenerContenidoCulturalUseCase()
                                    resultados.append("✅ Cultura.obtenerContenidoCultural: ${cultura.size} resultados\n")
                                } catch (e: Exception) {
                                    resultados.append("❌ Cultura.obtenerContenidoCultural: ${e.message}\n")
                                }

                                try {
                                    val culturaDetalle = obtenerContenidoCulturalDetalleUseCase(id = 1)
                                    resultados.append("✅ Cultura.obtenerContenidoCulturalDetalle: '${culturaDetalle?.titulo}'\n")
                                } catch (e: Exception) {
                                    resultados.append("❌ Cultura.obtenerContenidoCulturalDetalle: ${e.message}\n")
                                }

                                try {
                                    val logros = obtenerLogrosUseCase()
                                    resultados.append("✅ Logro.obtenerLogros: ${logros.size} resultados\n")
                                } catch (e: Exception) {
                                    resultados.append("❌ Logro.obtenerLogros: ${e.message}\n")
                                }

                                val id = idSesionActual
                                if (id == null) {
                                    resultados.append("⚠️ Usuario.obtenerUsuario: iniciá sesión primero\n")
                                } else {
                                    try {
                                        val usuario = obtenerUsuarioUseCase(id = id)
                                        resultados.append(
                                            "✅ Usuario.obtenerUsuario: nombre_usuario='${usuario?.nombreUsuario}' " +
                                                    "nombre='${usuario?.nombre}' apellido='${usuario?.apellido}'\n"
                                        )
                                    } catch (e: Exception) {
                                        resultados.append("❌ Usuario.obtenerUsuario: ${e.message}\n")
                                    }
                                }

                                try {

                                    val contenido = obtenerContenidoLeccionUseCase(leccionId = 17)
                                    val detalle = when (contenido) {
                                        is ContenidoLeccion.Frases -> {
                                            val primera = contenido.oraciones.firstOrNull()
                                            "${contenido.oraciones.size} oraciones — " +
                                                    "'${primera?.textoOrigen}' → '${primera?.textoDestino}'"
                                        }
                                        is ContenidoLeccion.Vocabulario ->
                                            "⚠️ devolvió Vocabulario, se esperaba Frases"
                                    }
                                    resultados.append("✅ Lección de frases (17): $detalle\n")
                                } catch (e: Exception) {
                                    resultados.append("❌ Lección de frases (17): ${e.message}\n")
                                }

                                mensajeRepos = resultados.toString()
                            }
                        }) {
                            Text("Probar Diccionario, Cultura, Logro y Usuario")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = mensajeRepos)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            supabaseClient.handleDeeplinks(intent)
        }
    }
}


