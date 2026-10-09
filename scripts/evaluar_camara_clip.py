"""Mide un modelo de imagen y texto (CLIP) con las fotos de prueba de la cámara.

Uso: .venv-traduccion/Scripts/python.exe scripts/evaluar_camara_clip.py <carpeta_fotos> <modelo_hf> [<modelo_hf> ...]
El nombre de cada foto lleva el concepto esperado: 03_banano.jpg -> "banano".
"""
import json
import os
import sys
import time

import torch
from PIL import Image
from transformers import CLIPModel, CLIPProcessor

RAIZ = os.path.dirname(os.path.abspath(__file__))
VOCAB = json.load(open(os.path.join(RAIZ, 'datos', 'camara_vocabulario.json'), encoding='utf-8'))
ESPERADO = {'taza': 'taza', 'silla': 'silla', 'banano': 'banano', 'perro': 'perro', 'gato': 'gato',
            'botellas': 'botella', 'libro': 'libro', 'zapato': 'zapato', 'laptop': 'computadora', 'lapiz': 'lápiz',
            'gallina': 'gallina', 'coco': 'coco', 'mango': 'mango', 'vaso': 'vaso'}


def embeddings_texto(modelo, procesador):
    vectores = []
    for concepto in VOCAB['conceptos']:
        frases = [p.format(d) for d in concepto['en'] for p in VOCAB['plantillas']]
        entrada = procesador(text=frases, return_tensors='pt', padding=True)
        with torch.no_grad():
            v = modelo.get_text_features(**entrada)
        v = v / v.norm(dim=-1, keepdim=True)
        v = v.mean(dim=0)
        vectores.append(v / v.norm())
    return torch.stack(vectores)


def evaluar(nombre, fotos):
    modelo = CLIPModel.from_pretrained(nombre).eval()
    procesador = CLIPProcessor.from_pretrained(nombre)
    textos = embeddings_texto(modelo, procesador)
    aciertos, top3, tiempos = 0, 0, []
    for archivo in fotos:
        clave = os.path.splitext(archivo)[0].split('_', 1)[1].split('_')[0]
        esperado = ESPERADO.get(clave)
        imagen = Image.open(os.path.join(sys.argv[1], archivo)).convert('RGB')
        inicio = time.time()
        with torch.no_grad():
            v = modelo.get_image_features(**procesador(images=imagen, return_tensors='pt'))
        tiempos.append(time.time() - inicio)
        v = v / v.norm(dim=-1, keepdim=True)
        prob = (100 * v @ textos.T).softmax(dim=-1)[0]
        mejores = prob.topk(3)
        nombres = [VOCAB['conceptos'][i]['es'] for i in mejores.indices.tolist()]
        aciertos += nombres[0] == esperado
        top3 += esperado in nombres
        print(f'  {archivo:22} esperado={esperado:12} -> ' +
              ', '.join(f'{n} {p:.2f}' for n, p in zip(nombres, mejores.values.tolist())))
    parametros = sum(p.numel() for p in modelo.vision_model.parameters()) / 1e6
    print(f'{nombre}: top1 {aciertos}/{len(fotos)}  top3 {top3}/{len(fotos)}  '
          f'visión {parametros:.0f}M parámetros  {1000 * sum(tiempos) / len(tiempos):.0f} ms/foto (CPU PC)\n')


if __name__ == '__main__':
    fotos = sorted(f for f in os.listdir(sys.argv[1]) if f.endswith('.jpg'))
    for nombre in sys.argv[2:]:
        evaluar(nombre, fotos)
