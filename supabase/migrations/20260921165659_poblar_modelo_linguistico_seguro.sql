-- Población conservadora del modelo lingüístico.
-- Solo reutiliza relaciones existentes que cumplen criterios deterministas.

create or replace view linguistica_auditoria.candidatos_traduccion_univoca
with (security_invoker = true)
as
with duplicadas as (
    select idioma_id, texto_normalizado
    from public.palabra
    group by idioma_id, texto_normalizado
    having count(*) > 1
), relaciones as (
    select
        t.id as traduccion_id,
        po.id as palabra_origen_id,
        pd.id as palabra_destino_id,
        po.idioma_id as idioma_origen_id,
        pd.idioma_id as idioma_destino_id,
        po.texto_normalizado as origen_normalizado,
        pd.texto_normalizado as destino_normalizado,
        count(*) over (partition by po.id, pd.idioma_id) as destinos_del_origen,
        count(*) over (partition by pd.id, po.idioma_id) as origenes_del_destino
    from public.traduccion t
    join public.palabra po on po.id = t.palabra_origen_id
    join public.palabra pd on pd.id = t.palabra_destino_id
    where po.idioma_id <> pd.idioma_id
)
select r.*
from relaciones r
where r.destinos_del_origen = 1
  and r.origenes_del_destino = 1
  and r.origen_normalizado ~ '^[[:alpha:]][[:alpha:]''’-]*$'
  and r.destino_normalizado ~ '^[[:alpha:]][[:alpha:]''’-]*$'
  and not exists (
      select 1 from duplicadas d
      where d.idioma_id = r.idioma_origen_id
        and d.texto_normalizado = r.origen_normalizado
  )
  and not exists (
      select 1 from duplicadas d
      where d.idioma_id = r.idioma_destino_id
        and d.texto_normalizado = r.destino_normalizado
  );

create or replace view linguistica_auditoria.clasificacion_candidatos_expresion
with (security_invoker = true)
as
select
    p.id as palabra_id,
    p.idioma_id,
    p.texto,
    p.texto_normalizado,
    case
        when exists (
            select 1 from public.oracion_ejemplo o
            where (o.idioma_origen_id = p.idioma_id and o.texto_origen = p.texto)
               or (o.idioma_destino_id = p.idioma_id and o.texto_destino = p.texto)
        ) then 'frase_documentada'
        when p.texto ~ '[,;]' then 'lista_o_enumeracion'
        when p.texto_normalizado ~ '(^| )(significa|se refiere|acción de|persona que)( |$)' then 'posible_definicion'
        when p.texto ~ '[.!?¿¡]$' then 'posible_frase'
        when array_length(regexp_split_to_array(p.texto_normalizado, '\s+'), 1) between 2 and 3 then 'posible_locucion'
        when array_length(regexp_split_to_array(p.texto_normalizado, '\s+'), 1) >= 4 then 'posible_frase'
        else 'fragmento_o_ambiguo'
    end as clasificacion,
    case
        when exists (
            select 1 from public.oracion_ejemplo o
            where (o.idioma_origen_id = p.idioma_id and o.texto_origen = p.texto)
               or (o.idioma_destino_id = p.idioma_id and o.texto_destino = p.texto)
        ) then 'alta'
        else 'requiere_revision'
    end as certeza
from public.palabra p
where p.texto_normalizado ~ '\s'
   or p.texto ~ '[,;:]'
   or length(p.texto_normalizado) > 80;

create or replace view linguistica_auditoria.clasificacion_traducciones_mismo_idioma
with (security_invoker = true)
as
select
    t.id as traduccion_id,
    po.id as palabra_origen_id,
    pd.id as palabra_destino_id,
    po.idioma_id,
    po.texto as texto_origen,
    pd.texto as texto_destino,
    case
        when po.texto_normalizado = pd.texto_normalizado then 'duplicado_normalizado'
        when regexp_replace(po.texto_normalizado, '[^[:alnum:]]', '', 'g') =
             regexp_replace(pd.texto_normalizado, '[^[:alnum:]]', '', 'g') then 'variante_ortografica_alta_certeza'
        when po.texto_normalizado ~ '\s' or pd.texto_normalizado ~ '\s' then 'posible_expresion'
        else 'indeterminado'
    end as clasificacion,
    case
        when regexp_replace(po.texto_normalizado, '[^[:alnum:]]', '', 'g') =
             regexp_replace(pd.texto_normalizado, '[^[:alnum:]]', '', 'g')
             and po.texto_normalizado <> pd.texto_normalizado then 'alta'
        else 'requiere_revision'
    end as certeza
from public.traduccion t
join public.palabra po on po.id = t.palabra_origen_id
join public.palabra pd on pd.id = t.palabra_destino_id
where po.idioma_id = pd.idioma_id;

create or replace view linguistica_auditoria.detalle_duplicados
with (security_invoker = true)
as
select
    p.idioma_id,
    p.texto_normalizado,
    min(p.id) as palabra_canonica_candidata,
    array_agg(p.id order by p.id) as palabra_ids,
    array_agg(p.texto order by p.id) as textos,
    array_agg(distinct pf.fuente_id) filter (where pf.fuente_id is not null) as fuente_ids,
    array_agg(distinct t.id) filter (where t.id is not null) as traduccion_ids,
    array_agg(distinct lp.leccion_id) filter (where lp.leccion_id is not null) as leccion_ids,
    count(distinct fav.usuario_id) as usuarios_con_favorito,
    array_agg(distinct a.id) filter (where a.id is not null) as acepcion_ids,
    array_agg(distinct v.id) filter (where v.id is not null) as variante_ids
from public.palabra p
left join public.palabra_fuente pf on pf.palabra_id = p.id
left join public.traduccion t on t.palabra_origen_id = p.id or t.palabra_destino_id = p.id
left join public.leccion_palabra lp on lp.palabra_id = p.id
left join public.palabra_favorita fav on fav.palabra_id = p.id
left join public.acepcion a on a.palabra_id = p.id
left join public.variante_palabra v on v.palabra_id = p.id
group by p.idioma_id, p.texto_normalizado
having count(distinct p.id) > 1;

create or replace view linguistica_auditoria.integridad_palabra_fuente
with (security_invoker = true)
as
select
    (select count(*) from public.palabra where fuente_id is not null) as relaciones_legacy,
    (select count(*) from public.palabra_fuente) as relaciones_puente,
    (select count(*) from public.palabra p where p.fuente_id is not null and not exists (
        select 1 from public.palabra_fuente pf
        where pf.palabra_id = p.id and pf.fuente_id = p.fuente_id
    )) as asociaciones_perdidas,
    (select count(*) from (
        select palabra_id, fuente_id from public.palabra_fuente
        group by palabra_id, fuente_id having count(*) > 1
    ) d) as duplicados_exactos;

insert into public.acepcion (
    palabra_id, numero_acepcion, definicion, contexto,
    categoria_gramatical, estado_validacion
)
select palabra_id, 1, null, null, null, 'importada'
from (
    select palabra_origen_id as palabra_id
    from linguistica_auditoria.candidatos_traduccion_univoca
    union
    select palabra_destino_id
    from linguistica_auditoria.candidatos_traduccion_univoca
) candidatas
on conflict (palabra_id, numero_acepcion) do nothing;

insert into public.traduccion_acepcion (
    acepcion_origen_id, acepcion_destino_id, tipo,
    estado_validacion, nivel_confianza, nota
)
select
    ao.id,
    ad.id,
    'equivalencia_legacy_univoca',
    'importada',
    null,
    'Migrada desde traduccion.id=' || c.traduccion_id
from linguistica_auditoria.candidatos_traduccion_univoca c
join public.acepcion ao
  on ao.palabra_id = c.palabra_origen_id and ao.numero_acepcion = 1
join public.acepcion ad
  on ad.palabra_id = c.palabra_destino_id and ad.numero_acepcion = 1
on conflict (acepcion_origen_id, acepcion_destino_id, tipo) do nothing;

insert into public.expresion (
    idioma_id, texto, texto_normalizado, tipo,
    estado_validacion, fuente_id
)
select
    datos.idioma_id,
    min(datos.texto),
    datos.texto_normalizado,
    'frase_ejemplo',
    'importada',
    min(datos.fuente_id)
from (
    select idioma_origen_id as idioma_id, texto_origen as texto, fuente_id,
           lower(regexp_replace(trim(normalize(texto_origen, NFKC)), '\s+', ' ', 'g')) as texto_normalizado
    from public.oracion_ejemplo
    union all
    select idioma_destino_id, texto_destino, fuente_id,
           lower(regexp_replace(trim(normalize(texto_destino, NFKC)), '\s+', ' ', 'g'))
    from public.oracion_ejemplo
) datos
where datos.texto is not null and btrim(datos.texto) <> ''
  and not exists (
      select 1 from public.expresion e
      where e.idioma_id = datos.idioma_id
        and e.texto_normalizado = datos.texto_normalizado
        and e.tipo = 'frase_ejemplo'
  )
group by datos.idioma_id, datos.texto_normalizado;

insert into public.traduccion_expresion (
    expresion_origen_id, expresion_destino_id,
    estado_validacion, nota
)
select
    eo.id,
    ed.id,
    'importada',
    'Migrada desde oracion_ejemplo.id=' || o.id
from public.oracion_ejemplo o
join lateral (
    select id from public.expresion
    where idioma_id = o.idioma_origen_id
      and texto_normalizado = lower(regexp_replace(trim(normalize(o.texto_origen, NFKC)), '\s+', ' ', 'g'))
      and tipo = 'frase_ejemplo'
    order by id limit 1
) eo on true
join lateral (
    select id from public.expresion
    where idioma_id = o.idioma_destino_id
      and texto_normalizado = lower(regexp_replace(trim(normalize(o.texto_destino, NFKC)), '\s+', ' ', 'g'))
      and tipo = 'frase_ejemplo'
    order by id limit 1
) ed on true
where eo.id <> ed.id
on conflict (expresion_origen_id, expresion_destino_id) do nothing;

revoke all on linguistica_auditoria.candidatos_traduccion_univoca,
    linguistica_auditoria.clasificacion_candidatos_expresion,
    linguistica_auditoria.clasificacion_traducciones_mismo_idioma,
    linguistica_auditoria.detalle_duplicados,
    linguistica_auditoria.integridad_palabra_fuente
from public, anon, authenticated;
