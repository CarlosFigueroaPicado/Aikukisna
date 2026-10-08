# Candidato de entrega con cobertura limitada

## Alcance aceptado

Esta entrega conserva las funciones existentes del Traductor.
No incorpora el modelo que continúa entrenándose ni ofrece
traducción libre de conversaciones. No representa una certificación completa
de producción.

El Traductor consulta palabras, expresiones y oraciones completas registradas
localmente. Permite seleccionar origen y destino entre Miskito, español,
Kriol e inglés estándar; la existencia del selector no garantiza contenido
para todas las consultas o direcciones.

La versión rechaza la composición de una frase a partir de equivalencias
de segmentos separados. Al invertir idiomas, una traducción sin alternativas
pasa al campo de entrada. Las respuestas pendientes no se muestran si el
usuario ya cambió el texto o el par de idiomas.

El dictado de Miskito y Kriol requiere conexión. Su transcripción debe
revisarse antes de traducir; no se acredita reconocimiento offline para
estas lenguas.

## Validación requerida para el candidato

- Resolución de dependencias Gradle, clean, pruebas unitarias y assembleDebug.
- Instalación del APK actualizado conservando los datos de la aplicación.
- Aviso de cobertura retirado de la interfaz por solicitud del usuario;
  se conserva el rechazo de entradas sin equivalencia verificada.
- Una equivalencia local positiva, su sentido inverso y una entrada ausente.
- Comprobación de frases completas registradas para Miskito y Kriol.
- Distinción entre verificaciones nuevas y evidencia física previa.

## Entrega y pendientes

### Actualización sin aviso en pantalla

El candidato `entregas/candidato-20261001-225217/aikukisna-debug.apk`
elimina únicamente el aviso general de cobertura del Traductor. No modifica
el motor ni elimina la negativa ante consultas sin equivalencia verificada.
Completó sync → clean → tests → assembleDebug con 81 pruebas y cero fallos;
se instaló correctamente con `adb install -r` en el Samsung conectado.
SHA-256: `70F0AA1ED70962AA5BA4B557B140EEF92265363A61837BB9EEE55C7A3B9EEAC9`.
La prueba física offline de esta actualización todavía está pendiente.

### Estado del candidato instalado

- Completado: sync de dependencias → clean → tests → assembleDebug.
- Resultado unitario: 81 pruebas, sin fallos, errores ni omisiones.
- APK: `entregas/candidato-limitado-20261001-221414/aikukisna-cobertura-limitada-debug.apk`.
- SHA-256: `C10F6DE2374F992EABF848CC55431FCFFACCA9E13BA94CB1D7116F0D7E18B96E`.
- Firma del APK verificada e instalación con conservación de datos completada
  en Samsung SM-S908U1. Aviso de cobertura limitada observado en el Traductor.
- Grabación física iniciada el 1 de octubre a las 22:38:25, hora local,
  mediante screenrecord desacoplado de ADB; límite de 900 segundos.
  Archivo en dispositivo: `/sdcard/aikukisna_candidato_offline_20261001_223825.mp4`.
  Captura detenida antes de recompilar el cambio visual y recuperada en
  `docs/evidencia_funcional/aikukisna_candidato_offline_20261001_223825.mp4`.
- La aprobación funcional offline sigue pendiente: esta captura interrumpida
  no acredita la ejecución de las pruebas.

El artefacto será un APK debug para instalación directa y evaluación, con
huella SHA-256. No se publica en tiendas ni se genera release firmado.
Los archivos de entrega se conservan fuera de `build` para que sobrevivan
a futuros clean.

Siguen pendientes el traductor conversacional offline completo, su cobertura
entre todos los pares, la validación lingüística del modelo, la integración
Android y una revisión integral de producción. La evidencia previa de Tuki
y Cámara está en `docs/evidencia_funcional`; no debe confundirse con una
nueva prueba física de este candidato.
