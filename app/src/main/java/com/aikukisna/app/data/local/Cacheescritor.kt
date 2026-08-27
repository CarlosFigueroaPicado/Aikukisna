package com.aikukisna.app.data.local

import com.aikukisna.app.data.local.dao.CategoriaDao
import com.aikukisna.app.data.local.dao.CompletarLeccionPendienteDao
import com.aikukisna.app.data.local.dao.FuenteDocumentoDao
import com.aikukisna.app.data.local.dao.IdiomaDao
import com.aikukisna.app.data.local.dao.LeccionDao
import com.aikukisna.app.data.local.dao.LeccionPalabraDao
import com.aikukisna.app.data.local.dao.OracionEjemploDao
import com.aikukisna.app.data.local.dao.PalabraDao
import com.aikukisna.app.data.local.dao.TraduccionDao
import com.aikukisna.app.data.local.entity.CategoriaEntity
import com.aikukisna.app.data.local.entity.CompletarLeccionPendienteEntity
import com.aikukisna.app.data.local.entity.FuenteDocumentoEntity
import com.aikukisna.app.data.local.entity.IdiomaEntity
import com.aikukisna.app.data.local.entity.LeccionEntity
import com.aikukisna.app.data.local.entity.LeccionPalabraEntity
import com.aikukisna.app.data.local.entity.OracionEjemploEntity
import com.aikukisna.app.data.local.entity.PalabraEntity
import com.aikukisna.app.data.local.entity.TraduccionEntity
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.OracionEjemplo
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CacheEscritor @Inject constructor(
    private val idiomaDao: IdiomaDao,
    private val categoriaDao: CategoriaDao,
    private val fuenteDocumentoDao: FuenteDocumentoDao,
    private val palabraDao: PalabraDao,
    private val traduccionDao: TraduccionDao,
    private val leccionDao: LeccionDao,
    private val oracionEjemploDao: OracionEjemploDao,
    private val leccionPalabraDao: LeccionPalabraDao,
    private val completarLeccionPendienteDao: CompletarLeccionPendienteDao
) {


    suspend fun cachearIdiomas(idiomas: List<Idioma>) {
        if (idiomas.isEmpty()) return
        idiomaDao.guardarTodos(idiomas.map { it.aEntity() }.distinctBy { it.id })
    }

    suspend fun cachearCategorias(categorias: List<Categoria>) {
        if (categorias.isEmpty()) return
        categoriaDao.guardarTodos(categorias.map { it.aEntity() }.distinctBy { it.id })
    }

    suspend fun cachearFuentes(fuentes: List<FuenteDocumento>) {
        if (fuentes.isEmpty()) return
        fuenteDocumentoDao.guardarTodos(fuentes.map { it.aEntity() }.distinctBy { it.id })
    }

    suspend fun cachearPalabras(palabras: List<Palabra>) {
        if (palabras.isEmpty()) return
        cachearIdiomas(palabras.map { it.idioma })
        cachearCategorias(palabras.mapNotNull { it.categoria })
        cachearFuentes(palabras.map { it.fuente })
        palabraDao.guardarTodas(palabras.map { it.aEntity() })
    }

    suspend fun cachearTraducciones(traducciones: List<Traduccion>) {
        if (traducciones.isEmpty()) return
        cachearPalabras(traducciones.flatMap { listOf(it.palabraOrigen, it.palabraDestino) })
        traduccionDao.guardarTodas(traducciones.map { it.aEntity() })
    }

    suspend fun cachearLecciones(lecciones: List<Leccion>) {
        if (lecciones.isEmpty()) return
        cachearIdiomas(lecciones.map { it.idiomaMeta })
        cachearCategorias(lecciones.mapNotNull { it.categoria })
        leccionDao.guardarTodas(lecciones.map { it.aEntity() })
    }

    suspend fun cachearVinculosLeccionPalabra(vinculos: List<Pair<Int, Int>>) {
        if (vinculos.isEmpty()) return
        leccionPalabraDao.guardarTodas(vinculos.map { (leccionId, palabraId) ->
            LeccionPalabraEntity(leccionId, palabraId)
        })
    }

    suspend fun cachearOraciones(leccionId: Int, oraciones: List<OracionEjemplo>) {
        if (oraciones.isEmpty()) return
        cachearFuentes(oraciones.map { it.fuente })
        oracionEjemploDao.guardarTodas(oraciones.map { it.aEntity(leccionId) })
    }

    // --- Lectura (reconstrucción de dominio desde la caché local) ---

    suspend fun leerPalabra(id: Int): Palabra? {
        val entity = palabraDao.obtenerPorId(id) ?: return null
        return entity.aPalabraCacheada()
    }

    suspend fun buscarPalabrasCacheadas(query: String, idiomaId: Int, limite: Int, offset: Int): List<Palabra> {
        return palabraDao.buscar(query, idiomaId, limite, offset).mapNotNull { it.aPalabraCacheada() }
    }

    suspend fun leerTraducciones(palabraId: Int): List<Traduccion> {
        return traduccionDao.obtenerPorPalabra(palabraId).mapNotNull { it.aTraduccionCacheada() }
    }

    suspend fun leerLecciones(nivel: Int?): List<Leccion> {
        return leccionDao.obtenerTodas(nivel).mapNotNull { it.aLeccionCacheada() }
    }

    suspend fun leerLeccion(id: Int): Leccion? {
        return leccionDao.obtenerPorId(id)?.aLeccionCacheada()
    }

    suspend fun leerVocabularioLeccion(leccionId: Int): List<Palabra> {
        val ids = leccionPalabraDao.obtenerPalabraIdsPorLeccion(leccionId)
        return palabraDao.obtenerPorIds(ids).mapNotNull { it.aPalabraCacheada() }
    }

    suspend fun leerOracionesLeccion(leccionId: Int): List<OracionEjemplo> {
        return oracionEjemploDao.obtenerPorLeccion(leccionId).mapNotNull { it.aOracionCacheada() }
    }

    suspend fun hayAlgoDescargado(): Boolean = palabraDao.contarTodas() > 0

    // --- Cola de lecciones completadas sin conexión ---

    suspend fun encolarLeccionPendiente(leccionId: Int, puntaje: Int) {
        completarLeccionPendienteDao.encolar(
            CompletarLeccionPendienteEntity(
                leccionId = leccionId,
                puntaje = puntaje,
                fechaCreadoEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun obtenerLeccionesPendientes(): List<CompletarLeccionPendienteEntity> =
        completarLeccionPendienteDao.obtenerTodas()

    suspend fun borrarLeccionPendiente(id: Int) = completarLeccionPendienteDao.borrar(id)

    // --- Mapeos privados ---

    private suspend fun PalabraEntity.aPalabraCacheada(): Palabra? {
        val idioma = idiomaDao.obtenerPorId(idiomaId)?.aDomain() ?: return null
        val fuente = fuenteDocumentoDao.obtenerPorId(fuenteId)?.aDomain() ?: return null
        val categoria = categoriaId?.let { categoriaDao.obtenerPorId(it)?.aDomain() }
        return Palabra(id = id, idioma = idioma, texto = texto, categoria = categoria, fuente = fuente)
    }

    private suspend fun TraduccionEntity.aTraduccionCacheada(): Traduccion? {
        val origen = palabraDao.obtenerPorId(palabraOrigenId)?.aPalabraCacheada() ?: return null
        val destino = palabraDao.obtenerPorId(palabraDestinoId)?.aPalabraCacheada() ?: return null
        return Traduccion(id = id, palabraOrigen = origen, palabraDestino = destino, nota = nota)
    }

    private suspend fun LeccionEntity.aLeccionCacheada(): Leccion? {
        val idiomaMeta = idiomaDao.obtenerPorId(idiomaMetaId)?.aDomain() ?: return null
        val categoria = categoriaId?.let { categoriaDao.obtenerPorId(it)?.aDomain() }
        return Leccion(
            id = id, titulo = titulo, capituloNumero = capituloNumero,
            nivel = nivel, categoria = categoria, idiomaMeta = idiomaMeta
        )
    }

    private suspend fun OracionEjemploEntity.aOracionCacheada(): OracionEjemplo? {
        val fuente = fuenteDocumentoDao.obtenerPorId(fuenteId)?.aDomain() ?: return null
        return OracionEjemplo(
            id = id, textoOrigen = textoOrigen, textoDestino = textoDestino,
            leccion = null, fuente = fuente
        )
    }
}

// --- Mapeos domain -> entity (privados a nivel de archivo) ---

private fun Idioma.aEntity() = IdiomaEntity(id = id, codigo = codigo, nombre = nombre)
private fun IdiomaEntity.aDomain() = Idioma(id = id, codigo = codigo, nombre = nombre)

private fun Categoria.aEntity() = CategoriaEntity(id = id, nombre = nombre)
private fun CategoriaEntity.aDomain() = Categoria(id = id, nombre = nombre)

private fun FuenteDocumento.aEntity() = FuenteDocumentoEntity(
    id = id, titulo = titulo, autor = autor, anio = anio, institucion = institucion
)
private fun FuenteDocumentoEntity.aDomain() = FuenteDocumento(
    id = id, titulo = titulo, autor = autor, anio = anio, institucion = institucion
)

private fun Palabra.aEntity() = PalabraEntity(
    id = id, idiomaId = idioma.id, texto = texto, categoriaId = categoria?.id, fuenteId = fuente.id
)

private fun Traduccion.aEntity() = TraduccionEntity(
    id = id, palabraOrigenId = palabraOrigen.id, palabraDestinoId = palabraDestino.id, nota = nota
)

private fun Leccion.aEntity() = LeccionEntity(
    id = id, titulo = titulo, capituloNumero = capituloNumero,
    nivel = nivel, categoriaId = categoria?.id, idiomaMetaId = idiomaMeta.id
)

private fun OracionEjemplo.aEntity(leccionId: Int) = OracionEjemploEntity(
    id = id, textoOrigen = textoOrigen, textoDestino = textoDestino,
    fuenteId = fuente.id, leccionId = leccionId
)