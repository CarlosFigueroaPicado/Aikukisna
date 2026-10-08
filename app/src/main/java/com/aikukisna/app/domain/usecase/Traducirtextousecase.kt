package com.aikukisna.app.domain.usecase

import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.conocimiento.OpcionTraduccion
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento
import com.aikukisna.app.domain.model.FuenteTraduccion
import com.aikukisna.app.domain.model.ResultadoTraduccion
import com.aikukisna.app.domain.repository.DiccionarioRepository
import com.aikukisna.app.domain.repository.RepositorioConocimiento
import javax.inject.Inject

class TraducirTextoUseCase @Inject constructor(
    private val conocimiento: RepositorioConocimiento
) {
    constructor(diccionario: DiccionarioRepository) : this(AdaptadorConocimientoLegado(diccionario))

    suspend operator fun invoke(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): ResultadoTraduccion {
        require(texto.isNotBlank()) { "El texto a traducir no puede estar vacío" }
        val clean = texto.trim()
        if (idiomaOrigenId == idiomaDestinoId) return ResultadoTraduccion(clean, FuenteTraduccion.DICCIONARIO)

        return when (val result = conocimiento.resolverTraduccion(clean, idiomaOrigenId, idiomaDestinoId)) {
            is ResolucionTraduccion.Expresion -> ResultadoTraduccion(result.texto, FuenteTraduccion.DICCIONARIO)
            is ResolucionTraduccion.Unica -> ResultadoTraduccion(
                result.opcion.palabra.texto,
                FuenteTraduccion.DICCIONARIO,
                contexto = result.opcion.contexto
            )
            is ResolucionTraduccion.Ambigua -> ResultadoTraduccion(
                texto = "Hay varias traducciones posibles. Elige según el contexto.",
                fuente = FuenteTraduccion.DICCIONARIO,
                alternativas = result.opciones.map { option ->
                    option.contexto?.takeIf(String::isNotBlank)?.let { "${option.palabra.texto} — $it" }
                        ?: option.palabra.texto
                }.distinct()
            )
            is ResolucionTraduccion.Sugerencia ->
                error("No encontré una coincidencia exacta. ¿Quisiste decir “${result.palabra.texto}”?")
            is ResolucionTraduccion.ComposicionDocumentada ->
                error("No hay una expresión completa verificada para este texto. Las equivalencias de palabras separadas no validan la frase completa.")
            is ResolucionTraduccion.Parcial -> {
                val detail = buildString {
                    if (result.conocidas.isNotEmpty()) append(" Reconocí: ${result.conocidas.joinToString()}.")
                    if (result.desconocidas.isNotEmpty()) append(" Falta validar: ${result.desconocidas.joinToString()}.")
                }
                error("No hay una expresión completa verificada para este texto.$detail")
            }
            ResolucionTraduccion.NoEncontrada -> error("No hay una traducción verificada para este texto")
        }
    }
}

private class AdaptadorConocimientoLegado(
    private val diccionario: DiccionarioRepository
) : RepositorioConocimiento {
    override suspend fun buscarPalabra(texto: String, idiomaId: Int): ResultadoBusquedaConocimiento {
        val normalized = NormalizadorLinguistico.normalizar(texto)
        val exact = diccionario.buscarPalabras(texto, idiomaId, 20)
            .firstOrNull { NormalizadorLinguistico.normalizar(it.texto) == normalized }
        return exact?.let { ResultadoBusquedaConocimiento.Exacta(it) }
            ?: ResultadoBusquedaConocimiento.NoEncontrada
    }

    override suspend fun obtenerEntrada(
        palabraId: Int,
        idiomaDestinoId: Int?
    ): com.aikukisna.app.domain.conocimiento.EntradaConocimiento? {
        val word = diccionario.obtenerPalabraPorId(palabraId) ?: return null
        val options = diccionario.obtenerTraducciones(palabraId).map { it.palabraDestino }
            .filter { idiomaDestinoId == null || it.idioma.id == idiomaDestinoId }
            .distinctBy { it.id }
            .map(::OpcionTraduccion)
        return com.aikukisna.app.domain.conocimiento.EntradaConocimiento(word, emptyList(), options, emptyList())
    }

    override suspend fun resolverTraduccion(
        texto: String,
        idiomaOrigenId: Int,
        idiomaDestinoId: Int
    ): ResolucionTraduccion {
        diccionario.buscarOracionExacta(texto, idiomaOrigenId, idiomaDestinoId)?.let {
            return ResolucionTraduccion.Expresion(
                it,
                com.aikukisna.app.domain.conocimiento.EstadoValidacion.IMPORTADA
            )
        }
        return when (val result = buscarPalabra(texto, idiomaOrigenId)) {
            is ResultadoBusquedaConocimiento.Exacta -> {
                val options = obtenerEntrada(result.palabra.id, idiomaDestinoId)?.traducciones.orEmpty()
                when (options.size) {
                    0 -> ResolucionTraduccion.NoEncontrada
                    1 -> ResolucionTraduccion.Unica(options.single())
                    else -> ResolucionTraduccion.Ambigua(options)
                }
            }
            else -> ResolucionTraduccion.NoEncontrada
        }
    }
}
