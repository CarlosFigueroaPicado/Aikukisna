package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.conocimiento.EntradaConocimiento
import com.aikukisna.app.domain.conocimiento.FraseVerificada
import com.aikukisna.app.domain.conocimiento.ResolucionTraduccion
import com.aikukisna.app.domain.conocimiento.ResultadoBusquedaConocimiento

interface RepositorioConocimiento {
    suspend fun buscarPalabra(texto: String, idiomaId: Int): ResultadoBusquedaConocimiento
    suspend fun obtenerEntrada(palabraId: Int, idiomaDestinoId: Int? = null): EntradaConocimiento?
    suspend fun resolverTraduccion(texto: String, idiomaOrigenId: Int, idiomaDestinoId: Int): ResolucionTraduccion

    /**
     * Frases reales del corpus en [idiomaId] con su traducción registrada a [idiomaTraduccionId].
     * Con [palabra] solo devuelve frases que la contienen; sin ella, frases al azar para practicar.
     */
    suspend fun buscarFrases(
        idiomaId: Int,
        idiomaTraduccionId: Int,
        palabra: String? = null,
        limite: Int = 3
    ): List<FraseVerificada> = emptyList()
}
