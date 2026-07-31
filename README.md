# Aikukisna

Aplicación móvil Android para el aprendizaje bidireccional Miskito↔Español, con arquitectura preparada para incorporar más idiomas (inglés, otras lenguas originarias y afrodescendientes). Desarrollado por **YAWANSA TECH** para Hackathon Nicaragua 2026 (hN10), Categoría Avanzado.

---

## 1. Descripción general del proyecto

En Nicaragua, miles de estudiantes de zonas rurales enfrentan dificultades en el aprendizaje de idiomas durante su transición de multigrado a secundaria regular, sin acompañamiento digital continuo. Entre ellos, jóvenes de familias miskitas que migran hacia el centro y pacífico del país enfrentan una doble barrera: aprender el idioma de su nueva escuela sin perder el que traen de casa.

**Aikukisna** responde a esto con una app que enseña miskito y español en ambas direcciones, combinando inteligencia artificial (tutor conversacional "Tuki"), gamificación y contenido cultural, con una arquitectura pensada para escalar a más idiomas de Nicaragua a futuro.

**Equipo (YAWANSA TECH):**
| Integrante | Rol |
|---|---|
| Carlos Figueroa | Backend: `domain`, `data`, `di` (Clean Architecture), integración Supabase, IA |
| Alinstong Jared Rodriguez Molina | Frontend: `presentation`, UI en Jetpack Compose |
| Yoseph Steven Zamora Mendoza| Diseño gráfico |
| Neyvin Sahir Espinoza López | Marketing |
| Ashly Francela Novoa Cerrato | Comunicadora |

---

## 2. Requisitos técnicos

| Requisito | Detalle |
|---|---|
| IDE | Android Studio (versión compatible con Kotlin 2.2.10 — *confirmar versión exacta usada por el equipo*) |
| JDK | 17 o superior *(confirmar)* |
| minSdk | 26 |
| targetSdk | *pendiente de confirmar* |
| Cuenta Supabase | Acceso al proyecto `oawvormcsdtphbfijmod` |
| API keys | Gemini (Tuki) y ElevenLabs (TTS) — ver sección 5.3 |

---

## 3. Arquitectura

El proyecto sigue **Clean Architecture**, separando responsabilidades en capas independientes:

```
app/
├── domain/          → Reglas de negocio, independiente de frameworks
│   ├── model/        (14 modelos: Palabra, Traduccion, Leccion, Usuario, etc.)
│   ├── repository/   (6 interfaces: AuthRepository, CulturaRepository,
│   │                  DiccionarioRepository, LeccionRepository, LogroRepository,
│   │                  UsuarioRepository)
│   └── usecase/      (23 casos de uso)
├── data/            → Implementación concreta de los repositorios (cliente
│                       Supabase-kt + caché local Room)
├── di/              → Módulos Hilt que conectan `data` con `domain`
└── presentation/    → UI en Jetpack Compose, ViewModels
```

**Backend:** Supabase (PostgreSQL 17.6.1) — 16 tablas en 3FN, con Row Level Security activo en todas. Autenticación vía Supabase Auth.

**Regla de negocio central:** en la tabla `leccion`, `capitulo_numero IS NOT NULL` indica una lección de vocabulario (usa `leccion_palabra`); `IS NULL` indica una lección de frases (usa `oracion_ejemplo` directo). Esta lógica está encapsulada en `LeccionRepository` mediante un `sealed class ContenidoLeccion`.

**Asistente de IA (Tuki):** Gemini 2.5 Flash, para tutoría conversacional.
**Texto a voz:** ElevenLabs, para pronunciación y contenido cultural en audio.

---

## 4. Dependencias

| Componente | Versión |
|---|---|
| Kotlin | 2.2.10 |
| KSP | 2.2.10-2.0.2 |
| Hilt | 2.60.1 |
| Room | 2.8.4 |
| Supabase-kt (BOM) | 3.6.0 |
| Ktor | 3.5.0 |
| minSdk | 26 |
| UI | Jetpack Compose |

---

## 5. Instalación y configuración

### 5.1 Clonar y abrir el proyecto
```bash
git clone <url-del-repositorio>
cd aikukisna
```
Abrir la carpeta en Android Studio y esperar la sincronización de Gradle.

### 5.2 Configurar el backend
El proyecto se conecta al proyecto Supabase real (`oawvormcsdtphbfijmod`, Postgres 17.6.1, 16 tablas con RLS activo). No requiere levantar infraestructura propia: se conecta directo al proyecto ya existente en la nube.

### 5.3 Variables de entorno

> ⚠️ **Pendiente de completar por el equipo de desarrollo.**

Las variables se configuran en `local.properties` y se exponen a la app vía `BuildConfig`. Se confirmaron **4 variables** configuradas, pero sus nombres exactos no están documentados aquí todavía. Como mínimo se necesita:

```properties
# local.properties (NO subir este archivo al repositorio)
SUPABASE_URL=<completar>
SUPABASE_ANON_KEY=<completar>
GEMINI_API_KEY=<completar>
ELEVENLABS_API_KEY=<completar>
```

*Nota: los nombres exactos de las 4 variables deben confirmarse contra el archivo real de `build.gradle.kts` antes de entregar este documento.*

### 5.4 Ejecutar
Sincronizar Gradle y ejecutar sobre un emulador o dispositivo con Android 8.0 (API 26) o superior.

---

## 6. Estructura modular

```
domain/
 ├── model/
 │    ├── Palabra.kt
 │    ├── Traduccion.kt
 │    ├── Leccion.kt
 │    ├── Usuario.kt
 │    └── ... (14 modelos en total, uno por entidad de la base de datos)
 ├── repository/
 │    ├── AuthRepository.kt
 │    ├── CulturaRepository.kt
 │    ├── DiccionarioRepository.kt
 │    ├── LeccionRepository.kt
 │    ├── LogroRepository.kt
 │    └── UsuarioRepository.kt
 └── usecase/
      └── ... (23 casos de uso: búsqueda de palabras, progreso de
               lecciones, favoritos, logros, memoria de Tuki,
               autenticación, demo de invitado)
```

**Propiedad de carpetas dentro del equipo:**
- `data/`, `domain/`, `di/` — Carlos Figueroa
- `presentation/` — Alistong

---

## 7. Manual de despliegue

El proyecto está en fase de hackathon — todavía no existe un pipeline de despliegue a producción (Google Play Store). Esta sección cubre cómo generar un build de prueba, no una publicación formal.

```bash
./gradlew assembleRelease
```

> ⚠️ **Pendiente de confirmar:** si el proyecto ya tiene configurado un keystore de firma para builds release. Sin esto, el comando anterior genera un build sin firmar, válido solo para pruebas internas.

Para instalar directamente en un dispositivo conectado por USB (debug):
```bash
./gradlew installDebug
```

---

## 8. Scripts

> ⚠️ **Pendiente de completar por el equipo de desarrollo.**

Comandos estándar de Gradle disponibles en cualquier proyecto Android (confirmar si existen scripts adicionales propios del proyecto):

```bash
./gradlew assembleDebug      # Compilar versión debug
./gradlew testDebugUnitTest  # Correr pruebas unitarias
./gradlew lint               # Análisis estático
```


---

## 9. Ejemplos de endpoints

La API se genera automáticamente vía PostgREST sobre el proyecto Supabase (`oawvormcsdtphbfijmod`). Todas las peticiones requieren el header `apikey` con la clave pública del proyecto.

**Obtener palabras del diccionario (lectura pública):**
```http
GET https://oawvormcsdtphbfijmod.supabase.co/rest/v1/palabra?select=*&limit=20
apikey: sb_publishable_sJwsYBDzTtSEm65l7rtzmQ_8W3XrbzZ
```

**Obtener lecciones de un nivel específico:**
```http
GET https://oawvormcsdtphbfijmod.supabase.co/rest/v1/leccion?nivel=eq.1&select=*
apikey: sb_publishable_sJwsYBDzTtSEm65l7rtzmQ_8W3XrbzZ
```

**Obtener traducciones de una palabra origen:**
```http
GET https://oawvormcsdtphbfijmod.supabase.co/rest/v1/traduccion?palabra_origen_id=eq.<id>&select=*
apikey: sb_publishable_sJwsYBDzTtSEm65l7rtzmQ_8W3XrbzZ
```

*Nota: `usuario`, `progreso_leccion`, `palabra_favorita`, `logro_desbloqueado` y `memoria_tuki` requieren autenticación (`authenticated`) y solo permiten acceso a la fila propia del usuario, según las políticas RLS configuradas.*

---

## Fuentes académicas del contenido

Diccionario Melgara Brown (2008) · Diccionario de Medicina Tradicional URACCAN (2006) · "Aprendamos el Miskito" Melgara Brown (2011) · Diccionario pueblosoriginarios.com (Jorge Matamoros, CIDCA 1996) · Estudio Etnográfico Conzemius (1932/1984).
