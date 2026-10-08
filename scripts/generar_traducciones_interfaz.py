"""Genera values-b+miq y values-b+bzk desde scripts/datos/traduccion_interfaz_*.json y la hoja de revisión.

Uso: python scripts/generar_traducciones_interfaz.py app/src/main/res scripts/datos docs/traduccion_interfaz_revision.csv
"""
import csv, json, os, re, sys

RES, S, HOJA = sys.argv[1], sys.argv[2], sys.argv[3]
base = open(os.path.join(RES, 'values', 'strings.xml'), encoding='utf-8').read()
es = {k: v for k, v in re.findall(r'<string name="([^"]+)"[^>]*>(.*?)</string>', base)}


def desescapar(v):
    return v.replace("\\'", "'").replace('\\"', '"').replace('&lt;', '<').replace('&gt;', '>').replace('&amp;', '&')


def escapar(v):
    v = v.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;').replace("'", "\\'").replace('"', '\\"')
    return '\\' + v if v[:1] in '@?' else v


marcas = lambda t: sorted(re.findall(r'%\d\$s', t))
traducciones = {}
for codigo in ('miq', 'bzk', 'en'):
    datos = json.load(open(os.path.join(S, f'traduccion_interfaz_{codigo}.json'), encoding='utf-8'))
    errores = [k for k in datos if k not in es]
    if errores:
        sys.exit(f'{codigo}: claves que no existen en español: {errores}')
    malas = [k for k, v in datos.items() if marcas(v) != marcas(desescapar(es[k]))]
    if malas:
        sys.exit(f'{codigo}: marcadores distintos en {malas}')
    no_latinas = [k for k, v in datos.items() if re.search(r'[Ѐ-ӿ]', v)]
    if no_latinas:
        sys.exit(f'{codigo}: caracteres no latinos en {no_latinas}')
    traducciones[codigo] = datos
    carpeta = os.path.join(RES, f'values-b+{codigo}')
    os.makedirs(carpeta, exist_ok=True)
    with open(os.path.join(carpeta, 'strings.xml'), 'w', encoding='utf-8') as f:
        f.write('<?xml version="1.0" encoding="utf-8"?>\n')
        if codigo != 'en':
            f.write('<!-- BORRADOR pendiente de revisión por hablantes nativos. Lo que falta se muestra en español. -->\n')
        f.write('<resources>\n')
        for k in es:
            if k in datos:
                formato = ' formatted="false"' if '%' in datos[k] and not re.search(r'%\d\$s', datos[k]) else ''
                f.write(f'    <string name="{k}"{formato}>{escapar(datos[k])}</string>\n')
        f.write('</resources>\n')
    print(codigo, len(datos), 'de', len(es))

with open(HOJA, 'w', encoding='utf-8-sig', newline='') as f:
    w = csv.writer(f)
    w.writerow(['prioridad', 'clave', 'español', 'miskitu_borrador', 'miskitu_corregido', 'kriol_borrador', 'kriol_corregido', 'ingles', 'observaciones'])
    for k, v in es.items():
        if k == 'app_name':
            continue
        # Primero lo que, mal traducido, impide usar la app o lleva a borrar algo.
        critica = any(p in k for p in ('sesion', 'contrasena', 'login', 'register', 'eliminar', 'permiso', 'no_se_pudo', 'error', 'cancelar', 'terminos'))
        w.writerow(['alta' if critica else 'normal', k, desescapar(v), traducciones['miq'].get(k, ''), '', traducciones['bzk'].get(k, ''), '', traducciones['en'].get(k, ''), ''])
print('hoja', HOJA)
