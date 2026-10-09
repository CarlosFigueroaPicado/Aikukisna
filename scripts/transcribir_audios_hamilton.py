"""Transcribe los audios del canal El Miskito Hamilton (permiso del autor, 2026-10-07) con marcas por palabra.

Uso: python scripts/transcribir_audios_hamilton.py [id_video ...]
Salida: fuentes_audio/hamilton/transcripcion/<id>.json (segmentos con palabras y tiempos).
El idioma se fija en español: el autor explica en español y el Miskitu se escribe casi como suena,
así que las palabras en Miskitu salen con una grafía aproximada que luego se compara con el diccionario.
"""
import json
import sys
import wave
from pathlib import Path

import numpy as np
from faster_whisper import WhisperModel

RAIZ = Path(__file__).resolve().parent.parent / "fuentes_audio" / "hamilton"
PRIORIDAD = ["QEXztFoNDeE", "BUmrcun-SLA", "joya24CZ19c", "8ruMeo3y4po", "yRw6_951rcM", "euKqz7qgO8M",
             "Bye0GbcMEMQ", "kKOWzlWGJWw", "dD5gMSH3fts"]


def leer_wav(ruta: Path) -> np.ndarray:
    """WAV mono 16 kHz (convertido antes con ffmpeg) a float32, para no depender de PyAV."""
    with wave.open(str(ruta)) as w:
        return np.frombuffer(w.readframes(w.getnframes()), dtype=np.int16).astype(np.float32) / 32768.0


def main():
    salida = RAIZ / "transcripcion"
    salida.mkdir(parents=True, exist_ok=True)
    todos = [p.stem for p in sorted((RAIZ / "wav").glob("*.wav"))]
    ids = sys.argv[1:] or PRIORIDAD + [i for i in todos if i not in PRIORIDAD]
    modelo = WhisperModel("small", device="cpu", compute_type="int8", cpu_threads=4)
    for vid in ids:
        destino = salida / f"{vid}.json"
        if destino.exists():
            continue
        segmentos, _ = modelo.transcribe(leer_wav(RAIZ / "wav" / f"{vid}.wav"), language="es",
                                         word_timestamps=True, vad_filter=True, beam_size=2,
                                         condition_on_previous_text=False)
        datos = [{"inicio": s.start, "fin": s.end, "texto": s.text.strip(),
                  "palabras": [{"p": w.word.strip(), "i": w.start, "f": w.end, "prob": w.probability}
                               for w in (s.words or [])]} for s in segmentos]
        destino.write_text(json.dumps(datos, ensure_ascii=False, indent=0), encoding="utf-8")
        print(vid, len(datos), "segmentos", flush=True)


if __name__ == "__main__":
    main()
