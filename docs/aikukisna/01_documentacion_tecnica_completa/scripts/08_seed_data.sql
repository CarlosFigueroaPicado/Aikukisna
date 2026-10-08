-- ============================================================
-- AIKUKISNA — 08_seed_data.sql
-- Responsabilidad única: datos semilla de CATÁLOGO (66 filas).
--
-- Nota importante sobre alcance de este archivo:
-- Este archivo NO incluye el contenido masivo del diccionario
-- (palabra: 63,568 filas; traduccion: 56,639 filas;
-- leccion_palabra: 355; oracion_ejemplo: 414;
-- cultura_contenido: 51). Se probó en este mismo proyecto que
-- un solo archivo con esa cantidad de filas excede el límite de
-- tamaño de consulta del SQL Editor de Supabase. Esos datos se
-- entregan aparte, en database/seed_data_masivo/, divididos en
-- bloques ejecutables uno a la vez (ver README.md para el orden).
-- ============================================================

-- idioma (2 filas)
INSERT INTO idioma (id, codigo, nombre) VALUES
(1, 'mi', 'Miskito'),
(2, 'es', 'Español');

-- fuente_documento (5 filas)
INSERT INTO fuente_documento (id, titulo, autor, anio, institucion) VALUES
(1, 'Diccionario Español-Miskito / Miskito-Español', 'Dionisio Francisco Melgara Brown', 2008, 'Waspam, Río Coco'),
(2, 'Diccionario Bilingüe de Términos de Medicina Tradicional en Lengua Mikitu', 'Ernesto Scott Lackwood', 2006, 'URACCAN - IMTRADEC'),
(3, 'Aprendamos el Miskito', 'Dionisio Francisco Melgara Brown', 2011, 'Waspam, Río Coco'),
(4, 'Diccionario Miskito-Español', NULL, NULL, 'pueblosoriginarios.com'),
(5, 'Estudio Etnográfico sobre los Indios Miskitos y Sumus de Honduras y Nicaragua', 'Eduard Conzemius', 1932, 'Smithsonian Institution (trad. esp. 1984, Asociación Libro Libre)');

-- categoria (10 filas)
INSERT INTO categoria (id, nombre) VALUES
(1, 'General'), (2, 'Salud'), (3, 'Plantas Medicinales'), (4, 'Números'),
(5, 'Expresiones Idiomáticas'), (6, 'Familia'), (7, 'Comida'),
(8, 'Religión'), (9, 'Cuerpo'), (10, 'Gramática');

-- logro (12 filas)
INSERT INTO logro (id, nombre, descripcion, condicion_tipo, condicion_valor, categoria_id) VALUES
(1, 'Primeros pasos', 'Completaste tu primera lección', 'lecciones_completadas', 1, NULL),
(2, 'Estudiante dedicado', 'Completaste 5 lecciones', 'lecciones_completadas', 5, NULL),
(3, 'Casi políglota', 'Completaste 10 lecciones', 'lecciones_completadas', 10, NULL),
(4, 'Maestro del Miskito', 'Completaste las 37 lecciones', 'lecciones_completadas', 37, NULL),
(5, 'Racha de una semana', '7 días seguidos de actividad', 'racha_maxima', 7, NULL),
(6, 'Racha de un mes', '30 días seguidos de actividad', 'racha_maxima', 30, NULL),
(7, 'Coleccionista de palabras', '10 palabras marcadas como favoritas', 'palabras_favoritas', 10, NULL),
(8, 'Bibliófilo del Miskito', '50 palabras marcadas como favoritas', 'palabras_favoritas', 50, NULL),
(9, 'Primera charla con Tuki', 'Tu primera conversación con Tuki', 'memorias_tuki', 1, NULL),
(10, 'Amigo de Tuki', '20 interacciones registradas con Tuki', 'memorias_tuki', 20, NULL),
(11, 'Sabio de la salud', 'Completaste la lección de Medicina/Cuerpo', 'leccion_categoria_completada', 2, 2),
(12, 'Guardián de la familia', 'Completaste la lección de Parentesco', 'leccion_categoria_completada', 6, 6);

-- leccion (37 filas) — 16 capítulos de vocabulario + 21 de frases, en 7 niveles
INSERT INTO leccion (id, titulo, capitulo_numero, nivel, categoria_id, idioma_meta_id) VALUES
(1, 'Capítulo Primero: El Tiempo Pasado del Verbo', 1, 4, 10, 1),
(2, 'Capítulo Segundo: Los Acompañantes del Sustantivo', 2, 4, 10, 1),
(3, 'Capítulo Tercero: El Tiempo Presente del Verbo', 3, 4, 10, 1),
(4, 'Capítulo Cuarto: Las Posposiciones', 4, 4, 10, 1),
(5, 'Capítulo Quinto: El Imperativo y las Formas Negativas del Verbo', 5, 4, 10, 1),
(6, 'Capítulo Sexto: Los Participios', 6, 4, 10, 1),
(7, 'Capítulo Séptimo: Formas del Sustantivo', 7, 5, 10, 1),
(8, 'Capítulo Octavo: El Tiempo Futuro y una Manera de Expresar Obligación', 8, 5, 10, 1),
(9, 'Capítulo Noveno: Las Conjunciones, el Participio Pasado y los Usos del Verbo daukaia', 9, 5, 10, 1),
(10, 'Capítulo Décimo: Más Conjunciones y Yawan', 10, 5, 10, 1),
(11, 'Capítulo Undécimo: El Parentesco', 11, 5, 6, 1),
(12, 'Capítulo Duodécimo: Comidas y Cocina', 12, 5, 7, 1),
(13, 'Capítulo Décimotercero: La Palabra de Dios', 13, 6, 8, 1),
(14, 'Capítulo Décimocuarto: El Cuerpo y sus Enfermedades', 14, 6, 9, 1),
(15, 'Capítulo Décimoquinto: Wan Lal ba (La Cabeza)', 15, 6, 9, 1),
(16, 'Capítulo Décimosexto: Sustantivos Derivados del Verbo', 16, 6, 10, 1),
(17, 'Expresiones Idiomáticas 1', NULL, 1, 5, 1),
(18, 'Expresiones Idiomáticas 2', NULL, 1, 5, 1),
(19, 'Expresiones Idiomáticas 3', NULL, 1, 5, 1),
(20, 'Expresiones Idiomáticas 4', NULL, 1, 5, 1),
(21, 'Expresiones Idiomáticas 5', NULL, 1, 5, 1),
(22, 'Expresiones Idiomáticas 6', NULL, 1, 5, 1),
(23, 'Expresiones Idiomáticas 7', NULL, 2, 5, 1),
(24, 'Expresiones Idiomáticas 8', NULL, 2, 5, 1),
(25, 'Expresiones Idiomáticas 9', NULL, 2, 5, 1),
(26, 'Expresiones Idiomáticas 10', NULL, 2, 5, 1),
(27, 'Comidas y Cocina (frases) 1', NULL, 3, 7, 1),
(28, 'Comidas y Cocina (frases) 2', NULL, 3, 7, 1),
(29, 'Comidas y Cocina (frases) 3', NULL, 3, 7, 1),
(30, 'Tiempo y Hora 1', NULL, 3, 5, 1),
(31, 'Números 1', NULL, 7, 4, 1),
(32, 'Números 2', NULL, 7, 4, 1),
(33, 'Números 3', NULL, 7, 4, 1),
(34, 'Números 4', NULL, 7, 4, 1),
(35, 'Números 5', NULL, 7, 4, 1),
(36, 'Números 6', NULL, 7, 4, 1),
(37, 'Números 7', NULL, 7, 4, 1);

-- Sincronizar las secuencias de autoincremento tras insertar con IDs
-- explícitos (si esto no se hace, el próximo INSERT sin ID choca
-- con un id ya usado — bug real que ocurrió y se corrigió en este
-- proyecto).
SELECT setval('idioma_id_seq', (SELECT max(id) FROM idioma));
SELECT setval('fuente_documento_id_seq', (SELECT max(id) FROM fuente_documento));
SELECT setval('categoria_id_seq', (SELECT max(id) FROM categoria));
SELECT setval('logro_id_seq', (SELECT max(id) FROM logro));
SELECT setval('leccion_id_seq', (SELECT max(id) FROM leccion));
