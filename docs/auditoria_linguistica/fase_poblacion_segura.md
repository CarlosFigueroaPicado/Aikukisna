# Fase de población lingüística segura

Fecha de verificación remota: 2026-09-21.

## Datos migrados

- Acepciones unívocas creadas: 270.
- Traducciones por acepción creadas: 168.
- Frases documentadas creadas desde `oracion_ejemplo`: 990.
- Traducciones de frase creadas: 499.
- Variantes creadas automáticamente: 0.

Los registros se marcaron como `importada`. Ningún registro se marcó automáticamente como `validada`.

## Criterios

Las traducciones por acepción exigen una relación uno a uno por par de idiomas, textos de una sola palabra, ausencia de duplicados normalizados y lenguas distintas.

Las frases provienen exclusivamente de `oracion_ejemplo`. Los 37,433 candidatos detectados dentro de `palabra` no se migraron masivamente.

## Relaciones del mismo idioma

- Posible expresión: 41.
- Indeterminada: 7.
- Variante ortográfica de alta certeza: 0.

Las 48 relaciones permanecen intactas y requieren revisión lingüística.

## Duplicados

Los 52 grupos no se fusionaron. El reporte JSON propone como candidato técnico el ID menor de cada grupo y conserva IDs, fuentes, traducciones y lecciones. Esta propuesta no autoriza una fusión.

## Integridad verificada

- Relaciones en `palabra_fuente`: 63,708.
- Asociaciones legacy perdidas: 0.
- Duplicados exactos en `palabra_fuente`: 0.
- Textos normalizados vacíos en expresiones migradas: 0.
- Timestamps nulos en acepciones: 0.

## Seguridad y rendimiento

Se añadieron índices para `expresion.fuente_id`, `regla_pronunciacion.fuente_id` y `traduccion_expresion.expresion_destino_id`.

La política `usuario lee sus propios logros` usa ahora `(select auth.uid())`. Se fijó `search_path` vacío en `proteger_columnas_gamificacion`. Se restringió la ejecución pública de `completar_leccion`, `desbloquear_logro` y `crear_usuario_nuevo` sin retirar `SECURITY DEFINER`.

`obtener_correo_por_usuario` continúa accesible para el inicio de sesión por nombre de usuario; permite enumeración de correos y requiere rediseño. `pg_trgm` permanece en `public` porque tiene una dependencia activa. La protección de contraseñas filtradas queda pendiente en la configuración de Auth.

## Historial

Las operaciones remotas se ejecutaron desde SQL Editor. No se manipuló el historial interno de migraciones. La reconciliación con CLI permanece pendiente.
