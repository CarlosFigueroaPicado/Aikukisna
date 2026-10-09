"""Genera el SQL que carga las lecciones de Inglés alineadas al MINED (scripts/datos/ingles_mined_*.json).

Las palabras existentes se reutilizan; solo se crean las que faltan. Las frases son del equipo de
Aikukisna (no texto del libro) y quedan como 'importada' con nota de revisión docente pendiente.
Uso: python scripts/generar_sql_ingles_mined.py scripts/datos/ingles_mined_a0_a2.json > salida.sql
"""
import json
import sys

INGLES, ESPANOL = 4, 2
datos = json.load(open(sys.argv[1], encoding='utf-8'))


def q(texto):
    if texto is None:
        return 'null'
    assert '$q$' not in texto
    return f'$q${texto}$q$'


sql = ['begin;']
sql.append("""
create or replace function pg_temp.aik_palabra(p_idioma int, p_texto text, p_fuente int) returns int language plpgsql as $f$
declare v int;
begin
  select id into v from palabra
   where idioma_id = p_idioma and texto_normalizado = lower(trim(p_texto)) and estado_validacion <> 'rechazada'
   order by (estado_validacion = 'validada') desc, (estado_validacion = 'documentada') desc, id limit 1;
  if v is null then
    insert into palabra (idioma_id, texto, fuente_id, texto_normalizado, texto_busqueda, estado_validacion,
                         tipo_entrada, pronunciacion_verificada, created_at, updated_at)
    values (p_idioma, trim(p_texto), p_fuente, lower(trim(p_texto)), lower(trim(p_texto)), 'importada',
            case when position(' ' in trim(p_texto)) > 0 then 'locucion' else 'palabra' end, false, now(), now())
    returning id into v;
  end if;
  return v;
end $f$;

create or replace function pg_temp.aik_traduccion(p_en int, p_es int, p_fuente int) returns void language plpgsql as $f$
begin
  if exists (select 1 from traduccion where (palabra_origen_id = p_en and palabra_destino_id = p_es)
                                         or (palabra_origen_id = p_es and palabra_destino_id = p_en)) then
    return;
  end if;
  insert into traduccion (palabra_origen_id, palabra_destino_id, nota, estado_validacion, fuente_id, created_at, updated_at,
                          nivel_confianza, es_preferida)
  values (p_en, p_es, 'Equivalencia de la lección de Inglés alineada al MINED; pendiente de revisión docente.', 'importada',
          p_fuente, now(), now(), 0.8,
          not exists (select 1 from traduccion t join palabra d on d.id = case when t.palabra_origen_id = p_en then t.palabra_destino_id else t.palabra_origen_id end
                      where (t.palabra_origen_id = p_en or t.palabra_destino_id = p_en) and d.idioma_id = 2 and t.es_preferida));
end $f$;
""")

# Fuentes: una por libro y una para las frases propias.
sql.append("create temp table aik_fuente (clave text primary key, id int) on commit drop;")
for clave, f in datos['fuentes'].items():
    sql.append(f"""
with existente as (select id from fuente_documento where url = {q(f['url'])}),
nueva as (
  insert into fuente_documento (titulo, autor, anio, institucion, url, licencia, nota_uso)
  select {q(f['titulo'])}, 'Ministerio de Educación de Nicaragua (MINED)', {f['anio']}, 'MINED', {q(f['url'])},
         'Derechos reservados MINED',
         'Alineación curricular: se citan unidad, lección y página. Aikukisna no reproduce texto del libro.'
  where not exists (select 1 from existente) returning id)
insert into aik_fuente select {q(clave)}, coalesce((select id from nueva), (select id from existente));""")
sql.append(f"""
with existente as (select id from fuente_documento where titulo = 'Frases modelo de Aikukisna alineadas al currículo de Inglés del MINED'),
nueva as (
  insert into fuente_documento (titulo, autor, anio, institucion, url, licencia, nota_uso)
  select 'Frases modelo de Aikukisna alineadas al currículo de Inglés del MINED', 'Equipo Aikukisna', 2026, 'Aikukisna', null,
         'Propia (Aikukisna)', 'Frases y glosas escritas por el equipo siguiendo los temas oficiales; pendientes de revisión docente.'
  where not exists (select 1 from existente) returning id)
insert into aik_fuente select 'propia', coalesce((select id from nueva), (select id from existente));""")

NOTA_FRASE = 'Frase escrita por el equipo de Aikukisna para el tema oficial "{oficial}" ({libro}); no es texto del libro. Pendiente de revisión docente.'

for n, lec in enumerate(datos['lecciones']):
    fuente_libro = f"(select id from aik_fuente where clave = {q(lec['libro'])})"
    propia = "(select id from aik_fuente where clave = 'propia')"
    libro = datos['fuentes'][lec['libro']]
    var = f'l{n}'
    sql.append(f'\n-- {lec["unidad"]} · {lec["oficial"]}')
    if 'existente' in lec:
        sql.append(f"create temp table {var} on commit drop as select {int(lec['existente'])} as id;")
    else:
        sql.append(f"""create temp table {var} on commit drop as
  with existente as (select id from leccion where idioma_meta_id = {INGLES} and titulo = {q(lec['titulo'])}),
  nueva as (insert into leccion (titulo, capitulo_numero, nivel, categoria_id, idioma_meta_id)
            select {q(lec['titulo'])}, {lec['capitulo']}, {lec['nivel']}, {lec['categoria']}, {INGLES}
            where not exists (select 1 from existente) returning id)
  select coalesce((select id from nueva), (select id from existente)) as id;""")
    for en, es in lec.get('palabras', []):
        sql.append(f"""with p as (select pg_temp.aik_palabra({INGLES}, {q(en)}, {propia}) en, pg_temp.aik_palabra({ESPANOL}, {q(es)}, {propia}) es)
select pg_temp.aik_traduccion(en, es, {propia}) from p;
insert into leccion_palabra (leccion_id, palabra_id) select (select id from {var}), pg_temp.aik_palabra({INGLES}, {q(en)}, {propia}) on conflict do nothing;""")
    for en, es in lec.get('frases', []):
        nota = NOTA_FRASE.format(oficial=lec['oficial'], libro=libro['titulo'])
        sql.append(f"""with o as (
  insert into oracion_ejemplo (texto_origen, texto_destino, leccion_id, fuente_id, idioma_origen_id, idioma_destino_id,
                               estado_validacion, nota_validacion, created_at, updated_at)
  select {q(en)}, {q(es)}, (select id from {var}), {propia}, {INGLES}, {ESPANOL}, 'importada', {q(nota)}, now(), now()
  where not exists (select 1 from oracion_ejemplo where texto_origen = {q(en)} and leccion_id = (select id from {var}))
  returning id)
insert into leccion_oracion (leccion_id, oracion_id, tipo_vinculo, nota, created_at)
select (select id from {var}), id, 'principal', 'Frase modelo alineada al MINED', now() from o;""")
    corr = 'tematica' if 'existente' in lec else 'directa'
    sql.append(f"""insert into evidencia_curricular_leccion (leccion_id, etapa_codigo, fuente_id, grado, asignatura_area, unidad_oficial,
    contenido_oficial, actividad_aikukisna, pagina_seccion, tipo_correspondencia, aplica_sear, estado_validacion, observacion,
    naturaleza_aplicacion, es_texto_oficial_literal, created_at, updated_at, lengua_aplicacion_id)
select (select id from {var}), {q(libro['etapa'])}, {fuente_libro}, {libro['grado']}, 'Lengua Extranjera (Inglés)', {q(lec['unidad'])},
    {q(lec['oficial'])}, 'Tarjetas de vocabulario y quiz con frases modelo del tema', {q('p. ' + str(lec['pagina']))}, {q(corr)}, false,
    'documentada', {q(lec.get('observacion'))}, 'curriculo_nacional', false, now(), now(), {INGLES}
where not exists (select 1 from evidencia_curricular_leccion where leccion_id = (select id from {var}) and fuente_id = {fuente_libro}
                  and contenido_oficial = {q(lec['oficial'])});""")

sql.append('commit;')
print('\n'.join(sql))
