# Validación funcional

## Evidencia recuperada

Se recuperó del Samsung SM-S908U1 el archivo
`camara_traductor_20260929_01.mp4` (28 487 023 bytes). La hoja de contacto
`camara_traductor_20260929_contacto.png` permite localizar las pantallas.

En la misma grabación se observa Cámara con el resultado `silla → Chair` y
Traductor con la entrada `me rindo` y el mensaje de ausencia de traducción
verificada. La grabación empieza con el resultado de Cámara ya visible:
no acredita el disparo inicial ni permite evaluar la exactitud del objeto
reconocido. Tampoco demuestra por sí sola qué consulta de base de datos se ejecutó.

## Correcciones

- Tuki distingue idioma seleccionado, utilidad de XP y funciones del tutor
  de las consultas lingüísticas. Estas preguntas tienen respuesta directa y
  no consultan el diccionario ni el motor remoto.
- La respuesta de idioma utiliza el contexto del perfil; sin perfil informa
  que no puede confirmar el idioma.
- La explicación de XP refleja el nivel mostrado en Inicio (500 XP) y
  distingue el requisito de aprobación de lecciones (85%).
- Traductor deja de mostrar reglas genéricas sobre vocales y acentuación
  sin respaldo para el idioma elegido.
- El script de verificación ejecuta resolución de dependencias Gradle,
  clean, pruebas unitarias y assembleDebug, deteniéndose si falla una etapa.
  La resolución de dependencias es la comprobación CLI; no representa una
  sincronización de la interfaz de Android Studio.
- Las capturas de `build/test-captures`, cuando existen, se copian fuera de
  `build` antes del clean.

## Alcance de Room

El recorrido de código revisado enlaza CorpusCamaraRepositoryImpl con
SeleccionTraduccionCamaraDao y DiccionarioRepositoryImpl con CacheEscritor.
Las palabras y relaciones se leen por DAO. TraducirTextoUseCase utiliza
RepositorioConocimientoImpl, que consulta palabras, acepciones y expresiones
locales. Esto confirma el recorrido implementado, no una traza de consultas
de la grabación recuperada.

## Resultados de compilación y prueba física

- Secuencia CLI completada: dependencias → clean → testDebugUnitTest →
  assembleDebug. 81 pruebas, cero fallos, errores u omisiones.
- APK debug instalado mediante `adb install -r`, con resultado `Success`.
  No se generó release ni se borraron los datos de la aplicación.
- Se comprobó una copia temporal de la base del Samsung con la app detenida:
  `PRAGMA integrity_check` devolvió `ok`. El corpus contiene 30 selecciones
  Miskito, 30 Español, 29 Kriol y 30 Inglés Estándar, total 119.
- La selección de mesa enlaza la palabra española 23018 con la inglesa
  63697 (`Table`). La copia temporal se eliminó al terminar la consulta;
  no se incorporaron datos personales de la base al repositorio.
- En la instalación actualizada, Tuki respondió el idioma real del perfil
  (`Inglés Estándar`), la utilidad de XP, el beneficio de los puntos y su
  función como tutor. Se conservaron los XML de respuesta.
- Traductor mostró `mesa → Table` y rechazó `zzqxvplm` con el mensaje de
  ausencia de traducción verificada. La interfaz muestra el consejo de
  pronunciación corregido.
- La regresión está registrada en
  `regresion_tuki_traductor_20261001.mp4` y el proceso de compilación en
  `build_20260930.log`.

No se repitió la captura física de un objeto con el APK actualizado; la
evidencia de Cámara corresponde al video recuperado del 29 de septiembre.
La comprobación positiva y negativa de Traductor no demuestra cobertura
general de oraciones ni de todas las combinaciones de idiomas.
