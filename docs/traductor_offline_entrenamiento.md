# Traducción de oraciones offline y bidireccional

El traductor conversacional requiere un modelo de oraciones. La consulta
exacta de Room, el reconocimiento de voz y la concatenación de palabras no
sustituyen ese modelo. El APK actual todavía no incorpora un modelo de
traducción entrenado para Miskito o Kriol.

## Preparación del entrenamiento

`scripts/entrenar_traductor_byt5.py` usa ambos sentidos del par por defecto.
Cada ejemplo incluye su idioma de origen y destino en el prefijo. No crea
traducciones nuevas, no usa un idioma puente y no invierte automáticamente
ejemplos: utiliza las direcciones presentes en el corpus aprobado.

La separación de entrenamiento, validación y prueba considera la misma
equivalencia en ambos sentidos. El léxico auxiliar excluye equivalencias de
validación y prueba. El script rechaza particiones contaminadas, direcciones
vacías y carpetas de salida que ya contienen resultados.

Cada sentido produce métricas y predicciones separadas para revisión humana.
Una puntuación automática no constituye aprobación lingüística ni habilita
la instalación del modelo en Android.

## Cobertura observada

Los conteos son por dirección, con el mismo número en el sentido inverso.
Incluyen expresiones y oraciones, no exclusivamente conversación espontánea.

| Par | Entrenamiento | Validación | Prueba |
| --- | ---: | ---: | ---: |
| Miskito ↔ español | 2752 | 352 | 363 |
| Kriol ↔ inglés estándar | 281 | 37 | 41 |
| Kriol ↔ español | 83 | 11 | 8 |
| Español ↔ inglés estándar | 879 | 117 | 117 |
| Miskito ↔ inglés estándar | 0 | 0 | 0 |
| Miskito ↔ Kriol | 0 | 0 | 0 |

Estos conteos permiten comprobar la estructura del entrenamiento; no
garantizan cobertura suficiente ni calidad. En particular, Kriol tiene una
muestra limitada y las direcciones sin pares requieren material paralelo
adicional o un enfoque que se evalúe explícitamente.

## Comandos

Desde la raíz del proyecto, la verificación no requiere bibliotecas de ML:

```powershell
python scripts/entrenar_traductor_byt5.py --origen 1 --destino 2 --verificar-corpus
python scripts/entrenar_traductor_byt5.py --origen 3 --destino 4 --verificar-corpus
```

El entorno aislado usa `scripts/requirements-traduccion.txt`. El ensayo CPU
entrena un Transformer pequeño desde cero durante 20 pasos, con muestras
separadas por dirección. Sirve únicamente para comprobar entrenamiento,
generación y guardado; no representa el ajuste del ByT5 preentrenado ni
produce un traductor utilizable:

```powershell
.venv-traduccion/Scripts/python.exe scripts/entrenar_traductor_byt5.py --origen 1 --destino 2 --ensayo-cpu
.venv-traduccion/Scripts/python.exe scripts/entrenar_traductor_byt5.py --origen 3 --destino 4 --ensayo-cpu
```

Sin `--ensayo-cpu`, el script descarga y ajusta `google/byt5-small`. Debe
ejecutarse con recursos suficientes. Para conservar los resultados de otra
ejecución, se debe indicar una nueva carpeta con `--salida`.

## Trabajo pendiente para Android

1. Ajustar y evaluar un modelo con corpus suficiente, especialmente Kriol.
2. Revisar traducciones de oraciones nuevas con hablantes competentes en
   ambos sentidos; incluir negación, personas, tiempos y ambigüedades.
3. Exportar y optimizar el modelo para un runtime Android, comprobando
   equivalencia de salida, memoria, tamaño y latencia en el Samsung.
4. Integrar inferencia local primero y apoyo remoto cuando exista conexión,
   sin registrar automáticamente respuestas generadas como datos validados.
5. Probar conversación bidireccional sin red. La voz se valida aparte del
   motor de traducción.

## Ejecución realizada

El entorno `.venv-traduccion` quedó instalado con PyTorch CPU y las
dependencias fijadas. `pip check` pasó sin incompatibilidades. Se ejecutaron
seis pruebas del preparador, todas aprobadas.

Los ensayos Miskito ↔ español y Kriol ↔ inglés estándar finalizaron sus
20 pasos y guardaron pesos, configuración, tokenizer y predicciones. Ambos
obtuvieron chrF++ 0 en cada sentido sobre la pequeña muestra reservada.
No son traductores útiles. Los resultados se conservan en
`docs/evidencia_entrenamiento`; los pesos locales están bajo
`modelos/traduccion/modelos`, excluidos de Git.

La configuración de `google/byt5-small` declara 299 637 760 parámetros.
En CPU, pesos, gradientes y los dos estados Adam en float32 representan
aproximadamente 4,46 GiB, sin activaciones. El script reserva además 2 GiB
como margen inicial; no constituye una garantía de memoria suficiente.

Tras cerrar los procesos de Gradle y Kotlin de la compilación de esta sesión,
la comprobación previa del entrenamiento completo detectó 4,70 GiB libres
frente a una reserva mínima estimada de 6,46 GiB. El entrenamiento completo
no empezó y los pesos preentrenados no se descargaron. Para continuar hay
que liberar más RAM o usar otro equipo; CUDA no es obligatoria, pero la CPU
puede requerir un tiempo de entrenamiento considerable.

No se modificó ni compiló Android durante esta preparación. La app conserva
su implementación anterior: la traducción libre de oraciones offline sigue
pendiente de entrenamiento suficiente, evaluación e integración.

## Ajuste para entrenamiento CPU con memoria limitada

El primer arranque del modelo preentrenado con Adam y lote 4 se detuvo al
observar menos de 1 GiB libre. No produjo un modelo final. El reinicio con
Adam y lote 1 no pasó la comprobación previa de memoria.

El script permite seleccionar Adafactor y calcula la reserva según sus
estados factorizados, en lugar de los dos estados completos de Adam. Se
combina lote 1 con acumulación 32 para conservar 32 ejemplos por actualización.
El checkpointing de gradientes reduce activaciones almacenadas, a cambio de
recalcularlas; no equivale a guardar checkpoints del entrenamiento en disco.

```powershell
$env:OMP_NUM_THREADS = '2'
$env:MKL_NUM_THREADS = '2'
.venv-traduccion/Scripts/python.exe -u scripts/entrenar_traductor_byt5.py --origen 1 --destino 2 --lote 1 --acumulacion 32 --ahorrar-memoria --optimizador adafactor
```

El primer intento con Adafactor tampoco pasó la comprobación previa: las
aplicaciones habían vuelto a ocupar memoria. Tras reducir otra vez sus
conjuntos de trabajo sin cerrarlas, el arranque del 1 de octubre se registra en
`docs/evidencia_entrenamiento/entrenamiento_miskito_espanol_adafactor_20261001_062343.log`.
El registro de arranque no demuestra que las ocho épocas hayan finalizado:
los resultados finales deben comprobarse en `evaluacion_prueba.json` y en
las predicciones por dirección del directorio del modelo, no en los ensayos.

En este arranque se observó la actualización 1/1656 completada, con 3,31 GiB
de RAM libre. La estimación inicial de la barra fue de unas 46 horas para
los pasos de entrenamiento; es provisional y no representa el tiempo de
evaluación y guardado. El proceso Python observado fue PID 37280. Kriol
todavía no tiene un entrenamiento completo iniciado.
