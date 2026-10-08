# Pruebas sin conexión — revisión final antes de la entrega

Meta: **≥ 95 % de las pruebas en ✅** con el teléfono en modo avión (o con la red de la app apagada).
Marca cada fila: ✅ funciona · ⚠️ funciona con límites · ❌ falla. Anota el tiempo cuando se pida.

## 0. Preparación (con Wi‑Fi, una sola vez)

| # | Paso | Resultado |
|---|------|-----------|
| 0.1 | Instalar el APK nuevo (con `TUKI_MODELO_AJUSTADO=true` y `TUKI_MODELO_TUTOR=true`) | |
| 0.2 | Iniciar sesión con una cuenta de prueba y esperar la carga de datos (~6 min) | |
| 0.3 | Configuración → Tuki sin conexión → descargar el modelo (~650 MB) | |
| 0.4 | Cerrar la app por completo y activar **modo avión** | |

## 1. Arranque y sesión

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 1.1 | Abrir la app en frío | Entra al inicio sin pedir login, en < 6 s | |
| 1.2 | No aparece ningún error de "sin conexión" bloqueante | Solo avisos discretos | |
| 1.3 | Modo invitado (desde una instalación con datos) | Vocabulario y quiz de invitado funcionan | |
| 1.4 | Cambiar idioma de la app (Español, English, Miskitu, Kriol) | Toda la interfaz cambia; Miskitu/Kriol marcados "borrador" | |

## 2. Lecciones (repetir en Miskitu, Kriol, Español e Inglés)

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 2.1 | Mapa de lecciones carga | Lista ordenada por capítulo, sin pantalla vacía | |
| 2.2 | Abrir una lección → vocabulario | Tarjetas con palabra, traducción "En {idioma}:" | |
| 2.3 | Botón **Escuchar** en una tarjeta | Español/Inglés: voz de Android. Miskitu: voz de El Miskito Hamilton si la palabra tiene clip aprobado; si no, aviso "no hay pronunciación verificada". Kriol: aviso (⚠️ esperado) | |
| 2.4 | Hacer el quiz completo | Preguntas, corrección y pantalla de resultados | |
| 2.5 | Al terminar, el progreso y los logros se actualizan | Se ve en Inicio y Perfil | |
| 2.6 | Inglés: lección de 8.º, 9.º y 10.º grado | Carga con vocabulario del libro MINED | |

## 3. Diccionario y favoritos

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 3.1 | Buscar "agua", "casa", "perro" | Resultados en < 1 s, sin duplicados | |
| 3.2 | Buscar sin tilde ("arbol") | Encuentra "árbol" | |
| 3.3 | Abrir detalle de palabra | Traducción, ejemplos y fuente | |
| 3.4 | Agregar y quitar favorito | Aparece en Favoritos | |

## 4. Traductor

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 4.1 | Palabra suelta español → Miskitu | Traducción verificada | |
| 4.2 | Frase corta (≤ 3 palabras) | Traducción literal etiquetada si no está en el corpus | |
| 4.3 | Oración larga | Traducción de Gemma con etiqueta "automática"; tiempo: ___ s | |
| 4.4 | Dictar por voz en español | Transcribe sin internet | |
| 4.5 | Dictar por voz en Miskitu/Kriol | ⚠️ esperado: no hay reconocedor para estas lenguas | |
| 4.6 | Escuchar una traducción al Miskitu que tenga clip aprobado (p. ej. "casa" → utla) | Suena la voz del hablante | |
| 4.7 | Modo español (lengua de apoyo como origen) | Dirección correcta | |

## 5. Tuki (con el modelo tutor)

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 5.1 | "Hola" | Saludo en el idioma que aprende, con el nombre del alumno | |
| 5.2 | "¿Cómo se dice perro en miskito?" | Respuesta con dato verificado; tiempo: ___ s | |
| 5.3 | "¿Qué significa yang?" | Definición correcta | |
| 5.4 | "Explícame los pronombres" | Explicación coherente, sin inventar palabras | |
| 5.5 | "Hazme una pregunta de práctica" | Pregunta del vocabulario de sus lecciones | |
| 5.6 | Pregunta fuera de tema | Redirige con amabilidad | |
| 5.7 | Cambiar de idioma y volver | Historial separado por idioma | |
| 5.8 | 10 mensajes seguidos | La app no se cierra ni se calienta en exceso | |

## 6. Cámara

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 6.1 | Apuntar a: taza, silla, libro, plátano, perro, zapato, mano, botella | ≥ 6 de 8 correctos; tiempo: ___ s | |
| 6.2 | Objeto desconocido | Mensaje de "no reconocido", sin inventar | |
| 6.3 | Ver palabra reconocida → escuchar / guardar | Funciona | |

## 7. Cultura, perfil y configuración

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 7.1 | Sección Cultura | Contenido del idioma elegido | |
| 7.2 | Editar perfil (nombre) | Se guarda y queda "pendiente de sincronizar" | |
| 7.3 | Configuración: todas las opciones abren | Sin errores | |

## 8. Regreso de la conexión

| # | Prueba | Esperado | Resultado |
|---|--------|----------|-----------|
| 8.1 | Quitar modo avión | La app no se reinicia | |
| 8.2 | Progreso, perfil y logros hechos sin conexión | Suben a Supabase (verificar en la base) | |

## Límites conocidos (no cuentan como falla)

- La primera instalación necesita internet una vez (inicio de sesión, datos y modelo).
- No hay reconocimiento de voz en Miskitu ni Kriol (no existe un modelo abierto).
- Miskitu: solo suenan las palabras con grabación aprobada de El Miskito Hamilton; el resto muestra "no hay pronunciación verificada".
- Kriol: todavía no hay grabaciones de hablantes, así que no se reproduce audio (no se usa una voz de otro idioma).
- Las respuestas generadas por Tuki y por el traductor están etiquetadas como no verificadas.

## Parte automática (la ejecuta Claude con el teléfono conectado)

- `EvaluacionDispositivoTest#tuki|traductor|camara|lecciones` con la red de la app apagada
  (`cmd connectivity set-package-networking-enabled false com.aikukisna.app`).
- Tiempo de arranque en frío, memoria con Gemma cargado y tamaño del APK.
- Pruebas unitarias: `./gradlew.bat :app:testDebugUnitTest`.
