-- ============================================================
-- AIKUKISNA — 05_create_views.sql
-- Responsabilidad única: vistas.
--
-- Las 2 vistas de este archivo no son de relleno: resuelven consultas
-- que ya se escribieron a mano, repetidas, durante el desarrollo del
-- proyecto (búsqueda bidireccional de traducción, y distinguir
-- lecciones de vocabulario vs. lecciones de frases). Exponerlas como
-- vista evita repetir el mismo JOIN complejo en cada lugar de la app.
-- ============================================================

-- Traducción en ambas direcciones en una sola fila por par de palabras.
-- `traduccion` guarda la relación en un solo sentido (origen -> destino);
-- esta vista evita tener que consultar dos veces (una por cada sentido)
-- para saber todas las traducciones de una palabra, sin importar en
-- qué dirección se haya guardado originalmente.
CREATE VIEW vista_traduccion_bidireccional AS
SELECT
    p_origen.id    AS palabra_id,
    p_origen.texto AS palabra_texto,
    p_origen.idioma_id AS palabra_idioma_id,
    p_destino.id    AS traduccion_id,
    p_destino.texto AS traduccion_texto,
    p_destino.idioma_id AS traduccion_idioma_id
FROM traduccion t
JOIN palabra p_origen  ON p_origen.id  = t.palabra_origen_id
JOIN palabra p_destino ON p_destino.id = t.palabra_destino_id
UNION
SELECT
    p_destino.id, p_destino.texto, p_destino.idioma_id,
    p_origen.id, p_origen.texto, p_origen.idioma_id
FROM traduccion t
JOIN palabra p_origen  ON p_origen.id  = t.palabra_origen_id
JOIN palabra p_destino ON p_destino.id = t.palabra_destino_id;

-- Cada lección, con su tipo ya calculado (regla de negocio confirmada
-- sin excepciones sobre las 37 lecciones reales): las que tienen
-- capitulo_numero usan vocabulario suelto (leccion_palabra); las que
-- no, usan frases directas (oracion_ejemplo). La app puede leer el
-- campo `tipo` en vez de repetir el IS NULL en cada pantalla.
CREATE VIEW vista_leccion_tipo AS
SELECT
    l.id,
    l.titulo,
    l.nivel,
    l.capitulo_numero,
    l.idioma_meta_id,
    CASE
        WHEN l.capitulo_numero IS NOT NULL THEN 'vocabulario'
        ELSE 'frases'
    END AS tipo
FROM leccion l;
