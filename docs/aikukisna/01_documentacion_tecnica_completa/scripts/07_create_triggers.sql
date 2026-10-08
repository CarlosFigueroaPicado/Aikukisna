-- ============================================================
-- AIKUKISNA — 07_create_triggers.sql
-- Responsabilidad única: triggers.
-- Requiere que 06_create_functions.sql ya se haya ejecutado.
-- ============================================================

-- Dispara crear_usuario_nuevo() cada vez que Supabase Auth
-- registra un usuario nuevo en auth.users.
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.crear_usuario_nuevo();
