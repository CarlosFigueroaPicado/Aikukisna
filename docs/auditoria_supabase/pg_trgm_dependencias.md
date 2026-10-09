# Auditoría de dependencias de `pg_trgm`

Fecha de comprobación remota: 2026-09-21.

## Estado encontrado

- Extensión: `pg_trgm` 1.6.
- Esquema: `public`.
- Índice dependiente: `public.idx_palabra_texto_trgm`.
- Definición: índice GIN sobre `public.palabra(texto gin_trgm_ops)`.
- Búsqueda Android relacionada: `DiccionarioRepositoryImpl` usa `ilike("texto", "%$query%")` al consultar Supabase.
- RPC, funciones o vistas propias que llamen `similarity`, `word_similarity`, `gin_trgm_ops` o `gist_trgm_ops`: no encontradas en el proyecto local.

## Decisión

No se movió la extensión. El índice depende del operador instalado por `pg_trgm` y sostiene la búsqueda remota del diccionario. Antes de moverla se necesita probar, en una rama o entorno no productivo, la recreación del índice con el nuevo esquema y ejecutar `EXPLAIN (ANALYZE, BUFFERS)` sobre las consultas reales.

No se preparó una migración de movimiento porque actualmente existe riesgo de degradar o interrumpir las búsquedas.
