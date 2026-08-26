# Reglas propias de R8/keep para Aikukisna.
#
# Kotlinx Serialization, Compose y Hilt ya traen sus propias reglas
# incluidas en cada librería (consumer rules) — no hace falta duplicarlas.
# Se agrega solo un respaldo mínimo para los DTOs propios del proyecto.
# Ningún DTO usa companion object nombrado, así que esto es defensa extra,
# no un requisito estricto.
-keepclassmembers,allowoptimization class com.aikukisna.app.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers,allowoptimization class com.aikukisna.app.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}