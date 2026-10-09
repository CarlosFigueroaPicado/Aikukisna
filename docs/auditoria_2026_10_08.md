# Auditoría de funcionamiento y optimización — 8 de octubre de 2026

Equipo: Samsung Galaxy S22 Ultra (SM-S908U1), Android 16, 11 GB de RAM. Versión de depuración con el modelo
de Tuki incluido (APK de 1,1 GB). Pruebas **sin conexión real** (modo avión) salvo donde se indica.

## 1. Resultado por módulo

| Módulo | Estado | Evidencia |
|---|---|---|
| Arranque | ✅ | 1,2 s en frío (3 mediciones: 1180, 1196, 1235 ms) |
| Modelo de Tuki incluido | ✅ | Se copia del APK al primer arranque en ~2 s, sin red |
| Datos sin conexión (réplica v19) | ✅ | Carga completa; la actualización de versión tarda ~4,5 min (ver §3) |
| Lecciones | ✅ | 11 lecciones de los 4 idiomas con tarjetas y quiz (incluida la 43, antes vacía) |
| Flujo completo de lección | ✅ | Tarjetas → quiz → resultados sin cierres; intento fallido = +0 XP |
| XP y nivel | ✅ | Regla nueva (20 + bono, solo la primera vez); cuenta de prueba corregida a 85 XP (nivel 1) |
| Diccionario | ✅ | Búsqueda en ~0,1 s ("agua" → lî, laya); detalle y favoritos funcionan |
| Favoritos | ✅ | Agregar/quitar y lista en Perfil |
| Traductor | ✅ | Palabras verificadas (60–150 ms); oraciones con Gemma (1–6 s) marcadas "automática" |
| Audios de hablante (Miskitu) | ✅ | 505 entradas registradas; utla, pain, tasba, yul… suenan sin conexión |
| Tuki | ✅ | Responde reglas, cultura, frases, cortesía; no inventa en preguntas fuera de tema |
| Cultura | ✅ | 56 textos, todos en forma directa; los 2 textos sensibles ocultos |
| Cámara (reconocimiento) | ⚠️ 11/14 | Falla en taza (café), libro (piedra) y vaso (plato) |
| Cámara (pantalla) | ⚠️ | En instalación nueva pide permiso de cámara (el estudiante debe otorgarlo) |
| Idioma de la app | ✅ | Español ↔ English sin errores; Miskitu y Kriol marcados como borrador |
| Configuración | ✅ | Idioma, lengua de apoyo y estado del modelo |
| Inicio de sesión con Google | ✅ | Funciona con la firma de depuración y con la de entrega (SHA-1 registrada) |

Sin cierres de la app en toda la auditoría (registro de fallos vacío).

## 2. Errores encontrados y corregidos en esta auditoría

1. **Audios registrados a medias** (243 de 505): se registraban mientras la réplica recargaba palabras. Ahora la
   tarea solo se marca completa cuando están todas y se completa en la siguiente consulta.
2. **Tuki inventaba con preguntas fuera de tema** ("capital de Francia" → "tâ tawanka France", vía Gemini en
   línea). Ahora las preguntas que no son de idiomas ni de cultura no se envían al modelo.
3. **Tuki tardaba 5–10 s en preguntas fuera de tema** (buscaba cada palabra en el diccionario de 4 idiomas).
   Ahora responde en ~0,1 s (la primera pregunta del día ~3,5 s mientras prepara su índice).
4. **Textos de la interfaz sin espacios** ("empezar elquiz", "Ahoracon…"): Android recorta los espacios de
   los extremos. 58 textos en 4 idiomas corregidos; prueba automática nueva para que no vuelva a pasar.
5. **Comentarios sobre Tuki** ("porque tardaste tanto") respondían con un texto cultural al azar: ahora tienen
   respuesta propia y las palabras comunes ya no cuentan como tema.
6. **Textos de cultura con frases de resumen** ("Describe también…"): 24 reescritos con los mismos datos.

## 3. Optimización

| Hallazgo | Impacto | Estado |
|---|---|---|
| **La sincronización en segundo plano descargaba las ~900 000 filas de la base al abrir la app y cada 30 min** | Agotó el tráfico de Supabase (5,3 GB de 5 GB) y gastaba datos móviles de los estudiantes | ✅ Corregido: solo se suben los cambios del estudiante. Medido: 12 KB en 2 min con la app abierta |
| Lecciones, cultura y logros consultaban primero a Supabase (hasta 30 s por consulta con señal débil) | Pantallas "cargando" con mala señal | ✅ Corregido: primero lo local; tiempo límite de red 12 s |
| Navegación entre pantallas | — | ✅ 2,5–2,9 s medidos con la herramienta (incluye ~1,5 s de lectura de pantalla): <1,5 s reales |
| Fluidez | — | ✅ 3,1 % de cuadros con tirones; percentil 90 = 9 ms |
| Memoria con Gemma cargado | ~1,05 GB (248 MB sin el modelo) | ⚠️ Riesgo en teléfonos de 3 GB: Android podría cerrar la app en segundo plano |
| Espacio en el teléfono | APK 1,1 GB + copia del modelo 0,55 GB + caché XNNPACK de MediaPipe 0,55 GB + base 0,09 GB ≈ **2,3 GB** | ⚠️ Pendiente de decisión (ver §4) |
| Recarga completa de la réplica al subir VERSION_REPLICA | 4,5 min para cambiar 20 textos | ⚠️ Pendiente: aplicar parches por tabla en lugar de recargar todo |
| Calidad de traducción de oraciones (Gemma v2) | "Mi hermano come arroz" → "Yang brira rais kaia" (incorrecto) | ⚠️ Limitación del modelo; salen etiquetadas como automáticas |

## 4. Recomendaciones pendientes

1. **Espacio (2,3 GB):** borrar la copia de `files/modelos` no es posible porque MediaPipe exige ruta de archivo;
   opciones: (a) aceptar el tamaño, (b) distribuir el modelo como paquete de recursos de Play
   (*Play Asset Delivery*, instalación junto con la app), (c) probar el motor en GPU para evitar la caché XNNPACK.
2. **Teléfonos de 3 GB:** liberar el modelo (`MotorGemmaLocal.liberar()`) al salir del traductor/Tuki.
3. **Recarga de réplica:** parches por tabla/fila para cambios pequeños de contenido.
4. **Cámara:** ampliar el vocabulario de conceptos (taza, libro, vaso) o umbral por concepto.
5. **Supabase:** el tráfico sigue sobre el límite hasta el 17 de octubre; con la corrección ya no crecerá por la
   sincronización, pero conviene decidir si pasar a Pro durante la entrega.
6. **Diccionario paginado:** la exploración sin buscar quedó en las ramas de GitHub (PR #66); esta auditoría usó
   la carpeta local, que aún no tiene esa fusión.

## 5. Cambios posteriores (8 de octubre, noche)

### Tipografía +1 sp (se subió +2 y luego se bajó 1)

| Estilo | Antes | Después |
|---|---|---|
| displaySmall | 36 | 37 |
| displayLarge (Agu Display) | 32 | 33 |
| titleLarge | 32 | 33 |
| titleMedium | 28 | 29 |
| titleSmall | 24 | 25 |
| headlineSmall | 20 | 21 |
| bodyLarge | 18 | 19 |
| bodyMedium | 16 | 17 |
| bodySmall | 14 | 15 |
| labelLarge | 14 | 15 |
| labelMedium (JetBrains Mono) | 13 | 14 |
| labelSmall | 11 | 12 |

Tamaños fijos en pantallas: barra de navegación 14→15; invitado (intro 24→25, resultados 24→25 y 22→23,
vocabulario 16→17 y 14→15); Inicio 18→19, 18→19, 15→16; Onboarding 16→17 (×2); Perfil 18→19 (×2).

### Correcciones

1. **Mundo siguiente bloqueado al repetir una lección y no aprobarla:** el intento fallido guardaba
   "en_progreso" encima de "completada". La copia local ya no baja una lección aprobada (conserva el mejor puntaje).
2. **Mapa de lecciones:** etiqueta "Mundo N" en el lado libre de cada nodo.
3. **Resultados:** Tuki dice "¡Excelente, lección superada!" y el XP mostrado es el real (antes 20 + porcentaje × 5).
4. **Tarjetas de vocabulario:** "Toca aquí para revelar la respuesta"; Siguiente se habilita al revelarla.
5. **Lentitud con señal débil:** perfil, progreso, favoritos y logros se muestran desde la copia local al
   instante y se actualizan en segundo plano (antes esperaban a Supabase hasta 12 s cada 2 min).
6. **Traductor y Tuki inventaban frases del corpus** ("hola cómo estás"): solo se reconocían oraciones completas.
   Ahora también las partes alineadas de una oración registrada ("¿Cómo estás?" ↔ "¿Nahki sma?") y, si unas
   pocas frases registradas cubren la oración, se usan antes que Gemma: "hola como estas" → "¡Naksa! ¿Nahki sma?".
7. **Sugerencias del diccionario lentas** (3–4 s por fragmento sin traducción): se comparan los textos guardados y
   solo se cargan las 5 mejores.
8. **Cámara en modo Texto:** leía todo el texto del cuadro y suponía que estaba en español (un cartel en inglés
   salía sin traducir). Ahora lee solo la línea tocada (o el cartel corto completo), la marca en pantalla, detecta
   el idioma del texto por las palabras que conoce el diccionario y descarta "traducciones" iguales al original.
