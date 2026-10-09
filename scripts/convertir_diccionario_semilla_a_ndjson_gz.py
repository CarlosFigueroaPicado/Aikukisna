#!/usr/bin/env python3
"""Convierte la réplica exportada en JSON a NDJSON comprimido.

El archivo de entrada es un arreglo de objetos ``{"linea": "..."}``. Cada
valor ``linea`` ya contiene, sin reinterpretación, un registro completo de la
réplica: tabla, clave, json y updated_at_epoch_ms.
"""

from __future__ import annotations

import gzip
import json
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ORIGEN = ROOT / "app/src/main/assets/diccionario_semilla.json"
DESTINO = ROOT / "app/src/main/assets/supabase_completa.ndjson.gzip"


def main() -> int:
    total = 0
    tablas: Counter[str] = Counter()

    with ORIGEN.open("rt", encoding="utf-8") as entrada, gzip.open(
        DESTINO, "wt", encoding="utf-8", newline="\n", compresslevel=9
    ) as salida:
        for numero_linea, texto in enumerate(entrada, start=1):
            texto = texto.strip()
            if not texto.startswith('"linea"'):
                continue

            campo = texto.removesuffix(",")
            envoltorio = json.loads("{" + campo + "}")
            registro_texto = envoltorio["linea"]
            registro = json.loads(registro_texto)

            requeridos = {"tabla", "clave", "json", "updated_at_epoch_ms"}
            faltantes = requeridos.difference(registro)
            if faltantes:
                raise ValueError(
                    f"Línea {numero_linea}: faltan campos {sorted(faltantes)}"
                )

            salida.write(
                json.dumps(
                    registro,
                    ensure_ascii=False,
                    separators=(",", ":"),
                )
            )
            salida.write("\n")
            total += 1
            tablas[str(registro["tabla"])] += 1

    print(f"Registros: {total}")
    print(f"Tablas: {len(tablas)}")
    for tabla, cantidad in sorted(tablas.items()):
        print(f"{tabla}: {cantidad}")
    print(f"Salida: {DESTINO}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
