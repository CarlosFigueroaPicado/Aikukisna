"""Genera la página para revisar a oído los clips de El Miskito Hamilton.

Uso:    python scripts/generar_revision_audios.py
Abrir:  servir fuentes_audio/hamilton/ (p. ej. python -m http.server 8765) y entrar a /revision.html
Las decisiones se guardan en el navegador y se exportan con "Descargar decisiones" a decisiones.json.
"""
import json
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent / "fuentes_audio" / "hamilton"

PLANTILLA = """<!doctype html>
<html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Revisión de audios Miskitu</title>
<style>
:root{--fondo:#f6f5f1;--tarjeta:#fff;--texto:#1d1d1b;--suave:#6b6b66;--ok:#1f7a4d;--no:#b3261e;--borde:#ddd9cf;--acento:#0b5cad}
@media (prefers-color-scheme:dark){:root{--fondo:#161614;--tarjeta:#21211e;--texto:#ecebe6;--suave:#a3a29b;--borde:#3a3934;--acento:#7db4ff}}
*{box-sizing:border-box}body{margin:0;background:var(--fondo);color:var(--texto);font:16px/1.45 system-ui,sans-serif}
header{position:sticky;top:0;background:var(--fondo);padding:12px 16px;border-bottom:1px solid var(--borde);display:flex;gap:12px;flex-wrap:wrap;align-items:center;z-index:2}
h1{font-size:18px;margin:0;flex:1 1 auto}.barra{height:6px;background:var(--borde);border-radius:3px;flex:1 1 100%}.barra>i{display:block;height:100%;background:var(--ok);border-radius:3px}
button{font:inherit;border:1px solid var(--borde);background:var(--tarjeta);color:var(--texto);border-radius:8px;padding:8px 12px;cursor:pointer}
button.ok{border-color:var(--ok)}button.no{border-color:var(--no);color:var(--no)}button.sel{background:var(--ok);color:#fff;border-color:var(--ok)}button.sel.no{background:var(--no);color:#fff}
main{max-width:760px;margin:0 auto;padding:16px}.ayuda{color:var(--suave);font-size:14px}
.clip{background:var(--tarjeta);border:1px solid var(--borde);border-radius:12px;padding:14px;margin:0 0 12px}
.clip.actual{outline:2px solid var(--acento)}.fila{display:flex;gap:8px;flex-wrap:wrap;align-items:center;margin-top:8px}
.meta{color:var(--suave);font-size:13px}.forma{font-weight:600}audio{width:100%;margin-top:8px}
</style></head><body>
<header><h1>Audios Miskitu — El Miskito Hamilton</h1><span id="cuenta"></span>
<button id="exportar">Descargar decisiones</button><div class="barra"><i id="progreso"></i></div></header>
<main><p class="ayuda">Escucha cada clip y elige la palabra que <b>realmente</b> dice, completa y clara. Si se corta,
dice otra cosa o mezcla español, pulsa <b>Rechazar</b>. Teclado: <b>Espacio</b> reproducir · <b>1–4</b> elegir ·
<b>R</b> rechazar · <b>↓/↑</b> siguiente/anterior.</p><div id="lista"></div></main>
<script>
const CLIPS = __DATOS__;
const CLAVE = "revision_audios_hamilton";
let decisiones = {}; try { decisiones = JSON.parse(localStorage.getItem(CLAVE) || "{}"); } catch (e) {}
let actual = 0;
// Se guarda en el navegador y también en el servidor local (decisiones.json), que es lo que se empaqueta.
const enviar = () => fetch("/decisiones", {method: "POST", headers: {"Content-Type": "application/json"},
  body: JSON.stringify(decisiones)}).catch(() => {});
const guardar = () => { try { localStorage.setItem(CLAVE, JSON.stringify(decisiones)); } catch (e) {} enviar(); pintarCuenta(); };
function pintarCuenta(){ const n = CLIPS.filter(c => c.clip in decisiones).length; document.getElementById("cuenta").textContent = n + " / " + CLIPS.length;
  document.getElementById("progreso").style.width = (100*n/CLIPS.length) + "%"; }
function decidir(i, valor){ decisiones[CLIPS[i].clip] = valor; guardar(); pintar(i); mover(i+1); }
function pintar(i){ const c = CLIPS[i], d = decisiones[c.clip], el = document.getElementById("c"+i);
  el.querySelectorAll("button[data-v]").forEach(b => b.classList.toggle("sel", b.dataset.v === d)); }
function mover(i){ if (i < 0 || i >= CLIPS.length) return; document.getElementById("c"+actual)?.classList.remove("actual");
  actual = i; const el = document.getElementById("c"+i); el.classList.add("actual"); el.scrollIntoView({block:"center"});
  const a = el.querySelector("audio"); a.currentTime = 0; a.play().catch(()=>{}); }
const lista = document.getElementById("lista");
CLIPS.forEach((c, i) => {
  const div = document.createElement("div"); div.className = "clip"; div.id = "c"+i;
  const yt = "https://www.youtube.com/watch?v=" + c.video + "&t=" + Math.max(0, Math.floor(c.inicio) - 3);
  div.innerHTML = `<div class="meta">#${i+1} · Whisper oyó “${c.oido}” · <a href="${yt}" target="_blank" rel="noopener">ver en el video</a></div>
    <audio controls preload="none" src="clips/${c.clip}"></audio><div class="fila"></div>`;
  const fila = div.querySelector(".fila");
  c.opciones.forEach((o, k) => { const b = document.createElement("button"); b.className = "ok"; b.dataset.v = o.forma;
    b.innerHTML = `${k+1}. <span class="forma">${o.forma}</span> <span class="meta">(${o.glosas.join(", ")})</span>`;
    b.onclick = () => decidir(i, o.forma); fila.appendChild(b); });
  const r = document.createElement("button"); r.className = "no"; r.dataset.v = "rechazado"; r.textContent = "Rechazar";
  r.onclick = () => decidir(i, "rechazado"); fila.appendChild(r);
  div.addEventListener("click", e => { if (e.target.tagName !== "BUTTON" && e.target.tagName !== "A") { document.getElementById("c"+actual)?.classList.remove("actual"); actual = i; div.classList.add("actual"); } });
  lista.appendChild(div); pintar(i);
});
document.addEventListener("keydown", e => {
  const c = CLIPS[actual]; if (!c) return;
  if (e.key === " ") { e.preventDefault(); const a = document.querySelector("#c"+actual+" audio"); a.currentTime = 0; a.play(); }
  else if (e.key === "ArrowDown") { e.preventDefault(); mover(actual+1); }
  else if (e.key === "ArrowUp") { e.preventDefault(); mover(actual-1); }
  else if (e.key.toLowerCase() === "r") decidir(actual, "rechazado");
  else if (/^[1-4]$/.test(e.key) && c.opciones[+e.key-1]) decidir(actual, c.opciones[+e.key-1].forma);
});
document.getElementById("exportar").onclick = () => {
  const blob = new Blob([JSON.stringify(decisiones, null, 1)], {type: "application/json"});
  const a = document.createElement("a"); a.href = URL.createObjectURL(blob); a.download = "decisiones.json"; a.click(); };
fetch("/decisiones", {cache: "no-store"}).then(r => r.ok ? r.json() : {}).then(guardadas => {
  decisiones = Object.assign({}, guardadas, decisiones); CLIPS.forEach((_, i) => pintar(i)); guardar();
  const primero = CLIPS.findIndex(c => !(c.clip in decisiones)); if (primero > 0) mover(primero);
}).catch(() => {});
pintarCuenta(); document.getElementById("c0")?.classList.add("actual");
</script></body></html>
"""


def main():
    clips = json.loads((RAIZ / "candidatos.json").read_text(encoding="utf-8"))
    datos = json.dumps(clips, ensure_ascii=False).replace("</", "<\\/")
    (RAIZ / "revision.html").write_text(PLANTILLA.replace("__DATOS__", datos), encoding="utf-8")
    print(len(clips), "clips en", RAIZ / "revision.html")


if __name__ == "__main__":
    main()
