-- Evolución aditiva del modelo lingüístico de Aikukisna.
-- No elimina tablas, columnas ni registros existentes.

create schema if not exists linguistica_auditoria;
revoke all on schema linguistica_auditoria from public, anon, authenticated;

alter table public.palabra
    add column if not exists texto_normalizado text,
    add column if not exists estado_validacion text not null default 'importada',
    add column if not exists created_at timestamptz not null default now(),
    add column if not exists updated_at timestamptz not null default now();

do $$
begin
    if not exists (
        select 1 from pg_constraint
        where conname = 'palabra_estado_validacion_check'
          and conrelid = 'public.palabra'::regclass
    ) then
        alter table public.palabra add constraint palabra_estado_validacion_check
        check (estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada'));
    end if;
end $$;

update public.palabra
set texto_normalizado = lower(regexp_replace(trim(normalize(texto, NFKC)), '\s+', ' ', 'g'))
where texto_normalizado is null;

alter table public.palabra alter column texto_normalizado set not null;

create index if not exists idx_palabra_idioma_texto_normalizado
    on public.palabra (idioma_id, texto_normalizado);
create index if not exists idx_palabra_updated_at on public.palabra (updated_at, id);

create table if not exists public.palabra_fuente (
    palabra_id integer not null references public.palabra(id) on delete cascade,
    fuente_id integer not null references public.fuente_documento(id),
    pagina_inicio integer,
    pagina_fin integer,
    nota text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (palabra_id, fuente_id),
    constraint palabra_fuente_paginas_check check (
        pagina_inicio is null or pagina_fin is null or pagina_fin >= pagina_inicio
    )
);

insert into public.palabra_fuente (palabra_id, fuente_id)
select id, fuente_id from public.palabra
where fuente_id is not null
on conflict (palabra_id, fuente_id) do nothing;

create table if not exists public.acepcion (
    id bigserial primary key,
    palabra_id integer not null references public.palabra(id) on delete cascade,
    numero_acepcion integer not null,
    definicion text,
    contexto text,
    categoria_gramatical text,
    estado_validacion text not null default 'pendiente_revision',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint acepcion_numero_check check (numero_acepcion > 0),
    constraint acepcion_estado_check check (
        estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada')
    ),
    unique (palabra_id, numero_acepcion)
);

create table if not exists public.traduccion_acepcion (
    id bigserial primary key,
    acepcion_origen_id bigint not null references public.acepcion(id) on delete cascade,
    acepcion_destino_id bigint not null references public.acepcion(id) on delete cascade,
    tipo text not null default 'equivalencia',
    estado_validacion text not null default 'pendiente_revision',
    nivel_confianza numeric(4,3),
    nota text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint traduccion_acepcion_distinta_check check (acepcion_origen_id <> acepcion_destino_id),
    constraint traduccion_acepcion_confianza_check check (
        nivel_confianza is null or (nivel_confianza >= 0 and nivel_confianza <= 1)
    ),
    constraint traduccion_acepcion_estado_check check (
        estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada')
    ),
    unique (acepcion_origen_id, acepcion_destino_id, tipo)
);

create table if not exists public.variante_palabra (
    id bigserial primary key,
    palabra_id integer not null references public.palabra(id) on delete cascade,
    texto text not null,
    texto_normalizado text not null,
    tipo text not null,
    estado_validacion text not null default 'pendiente_revision',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint variante_palabra_tipo_check check (
        tipo in ('ortografica', 'dialectal', 'alternativa', 'historica', 'abreviacion')
    ),
    constraint variante_palabra_estado_check check (
        estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada')
    ),
    unique (palabra_id, texto_normalizado, tipo)
);

create table if not exists public.expresion (
    id bigserial primary key,
    idioma_id integer not null references public.idioma(id),
    texto text not null,
    texto_normalizado text not null,
    tipo text not null default 'expresion',
    estado_validacion text not null default 'pendiente_revision',
    fuente_id integer references public.fuente_documento(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint expresion_estado_check check (
        estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada')
    )
);

create table if not exists public.traduccion_expresion (
    id bigserial primary key,
    expresion_origen_id bigint not null references public.expresion(id) on delete cascade,
    expresion_destino_id bigint not null references public.expresion(id) on delete cascade,
    estado_validacion text not null default 'pendiente_revision',
    nota text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint traduccion_expresion_distinta_check check (expresion_origen_id <> expresion_destino_id),
    constraint traduccion_expresion_estado_check check (
        estado_validacion in ('importada', 'pendiente_revision', 'documentada', 'validada', 'rechazada')
    ),
    unique (expresion_origen_id, expresion_destino_id)
);

create index if not exists idx_palabra_fuente_fuente on public.palabra_fuente (fuente_id);
create index if not exists idx_palabra_fuente_updated_at on public.palabra_fuente (updated_at, palabra_id);
create index if not exists idx_acepcion_palabra_estado on public.acepcion (palabra_id, estado_validacion);
create index if not exists idx_acepcion_updated_at on public.acepcion (updated_at, id);
create index if not exists idx_traduccion_acepcion_origen on public.traduccion_acepcion (acepcion_origen_id);
create index if not exists idx_traduccion_acepcion_destino on public.traduccion_acepcion (acepcion_destino_id);
create index if not exists idx_traduccion_acepcion_updated_at on public.traduccion_acepcion (updated_at, id);
create index if not exists idx_variante_idioma_busqueda
    on public.variante_palabra (texto_normalizado, palabra_id);
create index if not exists idx_variante_updated_at on public.variante_palabra (updated_at, id);
create index if not exists idx_expresion_idioma_texto
    on public.expresion (idioma_id, texto_normalizado);
create index if not exists idx_expresion_updated_at on public.expresion (updated_at, id);
create index if not exists idx_traduccion_expresion_origen
    on public.traduccion_expresion (expresion_origen_id);
create index if not exists idx_traduccion_expresion_updated_at
    on public.traduccion_expresion (updated_at, id);

create or replace function public.actualizar_updated_at_linguistico()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

do $$
declare item record;
begin
    for item in select * from (values
        ('palabra', 'palabra_actualizar_updated_at'),
        ('palabra_fuente', 'palabra_fuente_actualizar_updated_at'),
        ('acepcion', 'acepcion_actualizar_updated_at'),
        ('traduccion_acepcion', 'traduccion_acepcion_actualizar_updated_at'),
        ('variante_palabra', 'variante_palabra_actualizar_updated_at'),
        ('expresion', 'expresion_actualizar_updated_at'),
        ('traduccion_expresion', 'traduccion_expresion_actualizar_updated_at')
    ) as triggers(tabla, nombre)
    loop
        if not exists (
            select 1 from pg_trigger
            where tgname = item.nombre
              and tgrelid = format('public.%I', item.tabla)::regclass
        ) then
            execute format(
                'create trigger %I before update on public.%I for each row execute function public.actualizar_updated_at_linguistico()',
                item.nombre, item.tabla
            );
        end if;
    end loop;
end $$;

alter table public.palabra_fuente enable row level security;
alter table public.acepcion enable row level security;
alter table public.traduccion_acepcion enable row level security;
alter table public.variante_palabra enable row level security;
alter table public.expresion enable row level security;
alter table public.traduccion_expresion enable row level security;

do $$
declare table_name text;
begin
    foreach table_name in array array[
        'palabra_fuente', 'acepcion', 'traduccion_acepcion',
        'variante_palabra', 'expresion', 'traduccion_expresion'
    ] loop
        if not exists (
            select 1 from pg_policies
            where schemaname = 'public'
              and tablename = table_name
              and policyname = 'lectura_contenido_linguistico'
        ) then
            execute format(
                'create policy lectura_contenido_linguistico on public.%I for select to anon, authenticated using (true)',
                table_name
            );
        end if;
    end loop;
end $$;

grant select on public.palabra_fuente, public.acepcion, public.traduccion_acepcion,
    public.variante_palabra, public.expresion, public.traduccion_expresion
to anon, authenticated;

create or replace view linguistica_auditoria.palabras_duplicadas
with (security_invoker = true)
as
select
    p.idioma_id,
    p.texto_normalizado,
    array_agg(p.id order by p.id) as palabra_ids,
    array_agg(p.texto order by p.id) as textos,
    array_agg(distinct p.fuente_id) filter (where p.fuente_id is not null) as fuente_ids,
    count(*) as cantidad
from public.palabra p
group by p.idioma_id, p.texto_normalizado
having count(*) > 1;

create or replace view linguistica_auditoria.traducciones_mismo_idioma
with (security_invoker = true)
as
select
    t.id as traduccion_id,
    po.id as palabra_origen_id,
    po.texto as texto_origen,
    pd.id as palabra_destino_id,
    pd.texto as texto_destino,
    po.idioma_id,
    t.nota,
    'pendiente_revision'::text as clasificacion
from public.traduccion t
join public.palabra po on po.id = t.palabra_origen_id
join public.palabra pd on pd.id = t.palabra_destino_id
where po.idioma_id = pd.idioma_id;

create or replace view linguistica_auditoria.candidatos_expresion
with (security_invoker = true)
as
select p.id, p.idioma_id, p.texto, p.texto_normalizado, p.fuente_id
from public.palabra p
where p.texto_normalizado ~ '\s'
   or p.texto ~ '[,;:]'
   or length(p.texto_normalizado) > 80;

create or replace view linguistica_auditoria.entradas_sin_fuente
with (security_invoker = true)
as
select p.id, p.idioma_id, p.texto, p.estado_validacion
from public.palabra p
left join public.palabra_fuente pf on pf.palabra_id = p.id
where pf.palabra_id is null;

create or replace view linguistica_auditoria.posibles_multiples_acepciones
with (security_invoker = true)
as
select
    t.palabra_origen_id as palabra_id,
    pd.idioma_id as idioma_destino_id,
    count(distinct t.palabra_destino_id) as traducciones_distintas,
    array_agg(distinct t.palabra_destino_id) as destino_ids
from public.traduccion t
join public.palabra pd on pd.id = t.palabra_destino_id
group by t.palabra_origen_id, pd.idioma_id
having count(distinct t.palabra_destino_id) > 1;

revoke all on all tables in schema linguistica_auditoria from public, anon, authenticated;
