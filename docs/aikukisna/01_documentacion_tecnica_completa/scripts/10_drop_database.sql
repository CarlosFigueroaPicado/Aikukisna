-- ============================================================
-- AIKUKISNA — 10_drop_database.sql (OPCIONAL)
-- Responsabilidad única: revertir todo lo creado por los 9
-- archivos anteriores, en orden inverso de dependencias.
--
-- ADVERTENCIA: esto borra permanentemente las 16 tablas y todos
-- sus datos. Usar solo en entornos de desarrollo/prueba, nunca
-- en producción sin respaldo.
-- ============================================================

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP FUNCTION IF EXISTS public.crear_usuario_nuevo();

DROP VIEW IF EXISTS vista_leccion_tipo;
DROP VIEW IF EXISTS vista_traduccion_bidireccional;

DROP TABLE IF EXISTS super_administrador CASCADE;
DROP TABLE IF EXISTS memoria_tuki CASCADE;
DROP TABLE IF EXISTS logro_desbloqueado CASCADE;
DROP TABLE IF EXISTS palabra_favorita CASCADE;
DROP TABLE IF EXISTS progreso_leccion CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TABLE IF EXISTS logro CASCADE;
DROP TABLE IF EXISTS cultura_contenido CASCADE;
DROP TABLE IF EXISTS oracion_ejemplo CASCADE;
DROP TABLE IF EXISTS leccion_palabra CASCADE;
DROP TABLE IF EXISTS traduccion CASCADE;
DROP TABLE IF EXISTS palabra CASCADE;
DROP TABLE IF EXISTS leccion CASCADE;
DROP TABLE IF EXISTS categoria CASCADE;
DROP TABLE IF EXISTS fuente_documento CASCADE;
DROP TABLE IF EXISTS idioma CASCADE;
