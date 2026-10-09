"""Convierte el corpus aprobado de traducción al formato de conversación de Gemma 3.

Entrada (generada por preparar_corpus_traduccion.py):
    modelos/traduccion/corpus/{entrenamiento,validacion,prueba,lexico_documentado}.jsonl

Salida:
    modelos/traduccion/gemma/{entrenamiento,validacion,prueba}.jsonl
    modelos/traduccion/gemma/informe.json

Cada ejemplo usa exactamente la instrucción que la app envía al modelo en
GemmaTraductorLocal (PLANTILLA), para que lo aprendido se aplique tal cual.
Las particiones se respetan: el léxico solo se agrega al entrenamiento y se
excluyen las palabras que aparecen en validación o prueba.

Uso:
    python scripts/preparar_dataset_gemma.py [--max-lexico 60000] [--repetir-oraciones 3]
"""
import argparse
import json
import random
import re
import unicodedata
from collections import Counter
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
CORPUS = RAIZ / "modelos" / "traduccion" / "corpus"
SALIDA = RAIZ / "modelos" / "traduccion" / "gemma"

# Deben coincidir con GemmaTraductorLocal.NOMBRES_AJUSTADO en la app.
NOMBRES = {1: "miskito", 2: "español", 3: "inglés kriol", 4: "inglés"}
INSTRUCCION = "Traduce del {origen} al {destino}. Responde solo con la traducción."


def limpiar(texto: str) -> str:
    texto = unicodedata.normalize("NFC", texto).strip()
    texto = re.sub(r"\s+", " ", texto)
    texto = texto.rstrip(" .,;:")
    if texto.count(")") > texto.count("("):
        texto = texto.replace(")", "").strip()
    return texto


def valido(origen: str, destino: str) -> bool:
    if not origen or not destino or origen.lower() == destino.lower():
        return False
    # Definiciones largas del diccionario ("60 kat kulki ba piua" = "minuto") no son traducciones.
    if len(origen) > 160 or len(destino) > 160:
        return False
    return not re.search(r"https?://|\[|\]|acepci[oó]n t[eé]cnica", origen + destino, re.IGNORECASE)


def ejemplo(registro: dict) -> dict | None:
    origen, destino = limpiar(registro["origen"]), limpiar(registro["destino"])
    if not valido(origen, destino):
        return None
    o, d = registro["idioma_origen_id"], registro["idioma_destino_id"]
    instruccion = INSTRUCCION.format(origen=NOMBRES[o], destino=NOMBRES[d])
    return {
        "prompt": f"{instruccion}\n\n{origen}",
        "respuesta": destino,
        "direccion": f"{o}_a_{d}",
        "tipo": registro.get("tipo", ""),
        "referencia": registro.get("referencia", ""),
    }


def leer(nombre: str) -> list[dict]:
    with open(CORPUS / nombre, encoding="utf8") as archivo:
        return con_inversos([json.loads(linea) for linea in archivo if linea.strip()])


def con_inversos(registros: list[dict]) -> list[dict]:
    """Agrega el par en sentido contrario cuando falta (Wiktionary y Tatoeba se guardaron solo ES→EN).

    El inverso queda en la misma partición que el original, así que no filtra validación ni prueba.
    """
    def clave(o, d, a, b):
        return (o, d, limpiar(a).lower(), limpiar(b).lower())

    existentes = {
        clave(r["idioma_origen_id"], r["idioma_destino_id"], r["origen"], r["destino"]) for r in registros
    }
    inversos = []
    for r in registros:
        k = clave(r["idioma_destino_id"], r["idioma_origen_id"], r["destino"], r["origen"])
        if k not in existentes:
            existentes.add(k)
            inversos.append({
                **r,
                "origen": r["destino"], "destino": r["origen"],
                "idioma_origen_id": r["idioma_destino_id"], "idioma_destino_id": r["idioma_origen_id"],
                "referencia": f"{r.get('referencia', '')}:inverso",
            })
    return registros + inversos


def escribir(nombre: str, filas: list[dict]) -> None:
    with open(SALIDA / nombre, "w", encoding="utf8") as archivo:
        for fila in filas:
            archivo.write(json.dumps(fila, ensure_ascii=False) + "\n")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--max-lexico", type=int, default=60000,
                        help="Máximo de pares de palabras del léxico (limita el tiempo de entrenamiento).")
    parser.add_argument("--repetir-oraciones", type=int, default=3,
                        help="Veces que se repite cada frase u oración: hay muchas menos que palabras.")
    parser.add_argument("--semilla", type=int, default=20261004)
    args = parser.parse_args()
    azar = random.Random(args.semilla)
    SALIDA.mkdir(parents=True, exist_ok=True)

    validacion = [e for e in map(ejemplo, leer("validacion.jsonl")) if e]
    prueba = [e for e in map(ejemplo, leer("prueba.jsonl")) if e]
    reservados = {limpiar(r[lado]).lower() for r in leer("validacion.jsonl") + leer("prueba.jsonl") for lado in ("origen", "destino")}

    frases = [e for e in map(ejemplo, leer("entrenamiento.jsonl")) if e]
    lexico = [
        e for e in map(ejemplo, leer("lexico_documentado.jsonl"))
        if e and e["prompt"].split("\n\n", 1)[1].lower() not in reservados
    ]
    # Se reparte el cupo del léxico de forma proporcional, pero cada dirección conserva al menos
    # todo lo que tiene si es pequeña (Kriol), para no dejarla fuera.
    por_direccion: dict[str, list[dict]] = {}
    for e in lexico:
        por_direccion.setdefault(e["direccion"], []).append(e)
    total_lexico = sum(len(v) for v in por_direccion.values())
    lexico_elegido = []
    for direccion, filas in sorted(por_direccion.items()):
        azar.shuffle(filas)
        cupo = max(min(len(filas), 3000), int(args.max_lexico * len(filas) / total_lexico))
        lexico_elegido += filas[:cupo]

    entrenamiento = frases * args.repetir_oraciones + lexico_elegido
    azar.shuffle(entrenamiento)

    escribir("entrenamiento.jsonl", entrenamiento)
    escribir("validacion.jsonl", validacion)
    escribir("prueba.jsonl", prueba)
    informe = {
        "plantilla": INSTRUCCION,
        "nombres_idiomas": NOMBRES,
        "entrenamiento": len(entrenamiento),
        "frases_unicas": len(frases),
        "lexico_usado": len(lexico_elegido),
        "validacion": len(validacion),
        "prueba": len(prueba),
        "entrenamiento_por_direccion": dict(Counter(e["direccion"] for e in entrenamiento)),
        "prueba_por_direccion": dict(Counter(e["direccion"] for e in prueba)),
    }
    (SALIDA / "informe.json").write_text(json.dumps(informe, ensure_ascii=False, indent=2), encoding="utf8")
    print(json.dumps(informe, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
