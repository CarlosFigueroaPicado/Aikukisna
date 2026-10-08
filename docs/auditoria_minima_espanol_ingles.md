# Auditoría mínima de producción: español e inglés estándar

## Resultado

La réplica contiene siete reglas: tres de español y cuatro de inglés estándar. Ninguna tiene ejemplos asociados en `ejemplo_regla_gramatical`. El catálogo ejecutable ya clasifica las siete como `no_ejecutable`, por lo que el comportamiento conservador actual es `NoAplicable`.

No se añadieron paradigmas, conjugaciones, irregularidades ni ejemplos inferidos.

## Español

La semilla contiene:

- concordancia sujeto–verbo;
- concordancia de género y número;
- estructura básica de la oración.

Las tres reglas remiten a `fuente_id = 50`, *Nueva gramática básica de la lengua española*, de RAE/ASALE. La fuente oficial confirma que se trata de una obra descriptiva y normativa publicada en 2011. Las reglas se conservan solo para análisis pedagógico porque la semilla no contiene ejemplos enlazados, paradigmas ni excepciones suficientes para generación.

Fuente: https://www.rae.es/obras-academicas/gramatica/nueva-gramatica-basica

Inventario existente en la réplica:

- 32 412 palabras españolas.
- 4 245 expresiones españolas.

Ese volumen léxico no se convirtió en evidencia gramatical: palabra o expresión documentada no equivale a paradigma, concordancia o función sintáctica validada.

## Inglés estándar

La semilla contiene:

- presente simple;
- preguntas con `do/does`;
- negación con `do/does not`;
- pronombres personales.

Las primeras tres reglas remiten a `fuente_id = 52`, British Council LearnEnglish. La página oficial documenta la forma base, la tercera persona singular, preguntas con `do/does` y negaciones con `don't/doesn't`. Se mantienen en `NoAplicable` porque no existen ejemplos enlazados dentro de la semilla ni tratamiento local completo de excepciones, `be`, `have`, ortografía o irregularidades.

Fuente: https://learnenglish.britishcouncil.org/free-resources/grammar/english-grammar-reference/present-simple

La regla de pronombres personales usa `fuente_id = 53`, pero ese registro apunta a una página de pronunciación de Cambridge. Esa fuente no sustenta el paradigma de pronombres. La regla queda pendiente de corregir trazabilidad y no debe aprobarse para un corpus nuevo.

Inventario existente en la réplica:

- 6 235 palabras de inglés estándar.
- 1 364 expresiones de inglés estándar.

## Decisión mínima de producción

- Seis reglas quedan documentadas exclusivamente para análisis.
- Una regla —pronombres ingleses— queda pendiente por fuente incorrecta.
- Siete reglas permanecen fuera de generación.
- No se crea un sembrado adicional para español o inglés en esta fase: las reglas ya existen en la réplica y el catálogo las bloquea como `no_ejecutable`.
- Toda solicitud que dependa de paradigmas, irregularidades o análisis no documentado debe devolver `NoAplicable`.

Archivo trazable: `docs/evidencia_minima_espanol_ingles_revision.jsonl`.
