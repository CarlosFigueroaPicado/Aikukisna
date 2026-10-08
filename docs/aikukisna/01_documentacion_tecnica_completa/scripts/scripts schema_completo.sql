-- ============================================================
-- AIKUKISNA — ESQUEMA COMPLETO DE BASE DE DATOS
-- Estado verificado en vivo contra Supabase (proyecto oawvormcsdtphbfijmod)
-- Este script reproduce la ESTRUCTURA completa (16 tablas, seguridad,
-- trigger).
-- ============================================================
CREATE SCHEMA IF NOT EXISTS public;

-- ============================================================
-- 1) TABLAS
-- ============================================================

CREATE TABLE idioma (
    id SERIAL PRIMARY KEY,
    codigo TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL
);

CREATE TABLE fuente_documento (
    id SERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    autor TEXT,
    anio INTEGER,
    institucion TEXT
);

CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE leccion (
    id SERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    capitulo_numero INTEGER,
    nivel INTEGER NOT NULL,
    categoria_id INTEGER REFERENCES categoria(id),
    idioma_meta_id INTEGER NOT NULL REFERENCES idioma(id)
);

CREATE TABLE palabra (
    id SERIAL PRIMARY KEY,
    idioma_id INTEGER NOT NULL REFERENCES idioma(id),
    texto TEXT NOT NULL,
    categoria_id INTEGER REFERENCES categoria(id),
    fuente_id INTEGER NOT NULL REFERENCES fuente_documento(id)
);
CREATE INDEX idx_palabra_texto ON palabra (idioma_id, lower(texto));

CREATE TABLE traduccion (
    id SERIAL PRIMARY KEY,
    palabra_origen_id INTEGER NOT NULL REFERENCES palabra(id) ON DELETE CASCADE,
    palabra_destino_id INTEGER NOT NULL REFERENCES palabra(id) ON DELETE CASCADE,
    nota TEXT,
    UNIQUE (palabra_origen_id, palabra_destino_id)
);
CREATE INDEX idx_traduccion_origen ON traduccion (palabra_origen_id);

CREATE TABLE leccion_palabra (
    leccion_id INTEGER NOT NULL REFERENCES leccion(id) ON DELETE CASCADE,
    palabra_id INTEGER NOT NULL REFERENCES palabra(id) ON DELETE CASCADE,
    PRIMARY KEY (leccion_id, palabra_id)
);

CREATE TABLE oracion_ejemplo (
    id SERIAL PRIMARY KEY,
    texto_miskito TEXT NOT NULL,
    texto_espanol TEXT NOT NULL,
    leccion_id INTEGER REFERENCES leccion(id) ON DELETE SET NULL,
    fuente_id INTEGER NOT NULL REFERENCES fuente_documento(id)
);

CREATE TABLE cultura_contenido (
    id SERIAL PRIMARY KEY,
    titulo TEXT NOT NULL,
    contenido TEXT NOT NULL,
    rango_pagina_inicio INTEGER,
    rango_pagina_fin INTEGER,
    fuente_id INTEGER NOT NULL REFERENCES fuente_documento(id)
);

CREATE TABLE logro (
    id SERIAL PRIMARY KEY,
    nombre TEXT NOT NULL,
    descripcion TEXT NOT NULL,
    condicion_tipo TEXT NOT NULL
        CHECK (condicion_tipo IN ('lecciones_completadas','racha_maxima','palabras_favoritas','memorias_tuki','leccion_categoria_completada')),
    condicion_valor INTEGER NOT NULL,
    categoria_id INTEGER REFERENCES categoria(id)
);

CREATE TABLE usuario (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre_usuario TEXT,
    nombre TEXT,
    apellido TEXT,
    correo TEXT,
    edad INTEGER,
    pais TEXT,
    ciudad TEXT,
    idioma_meta_id INTEGER REFERENCES idioma(id),
    xp INTEGER NOT NULL DEFAULT 0,
    racha_actual INTEGER NOT NULL DEFAULT 0,
    racha_maxima INTEGER NOT NULL DEFAULT 0,
    ultima_actividad DATE
);

CREATE TABLE progreso_leccion (
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    leccion_id INTEGER NOT NULL REFERENCES leccion(id) ON DELETE CASCADE,
    estado TEXT NOT NULL DEFAULT 'no_iniciada'
        CHECK (estado IN ('no_iniciada','en_progreso','completada')),
    puntaje INTEGER,
    fecha_completado TIMESTAMPTZ,
    PRIMARY KEY (usuario_id, leccion_id)
);

CREATE TABLE palabra_favorita (
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    palabra_id INTEGER NOT NULL REFERENCES palabra(id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, palabra_id)
);

CREATE TABLE logro_desbloqueado (
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    logro_id INTEGER NOT NULL REFERENCES logro(id) ON DELETE CASCADE,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (usuario_id, logro_id)
);

CREATE TABLE memoria_tuki (
    id SERIAL PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    tipo TEXT NOT NULL
        CHECK (tipo IN ('aprendizaje','conversacion','preferencia','recomendacion')),
    resumen TEXT NOT NULL,
    fecha TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Panel de superusuario: tabla aislada, sin GRANT a anon/authenticated.
-- Solo se administra desde el SQL Editor de Supabase.
CREATE TABLE super_administrador (
    usuario_id UUID PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE
);

-- ============================================================
-- 2) ROW LEVEL SECURITY
-- ============================================================

ALTER TABLE usuario ENABLE ROW LEVEL SECURITY;
ALTER TABLE progreso_leccion ENABLE ROW LEVEL SECURITY;
ALTER TABLE palabra_favorita ENABLE ROW LEVEL SECURITY;
ALTER TABLE logro_desbloqueado ENABLE ROW LEVEL SECURITY;
ALTER TABLE memoria_tuki ENABLE ROW LEVEL SECURITY;
ALTER TABLE palabra ENABLE ROW LEVEL SECURITY;
ALTER TABLE traduccion ENABLE ROW LEVEL SECURITY;
ALTER TABLE leccion ENABLE ROW LEVEL SECURITY;
ALTER TABLE leccion_palabra ENABLE ROW LEVEL SECURITY;
ALTER TABLE oracion_ejemplo ENABLE ROW LEVEL SECURITY;
ALTER TABLE cultura_contenido ENABLE ROW LEVEL SECURITY;
ALTER TABLE logro ENABLE ROW LEVEL SECURITY;
ALTER TABLE categoria ENABLE ROW LEVEL SECURITY;
ALTER TABLE idioma ENABLE ROW LEVEL SECURITY;
ALTER TABLE fuente_documento ENABLE ROW LEVEL SECURITY;
ALTER TABLE super_administrador ENABLE ROW LEVEL SECURITY;

-- Lectura pública en tablas de contenido
CREATE POLICY "lectura publica" ON palabra FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON traduccion FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON leccion FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON leccion_palabra FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON oracion_ejemplo FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON cultura_contenido FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON logro FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON categoria FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON idioma FOR SELECT USING (true);
CREATE POLICY "lectura publica" ON fuente_documento FOR SELECT USING (true);

-- Cada usuario ve/edita solo su propia fila
CREATE POLICY "usuario ve y edita su propio perfil" ON usuario FOR ALL USING (auth.uid() = id);
CREATE POLICY "usuario ve y edita su propio progreso" ON progreso_leccion FOR ALL USING (auth.uid() = usuario_id);
CREATE POLICY "usuario ve y edita sus propios favoritos" ON palabra_favorita FOR ALL USING (auth.uid() = usuario_id);
CREATE POLICY "usuario ve sus propios logros" ON logro_desbloqueado FOR ALL USING (auth.uid() = usuario_id);
CREATE POLICY "usuario ve sus propias memorias de tuki" ON memoria_tuki FOR ALL USING (auth.uid() = usuario_id);

-- Panel de superusuario: solo puede crear/editar/borrar lecciones
-- quien esté en super_administrador
CREATE POLICY "solo superadmin crea lecciones" ON leccion
    FOR INSERT WITH CHECK (EXISTS (SELECT 1 FROM super_administrador WHERE usuario_id = auth.uid()));
CREATE POLICY "solo superadmin edita lecciones" ON leccion
    FOR UPDATE USING (EXISTS (SELECT 1 FROM super_administrador WHERE usuario_id = auth.uid()));
CREATE POLICY "solo superadmin borra lecciones" ON leccion
    FOR DELETE USING (EXISTS (SELECT 1 FROM super_administrador WHERE usuario_id = auth.uid()));

-- Un usuario autenticado puede verificar (solo) si él mismo es superadmin
CREATE POLICY "usuario solo verifica si el mismo es superadmin" ON super_administrador
    FOR SELECT USING (usuario_id = auth.uid());

-- ============================================================
-- 3) PERMISOS DE TABLA (mínimo necesario — RLS es la capa fina,
--    esto es la capa gruesa; sin esto, TRUNCATE podría bypasear RLS)
-- ============================================================

REVOKE ALL ON idioma, fuente_documento, categoria, leccion, palabra,
    traduccion, leccion_palabra, oracion_ejemplo, cultura_contenido, logro
FROM anon, authenticated;
GRANT SELECT ON idioma, fuente_documento, categoria, leccion, palabra,
    traduccion, leccion_palabra, oracion_ejemplo, cultura_contenido, logro
TO anon, authenticated;
GRANT INSERT, UPDATE, DELETE ON leccion TO authenticated; -- gateado por RLS arriba

REVOKE ALL ON usuario, progreso_leccion, palabra_favorita, logro_desbloqueado, memoria_tuki
FROM anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON usuario, progreso_leccion,
    palabra_favorita, logro_desbloqueado, memoria_tuki
TO authenticated;

REVOKE ALL ON super_administrador FROM anon, authenticated;
GRANT SELECT ON super_administrador TO authenticated; -- gateado por RLS arriba

-- Causa raíz: cualquier tabla nueva futura no hereda permisos amplios por defecto
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES FROM anon, authenticated;

-- ============================================================
-- 4) TRIGGER: crear perfil en `usuario` automáticamente al registrarse
-- ============================================================

CREATE OR REPLACE FUNCTION public.crear_usuario_nuevo()
RETURNS TRIGGER
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.usuario (id, correo, nombre_usuario)
    VALUES (NEW.id, NEW.email, split_part(NEW.email, '@', 1));
    RETURN NEW;
END;
$$;

CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.crear_usuario_nuevo();

-- ============================================================
