#!/usr/bin/env python3
"""Ajusta un modelo maestro ByT5 con un par de idiomas validado."""

from __future__ import annotations

import argparse
import json
import random
import math
from collections import Counter
from pathlib import Path

from preparar_corpus_traduccion import clave_textual


RAIZ = Path(__file__).resolve().parents[1]
CORPUS = RAIZ / "modelos/traduccion/corpus"
SALIDA = RAIZ / "modelos/traduccion/modelos"
MODELO_BASE = "google/byt5-small"


def leer_jsonl(ruta: Path, origen: int, destino: int, bidireccional: bool = True) -> list[dict]:
    filas = []
    with ruta.open("r", encoding="utf-8") as entrada:
        for linea in entrada:
            fila = json.loads(linea)
            direccion = (fila["idioma_origen_id"], fila["idioma_destino_id"])
            if direccion == (origen, destino) or (bidireccional and direccion == (destino, origen)):
                if fila["estado_validacion"] not in {"documentada", "validada"} and not (
                    fila["estado_validacion"] == "importada" and fila.get("validacion_humana") is True
                ):
                    continue
                filas.append(fila)
    return filas


def clave_par(fila: dict) -> tuple[str, str]:
    return tuple(sorted((
        f"{fila['idioma_origen_id']}:{clave_textual(fila['origen'])}",
        f"{fila['idioma_destino_id']}:{clave_textual(fila['destino'])}",
    )))


def preparar_datos(corpus: Path, origen: int, destino: int, bidireccional: bool) -> tuple[dict, list, dict]:
    if origen == destino or origen not in {1, 2, 3, 4} or destino not in {1, 2, 3, 4}:
        raise ValueError("Selecciona dos idiomas distintos entre 1, 2, 3 y 4.")
    conjuntos = {
        nombre: leer_jsonl(corpus / f"{nombre}.jsonl", origen, destino, bidireccional)
        for nombre in ("entrenamiento", "validacion", "prueba")
    }
    direcciones = [(origen, destino)] + ([(destino, origen)] if bidireccional else [])
    conteos = {
        nombre: Counter((fila["idioma_origen_id"], fila["idioma_destino_id"]) for fila in filas)
        for nombre, filas in conjuntos.items()
    }
    faltantes = [
        f"{nombre}: {idioma_origen} -> {idioma_destino}"
        for nombre, conteo in conteos.items()
        for idioma_origen, idioma_destino in direcciones
        if not conteo[(idioma_origen, idioma_destino)]
    ]
    claves = {nombre: {clave_par(fila) for fila in filas} for nombre, filas in conjuntos.items()}
    nombres = list(claves)
    for indice, nombre in enumerate(nombres):
        for otro in nombres[indice + 1:]:
            if claves[nombre] & claves[otro]:
                raise ValueError(f"Hay equivalencias repetidas entre {nombre} y {otro}, incluso en sentido inverso.")
    reservadas = claves["validacion"] | claves["prueba"]
    lexico = [
        fila for fila in leer_jsonl(corpus / "lexico_documentado.jsonl", origen, destino, bidireccional)
        if clave_par(fila) not in reservadas and clave_par(fila) not in claves["entrenamiento"]
    ]
    informe = {
        "bidireccional": bidireccional,
        "direcciones": [f"{inicio}_a_{fin}" for inicio, fin in direcciones],
        "particiones": {
            nombre: {f"{inicio}_a_{fin}": conteo[(inicio, fin)] for inicio, fin in direcciones}
            for nombre, conteo in conteos.items()
        },
        "lexico_auxiliar_disponible": len(lexico),
        "faltantes": faltantes,
        "listo_para_entrenar": not faltantes,
        "apto_para_produccion": False,
    }
    return conjuntos, lexico, informe


def texto_entrada(fila: dict) -> str:
    return f"traducir {fila['idioma_origen']} a {fila['idioma_destino']}: {fila['origen']}"


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--origen", type=int, required=True)
    parser.add_argument("--destino", type=int, required=True)
    parser.add_argument("--corpus", type=Path, default=CORPUS)
    parser.add_argument("--salida", type=Path, default=SALIDA)
    parser.add_argument("--modelo-base", default=MODELO_BASE)
    parser.add_argument("--epocas", type=float, default=8.0)
    parser.add_argument("--semilla", type=int, default=20260926)
    parser.add_argument("--proporcion-lexico", type=float, default=0.20)
    parser.add_argument("--bidireccional", action=argparse.BooleanOptionalAction, default=True)
    parser.add_argument("--verificar-corpus", action="store_true")
    parser.add_argument("--ensayo-cpu", action="store_true", help="Prueba técnica de 20 pasos con un modelo pequeño desde cero; no apto para traducir en producción.")
    parser.add_argument("--lote", type=int, default=4)
    parser.add_argument("--acumulacion", type=int, default=8)
    parser.add_argument("--ahorrar-memoria", action="store_true")
    parser.add_argument("--optimizador", choices=("adamw_torch", "adafactor"), default="adamw_torch")
    args = parser.parse_args()

    if not 0 <= args.proporcion_lexico <= 1 or args.epocas <= 0:
        parser.error("La proporción de léxico debe estar entre 0 y 1 y las épocas deben ser positivas.")
    if args.lote < 1 or args.acumulacion < 1:
        parser.error("El lote y la acumulación deben ser positivos.")
    try:
        conjuntos, lexico, informe = preparar_datos(args.corpus, args.origen, args.destino, args.bidireccional)
    except ValueError as exc:
        parser.error(str(exc))
    if args.verificar_corpus:
        print(json.dumps(informe, ensure_ascii=False, indent=2))
        return 0 if informe["listo_para_entrenar"] else 2
    if not informe["listo_para_entrenar"]:
        parser.error("Faltan datos para: " + "; ".join(informe["faltantes"]))

    try:
        import torch
        import psutil
        from datasets import Dataset
        from sacrebleu.metrics import CHRF
        from transformers import (
            AutoModelForSeq2SeqLM,
            AutoTokenizer,
            AutoConfig,
            DataCollatorForSeq2Seq,
            Seq2SeqTrainer,
            Seq2SeqTrainingArguments,
            set_seed,
            ByT5Tokenizer,
            T5Config,
            T5ForConditionalGeneration,
        )
    except ImportError as exc:
        raise SystemExit(
            "Faltan dependencias. Instala scripts/requirements-traduccion.txt en un entorno aislado."
        ) from exc

    entrenamiento = conjuntos["entrenamiento"]
    validacion = conjuntos["validacion"]
    prueba = conjuntos["prueba"]

    rng = random.Random(args.semilla)
    maximo_lexico = min(len(lexico), round(len(entrenamiento) * args.proporcion_lexico))
    entrenamiento = entrenamiento + rng.sample(lexico, maximo_lexico)
    rng.shuffle(entrenamiento)

    if args.ensayo_cpu:
        def muestra_por_direccion(filas: list[dict], limite: int) -> list[dict]:
            muestra = []
            for direccion in informe["direcciones"]:
                inicio, fin = map(int, direccion.split("_a_"))
                candidatas = [fila for fila in filas if (fila["idioma_origen_id"], fila["idioma_destino_id"]) == (inicio, fin)]
                muestra.extend(rng.sample(candidatas, min(limite, len(candidatas))))
            return muestra

        entrenamiento = muestra_por_direccion(entrenamiento, 32)
        validacion = muestra_por_direccion(validacion, 4)
        prueba = muestra_por_direccion(prueba, 4)

    idiomas = {
        fila["idioma_origen_id"]: fila["idioma_origen"]
        for fila in entrenamiento + validacion + prueba
    }
    idiomas.update(
        {
            fila["idioma_destino_id"]: fila["idioma_destino"]
            for fila in entrenamiento + validacion + prueba
        }
    )
    nombre_origen = idiomas[args.origen]
    nombre_destino = idiomas[args.destino]
    nombre_salida = f"{min(args.origen, args.destino)}_y_{max(args.origen, args.destino)}" if args.bidireccional else f"{args.origen}_a_{args.destino}"
    if args.ensayo_cpu:
        nombre_salida += "_ensayo_cpu"
    salida = args.salida / nombre_salida
    if salida.exists() and any(salida.iterdir()):
        parser.error(f"La salida ya contiene archivos; selecciona otra carpeta con --salida: {salida}")
    salida.mkdir(parents=True, exist_ok=True)

    set_seed(args.semilla)
    if args.ensayo_cpu:
        torch.set_num_threads(2)
        tokenizer = ByT5Tokenizer()
        model = T5ForConditionalGeneration(T5Config(
            vocab_size=len(tokenizer), d_model=128, d_kv=32, d_ff=256,
            num_layers=2, num_decoder_layers=2, num_heads=4,
            pad_token_id=tokenizer.pad_token_id, eos_token_id=tokenizer.eos_token_id,
            decoder_start_token_id=tokenizer.pad_token_id,
        ))
    else:
        if not torch.cuda.is_available():
            config = AutoConfig.from_pretrained(args.modelo_base)
            with torch.device("meta"):
                estimador = AutoModelForSeq2SeqLM.from_config(config)
            parametros = sum(parametro.numel() for parametro in estimador.parameters())
            if args.optimizador == "adafactor":
                estados = sum(
                    math.prod(parametro.shape[:-2]) * (parametro.shape[-2] + parametro.shape[-1])
                    if parametro.ndim >= 2 else parametro.numel()
                    for parametro in estimador.parameters()
                )
                memoria_entrenamiento = parametros * 8 + estados * 4
            else:
                memoria_entrenamiento = parametros * 16
            del estimador
            minimo_estimado = memoria_entrenamiento + 2 * 1024**3
            disponible = psutil.virtual_memory().available
            if disponible < minimo_estimado:
                parser.error(
                    f"RAM libre insuficiente para este entrenamiento CPU: {disponible / 1024**3:.2f} GiB; "
                    f"reserva estimada mínima {minimo_estimado / 1024**3:.2f} GiB. "
                    "Libera memoria o utiliza otro equipo. La estimación no garantiza que todas las secuencias quepan."
                )
        tokenizer = AutoTokenizer.from_pretrained(args.modelo_base)
        model = AutoModelForSeq2SeqLM.from_pretrained(args.modelo_base)
    if args.ahorrar_memoria:
        model.config.use_cache = False

    def convertir(fila: dict) -> dict:
        entrada = tokenizer(
            texto_entrada(fila),
            max_length=256,
            truncation=True,
        )
        etiquetas = tokenizer(
            text_target=fila["destino"],
            max_length=256,
            truncation=True,
        )
        entrada["labels"] = etiquetas["input_ids"]
        return entrada

    datasets = {
        "train": Dataset.from_list(entrenamiento).map(convertir, remove_columns=list(entrenamiento[0])),
        "validation": Dataset.from_list(validacion).map(convertir, remove_columns=list(validacion[0])),
        "test": Dataset.from_list(prueba).map(convertir, remove_columns=list(prueba[0])),
    }
    chrf = CHRF(word_order=2)

    def metricas(prediccion) -> dict[str, float]:
        predicciones, etiquetas = prediccion
        etiquetas = [[token if token != -100 else tokenizer.pad_token_id for token in fila] for fila in etiquetas]
        textos_predichos = tokenizer.batch_decode(predicciones, skip_special_tokens=True)
        textos_referencia = tokenizer.batch_decode(etiquetas, skip_special_tokens=True)
        return {"chrf_pp": chrf.corpus_score(textos_predichos, [textos_referencia]).score}

    argumentos = Seq2SeqTrainingArguments(
        output_dir=str(salida),
        eval_strategy="epoch",
        save_strategy="epoch",
        logging_strategy="steps",
        logging_steps=25,
        learning_rate=2e-4,
        optim=args.optimizador,
        per_device_train_batch_size=1 if args.ensayo_cpu else args.lote,
        per_device_eval_batch_size=1 if args.ensayo_cpu else args.lote,
        gradient_accumulation_steps=1 if args.ensayo_cpu else args.acumulacion,
        gradient_checkpointing=args.ahorrar_memoria,
        dataloader_pin_memory=torch.cuda.is_available(),
        max_steps=20 if args.ensayo_cpu else -1,
        use_cpu=args.ensayo_cpu,
        num_train_epochs=args.epocas,
        predict_with_generate=True,
        generation_max_length=256,
        load_best_model_at_end=True,
        metric_for_best_model="chrf_pp",
        greater_is_better=True,
        save_total_limit=2,
        fp16=not args.ensayo_cpu and torch.cuda.is_available(),
        report_to=[],
        seed=args.semilla,
    )
    entrenador = Seq2SeqTrainer(
        model=model,
        args=argumentos,
        train_dataset=datasets["train"],
        eval_dataset=datasets["validation"],
        data_collator=DataCollatorForSeq2Seq(tokenizer=tokenizer, model=model),
        processing_class=tokenizer,
        compute_metrics=metricas,
    )
    entrenador.train()
    metricas_prueba = entrenador.evaluate(datasets["test"], metric_key_prefix="prueba")
    metricas_por_direccion = {}
    for direccion in informe["direcciones"]:
        inicio, fin = map(int, direccion.split("_a_"))
        indices = [
            indice for indice, fila in enumerate(prueba)
            if (fila["idioma_origen_id"], fila["idioma_destino_id"]) == (inicio, fin)
        ]
        evaluacion = entrenador.predict(datasets["test"].select(indices), metric_key_prefix=direccion)
        metricas_por_direccion[direccion] = evaluacion.metrics
        predicciones = tokenizer.batch_decode(evaluacion.predictions, skip_special_tokens=True)
        with (salida / f"predicciones_{direccion}.jsonl").open("w", encoding="utf-8") as archivo:
            for indice, prediccion in zip(indices, predicciones, strict=True):
                fila = prueba[indice]
                archivo.write(json.dumps({
                    "origen": fila["origen"], "referencia": fila["destino"],
                    "prediccion": prediccion, "tipo": fila["tipo"],
                    "referencia_corpus": fila["referencia"],
                    "validacion_humana_prediccion": "pendiente",
                }, ensure_ascii=False) + "\n")
    metricas_prueba["por_direccion"] = metricas_por_direccion
    entrenador.save_model(str(salida / "mejor_modelo"))
    tokenizer.save_pretrained(str(salida / "mejor_modelo"))
    (salida / "evaluacion_prueba.json").write_text(
        json.dumps(metricas_prueba, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    (salida / "configuracion_corpus.json").write_text(
        json.dumps(
            {
                "modelo_base": None if args.ensayo_cpu else args.modelo_base,
                "ensayo_tecnico_sin_preentrenamiento": args.ensayo_cpu,
                "lote": 1 if args.ensayo_cpu else args.lote,
                "acumulacion": 1 if args.ensayo_cpu else args.acumulacion,
                "ahorro_memoria": args.ahorrar_memoria,
                "optimizador": args.optimizador,
                "apto_para_produccion": False,
                "idioma_origen_id": args.origen,
                "idioma_destino_id": args.destino,
                "idioma_origen": nombre_origen,
                "idioma_destino": nombre_destino,
                "entrenamiento": len(entrenamiento),
                "validacion": len(validacion),
                "prueba": len(prueba),
                "lexico_auxiliar": maximo_lexico,
                "semilla": args.semilla,
                "cobertura": informe,
            },
            ensure_ascii=False,
            indent=2,
        ) + "\n",
        encoding="utf-8",
    )
    print(json.dumps(metricas_prueba, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
