package com.aikukisna.app.di

import com.aikukisna.app.data.local.ia.MotorGemmaLocal
import com.aikukisna.app.domain.assistant.TukiAssistant
import com.aikukisna.app.domain.repository.ReconocedorObjetosLocal
import com.aikukisna.app.domain.repository.DetectorObjetosLocal
import com.aikukisna.app.domain.repository.RecortadorImagen
import com.aikukisna.app.domain.usecase.ReconocerObjetoUseCase
import com.aikukisna.app.domain.usecase.ObtenerVocabularioLeccionUseCase
import com.aikukisna.app.domain.usecase.GenerarQuizLeccionUseCase
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.usecase.TraducirOracionUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Acceso al grafo real de Hilt desde las pruebas instrumentadas (app/src/androidTest), para
 * evaluar Tuki, el traductor y la cámara en el teléfono con el modelo y la base offline reales.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface PruebasDispositivoEntryPoint {
    fun tukiAssistant(): TukiAssistant
    fun traducirOracion(): TraducirOracionUseCase
    fun motorGemma(): MotorGemmaLocal
    fun reconocerObjeto(): ReconocerObjetoUseCase
    fun reconocedorObjetosLocal(): ReconocedorObjetosLocal
    fun detectorObjetos(): DetectorObjetosLocal
    fun recortador(): RecortadorImagen
    fun vocabularioLeccion(): ObtenerVocabularioLeccionUseCase
    fun quizLeccion(): GenerarQuizLeccionUseCase
    fun conectividad(): NetworkAvailability
    fun motorPronunciacion(): com.aikukisna.app.domain.repository.PronunciationEngine
    fun audiosPronunciacion(): com.aikukisna.app.domain.repository.AudioPronunciacionRepository
    fun sembradorAudiosHumanos(): com.aikukisna.app.data.local.SembradorAudiosHumanos
}
