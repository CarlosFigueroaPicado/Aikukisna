import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from entrenar_traductor_byt5 import leer_jsonl, preparar_datos, texto_entrada


class PreparacionEntrenamientoTest(unittest.TestCase):
    def setUp(self):
        self.temporal = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporal.cleanup)
        self.corpus = Path(self.temporal.name)
        self.filas = {}
        for nombre in ("entrenamiento", "validacion", "prueba"):
            directa = {
                "origen": f"origen {nombre}", "destino": f"destino {nombre}",
                "idioma_origen_id": 1, "idioma_destino_id": 2,
                "idioma_origen": "Miskito", "idioma_destino": "Español",
                "estado_validacion": "documentada", "validacion_humana": False,
            }
            inversa = {
                **directa, "origen": directa["destino"], "destino": directa["origen"],
                "idioma_origen_id": 2, "idioma_destino_id": 1,
                "idioma_origen": "Español", "idioma_destino": "Miskito",
            }
            self.filas[nombre] = [directa, inversa]
            self.escribir(nombre, self.filas[nombre])
        self.escribir("lexico_documentado", [])

    def escribir(self, nombre, filas):
        (self.corpus / f"{nombre}.jsonl").write_text(
            "".join(json.dumps(fila, ensure_ascii=False) + "\n" for fila in filas), encoding="utf-8"
        )

    def test_prefijo_corresponde_a_cada_direccion(self):
        directa, inversa = self.filas["entrenamiento"]
        self.assertTrue(texto_entrada(directa).startswith("traducir Miskito a Español: "))
        self.assertTrue(texto_entrada(inversa).startswith("traducir Español a Miskito: "))

    def test_exige_ambas_direcciones_sin_fabricar_inversas(self):
        self.escribir("prueba", self.filas["prueba"][:1])
        _, _, informe = preparar_datos(self.corpus, 1, 2, True)
        self.assertFalse(informe["listo_para_entrenar"])
        self.assertIn("prueba: 2 -> 1", informe["faltantes"])
        _, _, unidireccional = preparar_datos(self.corpus, 1, 2, False)
        self.assertTrue(unidireccional["listo_para_entrenar"])

    def test_detecta_filtracion_invertida_entre_particiones(self):
        self.escribir("prueba", [self.filas["entrenamiento"][1]])
        with self.assertRaisesRegex(ValueError, "equivalencias repetidas"):
            preparar_datos(self.corpus, 1, 2, True)

    def test_excluye_lexico_reservado_y_ya_presente(self):
        self.escribir("lexico_documentado", self.filas["validacion"] + self.filas["entrenamiento"])
        _, lexico, _ = preparar_datos(self.corpus, 1, 2, True)
        self.assertEqual([], lexico)

    def test_no_acepta_datos_rechazados_ni_importados_sin_aprobacion(self):
        base = self.filas["entrenamiento"][0]
        self.escribir("lexico_documentado", [
            {**base, "estado_validacion": "rechazada", "validacion_humana": True},
            {**base, "estado_validacion": "importada", "validacion_humana": False},
            {**base, "estado_validacion": "importada", "validacion_humana": True},
        ])
        filas = leer_jsonl(self.corpus / "lexico_documentado.jsonl", 1, 2)
        self.assertEqual(1, len(filas))
        self.assertTrue(filas[0]["validacion_humana"])

    def test_no_acepta_idiomas_iguales_o_desconocidos(self):
        for origen, destino in [(1, 1), (1, 5)]:
            with self.assertRaises(ValueError):
                preparar_datos(self.corpus, origen, destino, True)


if __name__ == "__main__":
    unittest.main()
