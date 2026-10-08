-- ============================================================
-- AIKUKISNA — 04_create_indexes.sql
-- Responsabilidad única: índices de rendimiento.
-- Nota: los UNIQUE de 03_create_constraints.sql ya crean su propio
-- índice automáticamente (idioma.codigo, categoria.nombre,
-- traduccion(origen,destino)) — no se duplican aquí.
-- ============================================================

-- Búsqueda de palabras por texto, insensible a mayúsculas/minúsculas.
-- Es el índice más usado: soporta el buscador del diccionario
-- (63,568 filas — sin este índice, cada búsqueda sería un table scan).
CREATE INDEX idx_palabra_texto ON palabra (idioma_id, lower(texto));

-- Acelera el JOIN más frecuente: buscar todas las traducciones
-- de una palabra dada (usado en cada consulta bidireccional).
CREATE INDEX idx_traduccion_origen ON traduccion (palabra_origen_id);
