package com.aikukisna.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PalabraDto(
    val id: Int,
    val idioma: IdiomaDto,
    val texto: String,
    val categoria: CategoriaDto? = null,
    @SerialName("fuente_documento") val fuente: FuenteDocumentoDto,
    val pronunciacion: String? = null,
    @SerialName("pronunciacion_fonetica") val pronunciacionFonetica: String? = null,
    @SerialName("pronunciacion_verificada") val pronunciacionVerificada: Boolean = false,
    @SerialName("texto_normalizado") val textoNormalizado: String? = null,
    @SerialName("estado_validacion") val estadoValidacion: String = "importada",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
