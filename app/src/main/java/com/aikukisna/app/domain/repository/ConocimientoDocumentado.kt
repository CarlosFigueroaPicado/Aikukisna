package com.aikukisna.app.domain.repository

enum class TipoFragmento { GRAMATICA, PRONUNCIACION, CULTURA }

/** Un fragmento de conocimiento documentado (regla, nota de pronunciación o texto cultural) con su fuente. */
data class FragmentoConocimiento(
    val tipo: TipoFragmento,
    val idiomaId: Int?,
    val titulo: String,
    val texto: String,
    val ejemplos: List<String> = emptyList(),
    val fuente: String? = null
)

/** Material con el que Tuki enseña sin conexión: todo viene de fuentes registradas en la base local. */
interface ConocimientoDocumentado {
    suspend fun fragmentos(idiomaId: Int): List<FragmentoConocimiento>
}
