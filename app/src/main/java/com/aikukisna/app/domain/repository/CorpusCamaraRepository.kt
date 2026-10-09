package com.aikukisna.app.domain.repository

import com.aikukisna.app.domain.model.SeleccionTraduccionCamara

interface CorpusCamaraRepository {
    suspend fun buscarPorEtiquetaIngles(
        etiqueta: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamara?

    suspend fun buscarPorConceptoEspanol(
        concepto: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamara?
}
