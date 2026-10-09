-- El login por nombre de usuario consulta public.usuario desde la Edge Function.
-- La función se conserva para rollback, pero queda sin permisos de ejecución delegados.
revoke execute on function public.obtener_correo_por_usuario(text)
from public, anon, authenticated, service_role;
