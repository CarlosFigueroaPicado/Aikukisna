# Auditoría documental inicial: Kriol

## Alcance

Esta fase es exclusivamente documental. No aprueba reglas, no crea evidencia ejecutable y no modifica Room, el sembrado ni el motor. La fuente estructural principal encontrada es `fuente_id = 13`, *Nicaraguan Creole English — APiCS Online*, de Angela Bartens.

- Fuente primaria: https://apics-online.info/surveys/11
- Idioma local: `idioma_id = 3`, Inglés Kriol.
- Archivo de revisión: `docs/evidencia_gramatical_kriol_revision.jsonl`.

## Inventario trazable

El archivo de revisión contiene 110 filas independientes:

- 32 reglas documentadas.
- 78 ejemplos completos enlazados a esas reglas.
- 110 filas con `fuente_id = 13`.
- 100 filas aprobadas para sembrado documental local.
- 10 asociaciones permanecen pendientes y fuera del sembrado.
- Las 32 reglas están aprobadas como evidencia documental.
- 68 de los 78 ejemplos están aprobados.
- La aprobación no habilita generación automática.

Distribución de las reglas:

- TAM: 7 reglas.
- Modales: 7 reglas.
- Negación: 2 reglas.
- Artículos: 2 reglas.
- Plural: 1 regla.
- Otras marcas y estructuras: 13 reglas.

Las glosas se conservaron en su idioma documentado:

- 72 ejemplos tienen glosa en inglés estándar (`idioma_glosa_id = 4`).
- 6 ejemplos tienen glosa en español (`idioma_glosa_id = 2`).

El nombre histórico `traduccion_espanol` de la tabla de origen no se tomó como prueba de que todas las glosas fueran españolas.

## Evidencia explícita encontrada

### TAM

Existen reglas y ejemplos enlazados para:

- progresivo posverbal `-in`;
- marcadores TAM preverbales como clase;
- habitual presente `doz/daz`;
- habitual pasado `yuuztu`;
- habitual `stodi`;
- pasado preverbal `mi`;
- progresivo preverbal `de`.

La fuente primaria también enumera formas todavía no representadas como reglas independientes en la réplica: `di(d)`, `mi di`, `don`, `di don`, `gwain`, `gwain go`, `wil`, `waa(n)` y `wuda`. Se registran como faltantes; no se crean automáticamente.

### Modales

Existen reglas para `kyan/kan`, `kyaan`, `hafu/haftu`, `mos`, `mosn`, `waahn` y complementos con `waa(hn)`. La presencia de una variante en el título no implica que todas sus variantes tengan un ejemplo completo: por ejemplo, los ejemplos encontrados respaldan `haftu`, pero no bastan por sí solos para aprobar `hafu`.

### Negación

Existen reglas para negación preverbal `no/nou` y negación pasada `neva`. La fila `nou → now` no demuestra negación y quedó marcada para cotejo específico. No puede utilizarse para validar `nou` como negador.

### Artículos

Existen reglas para el indefinido `a` y el definido `di`. El ejemplo `A gat hediek.` no demuestra el artículo indefinido porque `A` corresponde a la primera persona según su glosa. `di ring → this ring` requiere cotejo frente a la forma `dis ring` visible en APiCS.

### Plural

Existe una regla para `dem` pospuesto, con ejemplos de plural nominal y asociativo. APiCS documenta además plural `-s/-z`, pero la réplica no contiene una regla independiente que permita auditar sus restricciones. No se añadió una regla nueva.

### Ejemplos completos

Los 78 ejemplos enlazados a reglas se conservaron como unidades documentales completas. No se segmentaron automáticamente en sujeto, verbo, objeto, TAM, modalidad o negación.

La réplica también contiene 439 expresiones Kriol —371 documentadas y 68 importadas— y 327 palabras Kriol —303 documentadas y 24 importadas—. No se incorporaron automáticamente al JSONL gramatical porque una expresión o palabra aislada no demuestra por sí sola una función gramatical ni una regla.

## Casos que requieren cotejo específico

Diez ejemplos quedaron marcados como `requiere_cotejo_especifico`, con decisión `pendiente` y `apta_para_sembrado = false`:

- `A gat hediek.` no demuestra artículo indefinido.
- `di ring → this ring` presenta posible conflicto con `dis ring`.
- `nou → now` no demuestra negación.
- `tuu → two` no demuestra grado.
- `Grani, so wier is mai rat?` no demuestra inequívocamente grado ante adjetivo.
- `tu bwai → two boys` no demuestra preposición.
- `Mi fried.`, `Mi iina toun.` y `Dis buk fa mi.` no demuestran el marcador de pasado `mi`.
- `Di watch man woz de.` usa `de` con valor locativo según la glosa, no como progresivo.

Estos hallazgos no invalidan automáticamente las reglas fuente; impiden usar esos ejemplos concretos como prueba de la función a la que están asociados.

## Restricciones aplicadas

- No convertir una regla documentada en regla generativa durante la auditoría.
- No deducir formas Kriol desde inglés estándar ni desde otra variedad criolla.
- No normalizar automáticamente grafías.
- No traducir glosas inglesas como si fueran españolas.
- No promover expresiones o palabras a componentes gramaticales sin vínculo explícito.
- No aprobar variantes por aparecer únicamente en el título de una regla.
- No sembrar ninguna fila hasta completar la revisión individual.

## Estado final de esta fase

El lote mínimo de producción queda cerrado con 100 evidencias aprobadas: 32 reglas y 68 ejemplos. Las diez asociaciones señaladas permanecen desactivadas. Los vacíos TAM y las variantes sin ejemplo completo se aplazan y conservan comportamiento `NoAplicable`; no bloquean producción.
