# Fuentes para ampliar el corpus: Kriol, Español e Inglés al nivel del Miskito

Investigación del 2026-10-04. Objetivo: que Kriol, Español e Inglés Estándar tengan una cobertura
comparable a la del Miskito, sin incorporar datos cuya licencia lo impida.

## Punto de partida (réplica local, 2026-10-04)

| | Miskito | Español | Kriol | Inglés |
|---|---:|---:|---:|---:|
| Palabras | 33 137 | 32 412 | **327** | 6 235 |
| Palabras con traducción al español | 33 133 | — | 281 | 6 183 |
| Expresiones y frases | 2 848 | 4 245 | 439 | 1 364 |
| Pares de entrenamiento (frases, por dirección) | 2 752 | — | 83 con español / 281 con inglés | 879 con español |

Meta propuesta: **los mismos conceptos** en los cuatro idiomas. Los ~30 000 conceptos en español que
ya tienen Miskito son la lista de referencia: para cada uno se busca su inglés y su Kriol.

## Kriol (Nicaraguan Creole English, ISO 639-3 `bzk`)

El Kriol nicaragüense tiene muy pocos datos abiertos. La literatura lo describe como casi idéntico y
mutuamente inteligible con el Kriol de Belice (`bzj`), que sí tiene un diccionario y textos grandes.

| Fuente | Qué aporta | Cantidad | Licencia | Acción |
|---|---|---:|---|---|
| [APiCS Online — Nicaraguan Creole English](https://apics-online.info/contributions/11) ([CLDF](https://zenodo.org/records/3823888)) | Frases con glosa en inglés | 402 (ya hay ~357 en el corpus) | CC BY 4.0 | **Usar**: importar las ~45 que faltan |
| [Kriol–Inglish Dikshineri (Belize, 2.ª ed. 2024)](http://nationalkriolcouncil.org/the_kriol_langwij_language_arm/dikshineri_dictionary) · [PDF SIL 2009](https://www.sil.org/system/files/reapdata/91/95/49/91954925699176285931841170600318709537/BelizeKriol_EngDic_49337_2009.pdf) | Diccionario Kriol ↔ Inglés | miles de entradas | Derechos reservados (National Kriol Council / SIL) | **Pedir permiso**; luego validar cada entrada con hablantes nicaragüenses |
| [Di Nyoo Testiment eena Bileez Kriol](https://ebible.org/find/details.php?id=bzj) | Frases alineadas por versículo con el inglés | ~7 900 versículos | CC BY-NC-ND 4.0 (Wycliffe) | **Pedir permiso**: "sin obras derivadas" impide entrenar sin autorización |
| [Decker y Keener (2001), informe SIL sobre el Kriol de Bluefields y Corn Island](https://www.sil.org/system/files/reapdata/13/58/54/135854443796468557762993980716255697939/SILESR2001_004.pdf) | Descripción y posibles listas de palabras | por revisar | SIL (descarga manual; bloquea descargas automáticas) | Descargar a mano y revisar |
| Holm (1978), *The Creole English of Nicaragua's Miskito Coast* (tesis, U. de Londres) | Léxico comparado del Kriol nicaragüense | por revisar | Derechos reservados | Consulta bibliográfica para validar; no copiar |
| [Global Recordings — Nicaragua English Creole](https://globalrecordings.net/en/language/bzk) | Audio | — | Restringida | Solo referencia de pronunciación |
| [Wiktionary — Nicaraguan Creole](https://en.wiktionary.org/wiki/Category:Nicaraguan_Creole_language) | — | **0 entradas** | — | Sin datos |
| URACCAN / BICU y el Programa de Educación Intercultural Bilingüe (MINED) | Textos escolares en Kriol, hablantes | — | Institucional | **Convenio**: la vía real para llegar al nivel del Miskito |

**Conclusión para Kriol:** no existe en internet un corpus abierto que lo ponga al nivel del Miskito.
La ruta viable tiene dos partes:

1. Pedir permiso para usar el diccionario de Belice y el Nuevo Testamento como **borrador**.
2. Validar y adaptar ese borrador con hablantes de Bluefields, Corn Island y Pearl Lagoon (URACCAN/BICU),
   usando como guía la lista de conceptos de referencia.

Las entradas de Belice deben marcarse con su origen hasta que un hablante nicaragüense las valide.

### Revisión del 2026-10-07 (paridad de lecciones)

Al alinear el Inglés al MINED (104 lecciones) se crearon lecciones paralelas solo con vocabulario documentado.
El Kriol quedó en 18 lecciones porque el diccionario tiene 327 palabras en Kriol y solo 86 de las 776 palabras
de esas lecciones tienen equivalente documentado. Nuevas búsquedas, sin resultado utilizable:

| Fuente revisada | Resultado |
|---|---|
| Biblioteca digital del MINED (incluida la categoría Primaria SEAR) | No hay materiales en Kriol ni en Miskitu; solo Español SEAR 2.º grado |
| [Global Recordings — Bluefields](https://globalrecordings.net/en/language/15049) | No tiene grabaciones en este idioma |
| [Decker y Keener (2001), SIL](https://www.sil.org/resources/archives/9216) | Informe sociolingüístico de 15 páginas, sin léxico utilizable |
| Artículos de *Ciencia e Interculturalidad* (URACCAN, CAMJOL) | Describen el Kriol en la escuela, sin glosarios; licencia CC BY-NC-ND (sin obras derivadas) |
| APiCS (CLDF, CC BY 4.0) | De 402 ejemplos, faltan 33 y son fragmentos ("[...]"), poco útiles para enseñar |

**Problema de procedencia encontrado:** 50 frases "Kriol" atribuidas al *Libro de Texto de Lengua y Literatura,
Sexto Grado* (fuente 6) y al *SEAR* (fuente 8) no pueden venir de esas fuentes y usan ortografía del criollo
jamaicano (*fi*, *nyam*, *ina*). Se marcaron `pendiente_revision` con nota; aparecen en 13 lecciones.

**Herramienta para el convenio:** [kriol_plantilla_recoleccion.csv](kriol_plantilla_recoleccion.csv) lista las
776 palabras de las lecciones alineadas al MINED (nivel, lección, unidad oficial, español, inglés, Kriol ya
documentado) con columnas vacías para que hablantes de Bluefields, Corn Island o Pearl Lagoon escriban la forma
nicaragüense. Es el insumo concreto para la [propuesta de convenio con URACCAN/BICU](solicitudes/03_propuesta_convenio_uraccan_bicu.md);
las cartas de permiso ([National Kriol Council](solicitudes/01_carta_national_kriol_council.md),
[Wycliffe](solicitudes/02_carta_wycliffe_nuevo_testamento.md)) siguen sin enviar.

## Español ↔ Inglés Estándar

Sí hay datos abiertos en abundancia.

| Fuente | Qué aporta | Cantidad | Licencia | Acción |
|---|---|---:|---|---|
| [Wiktionary vía kaikki.org](https://kaikki.org/dictionary/rawdata.html) (`es-extract.jsonl.gz`, 98 MB) | Palabras en español con traducciones al inglés | ~770 000 formas | CC BY-SA 3.0/4.0 | **Usar** para completar inglés en los ~30 000 conceptos de referencia (atribución y misma licencia para el conjunto derivado) |
| [Tatoeba `spa-eng`](https://www.manythings.org/anki/) | Frases cortas Español ↔ Inglés | 144 215 pares | CC BY 2.0 FR (atribuir a cada autor) | **Usar** frases cortas para el traductor y las lecciones; guardar el ID y el autor de cada frase |
| [Apéndice: Nicaraguanismos (Wikcionario)](https://es.wiktionary.org/wiki/Ap%C3%A9ndice:Nicaraguanismos) | Vocabulario del español de Nicaragua | cientos | CC BY-SA | Usar para que el español refleje el uso local |
| Diccionario del Español de Nicaragua (Academia Nicaragüense, 2007) | Referencia del español nicaragüense | — | Derechos reservados | Solo consulta |

Con Wiktionary, el inglés pasaría de 6 235 a decenas de miles de palabras alineadas con los mismos
conceptos que el Miskito. Además, mejoraría la traducción Miskito ↔ Inglés, que se hace pasando por el español.

## Problema detectado en la app

La app usa el código `jam` para "Inglés Kriol", pero `jam` es el **criollo jamaicano**. El código
correcto del Kriol nicaragüense es `bzk`. Afecta, por ejemplo, el reconocimiento de voz y los metadatos.

## Siguientes pasos propuestos

1. ~~Descargar Wiktionary (98 MB) y Tatoeba (~5 MB), generar pares Español ↔ Inglés para los conceptos de
   referencia y cargarlos como `importada`, con su fuente registrada.~~ Hecho el
   2026-10-05: 14 992 traducciones de palabras Español → Inglés de Wiktionary (fuente 61; 9 043 palabras nuevas
   en inglés) y 3 772 pares de frases de Tatoeba (fuente 62; la nota de cada par guarda los ID y autores).
   Se excluyeron entradas vulgares u ofensivas, pares mal alineados y frases con contenido religioso o
   político polémico.
2. Importar las frases de APiCS que faltan (CC BY 4.0).
3. Escribir a National Kriol Council / SIL (diccionario) y a Wycliffe (Nuevo Testamento) para pedir permiso.
4. Proponer a URACCAN/BICU un convenio de validación del Kriol, con la lista de conceptos de referencia.
5. ~~Corregir el código `jam` → `bzk`.~~ Hecho (app y tabla `idioma` en Supabase).
