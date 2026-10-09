"""Conjunto de entrenamiento para que Tuki (Gemma 3 1B) converse como profesor sin inventar.

El modelo ajustado actual solo traduce (GemmaAssistantEngine.conversational() = !ajustado). Este
conjunto mezcla esa tarea de traducción con diálogos de tutoría en el MISMO formato que arma la app
(TukiIdentity.personaje + reglasVeracidad + contexto del idioma + "Datos verificados" + pregunta):

  - cómo se dice / qué significa / ejemplo, respondidos SOLO con los datos verificados del prompt;
  - explicaciones de reglas gramaticales documentadas y de textos culturales;
  - preguntas sin datos verificados, respondidas con honestidad ("todavía no tengo esa palabra").

Todas las respuestas salen de datos documentados de la semilla de Supabase; las frases que envuelven
los datos son plantillas fijas escritas por el equipo. Nada se genera con otro modelo.

Entrada:  app/src/main/assets/supabase_completa.ndjson.gzip
          modelos/traduccion/gemma/entrenamiento.jsonl (traducción; opcional, se mezcla una parte)
Salida:   modelos/traduccion/gemma_tutor/{entrenamiento,validacion}.jsonl + informe.json
Uso:      python scripts/preparar_dataset_tuki_tutor.py [--por-idioma 3000] [--proporcion-traduccion 0.5]
"""
import argparse
import gzip
import json
import random
import re
import unicodedata
from collections import Counter, defaultdict
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
SEMILLA = RAIZ / "app" / "src" / "main" / "assets" / "supabase_completa.ndjson.gzip"
TRADUCCION = RAIZ / "modelos" / "traduccion" / "gemma" / "entrenamiento.jsonl"
SALIDA = RAIZ / "modelos" / "traduccion" / "gemma_tutor"

MISKITO, ESPANOL, KRIOL, INGLES = 1, 2, 3, 4
NOMBRE = {MISKITO: "Miskito", ESPANOL: "Español", KRIOL: "Inglés Kriol", INGLES: "Inglés Estándar"}
ESTADOS_VALIDOS = {"documentada", "validada", "importada"}

# Copias exactas de TukiIdentity y de IdiomasTuki.contextoModelo en la app.
PERSONAJE = """Eres Tuki, el profesor de Aikukisna, una app para aprender Miskito, Inglés Kriol, Inglés Estándar y Español de la Costa Caribe de Nicaragua.
Conversas como un buen profesor con su estudiante: en español sencillo, cálido y paciente. Tuteas al estudiante.
Respondes cualquier pregunta sobre el idioma que aprende: palabras, frases, gramática, pronunciación, cultura y cómo practicar.
Explicas con un ejemplo corto cuando ayuda, corriges con amabilidad si el estudiante se equivoca y le propones practicar.
Responde en 2 a 4 oraciones cortas. No uses metáforas ni poesía. Si no sabes algo, dilo con sencillez."""
REGLAS = """Reglas importantes:
- Para palabras, frases o pronunciación en Miskito o Kriol usa SOLO los datos del bloque "Datos verificados". Si no están ahí, dilo con naturalidad ("todavía no tengo esa palabra en mi diccionario") y ofrece algo relacionado que sí esté.
- Nunca inventes progreso, XP, rachas ni lecciones: usa solo el "Contexto del estudiante".
- No menciones que eres un modelo de IA, ni tablas, identificadores o detalles técnicos."""


def contexto_idioma(meta: int) -> str:
    m = NOMBRE[meta]
    return (f"El estudiante está aprendiendo {m}. Los cuatro idiomas de la app son Miskito, Inglés Kriol, Inglés Estándar y Español. "
            f"Habla de {m}; si pide otro idioma, recuérdale que está aprendiendo {m} y que puede cambiarlo en la pantalla principal (Inicio).")


def prompt(meta: int, verificados: list[str], pregunta: str) -> str:
    datos = "Datos verificados:\n" + "\n".join(verificados) if verificados else "Datos verificados: ninguno para esta consulta."
    return f"{PERSONAJE}\n\n{REGLAS}\n\n{contexto_idioma(meta)}\n{datos}\n\n{pregunta}"


def limpiar(texto: str) -> str:
    texto = unicodedata.normalize("NFC", texto or "").strip()
    texto = re.sub(r"\s*\([^)]*\)", "", texto)
    return re.sub(r"\s+", " ", texto).strip(" .,;:")


def corta(texto: str) -> bool:
    return 0 < len(texto) <= 30 and len(texto.split()) <= 3


def cargar():
    tablas = defaultdict(list)
    interesan = {"palabra", "traduccion", "oracion_ejemplo", "regla_gramatical", "ejemplo_regla_gramatical", "cultura_contenido"}
    with gzip.open(SEMILLA, "rt", encoding="utf-8") as f:
        for linea in f:
            fila = json.loads(linea)
            if fila["tabla"] in interesan:
                tablas[fila["tabla"]].append(fila["json"])
    return tablas


def pares_vocabulario(t) -> dict[int, list[tuple[str, str]]]:
    """Pares (español, palabra del idioma) con la traducción preferida y documentada."""
    palabras = {p["id"]: p for p in t["palabra"] if p["estado_validacion"] in ESTADOS_VALIDOS}
    mejor: dict[tuple[int, int], tuple] = {}
    for tr in t["traduccion"]:
        if tr["estado_validacion"] not in ESTADOS_VALIDOS:
            continue
        a, b = palabras.get(tr["palabra_origen_id"]), palabras.get(tr["palabra_destino_id"])
        if not a or not b:
            continue
        es, otra = (a, b) if a["idioma_id"] == ESPANOL else (b, a) if b["idioma_id"] == ESPANOL else (None, None)
        if not es or otra["idioma_id"] not in (MISKITO, KRIOL, INGLES):
            continue
        clave = (es["id"], otra["idioma_id"])
        rango = (bool(tr.get("es_preferida")), float(tr.get("nivel_confianza") or 0))
        if clave not in mejor or rango > mejor[clave][0]:
            mejor[clave] = (rango, limpiar(es["texto"]), limpiar(otra["texto"]))
    salida = defaultdict(list)
    for (_, idioma), (_, es, otra) in mejor.items():
        if corta(es) and corta(otra) and es.lower() != otra.lower():
            salida[idioma].append((es, otra))
    return salida


def frases_por_idioma(t) -> dict[int, list[tuple[str, str]]]:
    salida = defaultdict(list)
    for o in t["oracion_ejemplo"]:
        if o["estado_validacion"] not in ("documentada", "validada"):
            continue
        ids = (o["idioma_origen_id"], o["idioma_destino_id"])
        if ESPANOL not in ids:
            continue
        otro = ids[0] if ids[1] == ESPANOL else ids[1]
        texto, es = (o["texto_origen"], o["texto_destino"]) if ids[0] == otro else (o["texto_destino"], o["texto_origen"])
        if 3 <= len(texto) <= 140 and "[" not in texto:
            salida[otro].append((texto.strip(), es.strip()))
    return salida


def dialogos(t, por_idioma: int, rnd: random.Random) -> list[dict]:
    vocab, frases = pares_vocabulario(t), frases_por_idioma(t)
    ejemplos = []
    for meta in (MISKITO, KRIOL, INGLES):
        idioma = NOMBRE[meta]
        pares = vocab[meta][:]
        rnd.shuffle(pares)
        for es, otra in pares[:por_idioma]:
            dato = [f"{es} = {otra}"]
            ejemplos.append({"prompt": prompt(meta, dato, f"¿Cómo se dice {es} en {idioma}?"),
                             "respuesta": f"En {idioma}, «{es}» se dice «{otra}». Repítela en voz alta. ¿Quieres que practiquemos con otra palabra?"})
            ejemplos.append({"prompt": prompt(meta, dato, f"¿Qué significa {otra}?"),
                             "respuesta": f"«{otra}» significa «{es}». ¿Te animas a usarla en una frase corta?"})
            # Honestidad: la misma pregunta sin el dato no debe inventar la palabra.
            if rnd.random() < 0.25:
                ejemplos.append({"prompt": prompt(meta, [], f"¿Cómo se dice {es} en {idioma}?"),
                                 "respuesta": f"Todavía no tengo «{es}» en mi diccionario de {idioma}, y prefiero no inventarla. "
                                              "Pregúntame por otra palabra o pídeme frases de una lección."})
        lista = frases[meta][:]
        rnd.shuffle(lista)
        for texto, es in lista[: por_idioma // 3]:
            palabra = max(re.findall(r"[\wÀ-ÿ']+", texto), key=len, default="")
            if len(palabra) < 4:
                continue
            ejemplos.append({"prompt": prompt(meta, [f"Frase: {texto} = {es}"], f"Dame un ejemplo con «{palabra}»."),
                             "respuesta": f"Aquí tienes una frase: «{texto}», que significa «{es}». Léela en voz alta y luego cambia una palabra."})
    # Reglas gramaticales y cultura documentadas.
    ejemplos_regla = defaultdict(list)
    for e in t["ejemplo_regla_gramatical"]:
        if e["estado_validacion"] in ESTADOS_VALIDOS and e.get("traduccion_espanol"):
            ejemplos_regla[e["regla_id"]].append(f"{e['texto_idioma']} — {e['traduccion_espanol']}")
    for r in t["regla_gramatical"]:
        if r["estado_validacion"] in ("rechazada",) or r["idioma_id"] not in NOMBRE or not r.get("descripcion"):
            continue
        descripcion = r["descripcion"].strip()
        if len(descripcion) > 600:
            continue
        ej = ejemplos_regla.get(r["id"], [])[:1]
        datos = [f"Regla: {r['titulo']}. {descripcion}"] + [f"Ejemplo: {x}" for x in ej]
        respuesta = f"Te explico «{r['titulo']}»: {descripcion}"
        if ej:
            respuesta += f" Por ejemplo: {ej[0]}."
        ejemplos.append({"prompt": prompt(r["idioma_id"], datos, f"Explícame {r['titulo'].lower()}"),
                         "respuesta": respuesta + " ¿Practicamos con otro ejemplo?"})
    for c in t["cultura_contenido"]:
        contenido = (c.get("contenido") or "").strip()
        if not contenido or len(contenido) > 500:
            continue
        meta = rnd.choice((MISKITO, KRIOL, INGLES, ESPANOL))
        ejemplos.append({"prompt": prompt(meta, [f"Cultura: {c['titulo']}. {contenido}"], f"Cuéntame sobre {c['titulo'].lower()}"),
                         "respuesta": f"{contenido} ¿Quieres que te cuente algo más?"})
    return ejemplos


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--por-idioma", type=int, default=3000)
    p.add_argument("--proporcion-traduccion", type=float, default=0.5)
    p.add_argument("--semilla", type=int, default=7)
    a = p.parse_args()
    rnd = random.Random(a.semilla)
    tutor = dialogos(cargar(), a.por_idioma, rnd)
    traduccion = []
    if TRADUCCION.exists():
        traduccion = [json.loads(l) for l in TRADUCCION.open(encoding="utf-8")]
        rnd.shuffle(traduccion)
        traduccion = traduccion[: int(len(tutor) * a.proporcion_traduccion / (1 - a.proporcion_traduccion))]
    todo = tutor + traduccion
    rnd.shuffle(todo)
    corte = max(1, len(todo) // 20)
    SALIDA.mkdir(parents=True, exist_ok=True)
    for nombre, filas in (("validacion", todo[:corte]), ("entrenamiento", todo[corte:])):
        with (SALIDA / f"{nombre}.jsonl").open("w", encoding="utf-8") as f:
            for fila in filas:
                f.write(json.dumps(fila, ensure_ascii=False) + "\n")
    # La partición de prueba de traducción se conserva para medir que el traductor no empeore.
    prueba = TRADUCCION.with_name("prueba.jsonl")
    if prueba.exists():
        (SALIDA / "prueba.jsonl").write_text(prueba.read_text(encoding="utf-8"), encoding="utf-8")
    informe = {"tutoria": len(tutor), "traduccion": len(traduccion), "total": len(todo),
               "tipos": Counter(e["respuesta"].split(" ")[0] for e in tutor).most_common(8)}
    (SALIDA / "informe.json").write_text(json.dumps(informe, ensure_ascii=False, indent=1), encoding="utf-8")
    print(json.dumps(informe, ensure_ascii=False))


if __name__ == "__main__":
    main()
