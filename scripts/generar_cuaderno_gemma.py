"""Genera modelos/traduccion/gemma/entrenar_gemma_aikukisna.ipynb (cuaderno para Google Colab con GPU).

El cuaderno:
  1. Ajusta Gemma 3 1B-it con LoRA usando el dataset de preparar_dataset_gemma.py.
  2. Mide chrF++ por dirección en la partición de prueba, antes y después del ajuste.
  3. Fusiona LoRA, convierte a TFLite int4 (litert-torch) y empaqueta un .task de MediaPipe.

Genera también entrenar_gemma_aikukisna_kaggle.ipynb, la misma receta adaptada a Kaggle.

Uso: python scripts/generar_cuaderno_gemma.py
"""
import json
from pathlib import Path

SALIDA = Path(__file__).resolve().parent.parent / "modelos" / "traduccion" / "gemma" / "entrenar_gemma_aikukisna.ipynb"

celdas = []


def md(texto: str) -> None:
    celdas.append({"cell_type": "markdown", "metadata": {}, "source": texto.strip("\n")})


def code(texto: str) -> None:
    celdas.append({"cell_type": "code", "metadata": {}, "execution_count": None, "outputs": [], "source": texto.strip("\n")})


# Conversión al formato del teléfono. Se comparte con generar_cuaderno_conversion_kaggle.py.
# ai-edge-torch pasó a llamarse litert-torch (el paquete viejo ya no trae "generative"), y
# build_model_1b ya no recibe kv_cache_max_len.
CONVERSION_PIP = """
!pip -q install litert-torch mediapipe
# litert-lm-builder (opcional, solo para el formato .litertlm) trae código protobuf 6, pero mediapipe
# deja instalado protobuf 5 y la importación falla con VersionError. litert-torch lo importa dentro de
# un try/except ImportError, así que basta con quitarlo para que convierta a .tflite normalmente.
!pip -q uninstall -y litert-lm-builder litert_lm_builder
import importlib.util, os, shutil
_spec = importlib.util.find_spec("litert_lm_builder")
if _spec and _spec.submodule_search_locations:
    shutil.rmtree(list(_spec.submodule_search_locations)[0])
importlib.invalidate_caches()
assert importlib.util.find_spec("litert_lm_builder") is None, "litert_lm_builder sigue instalado"
"""

CONVERSION_CODIGO = """
from huggingface_hub import hf_hub_download
from litert_torch.generative.examples.gemma3 import gemma3
from litert_torch.generative.layers import kv_cache
from litert_torch.generative.utilities import converter
from litert_torch.generative.utilities.export_config import ExportConfig

# save_pretrained solo guarda tokenizer.json; MediaPipe necesita el tokenizer.model de SentencePiece,
# que es el mismo del modelo base (el ajuste no cambia el vocabulario).
TOKENIZADOR = hf_hub_download("google/gemma-3-1b-it", "tokenizer.model", token=globals().get("HF_TOKEN"))

KV_MAX = 1536
pytorch_model = gemma3.build_model_1b(MODELO)
config = ExportConfig()
config.kvcache_layout = kv_cache.KV_LAYOUT_TRANSPOSED
config.mask_as_input = True
converter.convert_to_tflite(
    pytorch_model,
    output_path="salida_tflite",
    output_name_prefix="gemma3_aikukisna",
    prefill_seq_len=[128, 512],
    kv_cache_max_len=KV_MAX,
    quantize="dynamic_int4_block32",
    export_config=config,
)
!ls -la salida_tflite
"""

CONVERSION_BUNDLE = """
# El mediapipe que se instala en Python 3.13 ya no trae el empaquetador (mediapipe.tasks.python.genai).
# Se crea un Python 3.12 aparte con uv solo para este paso, con una versión que sí lo incluye.
!pip -q install uv
!uv venv -q -p 3.12 /tmp/mp312
!uv pip install -q --python /tmp/mp312 "mediapipe==0.10.18" sentencepiece
import glob, os, subprocess

tflite = glob.glob("salida_tflite/*.tflite")[0]
script = f'''
from mediapipe.tasks.python.genai import bundler
bundler.create_bundle(bundler.BundleConfig(
    tflite_model={tflite!r},
    tokenizer_model={TOKENIZADOR!r},
    start_token="<bos>",
    stop_tokens=["<eos>", "<end_of_turn>"],
    output_filename="gemma3-1b-aikukisna-int4.task",
))
'''
# Sin prompt_prefix/suffix: GemmaTraductorLocal y GemmaAssistantEngine ya arman los turnos.
# MPLBACKEND=Agg: el cuaderno define un backend de matplotlib que el Python aparte no conoce.
subprocess.run(["/tmp/mp312/bin/python", "-c", script], check=True, env={**os.environ, "MPLBACKEND": "Agg"})
!ls -la gemma3-1b-aikukisna-int4.task
"""


md("""
# Entrenar a Tuki en Miskito y Kriol (Gemma 3 1B + LoRA)

**Antes de empezar**
1. *Entorno de ejecución → Cambiar tipo de entorno → GPU T4* (o mejor).
2. En Hugging Face, acepta la licencia de `google/gemma-3-1b-it` y crea un token de lectura.
3. Ten a mano `gemma_dataset.zip` (contiene `entrenamiento.jsonl`, `validacion.jsonl`, `prueba.jsonl`).

El resultado final es `gemma3-1b-aikukisna-int4.task`, que se publica como Release `modelo-tuki-v2`.
Las métricas automáticas **no sustituyen** la revisión lingüística humana.
""")

code("""
!pip -q install -U "transformers>=4.50" "peft>=0.14" "accelerate>=1.3" datasets sacrebleu sentencepiece
# Colab trae torchao 0.10, que peft rechaza; el entrenamiento no lo necesita. Se desinstala DESPUÉS
# de instalar lo demás, para que ninguna dependencia lo vuelva a traer.
!pip -q uninstall -y torchao
import importlib.util
assert importlib.util.find_spec("torchao") is None, "torchao sigue instalado: reinicia la sesión y repite esta celda"
""")

code("""
from huggingface_hub import login
login()  # pega tu token de Hugging Face
""")

code("""
# Todo se guarda en Google Drive: si Colab corta la sesión (límite de GPU), el entrenamiento se
# retoma desde el último punto de guardado en vez de empezar de cero.
from google.colab import drive, files
import zipfile, os, shutil
drive.mount("/content/drive")
CARPETA = "/content/drive/MyDrive/aikukisna_gemma"
os.makedirs(CARPETA, exist_ok=True)
ZIP = f"{CARPETA}/gemma_dataset.zip"
if not os.path.exists(ZIP):
    subido = files.upload()  # selecciona gemma_dataset.zip (solo la primera vez)
    shutil.copy(next(iter(subido)), ZIP)
zipfile.ZipFile(ZIP).extractall("datos")
print(os.listdir("datos"))
""")

md("## 1. Cargar modelo y datos")

code("""
import json, random, torch
from transformers import AutoTokenizer, AutoModelForCausalLM

BASE = "google/gemma-3-1b-it"
tok = AutoTokenizer.from_pretrained(BASE)
# fp16 desborda con Gemma 3 en T4: se entrena en fp32 (1B cabe de sobra en 16 GB con LoRA).
dtype = torch.bfloat16 if torch.cuda.is_bf16_supported() else torch.float32
modelo = AutoModelForCausalLM.from_pretrained(BASE, torch_dtype=dtype, attn_implementation="eager").to("cuda")

def leer(nombre):
    with open(f"datos/{nombre}", encoding="utf8") as f:
        return [json.loads(l) for l in f if l.strip()]

entrenamiento, validacion, prueba = leer("entrenamiento.jsonl"), leer("validacion.jsonl"), leer("prueba.jsonl")
print(len(entrenamiento), len(validacion), len(prueba))
""")

code("""
# Misma plantilla que GemmaTraductorLocal en la app.
def prompt(texto):
    return f"<start_of_turn>user\\n{texto}<end_of_turn>\\n<start_of_turn>model\\n"

MAX_LEN = 160

def codificar(ej):
    p = tok(prompt(ej["prompt"]), add_special_tokens=False)["input_ids"]
    r = tok(ej["respuesta"] + "<end_of_turn>", add_special_tokens=False)["input_ids"]
    ids = ([tok.bos_token_id] + p + r)[:MAX_LEN]
    # Solo se aprende la respuesta, no la instrucción.
    etiquetas = ([-100] * (1 + len(p)) + r)[:MAX_LEN]
    return {"input_ids": ids, "labels": etiquetas}

from datasets import Dataset
ds_entrenamiento = Dataset.from_list(entrenamiento).map(codificar, remove_columns=list(entrenamiento[0].keys()))
ds_validacion = Dataset.from_list(validacion[:400]).map(codificar, remove_columns=list(validacion[0].keys()))
""")

md("## 2. Medir el modelo base (antes del ajuste)")

code("""
import sacrebleu
from collections import defaultdict

@torch.no_grad()
def traducir(textos, lote=16):
    tok.padding_side = "left"
    salidas = []
    for i in range(0, len(textos), lote):
        entrada = tok([tok.bos_token + prompt(t) for t in textos[i:i + lote]], return_tensors="pt",
                      padding=True, add_special_tokens=False).to("cuda")
        gen = modelo.generate(**entrada, max_new_tokens=64, do_sample=False)
        for fila in gen[:, entrada["input_ids"].shape[1]:]:
            salidas.append(tok.decode(fila, skip_special_tokens=True).split("<end_of_turn>")[0].strip())
    return salidas

def evaluar(ejemplos, por_direccion=60):
    grupos = defaultdict(list)
    for e in ejemplos:
        grupos[e["direccion"]].append(e)
    resultado = {}
    for direccion, filas in sorted(grupos.items()):
        filas = filas[:por_direccion]
        hip = traducir([f["prompt"] for f in filas])
        resultado[direccion] = round(sacrebleu.corpus_chrf(hip, [[f["respuesta"] for f in filas]], word_order=2).score, 1)
        print(direccion, resultado[direccion], "| ej:", filas[0]["prompt"].split("\\n\\n")[1], "→", hip[0])
    return resultado

ARCHIVO_ANTES = f"{CARPETA}/evaluacion_base.json"
if os.path.exists(ARCHIVO_ANTES):
    antes = json.load(open(ARCHIVO_ANTES))  # ya medido en una sesión anterior
else:
    antes = evaluar(prueba)
    json.dump(antes, open(ARCHIVO_ANTES, "w"), indent=2)
""")

md("## 3. Ajuste fino con LoRA")

code("""
from peft import LoraConfig, get_peft_model
from transformers import Trainer, TrainingArguments, DataCollatorForSeq2Seq

modelo.gradient_checkpointing_enable()
modelo.enable_input_require_grads()
modelo = get_peft_model(modelo, LoraConfig(
    r=16, lora_alpha=32, lora_dropout=0.05, task_type="CAUSAL_LM",
    target_modules=["q_proj", "k_proj", "v_proj", "o_proj", "gate_proj", "up_proj", "down_proj"],
))
modelo.print_trainable_parameters()

args = TrainingArguments(
    output_dir=f"{CARPETA}/lora", num_train_epochs=1, per_device_train_batch_size=16,
    # warmup_steps en vez de warmup_ratio: transformers 5 eliminó warmup_ratio (110 ≈ 3 % de ~3 700 pasos).
    gradient_accumulation_steps=2, learning_rate=2e-4, lr_scheduler_type="cosine", warmup_steps=110,
    logging_steps=50, eval_strategy="steps", eval_steps=500, save_steps=250, save_total_limit=2,
    bf16=dtype == torch.bfloat16, report_to="none",
)
entrenador = Trainer(
    model=modelo, args=args, train_dataset=ds_entrenamiento, eval_dataset=ds_validacion,
    data_collator=DataCollatorForSeq2Seq(tok, padding=True, label_pad_token_id=-100),
)
import glob
puntos = glob.glob(f"{CARPETA}/lora/checkpoint-*")
if puntos:
    print("Retomando desde", max(puntos, key=lambda r: int(r.rsplit("-", 1)[1])))
entrenador.train(resume_from_checkpoint=bool(puntos))
""")

md("## 4. Medir después del ajuste")

code("""
modelo.eval()
despues = evaluar(prueba)
print("\\nchrF++ por dirección (antes → después)")
for d in sorted(despues):
    print(f"{d}: {antes.get(d)} → {despues[d]}")
json.dump({"antes": antes, "despues": despues}, open("evaluacion_prueba.json", "w"), indent=2)
shutil.copy("evaluacion_prueba.json", CARPETA)
""")

md("## 5. Fusionar LoRA y guardar el modelo completo")

code("""
fusionado = modelo.merge_and_unload()
fusionado.save_pretrained("gemma_aikukisna", safe_serialization=True)
tok.save_pretrained("gemma_aikukisna")
del modelo, fusionado, entrenador
torch.cuda.empty_cache()
""")

md("""
## 6. Convertir al formato del teléfono (.task de MediaPipe)

Necesita bastante RAM: si Colab se queda sin memoria, usa *Entorno con RAM alta*.
""")

code(CONVERSION_PIP)

code("""
MODELO = "gemma_aikukisna"  # carpeta con el modelo fusionado (model.safetensors + config.json)
""" + CONVERSION_CODIGO)

code(CONVERSION_BUNDLE + """
shutil.copy("gemma3-1b-aikukisna-int4.task", CARPETA)  # copia de respaldo
""")

md("""
## 7. Descargar resultados

Descarga el `.task` y `evaluacion_prueba.json`. El `.task` se publica en GitHub como Release
`modelo-tuki-v2` (archivo `gemma3-1b-aikukisna-int4.task`); después, en `local.properties` de la app:

```
TUKI_MODELO_AJUSTADO=true
```
""")

code("""
files.download("evaluacion_prueba.json")
files.download("gemma3-1b-aikukisna-int4.task")
""")

def variante_kaggle(celdas_colab: list[dict]) -> list[dict]:
    """Misma receta para Kaggle: GPU gratis ~30 h/semana y sesiones de hasta 12 h en segundo plano."""
    reemplazos = {
        "# Entrenar a Tuki": md_kaggle_intro,
        "from huggingface_hub import login": code_kaggle_login,
        "# Todo se guarda en Google Drive": code_kaggle_datos,
        "## 7. Descargar resultados": md_kaggle_salida,
    }
    salida = []
    for celda in celdas_colab:
        fuente = celda["source"]
        if fuente.startswith('files.download('):
            continue
        for inicio, nuevo in reemplazos.items():
            if fuente.lstrip().startswith(inicio):
                celda = {**celda, "source": nuevo.strip("\n")}
                break
        else:
            # En Kaggle CARPETA es el directorio de trabajo: copiar ahí sería copiar el archivo sobre sí mismo.
            lineas = [l for l in fuente.split("\n") if not l.startswith("shutil.copy(") or "subido" in l]
            celda = {**celda, "source": "\n".join(lineas)}
        salida.append(celda)
    return salida


md_kaggle_intro = """
# Entrenar a Tuki en Miskito y Kriol (Gemma 3 1B + LoRA) — versión Kaggle

**Antes de empezar (una sola vez)**
1. Verifica tu teléfono en *kaggle.com → Settings → Phone verification* (sin esto no hay GPU ni internet).
2. En Hugging Face, acepta la licencia de `google/gemma-3-1b-it` y crea un token de lectura.
3. En este cuaderno: *Add-ons → Secrets → Add secret* con nombre `HF_TOKEN` y el token como valor;
   activa la casilla para este cuaderno.
4. *Add Input → Upload* → sube `gemma_dataset.zip` como dataset (Kaggle lo descomprime solo).
5. Panel derecho → *Session options*: **Accelerator: GPU T4 x2** e **Internet: On**.

**Cómo ejecutarlo:** *Save Version → Save & Run All (Commit)*. Corre en segundo plano hasta 12 h
aunque cierres el navegador. Al terminar, los archivos quedan en la pestaña **Output** de esa versión.
"""

code_kaggle_login = """
import os
# Se usa una sola T4: con las dos, el Trainer activa DataParallel, que choca con gradient checkpointing.
os.environ["CUDA_VISIBLE_DEVICES"] = "0"
from huggingface_hub import login
from kaggle_secrets import UserSecretsClient
login(UserSecretsClient().get_secret("HF_TOKEN"))
"""

code_kaggle_datos = """
import glob, os, shutil, zipfile
CARPETA = "/kaggle/working"
os.makedirs("datos", exist_ok=True)
zips = glob.glob("/kaggle/input/**/gemma_dataset.zip", recursive=True)
if zips:
    zipfile.ZipFile(zips[0]).extractall("datos")
else:
    for nombre in ("entrenamiento.jsonl", "validacion.jsonl", "prueba.jsonl", "informe.json"):
        encontrados = glob.glob(f"/kaggle/input/**/{nombre}", recursive=True)
        if not encontrados and nombre == "informe.json":
            continue  # opcional: solo resume cuántos ejemplos hay
        assert encontrados, f"No encuentro {nombre}: agrega gemma_dataset.zip con Add Input"
        shutil.copy(encontrados[0], "datos")
print(os.listdir("datos"))
"""

md_kaggle_salida = """
## 7. Descargar resultados

Abre la versión terminada → pestaña **Output** → descarga `gemma3-1b-aikukisna-int4.task` y
`evaluacion_prueba.json`. El `.task` se publica en GitHub como Release `modelo-tuki-v2`; después, en
`local.properties` de la app: `TUKI_MODELO_AJUSTADO=true`.

Para ahorrar espacio en Output puedes borrar `lora/` y `gemma_aikukisna/` al final (no hacen falta en la app).
"""


def escribir(ruta: Path, lista: list[dict], acelerador: dict) -> None:
    cuaderno = {
        "cells": lista,
        "metadata": {
            **acelerador,
            "kernelspec": {"display_name": "Python 3", "name": "python3"},
            "language_info": {"name": "python"},
        },
        "nbformat": 4,
        "nbformat_minor": 0,
    }
    ruta.parent.mkdir(parents=True, exist_ok=True)
    ruta.write_text(json.dumps(cuaderno, ensure_ascii=False, indent=1), encoding="utf8")
    print(ruta)


escribir(SALIDA, celdas, {"accelerator": "GPU", "colab": {"provenance": [], "gpuType": "T4"}})
escribir(SALIDA.with_name("entrenar_gemma_aikukisna_kaggle.ipynb"), variante_kaggle(celdas),
         {"kaggle": {"accelerator": "nvidiaTeslaT4", "isInternetEnabled": True, "isGpuEnabled": True}})


# Cuaderno corto solo de conversión: usa el modelo ya entrenado (salida de una versión anterior en
# Kaggle) para no repetir las ~5 h de entrenamiento. No necesita GPU.
md_conversion = """
# Convertir el modelo de Tuki ya entrenado al formato del teléfono (.task) — Kaggle

**Antes de ejecutar**
1. *Add Input → Your Work → Notebooks* → agrega la salida del cuaderno de entrenamiento (la versión que
   terminó). Debe contener la carpeta `gemma_aikukisna/`.
2. *Add-ons → Secrets*: activa `HF_TOKEN` para este cuaderno (se usa para bajar `tokenizer.model`).
3. *Session options*: **Accelerator: None** (no hace falta GPU) e **Internet: On**.

**Ejecutar:** *Save Version → Save & Run All (Commit)*. Tarda unos 30–40 min. El `.task` queda en **Output**.
"""

code_conversion_datos = """
import glob, os
from huggingface_hub import HfApi, login
from huggingface_hub.errors import GatedRepoError
from kaggle_secrets import UserSecretsClient
HF_TOKEN = UserSecretsClient().get_secret("HF_TOKEN").strip()
login(HF_TOKEN)
print("Hugging Face:", HfApi(token=HF_TOKEN).whoami()["name"])
# Falla aquí, con un mensaje claro, si el token no puede leer Gemma (licencia sin aceptar o token
# "fine-grained" sin permiso para repos restringidos).
try:
    HfApi(token=HF_TOKEN).auth_check("google/gemma-3-1b-it")
except GatedRepoError as e:
    raise SystemExit(
        "El token no tiene acceso a google/gemma-3-1b-it: acepta la licencia en "
        "https://huggingface.co/google/gemma-3-1b-it con la misma cuenta y usa un token de tipo Read"
    ) from e

configs = glob.glob("/kaggle/input/**/gemma_aikukisna/config.json", recursive=True)
assert configs, "No encuentro gemma_aikukisna/: agrega la salida del cuaderno de entrenamiento con Add Input"
MODELO = os.path.dirname(configs[0])
print(MODELO, os.listdir(MODELO))
"""

md_conversion_fin = """
## Descargar

Pestaña **Output** → `gemma3-1b-aikukisna-int4.task` (unos 600–700 MB). Se publica en GitHub como
Release `modelo-tuki-v2`; después, en `local.properties` de la app: `TUKI_MODELO_AJUSTADO=true`.
"""

celdas = []
md(md_conversion)
code(CONVERSION_PIP)
code(code_conversion_datos)
code(CONVERSION_CODIGO)
code(CONVERSION_BUNDLE + """
!rm -rf salida_tflite  # solo se conserva el .task en Output
""")
md(md_conversion_fin)
escribir(SALIDA.with_name("convertir_gemma_aikukisna_kaggle.ipynb"), celdas,
         {"kaggle": {"accelerator": "none", "isInternetEnabled": True, "isGpuEnabled": False}})


# Variante "tutor": misma receta con el conjunto de preparar_dataset_tuki_tutor.py (diálogos de
# profesor + traducción). Se publica aparte para no reemplazar el traductor hasta compararlos.
CAMBIOS_TUTOR = [
    ("gemma_dataset.zip", "gemma_tutor_dataset.zip"),
    ("gemma_aikukisna", "gemma_aikukisna_tutor"),
    ("gemma3-1b-aikukisna-int4", "gemma3-1b-aikukisna-tutor-int4"),
    ("modelo-tuki-v2", "modelo-tuki-v3-tutor"),
]
for origen in sorted(SALIDA.parent.glob("*.ipynb")):
    texto = origen.read_text(encoding="utf8")
    for antes, despues in CAMBIOS_TUTOR:
        texto = texto.replace(antes, despues)
    destino = SALIDA.parent.parent / "gemma_tutor" / origen.name.replace("gemma_aikukisna", "gemma_aikukisna_tutor")
    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(texto, encoding="utf8")
