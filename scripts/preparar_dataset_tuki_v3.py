"""Conjunto para Tuki v3: se entrena PARTIENDO DEL MODELO v2 (que ya traduce bien) para que aprenda a conversar
como profesor sin olvidar la traducción.

Mezcla:
  - tutorías de modelos/traduccion/gemma_tutor (preparar_dataset_tuki_tutor.py), con el prompt completo de la app;
  - repaso de traducción: una muestra estratificada por dirección del conjunto con el que se entrenó el v2.
La prueba es la MISMA del v2 (modelos/traduccion/gemma/prueba.jsonl), para comparar chrF++ directamente.
También se guardan 40 tutorías de validación en muestras_tutoria.jsonl para leer cómo conversa.

Uso:    python scripts/preparar_dataset_tuki_v3.py [--tutorias 6000] [--traducciones 24000]
Salida: modelos/traduccion/gemma_v3/{entrenamiento,validacion,prueba,muestras_tutoria}.jsonl, informe.json y
        gemma_v3_dataset.zip (lo que se sube a Kaggle).
"""
import argparse
import json
import random
import zipfile
from collections import Counter, defaultdict
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent / "modelos" / "traduccion"
V2, TUTOR, SALIDA = RAIZ / "gemma", RAIZ / "gemma_tutor", RAIZ / "gemma_v3"


def leer(ruta: Path) -> list[dict]:
    return [json.loads(l) for l in ruta.read_text(encoding="utf-8").splitlines() if l.strip()]


def tutorias(filas: list[dict]) -> list[dict]:
    # Las tutorías no traen dirección: se marcan para que la evaluación de traducción no las mezcle.
    return [{"prompt": f["prompt"], "respuesta": f["respuesta"], "direccion": "tutoria", "tipo": "tutoria",
             "referencia": ""} for f in filas if "direccion" not in f]


def estratificar(filas: list[dict], total: int, azar: random.Random) -> list[dict]:
    """Muestra proporcional por dirección, con al menos 1 500 por dirección si las hay."""
    grupos = defaultdict(list)
    for f in filas:
        grupos[f["direccion"]].append(f)
    cuota = {d: max(min(1500, len(g)), round(total * len(g) / len(filas))) for d, g in grupos.items()}
    muestra = []
    for d, g in grupos.items():
        muestra += azar.sample(g, min(cuota[d], len(g)))
    azar.shuffle(muestra)
    return muestra[:total]


def escribir(nombre: str, filas: list[dict]) -> None:
    (SALIDA / nombre).write_text("".join(json.dumps(f, ensure_ascii=False) + "\n" for f in filas), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--tutorias", type=int, default=6000)
    parser.add_argument("--traducciones", type=int, default=24000)
    args = parser.parse_args()
    azar = random.Random(20261007)
    SALIDA.mkdir(parents=True, exist_ok=True)

    tutor_entrenamiento = tutorias(leer(TUTOR / "entrenamiento.jsonl"))
    tutor_validacion = tutorias(leer(TUTOR / "validacion.jsonl"))
    entrenamiento = (azar.sample(tutor_entrenamiento, min(args.tutorias, len(tutor_entrenamiento)))
                     + estratificar(leer(V2 / "entrenamiento.jsonl"), args.traducciones, azar))
    azar.shuffle(entrenamiento)
    azar.shuffle(tutor_validacion)
    validacion = tutor_validacion[:200] + azar.sample(leer(V2 / "validacion.jsonl"), 200)
    muestras = tutor_validacion[200:240]

    escribir("entrenamiento.jsonl", entrenamiento)
    escribir("validacion.jsonl", validacion)
    escribir("prueba.jsonl", leer(V2 / "prueba.jsonl"))
    escribir("muestras_tutoria.jsonl", muestras)
    informe = {"base": "modelo v2 (gemma_aikukisna)", "entrenamiento": len(entrenamiento),
               "por_direccion": dict(Counter(f["direccion"] for f in entrenamiento).most_common()),
               "validacion": len(validacion), "muestras_tutoria": len(muestras)}
    (SALIDA / "informe.json").write_text(json.dumps(informe, ensure_ascii=False, indent=1), encoding="utf-8")
    with zipfile.ZipFile(SALIDA / "gemma_v3_dataset.zip", "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        for nombre in ("entrenamiento.jsonl", "validacion.jsonl", "prueba.jsonl", "muestras_tutoria.jsonl", "informe.json"):
            z.write(SALIDA / nombre, nombre)
    print(json.dumps(informe, ensure_ascii=False))


if __name__ == "__main__":
    main()
