-- ============================================================
-- AIKUKISNA — 01_create_schema.sql
-- Responsabilidad única: esquema y extensiones de PostgreSQL.
-- ============================================================
--
-- Nota importante: en Supabase, el esquema `public` ya existe por
-- defecto en cada proyecto nuevo — no hace falta crearlo. Este
-- archivo existe igual, siguiendo la separación de responsabilidades
-- pedida, para dejar explícito qué esquema se usa y qué extensiones
-- de PostgreSQL requiere el proyecto.

-- Esquema de trabajo (ya existe en Supabase; se declara por claridad)
CREATE SCHEMA IF NOT EXISTS public;

-- Extensión necesaria para gen_random_uuid(), usada en pruebas
-- y disponible por defecto en Supabase.
CREATE EXTENSION IF NOT EXISTS pgcrypto;
