package com.aikukisna.app.data.repository

import android.content.Context
import com.aikukisna.app.data.local.SembradorAudiosHumanos
import com.aikukisna.app.data.local.dao.AudioPronunciacionDao
import com.aikukisna.app.data.local.entity.AudioPronunciacionEntity
import com.aikukisna.app.domain.model.AudioPronunciacion
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.OrigenAudioPronunciacion
import com.aikukisna.app.domain.model.ReferenciaAudioPronunciacion
import com.aikukisna.app.domain.model.normalizarFormaAudio
import com.aikukisna.app.domain.repository.AudioPronunciacionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AudioPronunciacionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: AudioPronunciacionDao,
    private val sembrador: SembradorAudiosHumanos
) : AudioPronunciacionRepository {

    /** Si al arrancar la réplica aún no estaba lista, las grabaciones se registran (o completan) al consultarlas. */
    private suspend fun asegurarGrabaciones() {
        if (sembrador.registroCompleto) return
        runCatching { sembrador.sembrarSiCambio() }
    }

    override suspend fun buscarVerificados(
        palabraId: Int,
        idioma: Idioma
    ): List<AudioPronunciacion> = asegurarGrabaciones().let { dao }
        .buscarVerificados(palabraId, idioma.codigo)
        .mapNotNull { it.aDomain(idioma) }

    override suspend fun buscarHumanosPorTexto(
        texto: String,
        idioma: Idioma
    ): List<AudioPronunciacion> {
        val forma = normalizarFormaAudio(texto)
        if (forma.isBlank()) return emptyList()
        asegurarGrabaciones()
        return dao.buscarHumanosPorTexto(forma, idioma.codigo).mapNotNull { it.aDomain(idioma) }
    }

    override suspend fun registrar(audio: AudioPronunciacion) {
        dao.guardar(audio.aEntity())
    }

    override suspend fun estaDisponibleOffline(audio: AudioPronunciacion): Boolean =
        leerAudio(audio) != null

    override suspend fun leerAudio(audio: AudioPronunciacion): ByteArray? = withContext(Dispatchers.IO) {
        val valor = audio.referencia.valor
        if (!referenciaSegura(valor)) return@withContext null

        runCatching {
            when (audio.referencia) {
                is ReferenciaAudioPronunciacion.Asset ->
                    context.assets.open(valor).use { it.readBytes() }

                is ReferenciaAudioPronunciacion.ArchivoInterno -> {
                    val base = File(context.filesDir, DIRECTORIO_INTERNO).canonicalFile
                    val archivo = File(base, valor).canonicalFile
                    if (!archivo.path.startsWith(base.path + File.separator) || !archivo.isFile) {
                        return@runCatching null
                    }
                    archivo.readBytes()
                }
            }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private fun AudioPronunciacion.aEntity() = AudioPronunciacionEntity(
        palabraId = palabraId,
        idiomaCodigo = idioma.codigo,
        tipoReferencia = when (referencia) {
            is ReferenciaAudioPronunciacion.Asset -> TIPO_ASSET
            is ReferenciaAudioPronunciacion.ArchivoInterno -> TIPO_ARCHIVO_INTERNO
        },
        referencia = referencia.valor,
        origen = origen.name,
        verificado = verificado
    )

    private fun AudioPronunciacionEntity.aDomain(idioma: Idioma): AudioPronunciacion? {
        val referenciaDomain = when (tipoReferencia) {
            TIPO_ASSET -> ReferenciaAudioPronunciacion.Asset(referencia)
            TIPO_ARCHIVO_INTERNO -> ReferenciaAudioPronunciacion.ArchivoInterno(referencia)
            else -> return null
        }
        val origenDomain = runCatching { OrigenAudioPronunciacion.valueOf(origen) }.getOrNull()
            ?: return null
        return AudioPronunciacion(
            palabraId = palabraId,
            idioma = idioma,
            referencia = referenciaDomain,
            verificado = verificado,
            origen = origenDomain
        )
    }

    private fun referenciaSegura(valor: String): Boolean {
        if (valor.isBlank() || valor.startsWith('/') || valor.startsWith('\\')) return false
        return valor.replace('\\', '/').split('/').none { it == ".." || it.isBlank() }
    }

    private companion object {
        const val TIPO_ASSET = "ASSET"
        const val TIPO_ARCHIVO_INTERNO = "ARCHIVO_INTERNO"
        const val DIRECTORIO_INTERNO = "pronunciaciones_asociadas"
    }
}
