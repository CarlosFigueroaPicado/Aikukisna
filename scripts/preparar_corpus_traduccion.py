#!/usr/bin/env python3
"""Prepara un corpus paralelo reproducible desde la réplica local de Supabase."""

from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import re
import unicodedata
from collections import Counter
from dataclasses import asdict, dataclass
from pathlib import Path


RAIZ = Path(__file__).resolve().parents[1]
REPLICA = RAIZ / "app/src/main/assets/supabase_completa.ndjson.gzip"
SALIDA = RAIZ / "modelos/traduccion/corpus"
VALIDACIONES = RAIZ / "modelos/traduccion/validaciones/aprobadas.jsonl"
ESTADOS_ENTRENABLES = {"documentada", "validada"}
# Fuentes abiertas cuyo material "importada" se acepta para entrenar: 61 = Wiktionary (kaikki.org,
# CC BY-SA), 62 = Tatoeba (CC BY 2.0 FR). Se filtraron a mano entradas ofensivas o mal alineadas.
# El estado "importada" es solo interno: en la app se muestran igual que el resto del diccionario.
FUENTES_ABIERTAS_ENTRENABLES = {61, 62}


@dataclass(frozen=True)
class Par:
    origen: str
    destino: str
    idioma_origen_id: int
    idioma_destino_id: int
    estado_validacion: str
    tipo: str
    referencia: str
    fuente_id: int | None


def normalizar(texto: str) -> str:
    texto = unicodedata.normalize("NFC", texto).strip()
    return re.sub(r"\s+", " ", texto)


def clave_textual(texto: str) -> str:
    texto = unicodedata.normalize("NFD", normalizar(texto).lower())
    texto = "".join(c for c in texto if unicodedata.category(c) != "Mn")
    return re.sub(r"[^\w]+", " ", texto).strip()


def clave_bilingue(par: Par) -> str:
    lados = sorted(
        (
            f"{par.idioma_origen_id}:{clave_textual(par.origen)}",
            f"{par.idioma_destino_id}:{clave_textual(par.destino)}",
        )
    )
    return "\u241f".join(lados)


def particion(par: Par) -> str:
    valor = int(hashlib.sha256(clave_bilingue(par).encode("utf-8")).hexdigest()[:8], 16) % 100
    if valor < 80:
        return "entrenamiento"
    if valor < 90:
        return "validacion"
    return "prueba"


def leer_replica(ruta: Path) -> dict[str, list[dict]]:
    necesarias = {
        "idioma",
        "palabra",
        "traduccion",
        "expresion",
        "traduccion_expresion",
        "oracion_ejemplo",
    }
    tablas = {nombre: [] for nombre in necesarias}
    with gzip.open(ruta, "rt", encoding="utf-8") as entrada:
        for numero, linea in enumerate(entrada, 1):
            if not linea.strip():
                continue
            envoltorio = json.loads(linea)
            tabla = envoltorio.get("tabla")
            if tabla in tablas:
                fila = envoltorio.get("json")
                if not isinstance(fila, dict):
                    raise ValueError(f"Registro inválido en la línea {numero}")
                tablas[tabla].append(fila)
    return tablas


def extraer_pares(tablas: dict[str, list[dict]]) -> list[Par]:
    expresiones = {fila["id"]: fila for fila in tablas["expresion"]}
    pares: list[Par] = []

    for fila in tablas["oracion_ejemplo"]:
        origen = normalizar(fila.get("texto_origen") or "")
        destino = normalizar(fila.get("texto_destino") or "")
        if not origen or not destino or origen == destino:
            continue
        pares.append(
            Par(
                origen=origen,
                destino=destino,
                idioma_origen_id=int(fila["idioma_origen_id"]),
                idioma_destino_id=int(fila["idioma_destino_id"]),
                estado_validacion=fila.get("estado_validacion") or "importada",
                tipo="oracion",
                referencia=f"oracion_ejemplo:{fila['id']}",
                fuente_id=fila.get("fuente_id"),
            )
        )

    for fila in tablas["traduccion_expresion"]:
        if fila.get("estado_validacion") == "rechazada":
            continue
        origen = expresiones.get(fila.get("expresion_origen_id"))
        destino = expresiones.get(fila.get("expresion_destino_id"))
        if not origen or not destino:
            continue
        texto_origen = normalizar(origen.get("texto") or "")
        texto_destino = normalizar(destino.get("texto") or "")
        if not texto_origen or not texto_destino or texto_origen == texto_destino:
            continue
        pares.append(
            Par(
                origen=texto_origen,
                destino=texto_destino,
                idioma_origen_id=int(origen["idioma_id"]),
                idioma_destino_id=int(destino["idioma_id"]),
                estado_validacion=fila.get("estado_validacion") or "importada",
                tipo="expresion",
                referencia=f"traduccion_expresion:{fila['id']}",
                fuente_id=origen.get("fuente_id") or destino.get("fuente_id"),
            )
        )
    return deduplicar(pares)


def extraer_lexico(tablas: dict[str, list[dict]]) -> list[Par]:
    palabras = {fila["id"]: fila for fila in tablas["palabra"]}
    pares: list[Par] = []
    for fila in tablas["traduccion"]:
        estado = fila.get("estado_validacion") or "importada"
        if estado == "rechazada":
            continue
        origen = palabras.get(fila.get("palabra_origen_id"))
        destino = palabras.get(fila.get("palabra_destino_id"))
        if not origen or not destino or origen.get("idioma_id") == destino.get("idioma_id"):
            continue
        texto_origen = normalizar(origen.get("texto") or "")
        texto_destino = normalizar(destino.get("texto") or "")
        if not texto_origen or not texto_destino or texto_origen == texto_destino:
            continue
        pares.append(
            Par(
                origen=texto_origen,
                destino=texto_destino,
                idioma_origen_id=int(origen["idioma_id"]),
                idioma_destino_id=int(destino["idioma_id"]),
                estado_validacion=estado,
                tipo="palabra",
                referencia=f"traduccion:{fila['id']}",
                fuente_id=fila.get("fuente_id") or origen.get("fuente_id") or destino.get("fuente_id"),
            )
        )
    return deduplicar(pares)


def deduplicar(pares: list[Par]) -> list[Par]:
    prioridad_estado = {"validada": 3, "documentada": 2, "importada": 1}
    prioridad_tipo = {"oracion": 2, "expresion": 1}
    elegidos: dict[tuple[int, int, str, str], Par] = {}
    for par in pares:
        clave = (
            par.idioma_origen_id,
            par.idioma_destino_id,
            clave_textual(par.origen),
            clave_textual(par.destino),
        )
        anterior = elegidos.get(clave)
        if anterior is None or (
            prioridad_estado.get(par.estado_validacion, 0), prioridad_tipo.get(par.tipo, 0)
        ) > (
            prioridad_estado.get(anterior.estado_validacion, 0), prioridad_tipo.get(anterior.tipo, 0)
        ):
            elegidos[clave] = par
    return sorted(
        elegidos.values(),
        key=lambda p: (p.idioma_origen_id, p.idioma_destino_id, clave_textual(p.origen), clave_textual(p.destino)),
    )


def escribir_jsonl(
    ruta: Path,
    pares: list[Par],
    idiomas: dict[int, str],
    referencias_validadas: set[str],
) -> None:
    with ruta.open("w", encoding="utf-8", newline="\n") as salida:
        for par in pares:
            registro = asdict(par)
            registro["idioma_origen"] = idiomas.get(par.idioma_origen_id, str(par.idioma_origen_id))
            registro["idioma_destino"] = idiomas.get(par.idioma_destino_id, str(par.idioma_destino_id))
            registro["validacion_humana"] = par.referencia in referencias_validadas
            salida.write(json.dumps(registro, ensure_ascii=False, separators=(",", ":")) + "\n")


def leer_validaciones(ruta: Path) -> set[str]:
    if not ruta.exists():
        return set()
    referencias: set[str] = set()
    with ruta.open("r", encoding="utf-8") as entrada:
        for numero, linea in enumerate(entrada, 1):
            if not linea.strip():
                continue
            registro = json.loads(linea)
            if registro.get("validacion") != "aprobada" or not registro.get("referencia"):
                raise ValueError(f"Validación inválida en {ruta}, línea {numero}")
            referencias.add(registro["referencia"])
    return referencias


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--replica", type=Path, default=REPLICA)
    parser.add_argument("--salida", type=Path, default=SALIDA)
    parser.add_argument("--validaciones", type=Path, default=VALIDACIONES)
    parser.add_argument("--sin-fuentes-abiertas", action="store_true",
                        help="Entrenar solo con material documentado o validado")
    args = parser.parse_args()
    fuentes_abiertas = set() if args.sin_fuentes_abiertas else FUENTES_ABIERTAS_ENTRENABLES

    tablas = leer_replica(args.replica)
    idiomas = {int(fila["id"]): normalizar(fila["nombre"]) for fila in tablas["idioma"]}
    pares = extraer_pares(tablas)
    lexico = extraer_lexico(tablas)
    referencias_validadas = leer_validaciones(args.validaciones)
    es_entrenable = lambda par: (
        par.estado_validacion in ESTADOS_ENTRENABLES
        or par.referencia in referencias_validadas
        or (par.estado_validacion == "importada" and par.fuente_id in fuentes_abiertas)
    )
    entrenables = [par for par in pares if es_entrenable(par)]
    pendientes = [par for par in pares if not es_entrenable(par)]

    args.salida.mkdir(parents=True, exist_ok=True)
    conjuntos = {nombre: [par for par in entrenables if particion(par) == nombre] for nombre in ("entrenamiento", "validacion", "prueba")}
    for nombre, elementos in conjuntos.items():
        escribir_jsonl(args.salida / f"{nombre}.jsonl", elementos, idiomas, referencias_validadas)
    escribir_jsonl(args.salida / "pendientes_validacion.jsonl", pendientes, idiomas, referencias_validadas)
    lexico_entrenable = [par for par in lexico if es_entrenable(par)]
    lexico_pendiente = [par for par in lexico if not es_entrenable(par)]
    escribir_jsonl(args.salida / "lexico_documentado.jsonl", lexico_entrenable, idiomas, referencias_validadas)
    escribir_jsonl(args.salida / "lexico_pendiente_validacion.jsonl", lexico_pendiente, idiomas, referencias_validadas)

    conteos = Counter(
        (par.idioma_origen_id, par.idioma_destino_id, par.estado_validacion, par.tipo)
        for par in pares
    )
    informe = {
        "replica": str(args.replica),
        "idiomas": idiomas,
        "total_deduplicado": len(pares),
        "total_entrenable": len(entrenables),
        "total_pendiente_validacion": len(pendientes),
        "total_lexico_documentado": len(lexico_entrenable),
        "total_lexico_pendiente_validacion": len(lexico_pendiente),
        "referencias_con_validacion_humana": len(referencias_validadas),
        "particiones": {nombre: len(elementos) for nombre, elementos in conjuntos.items()},
        "conteos": [
            {
                "idioma_origen_id": clave[0],
                "idioma_destino_id": clave[1],
                "estado_validacion": clave[2],
                "tipo": clave[3],
                "cantidad": cantidad,
            }
            for clave, cantidad in sorted(conteos.items())
        ],
    }
    (args.salida / "informe.json").write_text(
        json.dumps(informe, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(informe, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
