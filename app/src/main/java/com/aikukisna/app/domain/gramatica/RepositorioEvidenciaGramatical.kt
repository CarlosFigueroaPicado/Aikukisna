package com.aikukisna.app.domain.gramatica

interface RepositorioEvidenciaGramatical {
    suspend fun obtenerEvidencia(
        reglaId: Long,
        referencia: ReferenciaEvidenciaGramatical
    ): EvidenciaGramaticalRegla
}
