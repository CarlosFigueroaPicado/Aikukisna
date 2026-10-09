"""Aplica textos de cultura reescritos a la semilla offline y genera el SQL para Supabase.

No vuelve a descargar Supabase (el tráfico del plan gratuito es limitado): edita solo esas filas de
app/src/main/assets/supabase_completa.ndjson.gzip. Después hay que subir VERSION_REPLICA en
SembradorReplicaSupabase.kt para que los teléfonos recarguen la réplica.

Uso: python scripts/aplicar_cultura_reescrita.py scripts/datos/cultura_reescrita_2026_10_08.json
Salida: la semilla actualizada y <archivo>.sql junto al JSON.
"""
import gzip
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

SEMILLA = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "assets" / "supabase_completa.ndjson.gzip"


def main():
    origen = Path(sys.argv[1])
    textos = {int(k): v for k, v in json.loads(origen.read_text(encoding="utf-8")).items() if not k.startswith("_")}
    ahora = datetime.now(timezone.utc).isoformat()

    lineas, cambiadas = [], set()
    with gzip.open(SEMILLA, "rt", encoding="utf-8") as f:
        for linea in f:
            fila = json.loads(linea)
            datos = fila["json"]
            if fila.get("tabla") == "cultura_contenido" and datos.get("id") in textos:
                datos["contenido"] = textos[datos["id"]]
                if "updated_at" in datos:
                    datos["updated_at"] = ahora
                linea = json.dumps(fila, ensure_ascii=False) + "\n"
                cambiadas.add(datos["id"])
            lineas.append(linea)
    faltan = sorted(set(textos) - cambiadas)
    if faltan:
        raise SystemExit(f"No están en la semilla: {faltan}")
    with gzip.open(SEMILLA, "wt", encoding="utf-8") as f:
        f.writelines(lineas)

    sql = [f"update public.cultura_contenido set contenido = $t${texto}$t$, updated_at = now() where id = {i};"
           for i, texto in sorted(textos.items())]
    destino = origen.with_suffix(".sql")
    destino.write_text("\n".join(sql) + "\n", encoding="utf-8")
    print(f"semilla: {len(cambiadas)} filas actualizadas; SQL en {destino}")


if __name__ == "__main__":
    main()
