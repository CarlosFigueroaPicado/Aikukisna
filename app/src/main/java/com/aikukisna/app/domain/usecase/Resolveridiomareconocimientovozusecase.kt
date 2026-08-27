package com.aikukisna.app.domain.usecase

import javax.inject.Inject


enum class ConfiabilidadReconocimientoVoz { SOLIDO, DEGRADADO, NO_DISPONIBLE }

data class ResultadoIdiomaReconocimiento(
    val etiquetaAndroid: String?,
    val confiabilidad: ConfiabilidadReconocimientoVoz
)


class ResolverIdiomaReconocimientoVozUseCase @Inject constructor() {

    operator fun invoke(idiomaId: Int): ResultadoIdiomaReconocimiento = when (idiomaId) {

        2 -> ResultadoIdiomaReconocimiento("es-NI", ConfiabilidadReconocimientoVoz.SOLIDO)


        4 -> ResultadoIdiomaReconocimiento("en-US", ConfiabilidadReconocimientoVoz.SOLIDO)


        3 -> ResultadoIdiomaReconocimiento("en-JM", ConfiabilidadReconocimientoVoz.DEGRADADO)


        1 -> ResultadoIdiomaReconocimiento(null, ConfiabilidadReconocimientoVoz.NO_DISPONIBLE)

        else -> ResultadoIdiomaReconocimiento(null, ConfiabilidadReconocimientoVoz.NO_DISPONIBLE)
    }
}