# Análisis del tamaño del APK

Fecha: 2026-09-21.

El APK debug anterior medía aproximadamente 248.7 MiB. No se eliminó ni optimizó ningún componente.

## Contenido principal

| Componente | Tamaño sin comprimir aproximado |
|---|---:|
| Assets totales | 154.24 MiB |
| Modelo Vosk inglés | 67.61 MiB |
| Modelo Vosk español | 57.49 MiB |
| `diccionario_semilla.json` | 15.19 MiB |
| 206 pronunciaciones locales | 13.94 MiB |
| Recursos Android | 4.94 MiB |
| Bibliotecas nativas combinadas | 118.75 MiB |

## Bibliotecas nativas

Se empaquetan simultáneamente `arm64-v8a`, `armeabi-v7a`, `x86` y `x86_64`, además de pequeños binarios JNA para ABI antiguas. Los mayores archivos pertenecen a:

- Vosk: `libvosk.so`.
- ML Kit OCR: `libmlkit_google_ocr_pipeline.so`.
- ML Kit: `libmlkitcommonpipeline.so`.
- JNA: `libjnidispatch.so`.

Totales aproximados por ABI principal:

- `arm64-v8a`: 30.82 MiB.
- `armeabi-v7a`: 21.80 MiB.
- `x86`: 33.05 MiB.
- `x86_64`: 32.69 MiB.

## Dependencias relevantes

- `vosk-android` y JNA para reconocimiento offline.
- ML Kit Text Recognition para OCR.
- ML Kit Image Labeling para reconocimiento de objetos.
- CameraX.
- Supabase Auth, PostgREST y Functions.

## Conclusión

El tamaño se explica principalmente por los dos modelos Vosk, las cuatro arquitecturas nativas, las bibliotecas nativas de Vosk/ML Kit, el diccionario semilla y el audio offline. No se encontraron indicios suficientes para declarar recursos duplicados funcionalmente equivalentes. Cualquier reducción futura debe evaluar App Bundle, divisiones por ABI y descarga bajo demanda sin comprometer el requisito offline desde la primera instalación.
