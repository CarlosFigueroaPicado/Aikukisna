# Comparación del historial de migraciones

Fecha: 2026-09-21.

## Herramienta

- `supabase` global: no instalado.
- CLI oficial ejecutada mediante `npx`: versión 2.117.0.
- Se consultaron `supabase --help` y `supabase migration list --help`.

## Estado local

Migraciones locales:

- `20260921151606_ampliar_modelo_linguistico.sql`
- `20260921165659_poblar_modelo_linguistico_seguro.sql`
- `20260921165712_optimizar_seguridad_rendimiento.sql`
- `20260921180000_cerrar_rpc_correo_usuario.sql`
- `20260921180100_crear_revision_linguistica.sql`

Las tres primeras reflejan cambios aplicados anteriormente desde SQL Editor. Las dos últimas están preparadas y no se aplicaron.

## Estado remoto

El historial remoto contiene 20 versiones comprendidas entre `20260711162719` y `20260915131633`. Ninguna de las cinco versiones locales aparece registrada.

## Intento de reconciliación

1. `npx supabase@latest login` respondió que la sesión estaba iniciada.
2. `projects list` y `link --project-ref oawvormcsdtphbfijmod` devolvieron `Unauthorized`.
3. No se ejecutó `migration repair`, `db push`, `db pull` ni se modificó `supabase_migrations.schema_migrations`.

## Estado y siguiente paso

La reconciliación queda bloqueada hasta que la CLI disponga de una sesión válida. Con acceso oficial se debe:

1. Ejecutar `supabase link --project-ref oawvormcsdtphbfijmod`.
2. Ejecutar `supabase migration list`.
3. Confirmar nuevamente que el esquema de las tres primeras migraciones ya existe.
4. Usar `supabase migration repair <versión> --status applied` exclusivamente para esas versiones confirmadas.
5. Volver a ejecutar `supabase migration list`.
6. Aplicar las dos migraciones nuevas solamente después de probar el login real por username.

No se reejecutó SQL productivo ya aplicado.
