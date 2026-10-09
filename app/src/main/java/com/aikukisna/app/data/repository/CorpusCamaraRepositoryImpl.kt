package com.aikukisna.app.data.repository

import com.aikukisna.app.data.local.dao.SeleccionTraduccionCamaraDao
import com.aikukisna.app.data.local.entity.SeleccionTraduccionCamaraEntity
import com.aikukisna.app.domain.conocimiento.NormalizadorLinguistico
import com.aikukisna.app.domain.model.SeleccionTraduccionCamara
import com.aikukisna.app.domain.repository.CorpusCamaraRepository
import javax.inject.Inject

class CorpusCamaraRepositoryImpl @Inject constructor(
    private val dao: SeleccionTraduccionCamaraDao
) : CorpusCamaraRepository {

    override suspend fun buscarPorEtiquetaIngles(
        etiqueta: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamara? = dao.obtenerPorEtiqueta(
        NormalizadorLinguistico.normalizar(etiqueta),
        idiomaDestinoId
    )?.toDomain()

    override suspend fun buscarPorConceptoEspanol(
        concepto: String,
        idiomaDestinoId: Int
    ): SeleccionTraduccionCamara? = dao.obtenerPorConcepto(
        NormalizadorLinguistico.normalizar(concepto),
        idiomaDestinoId
    )?.toDomain()

    private fun SeleccionTraduccionCamaraEntity.toDomain() =
        SeleccionTraduccionCamara(
            palabraEspanolId = palabraEspanolId,
            palabraDestinoId = palabraDestinoId
        )
}
