"""Empaqueta en la app los clips de El Miskito Hamilton aprobados a oído y genera el SQL para Supabase.

Entrada: fuentes_audio/hamilton/candidatos.json + decisiones.json (exportado desde revision.html).
Salida:
  app/src/main/assets/pronunciaciones_humanas/hamilton_*.wav + indice.json (lo lee SembradorAudiosHumanos)
  fuentes_audio/hamilton/audio_pronunciacion.sql (filas para public.audio_pronunciacion, estado verificado)
El crédito va solo en los datos (fuente_id y hablante); el audio no se modifica más allá del recorte.

Uso: python scripts/empaquetar_audios_hamilton.py
"""
import hashlib
import json
import shutil
from pathlib import Path

PROYECTO = Path(__file__).resolve().parent.parent
RAIZ = PROYECTO / "fuentes_audio" / "hamilton"
ASSETS = PROYECTO / "app" / "src" / "main" / "assets" / "pronunciaciones_humanas"
FUENTE_ID = 85                  # fuente_documento "Aprender Miskito (lista de videos de YouTube)"
HABLANTE = "El Miskito Hamilton"
IDIOMA_CODIGO, IDIOMA_ID = "mi", 1


def sql_texto(valor: str) -> str:
    return "'" + valor.replace("'", "''") + "'"


def main():
    clips = {c["clip"]: c for c in json.loads((RAIZ / "candidatos.json").read_text(encoding="utf-8"))}
    decisiones = json.loads((RAIZ / "decisiones.json").read_text(encoding="utf-8"))
    ASSETS.mkdir(parents=True, exist_ok=True)
    for viejo in ASSETS.glob("hamilton_*.wav"):
        viejo.unlink()

    audios, filas = [], []
    for nombre_clip, forma in sorted(decisiones.items()):
        clip = clips.get(nombre_clip)
        if not clip or forma == "rechazado":
            continue
        opcion = next((o for o in clip["opciones"] if o["forma"] == forma), None)
        if not opcion:
            continue
        archivo = f"hamilton_{hashlib.sha1(nombre_clip.encode()).hexdigest()[:12]}.wav"
        shutil.copyfile(RAIZ / "clips" / nombre_clip, ASSETS / archivo)
        audios.append({"archivo": archivo, "idioma": IDIOMA_CODIGO, "forma": forma,
                       "palabra_ids": opcion["palabra_ids"]})
        for palabra_id in opcion["palabra_ids"]:
            filas.append(f"({palabra_id}, {IDIOMA_ID}, {sql_texto(forma)}, {sql_texto(archivo)}, 'humano', "
                         f"{sql_texto(HABLANTE)}, {FUENTE_ID}, {sql_texto(clip['video'])}, {clip['inicio']}, "
                         f"{clip['fin']}, 'verificado')")

    contenido = json.dumps(audios, ensure_ascii=False, sort_keys=True)
    version = int(hashlib.sha1(contenido.encode()).hexdigest()[:7], 16)  # cambia si cambia el contenido
    indice = {"version": version, "fuentes": [{"fuente_id": FUENTE_ID, "hablante": HABLANTE, "audios": audios}]}
    (ASSETS / "indice.json").write_text(json.dumps(indice, ensure_ascii=False, indent=1), encoding="utf-8")

    sql = [f"delete from public.audio_pronunciacion where fuente_id = {FUENTE_ID};"]
    if filas:
        sql.append("insert into public.audio_pronunciacion (palabra_id, idioma_id, forma, archivo, origen, hablante, "
                   "fuente_id, video_id, inicio_s, fin_s, estado_validacion)\nselect v.* from (values\n"
                   + ",\n".join(filas) +
                   "\n) as v(palabra_id, idioma_id, forma, archivo, origen, hablante, fuente_id, video_id, inicio_s, "
                   "fin_s, estado_validacion)\njoin public.palabra p on p.id = v.palabra_id;")
    (RAIZ / "audio_pronunciacion.sql").write_text("\n".join(sql) + "\n", encoding="utf-8")
    tamano = sum(p.stat().st_size for p in ASSETS.glob("hamilton_*.wav")) / 1e6
    print(f"{len(audios)} clips aprobados → {len(filas)} filas; {tamano:.1f} MB en assets")


if __name__ == "__main__":
    main()
