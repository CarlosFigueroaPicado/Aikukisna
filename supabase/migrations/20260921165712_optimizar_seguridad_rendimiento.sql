-- Correcciones independientes de rendimiento y seguridad confirmadas en remoto.

create index if not exists idx_expresion_fuente_id
    on public.expresion (fuente_id);

create index if not exists idx_regla_pronunciacion_fuente_id
    on public.regla_pronunciacion (fuente_id);

create index if not exists idx_traduccion_expresion_destino
    on public.traduccion_expresion (expresion_destino_id);

alter policy "usuario lee sus propios logros"
on public.logro_desbloqueado
using ((select auth.uid()) = usuario_id);

alter function public.proteger_columnas_gamificacion()
set search_path = '';

-- Estas funciones validan auth.uid() y necesitan elevar privilegios para
-- escribir mediante RLS. Se conserva SECURITY DEFINER, pero se restringe
-- su invocación directa a los roles que realmente la necesitan.
revoke execute on function public.completar_leccion(integer, integer) from public, anon;
grant execute on function public.completar_leccion(integer, integer) to authenticated;

revoke execute on function public.desbloquear_logro(integer) from public, anon;
grant execute on function public.desbloquear_logro(integer) to authenticated;

-- Es una función de disparador de auth.users; no es un RPC público.
revoke execute on function public.crear_usuario_nuevo() from public, anon, authenticated;

-- obtener_correo_por_usuario(text) se conserva sin cambios porque el inicio
-- por nombre de usuario depende actualmente de su ejecución anónima. Su
-- exposición permite enumerar correos y requiere un rediseño de autenticación.

-- pg_trgm permanece en public: existe al menos una dependencia activa y mover
-- una extensión se reserva para una migración específica con prueba de índices.

-- La protección de contraseñas filtradas debe habilitarse desde Auth y no SQL.
