"""Empareja las palabras en Miskitu que pronuncia El Miskito Hamilton con el diccionario y recorta un clip por palabra.

Permiso del autor (2026-10-07): se puede usar su voz; el crédito va en la base de datos, no en el audio.

Los videos siguen el patrón "palabra en español → palabra en Miskitu repetida tres veces". Por cada traducción
documentada español→Miskitu se busca, en las palabras transcritas justo después de la glosa en español, un tramo
que suene como la palabra en Miskitu (comparación fonética aproximada, porque Whisper escribe el Miskitu con
grafía española). Nada se aprueba aquí: los clips quedan como candidatos para revisión humana.

Uso:    python scripts/emparejar_audios_hamilton.py
Salida: fuentes_audio/hamilton/candidatos.json y fuentes_audio/hamilton/clips/*.wav
"""
import gzip
import json
import re
import subprocess
import unicodedata
from collections import defaultdict
from difflib import SequenceMatcher
from pathlib import Path

import imageio_ffmpeg

PROYECTO = Path(__file__).resolve().parent.parent
RAIZ = PROYECTO / "fuentes_audio" / "hamilton"
SEMILLA = PROYECTO / "app" / "src" / "main" / "assets" / "supabase_completa.ndjson.gzip"
MISKITO, ESPANOL = 1, 2
VENTANA_GLOSA = 7          # palabras en español que se miran antes del candidato
VENTANA_CANDIDATO = 10     # palabras después de la glosa donde puede estar el Miskitu
UMBRAL = 0.78
RELLENO_ANTES, RELLENO_DESPUES = 0.12, 0.20
VACIAS = set("""el la los las un una unos unas de del y o a al en que es se lo le por para con como
también tambien puedes decir dice eso esa ese esto este esta mismo igual otro otra ser muy mas más ya
""".split())


def sin_tildes(texto: str) -> str:
    return "".join(c for c in unicodedata.normalize("NFD", texto.lower()) if unicodedata.category(c) != "Mn")


def clave_es(texto: str) -> str:
    return re.sub(r"[^a-zñ ]", "", sin_tildes(texto).replace("ñ", "n")).strip()


def fonetica(texto: str) -> str:
    """Aproxima cómo suena: iguala las grafías que Whisper usa en español con la ortografía Miskitu."""
    t = re.sub(r"[^a-z]", "", sin_tildes(texto))
    for a, b in (("qu", "k"), ("ce", "se"), ("ci", "si"), ("c", "k"), ("hu", "w"), ("gu", "w"), ("v", "w"),
                 ("j", "h"), ("z", "s"), ("ll", "y"), ("rr", "r"), ("x", "ks")):
        t = t.replace(a, b)
    t = t.replace("h", "").replace("w", "b")
    return re.sub(r"(.)\1+", r"\1", t)


def forma(texto: str) -> str:
    """Forma escrita sin signos ni notas entre paréntesis: agrupa variantes de la misma entrada."""
    texto = re.sub(r"\([^)]*\)", " ", sin_tildes(texto))
    return " ".join(re.sub(r"[^a-z ]", " ", texto).split())


def cargar_diccionario():
    palabras, traducciones = {}, []
    for linea in gzip.open(SEMILLA, "rt", encoding="utf-8"):
        fila = json.loads(linea)
        datos = fila["json"]
        if fila["tabla"] == "palabra" and datos["idioma_id"] in (MISKITO, ESPANOL):
            if datos.get("estado_validacion") != "rechazada":
                palabras[datos["id"]] = datos
        elif fila["tabla"] == "traduccion" and datos.get("estado_validacion") != "rechazada":
            traducciones.append(datos)
    por_glosa = defaultdict(set)   # glosa española normalizada → ids de palabras Miskitu
    for t in traducciones:
        a, b = palabras.get(t["palabra_origen_id"]), palabras.get(t["palabra_destino_id"])
        if not a or not b or a["idioma_id"] == b["idioma_id"]:
            continue
        es, mi = (a, b) if a["idioma_id"] == ESPANOL else (b, a)
        glosa = clave_es(es["texto"])
        if glosa and len(mi["texto"].split()) <= 3:
            por_glosa[glosa].add(mi["id"])
    return palabras, por_glosa


def tokens(transcripcion):
    lista = []
    for segmento in transcripcion:
        for p in segmento["palabras"]:
            texto = clave_es(p["p"])
            if texto:
                lista.append({"t": texto, "i": p["i"], "f": p["f"], "prob": p["prob"]})
    return lista


def buscar(vid, lista, palabras, por_glosa):
    hallazgos = []
    for i in range(len(lista)):
        for largo_glosa in (3, 2, 1):
            if i + largo_glosa > len(lista):
                continue
            glosa = " ".join(x["t"] for x in lista[i:i + largo_glosa])
            if glosa in VACIAS or glosa not in por_glosa:
                continue
            inicio = i + largo_glosa
            siguientes = {fonetica(x["t"]) for x in lista[inicio:inicio + 3]}
            if fonetica(lista[i + largo_glosa - 1]["t"]) in siguientes:
                continue  # la "glosa" se repite enseguida: es la palabra en Miskitu, no la explicación en español
            for mi_id in por_glosa[glosa]:
                objetivo = fonetica(palabras[mi_id]["texto"])
                if len(objetivo) < 2:
                    continue
                mejor = None
                for j in range(inicio, min(inicio + VENTANA_CANDIDATO, len(lista))):
                    for largo in (1, 2, 3):
                        tramo = lista[j:j + largo]
                        if len(tramo) < largo:
                            break
                        if len({x["t"] for x in tramo}) < largo:
                            continue  # el tramo incluye la misma palabra dos veces: tomar una sola repetición
                        oido = fonetica("".join(x["t"] for x in tramo))
                        similitud = SequenceMatcher(None, oido, objetivo).ratio()
                        if similitud >= UMBRAL and (mejor is None or similitud > mejor[0]):
                            mejor = (similitud, tramo)
                if mejor:
                    tramo = mejor[1]
                    # Repeticiones: cuántas veces más aparece lo mismo enseguida (refuerza que es la palabra enseñada).
                    oido = fonetica("".join(x["t"] for x in tramo))
                    repeticiones = sum(1 for k in range(len(lista))
                                       if tramo[0]["i"] <= lista[k]["i"] <= tramo[-1]["f"] + 8
                                       and fonetica(lista[k]["t"]) == oido)
                    hallazgos.append({
                        "palabra_id": mi_id, "miskitu": palabras[mi_id]["texto"], "glosa": glosa,
                        "oido": " ".join(x["t"] for x in tramo), "similitud": round(mejor[0], 3),
                        "repeticiones": repeticiones, "prob": round(min(x["prob"] for x in tramo), 3),
                        "video": vid, "inicio": round(tramo[0]["i"], 2), "fin": round(tramo[-1]["f"], 2)})
            break
    return hallazgos


def nombre_clip(h) -> str:
    return f"{h['video']}_{int(h['inicio'] * 100)}.wav"


def alternativas(forma_buscada, original, palabras, ids_por_forma, umbral=0.75):
    """Tramos de cualquier video que suenan como la forma (más repetidos y parecidos primero), salvo el rechazado."""
    objetivo = fonetica(forma_buscada)
    hallados = []
    for ruta in sorted((RAIZ / "transcripcion").glob("*.json")):
        lista = tokens(json.loads(ruta.read_text(encoding="utf-8")))
        for j in range(len(lista)):
            for largo in (1, 2, 3):
                tramo = lista[j:j + largo]
                if len(tramo) < largo or len({x["t"] for x in tramo}) < largo:
                    continue
                similitud = SequenceMatcher(None, fonetica("".join(x["t"] for x in tramo)), objetivo).ratio()
                if similitud < umbral:
                    continue
                if ruta.stem == original["video"] and abs(tramo[0]["i"] - original["inicio"]) < 1:
                    continue
                oido = fonetica("".join(x["t"] for x in tramo))
                repeticiones = sum(1 for k in range(len(lista))
                                   if tramo[0]["i"] <= lista[k]["i"] <= tramo[-1]["f"] + 8
                                   and fonetica(lista[k]["t"]) == oido)
                hallados.append({"video": ruta.stem, "inicio": round(tramo[0]["i"], 2),
                                 "fin": round(tramo[-1]["f"], 2), "oido": " ".join(x["t"] for x in tramo),
                                 "similitud": round(similitud, 3), "repeticiones": repeticiones,
                                 "prob": min(x["prob"] for x in tramo)})
    # Una por tramo de tiempo (evita la misma repetición dos veces).
    hallados.sort(key=lambda h: (-h["similitud"], -h["repeticiones"], -h["prob"]))
    unicos = []
    for h in hallados:
        if all(h["video"] != u["video"] or abs(h["inicio"] - u["inicio"]) > 3 for u in unicos):
            unicos.append(h)
    return unicos


def recortar(h, ffmpeg):
    destino = RAIZ / "clips" / nombre_clip(h)
    if not destino.exists():
        inicio = max(0.0, h["inicio"] - RELLENO_ANTES)
        duracion = h["fin"] + RELLENO_DESPUES - inicio
        subprocess.run([ffmpeg, "-loglevel", "error", "-y", "-ss", f"{inicio:.2f}", "-t", f"{duracion:.2f}",
                        "-i", str(RAIZ / "original" / f"{h['video']}.m4a"), "-ac", "1", "-ar", "22050",
                        "-af", "loudnorm=I=-18:TP=-2", str(destino)], check=True)
    return destino.name


def main():
    palabras, por_glosa = cargar_diccionario()
    (RAIZ / "clips").mkdir(exist_ok=True)
    todos = []
    for ruta in sorted((RAIZ / "transcripcion").glob("*.json")):
        todos += buscar(ruta.stem, tokens(json.loads(ruta.read_text(encoding="utf-8"))), palabras, por_glosa)
    # Las variantes del diccionario con la misma forma ("utla", "utla.") comparten el clip.
    for h in todos:
        h["forma"] = forma(h["miskitu"])
    # Un clip por forma: el que ya se revisó (para no perder decisiones al agregar videos); si no, más
    # repeticiones, luego mayor similitud y confianza.
    archivo_decisiones = RAIZ / "decisiones.json"
    decididos = json.loads(archivo_decisiones.read_text(encoding="utf-8")) if archivo_decisiones.exists() else {}
    mejores = {}
    for h in todos:
        h["clip"] = nombre_clip(h)
        decision = decididos.get(h["clip"])
        # Aprobado para esta forma primero; un clip rechazado queda al final para que salga otra opción.
        orden = (decision == h["forma"], decision != "rechazado", h["repeticiones"] >= 2, h["similitud"], h["prob"])
        if h["forma"] not in mejores or orden > mejores[h["forma"]][0]:
            mejores[h["forma"]] = (orden, h)
    ids_por_forma = defaultdict(set)
    for p in palabras.values():
        if p["idioma_id"] == MISKITO:
            ids_por_forma[forma(p["texto"])].add(p["id"])
    ffmpeg = imageio_ffmpeg.get_ffmpeg_exe()
    candidatos = sorted((h for _, h in mejores.values()), key=lambda h: (h["video"], h["inicio"]))
    for h in candidatos:
        h["palabra_ids"] = sorted(ids_por_forma[h["forma"]])
        h["glosas"] = sorted({x["glosa"] for x in todos if x["forma"] == h["forma"]})
        h["clip"] = recortar(h, ffmpeg)
    # Un mismo tramo puede parecerse a varias entradas ("naika kat" / "naiwa kat"): se revisa una vez y la
    # persona elige cuál dice realmente.
    por_clip = {}
    for h in candidatos:
        grupo = por_clip.setdefault(h["clip"], {k: h[k] for k in ("clip", "video", "inicio", "fin", "oido",
                                                                    "repeticiones")} | {"opciones": []})
        grupo["opciones"].append({"forma": h["forma"], "palabra_ids": h["palabra_ids"], "glosas": h["glosas"],
                                  "similitud": h["similitud"]})
    # Formas cuyo clip se rechazó: búsqueda amplia (sin exigir la glosa en español al lado) para ofrecer otro clip.
    rechazadas = {h["forma"]: h for h in candidatos if decididos.get(h["clip"]) == "rechazado"}
    vistos = {h["clip"] for h in candidatos}
    for forma_rechazada, original in rechazadas.items():
        for alt in alternativas(forma_rechazada, original, palabras, ids_por_forma)[:3]:
            alt["clip"] = nombre_clip(alt)
            if alt["clip"] in vistos:
                continue
            vistos.add(alt["clip"])
            alt["clip"] = recortar(alt, ffmpeg)
            por_clip[alt["clip"]] = {k: alt[k] for k in ("clip", "video", "inicio", "fin", "oido", "repeticiones")} | {
                "opciones": [{"forma": forma_rechazada, "palabra_ids": original["palabra_ids"],
                              "glosas": original["glosas"], "similitud": alt["similitud"]}]}
    clips = sorted(por_clip.values(), key=lambda g: (g["video"], g["inicio"]))
    for g in clips:
        g["opciones"].sort(key=lambda o: -o["similitud"])
    (RAIZ / "candidatos.json").write_text(json.dumps(clips, ensure_ascii=False, indent=1), encoding="utf-8")
    print(len(todos), "coincidencias →", len(candidatos), "formas →", len(clips), "clips para revisar")


if __name__ == "__main__":
    main()
