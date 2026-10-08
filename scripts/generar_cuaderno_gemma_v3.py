"""Cuadernos de Kaggle para Tuki v3: parte del modelo v2 (ya ajustado para traducir) y le enseña a conversar
como profesor con scripts/preparar_dataset_tuki_v3.py, sin olvidar la traducción.

Se construyen a partir de los cuadernos Kaggle del v2 (scripts/generar_cuaderno_gemma.py) con cambios puntuales:
  - pesos iniciales: la carpeta gemma_aikukisna/ de la salida del cuaderno con que se entrenó el v2;
  - MAX_LEN 640 (el prompt de tutoría de la app mide ~400 tokens; con 160 se cortaba antes de la respuesta);
  - lotes de 4 × 8 acumulados y tasa 1e-4 (se ajusta un modelo ya entrenado);
  - la medición "antes" es el v2, así que antes/después se comparan directamente;
  - guarda respuestas de 40 tutorías de validación en muestras_tutoria.json para leer cómo conversa.

Uso:    python scripts/generar_cuaderno_gemma_v3.py   (después de generar_cuaderno_gemma.py)
Salida: modelos/traduccion/gemma_v3/{entrenar,convertir}_gemma_aikukisna_v3_kaggle.ipynb
"""
import json
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent / "modelos" / "traduccion"
ORIGEN, DESTINO = RAIZ / "gemma", RAIZ / "gemma_v3"

INTRO = """# Tuki v3: enseñar a conversar al modelo v2 sin perder la traducción — Kaggle

Parte del modelo **v2** (el que ya usa la app para traducir) y lo ajusta con tutorías + repaso de traducción.

**Antes de ejecutar**
1. *Add Input → Upload* → sube `gemma_v3_dataset.zip` como dataset (Kaggle lo descomprime solo).
2. *Add Input → Your Work → Notebooks* → agrega la **versión terminada del cuaderno con que entrenaste el v2**.
   Debe contener la carpeta `gemma_aikukisna/` (el modelo v2 fusionado). Si la borraste de su Output, avísale a Claude.
3. *Add-ons → Secrets*: activa `HF_TOKEN` para este cuaderno.
4. Panel derecho → *Session options*: **Accelerator: GPU T4 x2** e **Internet: On**.

**Cómo ejecutarlo:** *Save Version → Save & Run All (Commit)*. Unas 5–7 h en segundo plano. Al terminar, en **Output**:
`evaluacion_prueba.json` (antes = v2, después = v3), `muestras_tutoria.json` y la carpeta `gemma_aikukisna_v3/`.
"""

BASE_V2 = '''configs = glob.glob("/kaggle/input/**/gemma_aikukisna/config.json", recursive=True)
assert configs, ("No encuentro el modelo v2 (carpeta gemma_aikukisna/): Add Input → Your Work → Notebooks → "
                 "la versión terminada del cuaderno con que entrenaste el v2")
BASE = os.path.dirname(configs[0])  # modelo v2 fusionado: se parte de él, no de google/gemma-3-1b-it
print("Partiendo de", BASE)'''

MUESTRAS = '''# Cómo conversa: respuestas a tutorías que no vio en el entrenamiento.
muestras = leer("muestras_tutoria.jsonl")
generadas = traducir([m["prompt"] for m in muestras], lote=4, max_nuevos=160)
salida = []
for m, g in zip(muestras, generadas):
    datos = m["prompt"].split("Datos verificados")[-1].split("\\n\\n")[0]
    salida.append({"pregunta": m["prompt"].rsplit("\\n\\n", 1)[-1], "datos": "Datos verificados" + datos,
                   "esperada": m["respuesta"], "generada": g})
json.dump(salida, open("muestras_tutoria.json", "w", encoding="utf8"), ensure_ascii=False, indent=1)
for s in salida[:8]:
    print("P:", s["pregunta"], "\\n  esperada:", s["esperada"], "\\n  generada:", s["generada"], "\\n")'''

# El Input del v2 también trae una carpeta datos/ con entrenamiento.jsonl: hay que tomar TODOS los archivos de
# la carpeta del dataset v3 (la que tiene muestras_tutoria.jsonl), nunca mezclar.
DATOS_V3 = """import glob, json, os, shutil, zipfile
CARPETA = "/kaggle/working"
os.makedirs("datos", exist_ok=True)
ARCHIVOS = ("entrenamiento.jsonl", "validacion.jsonl", "prueba.jsonl", "muestras_tutoria.jsonl")
zips = glob.glob("/kaggle/input/**/gemma_v3_dataset.zip", recursive=True)
if zips:
    zipfile.ZipFile(zips[0]).extractall("datos")
else:
    marcas = glob.glob("/kaggle/input/**/muestras_tutoria.jsonl", recursive=True)
    if not marcas:
        for raiz, _, nombres in os.walk("/kaggle/input"):
            if any(n.endswith((".jsonl", ".zip")) for n in nombres):
                print(raiz, sorted(n for n in nombres if n.endswith((".jsonl", ".json", ".zip"))))
        raise SystemExit("No encuentro el dataset v3 (muestras_tutoria.jsonl). Arriba se listan los Inputs: sube "
                         "gemma_v3_dataset.zip con Add Input → Upload (o sus 4 .jsonl juntos en un mismo dataset).")
    origen = os.path.dirname(marcas[0])
    print("Dataset v3 en", origen)
    for nombre in ARCHIVOS + ("informe.json",):
        if os.path.exists(os.path.join(origen, nombre)):
            shutil.copy(os.path.join(origen, nombre), "datos")
faltan = [n for n in ARCHIVOS if not os.path.exists(os.path.join("datos", n))]
assert not faltan, f"Faltan en el dataset v3: {faltan}"
print(os.listdir("datos"))
informe = json.load(open("datos/informe.json")) if os.path.exists("datos/informe.json") else {}
print("Informe:", informe)
"""

# Si una corrida anterior se cortó, su salida (agregada como Input) trae lora/checkpoint-*: se copia para retomar.
RETOMAR = """import glob, math, shutil
def es_de_esta_corrida(ruta):
    # El Input del v2 también puede traer lora/checkpoint-*: solo vale un punto con los mismos pasos totales.
    estado = os.path.join(ruta, "trainer_state.json")
    if not os.path.exists(estado):
        return False
    pasos = math.ceil(len(ds_entrenamiento) / (args.per_device_train_batch_size * args.gradient_accumulation_steps))
    return json.load(open(estado)).get("max_steps") == pasos
if not glob.glob(f"{CARPETA}/lora/checkpoint-*"):
    previos = [p for p in glob.glob("/kaggle/input/**/lora/checkpoint-*", recursive=True) if es_de_esta_corrida(p)]
    if previos:
        ultimo = max(previos, key=lambda r: int(r.rsplit("-", 1)[1]))
        shutil.copytree(ultimo, f"{CARPETA}/lora/{os.path.basename(ultimo)}")
        print("Punto de control de una corrida anterior:", ultimo)"""

LOGIN_TOLERANTE = """try:
    login(UserSecretsClient().get_secret("HF_TOKEN").strip())
except Exception as e:  # no se necesita para entrenar desde el v2
    print("AVISO: no se pudo iniciar sesión en Hugging Face:", type(e).__name__, "- se sigue sin token.")
    print("Antes de la conversión, revisa el secret HF_TOKEN (token tipo Read, sin espacios).")"""

RESULTADOS = """## 7. Descargar resultados

En **Output** revisa `evaluacion_prueba.json` (antes = v2, después = v3) y `muestras_tutoria.json`, y pásaselos a Claude.
Solo si el v3 traduce casi igual que el v2 (español → Miskitu ≥ 29) se convierte con el cuaderno
`convertir_gemma_aikukisna_v3_kaggle.ipynb` y se publica como Release `modelo-tuki-v3`.

Para ahorrar espacio puedes borrar `lora/` de Output; **no borres** `gemma_aikukisna_v3/` (lo usa la conversión).
"""


def reemplazar(texto: str, antes: str, despues: str) -> str:
    assert antes in texto, f"No encontré en el cuaderno del v2: {antes[:70]!r}"
    return texto.replace(antes, despues)


def entrenamiento() -> dict:
    nb = json.loads((ORIGEN / "entrenar_gemma_aikukisna_kaggle.ipynb").read_text(encoding="utf-8"))
    celdas = nb["cells"]
    fuente = lambda i: celdas[i]["source"] if isinstance(celdas[i]["source"], str) else "".join(celdas[i]["source"])

    celdas[0]["source"] = INTRO
    celdas[7]["source"] = "## 2. Medir el modelo v2 (antes del ajuste)"
    # Se parte del v2 que ya está en Input: Hugging Face no hace falta para entrenar. Si el token falla
    # (copiado con un espacio, revocado), solo se avisa; la conversión sí lo necesitará.
    celdas[2]["source"] = reemplazar(fuente(2), 'login(UserSecretsClient().get_secret("HF_TOKEN"))', LOGIN_TOLERANTE)
    celdas[2]["source"] = reemplazar(fuente(2), 'os.environ["CUDA_VISIBLE_DEVICES"] = "0"',
                                     'os.environ["CUDA_VISIBLE_DEVICES"] = "0"\nos.environ["PYTORCH_CUDA_ALLOC_CONF"] = "expandable_segments:True"')
    celdas[3]["source"] = DATOS_V3
    celdas[5]["source"] = reemplazar(fuente(5), 'BASE = "google/gemma-3-1b-it"', BASE_V2)
    celdas[6]["source"] = reemplazar(fuente(6), "MAX_LEN = 160",
                                     "# El prompt de tutoría de la app mide ~400 tokens: con 160 se cortaba antes de la respuesta.\nMAX_LEN = 640")
    evaluacion = reemplazar(fuente(8), "def traducir(textos, lote=16):", "def traducir(textos, lote=16, max_nuevos=64):")
    celdas[8]["source"] = reemplazar(evaluacion, "max_new_tokens=64", "max_new_tokens=max_nuevos")
    ajuste = fuente(10)
    for antes, despues in (("per_device_train_batch_size=16", "per_device_train_batch_size=4"),
                           ("gradient_accumulation_steps=2", "gradient_accumulation_steps=8"),
                           ("learning_rate=2e-4", "learning_rate=1e-4"),
                           ("warmup_steps=110", "warmup_steps=30"),
                           # La evaluación de 8 ejemplos de 640 tokens con el vocabulario de Gemma (262 k) pide ~5 GB
                           # solo para los logits y agotó la T4 en el paso 500: se evalúa de a uno.
                           ("logging_steps=50,", "logging_steps=50, per_device_eval_batch_size=1,"),
                           ("import glob\npuntos =", RETOMAR + "\nimport glob\npuntos =")):
        ajuste = reemplazar(ajuste, antes, despues)
    celdas[10]["source"] = ajuste.replace("(110 ≈ 3 % de ~3 700 pasos)", "(30 ≈ 3 % de ~940 pasos)")
    medir = fuente(12)
    celdas[12]["source"] = medir
    nueva = {"cell_type": "code", "metadata": {}, "execution_count": None, "outputs": [], "source": MUESTRAS}
    celdas.insert(13, nueva)
    # Desde aquí los índices se corren uno. Guardado y conversión con nombres v3.
    for i in range(14, len(celdas)):
        texto = fuente(i)
        texto = texto.replace('"gemma_aikukisna"', '"gemma_aikukisna_v3"').replace("`gemma_aikukisna/`", "`gemma_aikukisna_v3/`")
        texto = texto.replace("gemma3-1b-aikukisna-int4", "gemma3-1b-aikukisna-v3-int4").replace("modelo-tuki-v2", "modelo-tuki-v3")
        celdas[i]["source"] = texto
    celdas[-1]["source"] = RESULTADOS
    # La conversión va en su propio cuaderno (sin GPU): se quitan aquí las celdas 6 (conversión) para no gastar horas de GPU.
    titulo_conversion = next(i for i, c in enumerate(celdas) if c["cell_type"] == "markdown" and "## 6." in fuente(i))
    del celdas[titulo_conversion:len(celdas) - 1]
    return nb


def conversion() -> dict:
    nb = json.loads((ORIGEN / "convertir_gemma_aikukisna_kaggle.ipynb").read_text(encoding="utf-8"))
    for celda in nb["cells"]:
        texto = celda["source"] if isinstance(celda["source"], str) else "".join(celda["source"])
        texto = texto.replace("gemma_aikukisna/", "gemma_aikukisna_v3/").replace("gemma3-1b-aikukisna-int4", "gemma3-1b-aikukisna-v3-int4")
        celda["source"] = texto.replace("modelo-tuki-v2", "modelo-tuki-v3")
    return nb


def main():
    DESTINO.mkdir(parents=True, exist_ok=True)
    for nombre, nb in (("entrenar_gemma_aikukisna_v3_kaggle.ipynb", entrenamiento()),
                       ("convertir_gemma_aikukisna_v3_kaggle.ipynb", conversion())):
        (DESTINO / nombre).write_text(json.dumps(nb, ensure_ascii=False, indent=1), encoding="utf-8")
        print(DESTINO / nombre)


if __name__ == "__main__":
    main()
