package com.aikukisna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.usecase.CerrarSesionUseCase
import com.aikukisna.app.domain.usecase.IniciarSesionUseCase
import com.aikukisna.app.domain.usecase.ObtenerLeccionesUseCase
import com.aikukisna.app.domain.usecase.RegistrarUsuarioUseCase
import com.aikukisna.app.ui.theme.AikukisnaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.aikukisna.app.domain.usecase.BuscarPalabrasUseCase
import com.aikukisna.app.domain.usecase.ObtenerContenidoCulturalDetalleUseCase
import com.aikukisna.app.domain.usecase.ObtenerContenidoCulturalUseCase
import com.aikukisna.app.domain.usecase.ObtenerLogrosUseCase
import com.aikukisna.app.domain.usecase.ObtenerPalabraDetalleUseCase
import com.aikukisna.app.domain.usecase.ObtenerUsuarioUseCase
import java.util.UUID

// Prueba de humo temporal: confirma que Hilt + data + Supabase real
// funcionan de punta a punta. Se reemplaza cuando `presentation` conecte
// la UI definitiva.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var obtenerLeccionesUseCase: ObtenerLeccionesUseCase

    @Inject
    lateinit var registrarUsuarioUseCase: RegistrarUsuarioUseCase

    @Inject
    lateinit var iniciarSesionUseCase: IniciarSesionUseCase

    @Inject
    lateinit var cerrarSesionUseCase: CerrarSesionUseCase

    @Inject
    lateinit var buscarPalabrasUseCase: BuscarPalabrasUseCase

    @Inject
    lateinit var obtenerContenidoCulturalUseCase: ObtenerContenidoCulturalUseCase

    @Inject
    lateinit var obtenerContenidoCulturalDetalleUseCase: ObtenerContenidoCulturalDetalleUseCase

    @Inject
    lateinit var obtenerLogrosUseCase: ObtenerLogrosUseCase

    @Inject
    lateinit var obtenerPalabraDetalleUseCase: ObtenerPalabraDetalleUseCase

    @Inject
    lateinit var obtenerUsuarioUseCase: ObtenerUsuarioUseCase

    private val correoPrueba = "prueba.aikukisna.hackathon@gmail.com"
    private val contrasenaPrueba = "Prueba123456"

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

                    LaunchedEffect(Unit) {
                        mensajeLecciones = try {
                            val lecciones = obtenerLeccionesUseCase()
                            "Conexión OK: se cargaron ${lecciones.size} lecciones desde Supabase."
                        } catch (e: Exception) {
                            "Error al conectar: ${e.message}"
                        }
                    }

                    Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                        Text(text = mensajeLecciones)

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(text = "Prueba de Auth (correo: $correoPrueba)")
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {
                                    val idiomaPrueba = Idioma(id = 2, codigo = "es", nombre = "Español")
                                    val usuario = registrarUsuarioUseCase(
                                        correo = correoPrueba,
                                        contrasena = contrasenaPrueba,
                                        nombre = "Prueba",
                                        apellido = "Hackathon",
                                        nombreUsuario = "prueba_hackathon",
                                        edad = 12,
                                        pais = "Nicaragua",
                                        ciudad = "Managua",
                                        idiomaMeta = idiomaPrueba
                                    )
                                    "Registro OK: usuario creado con id ${usuario.id}"
                                } catch (e: Exception) {
                                    "Error al registrar: ${e.message}"
                                }
                            }
                        }) {
                            Text("Registrar usuario de prueba")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(onClick = {
                            scope.launch {
                                mensajeAuth = try {
                                    val id = iniciarSesionUseCase(correoPrueba, contrasenaPrueba)
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
                                    cerrarSesionUseCase()
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
                                resultados.append("✅ Diccionario.obtenerPalabraDetalle: '${detalle?.palabra?.texto}' con ${detalle?.traducciones?.size} traducción(es)\n")
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

                            try {
                                val idPrueba = UUID.fromString("658a6cca-83ea-4cd8-9182-481075915c60")
                                val usuario = obtenerUsuarioUseCase(id = idPrueba)
                                resultados.append("✅ Usuario.obtenerUsuario: nombre_usuario='${usuario?.nombreUsuario}'\n")
                            } catch (e: Exception) {
                                resultados.append("❌ Usuario.obtenerUsuario: ${e.message}\n")
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
}