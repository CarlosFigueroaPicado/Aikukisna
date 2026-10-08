package com.aikukisna.app.di

import com.aikukisna.app.data.repository.AuthRepositoryImpl
import com.aikukisna.app.data.repository.AudioPronunciacionRepositoryImpl
import com.aikukisna.app.data.repository.DiccionarioRepositoryImpl
import com.aikukisna.app.domain.repository.AuthRepository
import com.aikukisna.app.domain.repository.AudioPronunciacionRepository
import com.aikukisna.app.domain.repository.DiccionarioRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.aikukisna.app.data.repository.LeccionRepositoryImpl
import com.aikukisna.app.domain.repository.LeccionRepository
import com.aikukisna.app.domain.repository.PreferenciasAprendizaje
import com.aikukisna.app.domain.repository.ConocimientoDocumentado
import com.aikukisna.app.data.repository.ConocimientoDocumentadoImpl
import com.aikukisna.app.data.local.PreferenciasAprendizajeLocal
import com.aikukisna.app.data.repository.CulturaRepositoryImpl
import com.aikukisna.app.domain.repository.CulturaRepository
import com.aikukisna.app.data.repository.LogroRepositoryImpl
import com.aikukisna.app.domain.repository.LogroRepository
import com.aikukisna.app.data.repository.UsuarioRepositoryImpl
import com.aikukisna.app.domain.repository.UsuarioRepository
import com.aikukisna.app.data.repository.IaRepositoryImpl
import com.aikukisna.app.domain.repository.IaRepository
import com.aikukisna.app.data.repository.VozRepositoryImpl
import com.aikukisna.app.domain.repository.VozRepository
import com.aikukisna.app.data.repository.SincronizacionRepositoryImpl
import com.aikukisna.app.domain.repository.SincronizacionRepository
import com.aikukisna.app.data.local.ConectividadHelper
import com.aikukisna.app.data.local.PronunciacionLocalCache
import com.aikukisna.app.data.local.ReconocedorObjetosMlKit
import com.aikukisna.app.data.local.DetectorObjetosMlKit
import com.aikukisna.app.data.local.ReconocedorTextoMlKit
import com.aikukisna.app.data.local.RecortadorImagenAndroid
import com.aikukisna.app.data.local.TukiMemoryStore
import com.aikukisna.app.data.repository.PronunciationEngineImpl
import com.aikukisna.app.domain.repository.NetworkAvailability
import com.aikukisna.app.domain.repository.PronunciationEngine
import com.aikukisna.app.domain.repository.PronunciationStorage
import com.aikukisna.app.domain.repository.ReconocedorObjetosLocal
import com.aikukisna.app.domain.repository.DetectorObjetosLocal
import com.aikukisna.app.domain.repository.ReconocedorTextoLocal
import com.aikukisna.app.domain.repository.RecortadorImagen
import com.aikukisna.app.domain.repository.MemoriaTukiStore
import com.aikukisna.app.domain.assistant.LocalAssistantEngine
import com.aikukisna.app.domain.assistant.RemoteAssistantEngine
import com.aikukisna.app.data.repository.OfflineAssistantEngine
import com.aikukisna.app.data.repository.GeminiAssistantEngine
import com.aikukisna.app.data.repository.GemmaAssistantEngine
import com.aikukisna.app.data.local.ia.GemmaTraductorLocal
import com.aikukisna.app.data.local.TablaEtiquetasMlKit
import com.aikukisna.app.data.local.SintetizadorVozSistema
import com.aikukisna.app.domain.repository.SintesisVozLocal
import com.aikukisna.app.domain.repository.EtiquetasObjetoEspanol
import com.aikukisna.app.domain.repository.TraductorAutomaticoLocal
import com.aikukisna.app.domain.assistant.OnDeviceAssistantEngine
import com.aikukisna.app.data.repository.RepositorioConocimientoImpl
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import com.aikukisna.app.data.gramatica.MotorGramaticalControladoImpl
import com.aikukisna.app.domain.gramatica.MotorGramaticalControlado
import com.aikukisna.app.data.gramatica.RepositorioEvidenciaGramaticalRoom
import com.aikukisna.app.domain.gramatica.RepositorioEvidenciaGramatical
import com.aikukisna.app.data.repository.CorpusCamaraRepositoryImpl
import com.aikukisna.app.domain.repository.CorpusCamaraRepository




@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    fun bindDiccionarioRepository(impl: DiccionarioRepositoryImpl): DiccionarioRepository

    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    fun bindLeccionRepository(impl: LeccionRepositoryImpl): LeccionRepository

    @Binds
    fun bindPreferenciasAprendizaje(impl: PreferenciasAprendizajeLocal): PreferenciasAprendizaje

    @Binds
    fun bindConocimientoDocumentado(impl: ConocimientoDocumentadoImpl): ConocimientoDocumentado

    @Binds
    fun bindCulturaRepository(impl: CulturaRepositoryImpl): CulturaRepository

    @Binds
    fun bindLogroRepository(impl: LogroRepositoryImpl): LogroRepository

    @Binds
    fun bindUsuarioRepository(impl: UsuarioRepositoryImpl): UsuarioRepository

    @Binds
    fun bindIaRepository(impl: IaRepositoryImpl): IaRepository

    @Binds
    fun bindVozRepository(impl: VozRepositoryImpl): VozRepository

    @Binds
    fun bindSincronizacionRepository(impl: SincronizacionRepositoryImpl): SincronizacionRepository

    @Binds
    fun bindPronunciationEngine(impl: PronunciationEngineImpl): PronunciationEngine

    @Binds
    fun bindPronunciationStorage(impl: PronunciacionLocalCache): PronunciationStorage

    @Binds
    fun bindAudioPronunciacionRepository(
        impl: AudioPronunciacionRepositoryImpl
    ): AudioPronunciacionRepository

    @Binds
    fun bindNetworkAvailability(impl: ConectividadHelper): NetworkAvailability

    @Binds
    fun bindReconocedorObjetosLocal(impl: ReconocedorObjetosMlKit): ReconocedorObjetosLocal

    @Binds
    fun bindReconocedorConceptosLocal(impl: com.aikukisna.app.data.local.ReconocedorConceptosClip): com.aikukisna.app.domain.repository.ReconocedorConceptosLocal

    @Binds
    fun bindDetectorObjetosLocal(impl: DetectorObjetosMlKit): DetectorObjetosLocal

    @Binds
    fun bindReconocedorTextoLocal(impl: ReconocedorTextoMlKit): ReconocedorTextoLocal

    @Binds
    fun bindRecortadorImagen(impl: RecortadorImagenAndroid): RecortadorImagen

    @Binds
    fun bindMemoriaTukiStore(impl: TukiMemoryStore): MemoriaTukiStore

    @Binds
    fun bindLocalAssistantEngine(impl: OfflineAssistantEngine): LocalAssistantEngine

    @Binds
    fun bindOnDeviceAssistantEngine(impl: GemmaAssistantEngine): OnDeviceAssistantEngine

    @Binds
    fun bindEtiquetasObjetoEspanol(impl: TablaEtiquetasMlKit): EtiquetasObjetoEspanol

    @Binds
    fun bindSintesisVozLocal(impl: SintetizadorVozSistema): SintesisVozLocal

    @Binds
    fun bindTraductorAutomaticoLocal(impl: GemmaTraductorLocal): TraductorAutomaticoLocal

    @Binds
    fun bindRemoteAssistantEngine(impl: GeminiAssistantEngine): RemoteAssistantEngine

    @Binds
    fun vincularRepositorioConocimiento(impl: RepositorioConocimientoImpl): RepositorioConocimiento

    @Binds
    fun vincularMotorGramaticalControlado(
        impl: MotorGramaticalControladoImpl
    ): MotorGramaticalControlado

    @Binds
    fun vincularRepositorioEvidenciaGramatical(
        impl: RepositorioEvidenciaGramaticalRoom
    ): RepositorioEvidenciaGramatical

    @Binds
    fun vincularCorpusCamaraRepository(
        impl: CorpusCamaraRepositoryImpl
    ): CorpusCamaraRepository

}
