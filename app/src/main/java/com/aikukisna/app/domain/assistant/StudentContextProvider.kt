package com.aikukisna.app.domain.assistant

import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.usecase.ObtenerLogrosConEstadoUseCase
import com.aikukisna.app.domain.usecase.ObtenerMapaLeccionesUseCase
import com.aikukisna.app.domain.usecase.ObtenerProximaLeccionUseCase
import java.util.UUID
import javax.inject.Inject

data class StudentContext(
    val name: String?,
    val languageName: String,
    val xp: Int,
    val completedLessons: Int,
    val streak: Int,
    val maximumStreak: Int,
    val unlockedAchievements: List<String>,
    val latestScore: Int?,
    val latestLessonId: Int?,
    val latestLessonTitle: String?,
    val latestLessonCompleted: Boolean?,
    val currentLessonId: Int?,
    val currentLessonTitle: String?,
    val online: Boolean,
    val pendingLessonSyncs: Int
) {
    fun minimalSummary(): String = buildString {
        append("Idioma de aprendizaje: ").append(languageName)
        append(". XP: ").append(xp)
        append(". Lecciones completadas: ").append(completedLessons)
        append(". Racha actual: ").append(streak)
        latestScore?.let { append(". Último puntaje: ").append(it).append('%') }
        currentLessonTitle?.let { append(". Lección actual: ").append(it) }
        append(". Conexión: ").append(if (online) "en línea" else "sin conexión")
        append(". Sincronizaciones pendientes: ").append(pendingLessonSyncs)
    }
}

class StudentContextProvider @Inject constructor(
    private val users: UsuarioRepository,
    private val nextLesson: ObtenerProximaLeccionUseCase,
    private val achievements: ObtenerLogrosConEstadoUseCase,
    private val lessons: LeccionRepository,
    private val network: NetworkAvailability
) {
    suspend fun name(userId: UUID?): String? = userId?.let { users.obtenerUsuario(it)?.nombre }

    suspend fun get(userId: UUID?, languageId: Int): StudentContext? {
        if (userId == null) return null
        val user = users.obtenerUsuario(userId) ?: return null
        val progress = users.obtenerProgreso(userId)
        val next = nextLesson(userId, languageId)
        val latest = progress.maxByOrNull { it.fechaCompletado ?: java.time.Instant.EPOCH }
        return StudentContext(
            name = user.nombre,
            languageName = user.idiomaMeta?.nombre ?: "el idioma seleccionado",
            xp = user.xp,
            completedLessons = progress.count {
                it.estado == "completada" && (it.puntaje ?: 0) >= ObtenerMapaLeccionesUseCase.PORCENTAJE_APROBACION
            },
            streak = user.rachaActual,
            maximumStreak = user.rachaMaxima,
            unlockedAchievements = achievements(userId).filter { it.desbloqueado }.map { it.logro.nombre },
            latestScore = latest?.puntaje,
            latestLessonId = latest?.leccion?.id,
            latestLessonTitle = latest?.leccion?.titulo,
            latestLessonCompleted = latest?.let {
                it.estado == "completada" && (it.puntaje ?: 0) >= ObtenerMapaLeccionesUseCase.PORCENTAJE_APROBACION
            },
            currentLessonId = next?.leccion?.id,
            currentLessonTitle = next?.leccion?.titulo,
            online = network.hayConexion(),
            pendingLessonSyncs = lessons.contarLeccionesPendientes()
        )
    }
}
