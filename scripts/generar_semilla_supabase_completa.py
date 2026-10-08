#!/usr/bin/env python3
"""Genera app/src/main/assets/supabase_completa.ndjson.gzip desde Supabase.

No incorpora tablas privadas de otros usuarios. Usa SUPABASE_SERVICE_ROLE_KEY solo
si está disponible en el entorno; nunca escribe esa clave dentro del proyecto.
"""
from __future__ import annotations
import gzip, json, os, sys, urllib.error, urllib.parse, urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
LOCAL = ROOT / "local.properties"
OUT = ROOT / "app/src/main/assets/supabase_completa.ndjson.gzip"
PAGE = 1000

TABLES = {
    "idioma": ["id"], "categoria": ["id"], "fuente_documento": ["id"],
    "palabra": ["id"], "palabra_canonica": ["palabra_id"],
    "palabra_fuente": ["palabra_id", "fuente_id"], "acepcion": ["id"],
    "traduccion": ["id"], "traduccion_acepcion": ["id"],
    "traduccion_fuente": ["traduccion_id", "fuente_id", "tipo_vinculo"],
    "variante_palabra": ["id"], "expresion": ["id"],
    "traduccion_expresion": ["id"],
    "expresion_contexto_cultural": ["expresion_id", "cultura_id", "tipo_relacion"],
    "oracion_ejemplo": ["id"], "regla_gramatical": ["id"],
    "regla_pronunciacion": ["id"], "ejemplo_regla_gramatical": ["id"],
    "cultura_contenido": ["id"], "leccion": ["id"],
    "leccion_palabra": ["leccion_id", "palabra_id"],
    "leccion_expresion": ["leccion_id", "expresion_id", "tipo_vinculo"],
    "leccion_oracion": ["leccion_id", "oracion_id", "tipo_vinculo"],
    "leccion_fuente": ["leccion_id", "fuente_id", "tipo_vinculo"],
    "leccion_cultura": ["leccion_id", "cultura_id", "tipo_vinculo"],
    "leccion_regla_gramatical": ["leccion_id", "regla_id"],
    "leccion_regla_pronunciacion": ["leccion_id", "regla_id"],
    "alineacion_curricular": ["id"], "mundo_gamificado": ["codigo"],
    "leccion_experiencia_gamificada": ["leccion_id"], "actividad_leccion": ["id"],
    "actividad_recurso": ["actividad_id", "tipo_recurso", "recurso_id"],
    "etapa_ruta_curricular": ["codigo"], "leccion_ruta_curricular": ["leccion_id"],
    "evidencia_curricular_leccion": ["id"], "logro": ["id"],
    "tuki_respuesta_sistema": ["id"],
}

# No se exportan: usuario, progreso_leccion, palabra_favorita, logro_desbloqueado,
# memoria_tuki, super_administrador, revision_linguistica. Son privados/administrativos.

def properties() -> dict[str, str]:
    out = {}
    for line in LOCAL.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        out[k.strip()] = v.strip()
    return out


def request_rows(url: str, key: str, table: str, offset: int) -> list[dict]:
    q = urllib.parse.urlencode({"select": "*", "offset": offset, "limit": PAGE})
    req = urllib.request.Request(
        f"{url}/rest/v1/{urllib.parse.quote(table)}?{q}",
        headers={"apikey": key, "Authorization": f"Bearer {key}", "Accept": "application/json"},
    )
    with urllib.request.urlopen(req, timeout=120) as r:
        return json.loads(r.read().decode("utf-8"))


def epoch_ms(row: dict) -> int:
    # El valor exacto se conserva dentro de json; 0 es válido para tablas sin updated_at.
    return 0


def main() -> int:
    props = properties()
    url = props["SUPABASE_URL"].rstrip("/")
    key = os.environ.get("SUPABASE_SERVICE_ROLE_KEY") or props.get("SUPABASE_ANON_KEY")
    if not key:
        raise SystemExit("Falta SUPABASE_SERVICE_ROLE_KEY o SUPABASE_ANON_KEY")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    previas = filas_previas_por_tabla()
    temporal = OUT.with_suffix(OUT.suffix + ".tmp")
    total = 0
    with gzip.open(temporal, "wt", encoding="utf-8", newline="\n") as gz:
        for table, pk in TABLES.items():
            try:
                filas = descargar_tabla(url, key, table, pk)
            except urllib.error.HTTPError as e:
                # Sin service role, RLS impide leer algunas tablas: se conservan las del archivo anterior.
                if table not in previas:
                    temporal.unlink(missing_ok=True)
                    raise SystemExit(f"{table}: HTTP {e.code} y no hay copia anterior; usa SUPABASE_SERVICE_ROLE_KEY")
                filas = previas[table]
                print(f"{table}: HTTP {e.code}, se conservan {len(filas)} filas anteriores")
            else:
                print(f"{table}: {len(filas)}")
            for linea in filas:
                gz.write(linea)
                gz.write("\n")
            total += len(filas)
    temporal.replace(OUT)
    print(f"TOTAL: {total}")
    print(OUT)
    return 0


def descargar_tabla(url: str, key: str, table: str, pk: list[str]) -> list[str]:
    filas, offset = [], 0
    while True:
        rows = request_rows(url, key, table, offset)
        for row in rows:
            clave = "|".join(str(row[c]) for c in pk)
            filas.append(json.dumps({"tabla": table, "clave": clave, "json": row, "updated_at_epoch_ms": epoch_ms(row)}, ensure_ascii=False, separators=(",", ":")))
        offset += len(rows)
        if len(rows) < PAGE:
            return filas


def filas_previas_por_tabla() -> dict[str, list[str]]:
    if not OUT.exists():
        return {}
    previas: dict[str, list[str]] = {}
    with gzip.open(OUT, "rt", encoding="utf-8") as gz:
        for linea in gz:
            linea = linea.rstrip("\n")
            if linea:
                previas.setdefault(json.loads(linea)["tabla"], []).append(linea)
    return previas


if __name__ == "__main__":
    raise SystemExit(main())
