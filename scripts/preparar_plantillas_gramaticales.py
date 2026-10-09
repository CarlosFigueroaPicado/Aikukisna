#!/usr/bin/env python3
"""Extrae reglas productivas para convertirlas en plantillas ejecutables validadas."""

from __future__ import annotations

import gzip
import json
from collections import defaultdict
from pathlib import Path


RAIZ = Path(__file__).resolve().parents[1]
REPLICA = RAIZ / "app/src/main/assets/supabase_completa.ndjson.gzip"
SALIDA = RAIZ / "modelos/traduccion/validaciones/plantillas_gramaticales_pendientes.jsonl"


def main() -> int:
    reglas: list[dict] = []
    ejemplos: dict[int, list[dict]] = defaultdict(list)
    with gzip.open(REPLICA, "rt", encoding="utf-8") as entrada:
        for linea in entrada:
            registro = json.loads(linea)
            fila = registro["json"]
            if registro["tabla"] == "regla_gramatical":
                if fila.get("productiva") and fila.get("estado_validacion") in {"documentada", "validada"}:
                    reglas.append(fila)
            elif registro["tabla"] == "ejemplo_regla_gramatical":
                if fila.get("estado_validacion") != "rechazada":
                    ejemplos[int(fila["regla_id"])].append(fila)

    SALIDA.parent.mkdir(parents=True, exist_ok=True)
    with SALIDA.open("w", encoding="utf-8", newline="\n") as salida:
        for regla in sorted(reglas, key=lambda item: (item["idioma_id"], item["prioridad"], item["id"])):
            registro = {
                "regla_id": regla["id"],
                "idioma_id": regla["idioma_id"],
                "codigo": regla["codigo"],
                "categoria": regla["categoria"],
                "titulo": regla["titulo"],
                "descripcion": regla["descripcion"],
                "patron_documentado": regla.get("patron"),
                "aplicacion_documentada": regla.get("aplicacion"),
                "estado_original": regla["estado_validacion"],
                "ejemplos": [
                    {
                        "texto_idioma": ejemplo["texto_idioma"],
                        "traduccion_espanol": ejemplo.get("traduccion_espanol"),
                        "estado_validacion": ejemplo.get("estado_validacion"),
                        "fuente_id": ejemplo.get("fuente_id"),
                    }
                    for ejemplo in ejemplos.get(regla["id"], [])
                ],
                "decision_validador": None,
                "patron_ejecutable": None,
                "restricciones": None,
                "observaciones_validador": None,
            }
            salida.write(json.dumps(registro, ensure_ascii=False, separators=(",", ":")) + "\n")
    print(f"Reglas pendientes: {len(reglas)}")
    print(SALIDA)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
