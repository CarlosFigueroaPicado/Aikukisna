-- ============================================================
-- AIKUKISNA — 02_create_tables.sql
-- Responsabilidad única: definición de tablas (columnas y llave
-- primaria). Las llaves foráneas, UNIQUE y CHECK van en
-- 03_create_constraints.sql — no se mezclan aquí a propósito.
-- Modelo normalizado en Tercera Forma Normal (3FN).
-- ============================================================

-- ---------- Catálogos ----------

-- Idiomas disponibles en la plataforma (Miskito, Español)
CREATE TABLE idioma (
    id      SERIAL PRIMARY KEY,
    codigo  TEXT NOT NULL,
    nombre  TEXT NOT NULL
);

-- Documentos/fuentes académicas de las que proviene el contenido
CREATE TABLE fuente_documento (
    id           SERIAL PRIMARY KEY,
    titulo       TEXT NOT NULL,
    autor        TEXT,
    anio         INTEGER,
    institucion  TEXT
);

-- Categorías temáticas (Salud, Familia, Números, etc.)
CREATE TABLE categoria (
    id      SERIAL PRIMARY KEY,
    nombre  TEXT NOT NULL
);

-- ---------- Contenido educativo ----------

-- Lecciones del curso, agrupadas en 7 niveles de dificultad
CREATE TABLE leccion (
    id               SERIAL PRIMARY KEY,
    titulo           TEXT NOT NULL,
    capitulo_numero  INTEGER,       -- NULL = lección de frases; NOT NULL = lección de vocabulario
    nivel            INTEGER NOT NULL,
    categoria_id     INTEGER,
    idioma_meta_id   INTEGER NOT NULL
);

-- Palabras del diccionario bidireccional (Miskito y Español)
CREATE TABLE palabra (
    id            SERIAL PRIMARY KEY,
    idioma_id     INTEGER NOT NULL,
    texto         TEXT NOT NULL,
    categoria_id  INTEGER,
    fuente_id     INTEGER NOT NULL
);

-- Traducciones entre palabras (relación dirigida origen -> destino)
CREATE TABLE traduccion (
    id                  SERIAL PRIMARY KEY,
    palabra_origen_id   INTEGER NOT NULL,
    palabra_destino_id  INTEGER NOT NULL,
    nota                TEXT
);

-- Tabla puente: qué palabras pertenecen a qué lección (N:M)
CREATE TABLE leccion_palabra (
    leccion_id  INTEGER NOT NULL,
    palabra_id  INTEGER NOT NULL,
    PRIMARY KEY (leccion_id, palabra_id)
);

-- Frases de ejemplo (usadas por lecciones sin vocabulario suelto)
CREATE TABLE oracion_ejemplo (
    id             SERIAL PRIMARY KEY,
    texto_miskito  TEXT NOT NULL,
    texto_espanol  TEXT NOT NULL,
    leccion_id     INTEGER,
    fuente_id      INTEGER NOT NULL
);

-- Contenido cultural / etnográfico (Conzemius 1932, lingüística)
CREATE TABLE cultura_contenido (
    id                    SERIAL PRIMARY KEY,
    titulo                TEXT NOT NULL,
    contenido             TEXT NOT NULL,
    rango_pagina_inicio   INTEGER,
    rango_pagina_fin      INTEGER,
    fuente_id             INTEGER NOT NULL
);

-- Logros/insignias que el usuario puede desbloquear
CREATE TABLE logro (
    id               SERIAL PRIMARY KEY,
    nombre           TEXT NOT NULL,
    descripcion      TEXT NOT NULL,
    condicion_tipo   TEXT NOT NULL,
    condicion_valor  INTEGER NOT NULL,
    categoria_id     INTEGER
);

-- ---------- Usuario y actividad (vacías hasta que haya uso real) ----------

-- Perfil de usuario. id referencia auth.users(id) de Supabase Auth.
CREATE TABLE usuario (
    id                UUID PRIMARY KEY,
    nombre_usuario    TEXT,
    nombre            TEXT,
    apellido          TEXT,
    correo            TEXT,
    edad              INTEGER,
    pais              TEXT,
    ciudad            TEXT,
    idioma_meta_id    INTEGER,
    xp                INTEGER NOT NULL DEFAULT 0,
    racha_actual      INTEGER NOT NULL DEFAULT 0,
    racha_maxima      INTEGER NOT NULL DEFAULT 0,
    ultima_actividad  DATE
);

-- Avance del usuario por lección (N:M usuario-leccion con atributos)
CREATE TABLE progreso_leccion (
    usuario_id        UUID NOT NULL,
    leccion_id        INTEGER NOT NULL,
    estado            TEXT NOT NULL DEFAULT 'no_iniciada',
    puntaje           INTEGER,
    fecha_completado  TIMESTAMPTZ,
    PRIMARY KEY (usuario_id, leccion_id)
);

-- Palabras marcadas como favoritas por el usuario (N:M)
CREATE TABLE palabra_favorita (
    usuario_id  UUID NOT NULL,
    palabra_id  INTEGER NOT NULL,
    PRIMARY KEY (usuario_id, palabra_id)
);

-- Logros ya desbloqueados por el usuario (N:M con fecha)
CREATE TABLE logro_desbloqueado (
    usuario_id  UUID NOT NULL,
    logro_id    INTEGER NOT NULL,
    fecha       TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, logro_id)
);

-- Historial de interacciones con el asistente de IA "Tuki"
CREATE TABLE memoria_tuki (
    id          SERIAL PRIMARY KEY,
    usuario_id  UUID NOT NULL,
    tipo        TEXT NOT NULL,
    resumen     TEXT NOT NULL,
    fecha       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Panel de administración: quién puede crear/editar lecciones.
-- Tabla deliberadamente aislada (ver 09_roles_permissions.sql):
-- nunca recibe permisos de anon/authenticated, solo se administra
-- manualmente desde el SQL Editor de Supabase.
CREATE TABLE super_administrador (
    usuario_id  UUID PRIMARY KEY
);
