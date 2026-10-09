package com.aikukisna.app.domain.conocimiento

import java.text.Normalizer
import java.util.Locale

object NormalizadorLinguistico {
    fun normalizar(texto: String): String = Normalizer.normalize(texto, Normalizer.Form.NFKC)
        .trim()
        .lowercase(Locale.ROOT)
        .replace(Regex("\\s+"), " ")
        .replace(Regex("^[\\p{P}\\p{S}]+"), "")
        .replace(Regex("[\\p{P}\\p{S}]+$"), "")
        .trim()
}
