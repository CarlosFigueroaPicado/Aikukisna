"""Sirve fuentes_audio/hamilton/ para revisar los clips y guarda cada decisión en decisiones.json.

Uso: python scripts/servir_revision_audios.py [puerto]   (solo escucha en 127.0.0.1)
La página revision.html envía todas sus decisiones con POST /decisiones; se fusionan con las ya guardadas.
"""
import json
import sys
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent / "fuentes_audio" / "hamilton"
ARCHIVO = RAIZ / "decisiones.json"
VALIDAS = {"rechazado"}


class Manejador(SimpleHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/decisiones":
            self.send_error(404)
            return
        largo = int(self.headers.get("Content-Length", "0"))
        if largo > 2_000_000:
            self.send_error(413)
            return
        try:
            nuevas = json.loads(self.rfile.read(largo))
            assert isinstance(nuevas, dict)
            nuevas = {str(k): str(v) for k, v in nuevas.items() if str(k).endswith(".wav") and "/" not in str(k)}
        except Exception:
            self.send_error(400)
            return
        actuales = json.loads(ARCHIVO.read_text(encoding="utf-8")) if ARCHIVO.exists() else {}
        actuales.update(nuevas)
        ARCHIVO.write_text(json.dumps(actuales, ensure_ascii=False, indent=1, sort_keys=True), encoding="utf-8")
        cuerpo = json.dumps({"guardadas": len(actuales)}).encode()
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(cuerpo)))
        self.end_headers()
        self.wfile.write(cuerpo)

    def do_GET(self):
        if self.path == "/decisiones":
            cuerpo = ARCHIVO.read_bytes() if ARCHIVO.exists() else b"{}"
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(cuerpo)
            return
        super().do_GET()


if __name__ == "__main__":
    puerto = int(sys.argv[1]) if len(sys.argv) > 1 else 8765
    ThreadingHTTPServer(("127.0.0.1", puerto), partial(Manejador, directory=str(RAIZ))).serve_forever()
