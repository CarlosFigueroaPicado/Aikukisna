package com.aikukisna.app.data.local

import com.aikukisna.app.data.local.dao.MemoriaTukiLocalDao
import com.aikukisna.app.data.local.entity.MemoriaTukiLocalEntity
import com.aikukisna.app.domain.repository.MemoriaTukiStore
import com.aikukisna.app.domain.repository.ContextoConversacionTuki
import com.aikukisna.app.domain.model.MensajeChat
import com.aikukisna.app.domain.model.RolChat
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TukiMemoryStore @Inject constructor(
    private val dao: MemoriaTukiLocalDao
) : MemoriaTukiStore {
    override suspend fun recordar(usuarioId: UUID, consulta: String, limite: Int): List<String> {
        val claves = palabrasClave(consulta)
        val memorias = dao.obtenerRecientes(usuarioId.toString(), 80)
        val seleccionadas = memorias
            .map { memoria -> memoria to memoria.palabrasClave.split('|').count(claves::contains) }
            .filter { (_, coincidencias) -> coincidencias > 0 }
            .sortedWith(compareByDescending<Pair<MemoriaTukiLocalEntity, Int>> { it.second }
                .thenByDescending { it.first.fechaEpochMs })
            .take(limite)
            .map { it.first }
        if (seleccionadas.isNotEmpty()) dao.registrarUso(seleccionadas.map { it.id })
        return seleccionadas.map { it.resumen }
    }

    override suspend fun obtenerContexto(usuarioId: UUID): ContextoConversacionTuki? =
        dao.obtenerUltima(usuarioId.toString())?.let {
            ContextoConversacionTuki(it.tema, it.ultimaPalabraOFrase, it.ultimaLeccionId, it.ultimaIntencion)
        }

    override suspend fun obtenerMensajesRecientes(usuarioId: UUID, idiomaId: Int?, limiteIntercambios: Int): List<MensajeChat> =
        (if (idiomaId == null) dao.obtenerRecientes(usuarioId.toString(), limiteIntercambios)
        else dao.obtenerRecientesPorTipo(usuarioId.toString(), tipoConversacion(idiomaId), limiteIntercambios))
            .asReversed().flatMap { item ->
            val query = item.resumen.substringAfter("Consulta: ").substringBefore("\nRespuesta:").trim()
            val answer = item.resumen.substringAfter("\nRespuesta:", "").trim()
            buildList {
                if (query.isNotBlank()) add(MensajeChat(RolChat.USUARIO, query))
                if (answer.isNotBlank()) add(MensajeChat(RolChat.TUKI, answer))
            }
        }

    override suspend fun aprender(
        usuarioId: UUID,
        consulta: String,
        respuesta: String,
        contexto: ContextoConversacionTuki
    ) {
        val consultaLimpia = consulta.trim().take(500)
        val respuestaLimpia = respuesta.trim().take(1_500)
        if (consultaLimpia.isEmpty() || respuestaLimpia.isEmpty()) return
        val fecha = System.currentTimeMillis()
        val contenido = "Consulta: $consultaLimpia\nRespuesta: $respuestaLimpia"
        dao.guardar(
            MemoriaTukiLocalEntity(
                id = hash("$usuarioId|$consultaLimpia|$respuestaLimpia"),
                usuarioId = usuarioId.toString(),
                tipo = contexto.idiomaId?.let(::tipoConversacion) ?: TIPO_CONVERSACION,
                resumen = contenido,
                palabrasClave = palabrasClave("$consultaLimpia $respuestaLimpia").joinToString("|"),
                fechaEpochMs = fecha,
                tema = contexto.tema,
                ultimaPalabraOFrase = contexto.ultimaPalabraOFrase,
                ultimaLeccionId = contexto.ultimaLeccionId,
                ultimaIntencion = contexto.ultimaIntencion
            )
        )
        dao.conservarRecientes(usuarioId.toString(), 300)
    }

    private fun palabrasClave(texto: String): Set<String> = texto.lowercase()
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .split(' ')
        .filter { it.length >= 3 && it !in PALABRAS_VACIAS }
        .toSet()

    private fun hash(valor: String): String = MessageDigest.getInstance("SHA-256")
        .digest(valor.toByteArray())
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    // El idioma va en el tipo ("CONVERSACION:4") para separar los chats sin migrar la tabla.
    private fun tipoConversacion(idiomaId: Int) = "$TIPO_CONVERSACION:$idiomaId"

    private companion object {
        const val TIPO_CONVERSACION = "CONVERSACION"
        val PALABRAS_VACIAS = setOf(
            "para", "como", "qué", "que", "una", "uno", "del", "las", "los",
            "con", "por", "esta", "este", "desde", "respuesta", "consulta"
        )
    }
}
