package com.aikukisna.app.domain.gramatica

interface MotorGramaticalControlado {
    suspend fun obtenerReglas(): List<ReglaGramaticalValidada>
    suspend fun obtenerRegla(codigo: String): ReglaGramaticalValidada?
    suspend fun analizar(codigo: String): ResultadoAnalisisGramatical?
    suspend fun generar(solicitud: SolicitudGeneracionGramatical): ResultadoReglaGramatical
    suspend fun generarDesdeRoom(
        codigoRegla: String,
        referencia: ReferenciaEvidenciaGramatical
    ): ResultadoReglaGramatical
}
