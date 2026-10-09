"""Exporta el codificador de imagen de TinyCLIP a ONNX (int8) y precalcula los vectores del vocabulario.

Salida (en app/src/main/assets/modelos/):
  - camara_clip_vision.onnx: imagen 224x224 normalizada -> vector unitario
  - camara_clip_conceptos.json: conceptos en español con su vector de texto
Uso: .venv-traduccion/Scripts/python.exe scripts/exportar_camara_clip.py [carpeta_fotos_para_verificar]
"""
import json
import os
import sys

import numpy as np
import onnxruntime as ort
import torch
from onnxruntime.quantization import QuantType, quantize_dynamic
from PIL import Image
from transformers import CLIPModel, CLIPProcessor

MODELO = 'wkcn/TinyCLIP-ViT-39M-16-Text-19M-YFCC15M'
RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SALIDA = os.path.join(RAIZ, 'app', 'src', 'main', 'assets', 'modelos')
VOCAB = json.load(open(os.path.join(RAIZ, 'scripts', 'datos', 'camara_vocabulario.json'), encoding='utf-8'))


class Vision(torch.nn.Module):
    def __init__(self, clip):
        super().__init__()
        self.clip = clip

    def forward(self, pixel_values):
        v = self.clip.get_image_features(pixel_values=pixel_values)
        return v / v.norm(dim=-1, keepdim=True)


def main():
    clip = CLIPModel.from_pretrained(MODELO).eval()
    procesador = CLIPProcessor.from_pretrained(MODELO)

    temporal = os.path.join(SALIDA, 'camara_clip_vision_f32.onnx')
    final = os.path.join(SALIDA, 'camara_clip_vision.onnx')
    torch.onnx.export(Vision(clip), torch.randn(1, 3, 224, 224), temporal, input_names=['pixel_values'],
                      output_names=['embedding'], opset_version=17, dynamo=False)
    # Solo MatMul/Gemm: el ONNX Runtime de Android no implementa ConvInteger (Conv cuantizada).
    quantize_dynamic(temporal, final, weight_type=QuantType.QInt8, op_types_to_quantize=['MatMul', 'Gemm'])
    os.remove(temporal)

    conceptos = []
    for concepto in VOCAB['conceptos']:
        frases = [p.format(d) for d in concepto['en'] for p in VOCAB['plantillas']]
        with torch.no_grad():
            t = clip.get_text_features(**procesador(text=frases, return_tensors='pt', padding=True))
        t = t / t.norm(dim=-1, keepdim=True)
        t = t.mean(dim=0)
        t = t / t.norm()
        conceptos.append({'es': concepto['es'], 'vector': [round(float(x), 5) for x in t]})
    json.dump({'modelo': MODELO, 'escala': float(clip.logit_scale.exp()), 'conceptos': conceptos},
              open(os.path.join(SALIDA, 'camara_clip_conceptos.json'), 'w', encoding='utf-8'), ensure_ascii=False)
    print('ONNX int8:', round(os.path.getsize(final) / 1e6, 1), 'MB;', len(conceptos), 'conceptos')

    if len(sys.argv) > 1:
        # Verificación: el modelo cuantizado debe acertar igual que el original.
        sesion = ort.InferenceSession(final)
        textos = np.array([c['vector'] for c in conceptos], dtype=np.float32)
        for archivo in sorted(f for f in os.listdir(sys.argv[1]) if f.endswith('.jpg')):
            pixeles = procesador(images=Image.open(os.path.join(sys.argv[1], archivo)).convert('RGB'),
                                 return_tensors='np')['pixel_values'].astype(np.float32)
            v = sesion.run(None, {'pixel_values': pixeles})[0][0]
            logits = 100 * textos @ v
            prob = np.exp(logits - logits.max()); prob /= prob.sum()
            i = int(prob.argmax())
            print(f'  {archivo:22} -> {conceptos[i]["es"]} {prob[i]:.2f}')


if __name__ == '__main__':
    main()
