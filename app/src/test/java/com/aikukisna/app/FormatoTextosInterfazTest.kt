package com.aikukisna.app

import java.io.File
import java.util.IllegalFormatException
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Un "%" suelto junto a un marcador ("Necesitas %1$s% para aprobar") hace que getString(id, args) lance
 * UnknownFormatConversionException y cierre la app. Se revisan los textos con argumentos de los cuatro idiomas.
 */
class FormatoTextosInterfazTest {

    @Test
    fun textosConArgumentosSeFormateanSinError() {
        val recursos = listOf(File("src/main/res"), File("app/src/main/res")).first { it.isDirectory }
        val errores = recursos.listFiles { f -> f.isDirectory && f.name.startsWith("values") }.orEmpty()
            .mapNotNull { File(it, "strings.xml").takeIf(File::isFile) }
            .flatMap { archivo ->
                Regex("""<string name="([^"]+)"(?![^>]*formatted="false")[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
                    .findAll(archivo.readText())
                    .filter { Regex("""%\d\$""").containsMatchIn(it.groupValues[2]) }
                    .mapNotNull { coincidencia ->
                        val texto = coincidencia.groupValues[2].replace("\\'", "'").replace("\\\"", "\"")
                        try {
                            String.format(texto, *Array(9) { "x" })
                            null
                        } catch (e: IllegalFormatException) {
                            "${archivo.parentFile.name}/${coincidencia.groupValues[1]}: ${e.javaClass.simpleName}"
                        }
                    }
                    .toList()
            }
        assertTrue("Textos con formato inválido:\n" + errores.joinToString("\n"), errores.isEmpty())
    }
}
