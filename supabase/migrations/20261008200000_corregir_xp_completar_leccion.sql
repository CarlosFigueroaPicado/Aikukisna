-- Antes: xp += 20 + puntaje*5 en cada llamada; la app envía porcentaje (0-100), así que una lección daba
-- hasta 520 XP y repetirla volvía a sumarlos. Ahora (igual que CompletarLeccionUseCase.xpGanado en la app):
-- primera aprobación = 20 + bono (+5 con >=90 %, +10 con 100 %); al repetir solo cuenta la mejora del bono.
create or replace function public.bono_xp_leccion(p_porcentaje integer)
returns integer
language sql
immutable
set search_path to 'public'
as $function$
    select case when p_porcentaje >= 100 then 10 when p_porcentaje >= 90 then 5 else 0 end;
$function$;

create or replace function public.completar_leccion(p_leccion_id integer, p_puntaje integer default 0)
returns void
language plpgsql
set search_path to 'public'
as $function$
declare
    v_usuario_id uuid := auth.uid();
    v_puntaje integer := least(greatest(coalesce(p_puntaje, 0), 0), 100);
    v_puntaje_anterior integer;
    v_xp_ganado integer;
    v_ultima_actividad date;
    v_racha_actual integer;
    v_racha_nueva integer;
begin
    if v_usuario_id is null then
        raise exception 'No hay usuario autenticado';
    end if;
    if not exists (select 1 from leccion where id = p_leccion_id) then
        raise exception 'La lección % no existe', p_leccion_id;
    end if;

    select least(puntaje, 100) into v_puntaje_anterior
    from progreso_leccion
    where usuario_id = v_usuario_id and leccion_id = p_leccion_id and estado = 'completada';

    v_xp_ganado := case
        when v_puntaje_anterior is null then 20 + public.bono_xp_leccion(v_puntaje)
        else greatest(public.bono_xp_leccion(v_puntaje) - public.bono_xp_leccion(v_puntaje_anterior), 0)
    end;

    insert into progreso_leccion (usuario_id, leccion_id, estado, puntaje, fecha_completado)
    values (v_usuario_id, p_leccion_id, 'completada', v_puntaje, now())
    on conflict (usuario_id, leccion_id)
    do update set
        estado = 'completada',
        puntaje = greatest(least(progreso_leccion.puntaje, 100), excluded.puntaje),
        fecha_completado = now();

    select ultima_actividad, racha_actual into v_ultima_actividad, v_racha_actual
    from usuario where id = v_usuario_id;

    v_racha_nueva := case
        when v_ultima_actividad = current_date then v_racha_actual
        when v_ultima_actividad = current_date - 1 then v_racha_actual + 1
        else 1
    end;

    perform set_config('aikukisna.permitir_gamificacion', 'true', true);
    update usuario
    set xp = xp + v_xp_ganado,
        racha_actual = v_racha_nueva,
        racha_maxima = greatest(racha_maxima, v_racha_nueva),
        ultima_actividad = current_date
    where id = v_usuario_id;
end;
$function$;

-- Datos: puntajes fuera de rango y XP inflado por la fórmula anterior.
update progreso_leccion set puntaje = 100 where puntaje > 100;

select set_config('aikukisna.permitir_gamificacion', 'true', true);
update usuario u
set xp = coalesce((
    select sum(20 + public.bono_xp_leccion(p.puntaje))
    from progreso_leccion p
    where p.usuario_id = u.id and p.estado = 'completada'
), 0);
