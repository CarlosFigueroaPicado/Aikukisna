package com.aikukisna.app.data.local

import com.aikukisna.app.data.local.dao.SeleccionTraduccionCamaraDao
import com.aikukisna.app.data.local.entity.SeleccionTraduccionCamaraEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PobladorCorpusCamara @Inject constructor(
    private val dao: SeleccionTraduccionCamaraDao
) {
    suspend fun sembrar() {
        dao.guardar(CONCEPTOS.flatMap(::crearSelecciones))
    }

    private fun crearSelecciones(concepto: ConceptoAprobado): List<SeleccionTraduccionCamaraEntity> {
        val identidad = SeleccionTraduccionCamaraEntity(
            conceptoEspanolNormalizado = concepto.espanol,
            etiquetaInglesNormalizada = concepto.etiquetaIngles,
            idiomaDestinoId = IDIOMA_ESPANOL,
            palabraEspanolId = concepto.palabraEspanolId,
            palabraDestinoId = concepto.palabraEspanolId,
            tipoRelacion = "identidad",
            relacionId = null,
            acepcionOrigenId = null,
            acepcionDestinoId = null,
            estadoValidacion = "validada",
            motivoRevision = concepto.motivoRevision
        )
        return listOf(identidad) + concepto.destinos.map { destino ->
            SeleccionTraduccionCamaraEntity(
                conceptoEspanolNormalizado = concepto.espanol,
                etiquetaInglesNormalizada = concepto.etiquetaIngles,
                idiomaDestinoId = destino.idiomaId,
                palabraEspanolId = concepto.palabraEspanolId,
                palabraDestinoId = destino.palabraDestinoId,
                tipoRelacion = destino.tipoRelacion,
                relacionId = destino.relacionId,
                acepcionOrigenId = destino.acepcionOrigenId,
                acepcionDestinoId = destino.acepcionDestinoId,
                estadoValidacion = "validada",
                motivoRevision = concepto.motivoRevision
            )
        }
    }

    private data class ConceptoAprobado(
        val espanol: String,
        val etiquetaIngles: String,
        val palabraEspanolId: Int,
        val destinos: List<DestinoAprobado>,
        val motivoRevision: String = "Selección por acepción revisada para reconocimiento de objetos"
    )

    private data class DestinoAprobado(
        val idiomaId: Int,
        val palabraDestinoId: Int,
        val tipoRelacion: String,
        val relacionId: Int,
        val acepcionOrigenId: Int?,
        val acepcionDestinoId: Int?
    )

    private companion object {
        const val IDIOMA_MISKITU = 1
        const val IDIOMA_ESPANOL = 2
        const val IDIOMA_KRIOL = 3
        const val IDIOMA_INGLES = 4

        fun acepcion(
            idiomaId: Int,
            palabraDestinoId: Int,
            relacionId: Int,
            acepcionOrigenId: Int,
            acepcionDestinoId: Int
        ) = DestinoAprobado(
            idiomaId,
            palabraDestinoId,
            "traduccion_acepcion",
            relacionId,
            acepcionOrigenId,
            acepcionDestinoId
        )

        fun palabra(
            idiomaId: Int,
            palabraDestinoId: Int,
            relacionId: Int
        ) = DestinoAprobado(
            idiomaId,
            palabraDestinoId,
            "traduccion",
            relacionId,
            null,
            null
        )

        val CONCEPTOS = listOf(
            ConceptoAprobado("mesa", "table", 23018, listOf(
                acepcion(IDIOMA_MISKITU, 23019, 27753, 28074, 46985),
                acepcion(IDIOMA_KRIOL, 63796, 27752, 28074, 17441),
                acepcion(IDIOMA_INGLES, 63697, 27751, 28074, 7817)
            )),
            ConceptoAprobado("libro", "book", 21627, listOf(
                acepcion(IDIOMA_MISKITU, 5748, 79802, 20456, 9297),
                acepcion(IDIOMA_KRIOL, 63613, 57624, 25339, 12679),
                acepcion(IDIOMA_INGLES, 63648, 111956, 20456, 6493)
            )),
            ConceptoAprobado("lápiz", "pencil", 21307, listOf(
                acepcion(IDIOMA_MISKITU, 21308, 26012, 12481, 25156),
                acepcion(IDIOMA_KRIOL, 63610, 57690, 25251, 10474),
                acepcion(IDIOMA_INGLES, 63645, 117757, 12481, 1431)
            )),
            ConceptoAprobado("cuaderno", "notebook", 11215, listOf(
                acepcion(IDIOMA_MISKITU, 11216, 15568, 10065, 50665),
                acepcion(IDIOMA_KRIOL, 63609, 57688, 20959, 24780),
                acepcion(IDIOMA_INGLES, 63644, 57617, 20959, 22117)
            )),
            ConceptoAprobado("árbol", "tree", 4167, listOf(
                acepcion(IDIOMA_MISKITU, 40546, 63633, 8468, 5155),
                acepcion(IDIOMA_KRIOL, 63616, 57705, 8468, 13844),
                acepcion(IDIOMA_INGLES, 63910, 58230, 8468, 26697)
            )),
            ConceptoAprobado("bebé", "baby", 37933, listOf(
                acepcion(IDIOMA_MISKITU, 37932, 40778, 9660, 5396),
                acepcion(IDIOMA_KRIOL, 63729, 57904, 9660, 23737),
                acepcion(IDIOMA_INGLES, 64403, 110607, 9660, 68444)
            )),
            ConceptoAprobado("pájaro", "bird", 24969, listOf(
                acepcion(IDIOMA_MISKITU, 5449, 29621, 21153, 22883),
                palabra(IDIOMA_KRIOL, 67728, 158868),
                acepcion(IDIOMA_INGLES, 64626, 110972, 21153, 67970)
            )),
            ConceptoAprobado("pez", "fish", 26148, listOf(
                acepcion(IDIOMA_MISKITU, 41073, 30800, 12162, 30239),
                palabra(IDIOMA_KRIOL, 63625, 157991),
                acepcion(IDIOMA_INGLES, 63660, 114072, 12162, 24932)
            )),
            ConceptoAprobado("tigre", "tiger", 32133, listOf(
                acepcion(IDIOMA_MISKITU, 20884, 88214, 3240, 20510),
                acepcion(IDIOMA_KRIOL, 63788, 36157, 3240, 12827),
                acepcion(IDIOMA_INGLES, 64010, 58423, 3240, 12976)
            )),
            ConceptoAprobado("puerta", "door", 27664, listOf(
                acepcion(IDIOMA_MISKITU, 27665, 32151, 11998, 8463),
                acepcion(IDIOMA_KRIOL, 63720, 268199, 307, 275),
                acepcion(IDIOMA_INGLES, 65397, 114246, 11998, 65798)
            )),
            ConceptoAprobado("agua", "water", 2088, listOf(
                acepcion(IDIOMA_MISKITU, 62484, 61362, 27630, 8038),
                acepcion(IDIOMA_KRIOL, 63753, 268227, 350, 349),
                acepcion(IDIOMA_INGLES, 67505, 121589, 27630, 65249)
            )),
            ConceptoAprobado("arroz", "rice", 4546, listOf(
                acepcion(IDIOMA_MISKITU, 4547, 7616, 25350, 7474),
                acepcion(IDIOMA_KRIOL, 63627, 202719, 25350, 9437),
                acepcion(IDIOMA_INGLES, 63662, 119329, 25350, 12260)
            )),
            ConceptoAprobado("barco", "ship", 5936, listOf(
                acepcion(IDIOMA_MISKITU, 5938, 9404, 11575, 52953),
                acepcion(IDIOMA_KRIOL, 63896, 268445, 704, 681),
                acepcion(IDIOMA_INGLES, 64047, 268444, 704, 505)
            )),
            ConceptoAprobado("cabeza", "head", 7044, listOf(
                acepcion(IDIOMA_MISKITU, 7045, 10759, 19464, 58390),
                acepcion(IDIOMA_KRIOL, 63807, 268455, 709, 590),
                acepcion(IDIOMA_INGLES, 63664, 268454, 709, 38)
            )),
            ConceptoAprobado("carne", "meat", 8015, listOf(
                acepcion(IDIOMA_MISKITU, 8017, 11878, 9351, 17187),
                acepcion(IDIOMA_KRIOL, 63715, 268162, 293, 285),
                acepcion(IDIOMA_INGLES, 64031, 268163, 293, 489)
            )),
            ConceptoAprobado("casa", "house", 8109, listOf(
                acepcion(IDIOMA_MISKITU, 14311, 67575, 22179, 18074),
                acepcion(IDIOMA_KRIOL, 63905, 268167, 294, 690),
                acepcion(IDIOMA_INGLES, 64040, 268166, 294, 498)
            )),
            ConceptoAprobado("cuchillo", "knife", 11371, listOf(
                acepcion(IDIOMA_MISKITU, 11373, 15728, 9587, 1021),
                acepcion(IDIOMA_KRIOL, 63735, 268206, 326, 318),
                acepcion(IDIOMA_INGLES, 66135, 116603, 9587, 66595)
            )),
            ConceptoAprobado("dedo", "finger", 12266, listOf(
                acepcion(IDIOMA_MISKITU, 12267, 16721, 3283, 22943),
                acepcion(IDIOMA_KRIOL, 64063, 121926, 3283, 67730),
                acepcion(IDIOMA_INGLES, 64089, 114090, 3283, 65928)
            )),
            ConceptoAprobado("mango", "mango", 22353, listOf(
                acepcion(IDIOMA_MISKITU, 22354, 27110, 11995, 54298),
                acepcion(IDIOMA_KRIOL, 63835, 268545, 748, 619),
                acepcion(IDIOMA_INGLES, 63984, 268544, 748, 442)
            ), "Acepción fruto; se excluyen asa, mano y pie"),
            ConceptoAprobado("mano", "hand", 22385, listOf(
                acepcion(IDIOMA_MISKITU, 63015, 80488, 18056, 14929),
                acepcion(IDIOMA_KRIOL, 63630, 134471, 18056, 10980),
                acepcion(IDIOMA_INGLES, 63665, 115153, 18056, 26090)
            )),
            ConceptoAprobado("mono", "monkey", 23412, listOf(
                acepcion(IDIOMA_MISKITU, 23414, 28123, 8000, 52723),
                acepcion(IDIOMA_KRIOL, 63861, 268559, 754, 645),
                acepcion(IDIOMA_INGLES, 64037, 268558, 754, 495)
            ), "Acepción animal; se excluye vestimenta"),
            ConceptoAprobado("mujer", "woman", 23668, listOf(
                acepcion(IDIOMA_MISKITU, 12046, 81583, 24806, 21083),
                acepcion(IDIOMA_KRIOL, 63750, 268189, 304, 348),
                acepcion(IDIOMA_INGLES, 63985, 268190, 304, 443)
            )),
            ConceptoAprobado("niña", "girl", 24016, listOf(
                acepcion(IDIOMA_MISKITU, 24017, 28753, 5646, 31143),
                acepcion(IDIOMA_KRIOL, 63748, 268242, 359, 340),
                acepcion(IDIOMA_INGLES, 63981, 268243, 359, 439)
            )),
            ConceptoAprobado("niño", "boy", 24026, listOf(
                acepcion(IDIOMA_MISKITU, 23619, 28320, 3238, 55750),
                acepcion(IDIOMA_KRIOL, 63817, 268196, 305, 600),
                acepcion(IDIOMA_INGLES, 63933, 268194, 305, 391)
            )),
            ConceptoAprobado("ropa", "clothes", 29725, listOf(
                acepcion(IDIOMA_MISKITU, 18251, 86374, 23364, 3100),
                acepcion(IDIOMA_KRIOL, 63725, 268201, 309, 279),
                acepcion(IDIOMA_INGLES, 64952, 112455, 23364, 68401)
            )),
            ConceptoAprobado("tortuga", "turtle", 32379, listOf(
                acepcion(IDIOMA_MISKITU, 8501, 88406, 9214, 29259),
                acepcion(IDIOMA_KRIOL, 63744, 268245, 360, 347),
                acepcion(IDIOMA_INGLES, 63914, 268246, 360, 372)
            )),
            ConceptoAprobado("papaya", "papaya", 25142, listOf(
                acepcion(IDIOMA_MISKITU, 25143, 29789, 10052, 40001),
                acepcion(IDIOMA_KRIOL, 63802, 268573, 760, 585),
                acepcion(IDIOMA_INGLES, 63947, 268572, 760, 405)
            )),
            ConceptoAprobado("palmera", "palm tree", 25013, listOf(
                acepcion(IDIOMA_MISKITU, 25014, 29663, 25759, 55271),
                acepcion(IDIOMA_KRIOL, 63740, 268219, 333, 320),
                acepcion(IDIOMA_INGLES, 66425, 117585, 25759, 65290)
            )),
            ConceptoAprobado("cangrejo", "crab", 7633, listOf(
                acepcion(IDIOMA_MISKITU, 7636, 11439, 15162, 7545),
                acepcion(IDIOMA_INGLES, 65229, 113411, 15162, 65691)
            ), "Kriol excluido: rahti designa una especie de cangrejo, no el término genérico"),
            ConceptoAprobado("canoa", "canoe", 7665, listOf(
                acepcion(IDIOMA_MISKITU, 7666, 11472, 10111, 6624),
                acepcion(IDIOMA_KRIOL, 63832, 268459, 711, 616),
                acepcion(IDIOMA_INGLES, 64055, 268458, 711, 513)
            ))
        )
    }
}
