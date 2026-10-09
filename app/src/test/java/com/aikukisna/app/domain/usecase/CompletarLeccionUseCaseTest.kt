package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.Logro
import com.aikukisna.app.domain.model.LogroDesbloqueado
import com.aikukisna.app.domain.model.MemoriaTuki
import com.aikukisna.app.domain.model.PalabraFavorita
import com.aikukisna.app.domain.model.ProgresoLeccion
import com.aikukisna.app.domain.model.Usuario
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.ContenidoLeccion
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.LogroRepository
import com.aikukisna.app.domain.repository.UsuarioRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletarLeccionUseCaseTest {

    private val usuarioId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val idioma = Idioma(1, "mi", "Miskitu")
    private val leccion = Leccion(10, "Lección 1", 1, 1, null, idioma)

    @Test
    fun `aprobar guarda progreso gamificacion y logro antes de terminar`() = runBlocking {
        val usuarioInicial = Usuario(
            id = usuarioId,
            nombre = "Ana",
            apellido = null,
            nombreUsuario = "ana",
            correo = null,
            edad = null,
            pais = null,
            ciudad = null,
            idiomaMeta = idioma,
            xp = 10,
            rachaActual = 2,
            rachaMaxima = 2,
            ultimaActividad = LocalDate.now().minusDays(1)
        )
        val usuarios = UsuarioRepositoryFalso(usuarioInicial)
        val lecciones = LeccionRepositoryFalso(listOf(leccion))
        val logros = LogroRepositoryFalso(
            listOf(Logro(1, "Primeros pasos", "Completaste tu primera lección", "lecciones_completadas", 1, null))
        )
        val caso = CompletarLeccionUseCase(
            lecciones,
            AuthRepositoryFalso(usuarioId),
            usuarios,
            EvaluarLogrosUseCase(logros, usuarios)
        )

        caso(leccion.id, 85)

        assertEquals(leccion.id to 85, lecciones.completada)
        assertEquals("completada", usuarios.progreso.single().estado)
        assertEquals(85, usuarios.progreso.single().puntaje)
        // 10 que ya tenía + 20 de la primera aprobación (85 % no llega al bono).
        assertEquals(30, usuarios.usuario.xp)
        assertEquals(3, usuarios.usuario.rachaActual)
        assertEquals(3, usuarios.usuario.rachaMaxima)
        assertEquals(LocalDate.now(), usuarios.usuario.ultimaActividad)
        assertEquals(listOf(1), logros.desbloqueados)
        assertTrue(usuarios.actualizacionesRemotas.isEmpty())
    }

    @Test
    fun `xp de una leccion se da una vez y al repetir solo cuenta la mejora del bono`() {
        assertEquals(20, CompletarLeccionUseCase.xpGanado(null, 85))
        assertEquals(25, CompletarLeccionUseCase.xpGanado(null, 90))
        assertEquals(30, CompletarLeccionUseCase.xpGanado(null, 100))
        assertEquals(0, CompletarLeccionUseCase.xpGanado(100, 100))
        assertEquals(0, CompletarLeccionUseCase.xpGanado(90, 85))
        assertEquals(5, CompletarLeccionUseCase.xpGanado(90, 100))
        assertEquals(10, CompletarLeccionUseCase.xpGanado(85, 100))
    }

    @Test
    fun `el umbral 85 desbloquea la siguiente y 84 no`() = runBlocking {
        val segunda = leccion.copy(id = 11, titulo = "Lección 2", capituloNumero = 2)
        val lecciones = LeccionRepositoryFalso(listOf(leccion, segunda))
        val usuarios = UsuarioRepositoryFalso(usuarioBase())
        val mapa = ObtenerMapaLeccionesUseCase(
            ObtenerLeccionesUseCase(lecciones),
            ObtenerProgresoUseCase(usuarios)
        )

        usuarios.progreso = mutableListOf(progreso(leccion, 84))
        val insuficiente = mapa(usuarioId, idioma.id, 1)
        assertEquals(EstadoLeccion.ACTUAL, insuficiente.lecciones[0].estado)
        assertEquals(EstadoLeccion.BLOQUEADA, insuficiente.lecciones[1].estado)

        usuarios.progreso = mutableListOf(progreso(leccion, 85))
        val aprobado = mapa(usuarioId, idioma.id, 1)
        assertEquals(EstadoLeccion.COMPLETADA, aprobado.lecciones[0].estado)
        assertEquals(EstadoLeccion.ACTUAL, aprobado.lecciones[1].estado)
    }

    private fun usuarioBase() = Usuario(
        usuarioId, null, null, null, null, null, null, null, idioma,
        0, 0, 0, null
    )

    private fun progreso(leccion: Leccion, puntaje: Int) = ProgresoLeccion(
        usuarioId, leccion, "completada", puntaje, null
    )
}

private class LeccionRepositoryFalso(
    private val lecciones: List<Leccion>
) : LeccionRepository {
    var completada: Pair<Int, Int>? = null

    override suspend fun obtenerLecciones(nivel: Int?): List<Leccion> =
        nivel?.let { requerido -> lecciones.filter { it.nivel == requerido } } ?: lecciones

    override suspend fun obtenerLeccionPorId(id: Int): Leccion? = lecciones.firstOrNull { it.id == id }
    override suspend fun obtenerContenidoLeccion(leccionId: Int): ContenidoLeccion =
        ContenidoLeccion.Vocabulario(emptyList())

    override suspend fun completarLeccion(leccionId: Int, puntaje: Int) {
        completada = leccionId to puntaje
    }

    override suspend fun sincronizarLeccionesPendientes(): Int = 0
    override suspend fun contarLeccionesPendientes(): Int = 0
}

private class UsuarioRepositoryFalso(usuarioInicial: Usuario) : UsuarioRepository {
    var usuario = usuarioInicial
    var progreso = mutableListOf<ProgresoLeccion>()
    val actualizacionesRemotas = mutableListOf<Usuario>()

    override fun observarIdiomaMeta() = kotlinx.coroutines.flow.flowOf(usuario.idiomaMeta)
    override suspend fun obtenerUsuario(id: UUID): Usuario = usuario
    override suspend fun sincronizarPerfilPendiente(id: UUID): Boolean = true
    override suspend fun actualizarUsuario(usuario: Usuario) {
        actualizacionesRemotas += usuario
    }
    override suspend fun guardarUsuarioLocal(usuario: Usuario) {
        this.usuario = usuario
    }
    override suspend fun obtenerProgreso(usuarioId: UUID): List<ProgresoLeccion> = progreso
    override suspend fun obtenerProgresoLocal(usuarioId: UUID): List<ProgresoLeccion> = progreso
    override suspend fun actualizarProgreso(progreso: ProgresoLeccion) {
        error("La finalización no debe escribir progreso directamente en Supabase")
    }
    override suspend fun guardarProgresoLocal(progreso: ProgresoLeccion) {
        this.progreso.removeAll { it.leccion.id == progreso.leccion.id }
        this.progreso += progreso
    }
    override suspend fun obtenerFavoritos(usuarioId: UUID): List<PalabraFavorita> = emptyList()
    override suspend fun marcarFavorito(usuarioId: UUID, palabraId: Int) = Unit
    override suspend fun quitarFavorito(usuarioId: UUID, palabraId: Int) = Unit
    override suspend fun sincronizarFavoritosPendientes(): Int = 0
    override suspend fun obtenerMemoriaTuki(usuarioId: UUID): List<MemoriaTuki> = emptyList()
    override suspend fun guardarMemoriaTuki(memoria: MemoriaTuki) = Unit
}

private class LogroRepositoryFalso(
    private val logros: List<Logro>
) : LogroRepository {
    val desbloqueados = mutableListOf<Int>()
    override suspend fun obtenerLogros(): List<Logro> = logros
    override suspend fun obtenerLogrosDesbloqueados(usuarioId: UUID): List<LogroDesbloqueado> = emptyList()
    override suspend fun desbloquearLogro(usuarioId: UUID, logroId: Int) {
        desbloqueados += logroId
    }
}

private class AuthRepositoryFalso(
    private val usuarioId: UUID
) : AuthRepository {
    override suspend fun usuarioActualId(): UUID = usuarioId
    override suspend fun registrarse(correo: String, contrasena: String, metadatos: JsonObject): UUID = usuarioId
    override suspend fun iniciarSesionConCorreo(correo: String, contrasena: String): UUID = usuarioId
    override suspend fun iniciarSesionConNombreUsuario(nombreUsuario: String, contrasena: String): UUID = usuarioId
    override suspend fun cerrarSesion() = Unit
    override suspend fun iniciarSesionConGoogle(idTokenGoogle: String, nonce: String?): UUID = usuarioId
    override suspend fun solicitarRestablecimientoContrasena(correo: String) = Unit
    override suspend fun actualizarContrasena(nuevaContrasena: String) = Unit
}
