-- ============================================================
-- AIKUKISNA — 09_roles_permissions.sql
-- Responsabilidad única: permisos de tabla a nivel de rol
-- (GRANT/REVOKE de PostgreSQL). Las políticas RLS de Supabase,
-- que deciden fila por fila, van aparte en supabase/policies.sql
-- — son un mecanismo distinto y se separan a propósito.
--
-- Roles usados: `anon` (sin sesión) y `authenticated` (con sesión),
-- ambos provistos por Supabase Auth.
-- ============================================================

-- ---------- Tablas de contenido: solo lectura ----------

REVOKE ALL ON idioma, fuente_documento, categoria, leccion, palabra,
    traduccion, leccion_palabra, oracion_ejemplo, cultura_contenido, logro
FROM anon, authenticated;

GRANT SELECT ON idioma, fuente_documento, categoria, leccion, palabra,
    traduccion, leccion_palabra, oracion_ejemplo, cultura_contenido, logro
TO anon, authenticated;

-- Excepción: `leccion` también admite escritura de `authenticated`,
-- pero gateada por la política RLS de superadmin (supabase/policies.sql).
-- El permiso de tabla por sí solo NO es suficiente para escribir.
GRANT INSERT, UPDATE, DELETE ON leccion TO authenticated;

-- ---------- Tablas de usuario: sin acceso anónimo ----------

REVOKE ALL ON usuario, progreso_leccion, palabra_favorita, logro_desbloqueado, memoria_tuki
FROM anon, authenticated;

GRANT SELECT, INSERT, UPDATE, DELETE ON usuario, progreso_leccion,
    palabra_favorita, logro_desbloqueado, memoria_tuki
TO authenticated;

-- Nota: ningún GRANT anterior incluye TRUNCATE. TRUNCATE ignora RLS
-- por completo en PostgreSQL, así que se excluye deliberadamente
-- de todos los roles públicos, sin excepción.

-- ---------- Tabla de superadministración ----------

REVOKE ALL ON super_administrador FROM anon, authenticated;
-- SELECT limitado (un usuario solo puede verificar si ÉL MISMO es
-- superadmin) vía política RLS en supabase/policies.sql — necesario
-- para que esa misma política pueda evaluarse sin error de permiso.
GRANT SELECT ON super_administrador TO authenticated;

-- ---------- Causa raíz: privilegios por defecto para tablas futuras ----------
-- Sin esto, Supabase otorga permisos amplios automáticamente a
-- cualquier tabla nueva que se cree después — hay que fijar esto
-- una sola vez para que no se repita el problema con cada tabla nueva.

ALTER DEFAULT PRIVILEGES IN SCHEMA public
    REVOKE ALL ON TABLES FROM anon, authenticated;
