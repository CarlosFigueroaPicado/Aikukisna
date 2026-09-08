-- ============================================================
-- AIKUKISNA — 06_create_functions.sql
-- Responsabilidad única: funciones. El trigger que la usa está
-- en 07_create_triggers.sql — no se mezclan aquí.
-- ============================================================

-- Crea automáticamente el perfil en `usuario` cuando alguien se
-- registra en Supabase Auth. Sin esto, un usuario recién registrado
-- no tendría fila en `usuario` hasta hacerla manualmente, lo cual
-- rompería cualquier operación posterior que dependa de auth.uid()
-- coincidiendo con una fila existente (favoritos, progreso, etc.).
--
-- SECURITY DEFINER es necesario aquí: el trigger se dispara en el
-- momento del registro, antes de que exista una sesión autenticada
-- normal que pueda pasar las políticas RLS de `usuario` por sí sola.
-- Se limita a una sola operación (un INSERT con datos fijos del
-- propio registro), no ejecuta SQL dinámico ni recibe entrada externa.
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
