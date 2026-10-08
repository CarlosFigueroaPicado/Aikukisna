# Diccionario de datos de Aikukisna

## Alcance de la referencia

Catálogo del esquema público consultado en Supabase el 1 de octubre de 2026. Incluye 44 tablas, 78 claves foráneas, 22 vistas y 135 índices. No incluye filas de usuarios ni claves de acceso. Las restricciones se reproducen como SQL descriptivo, no como instrucciones de modificación.

`NOT NULL` significa que el campo no admite nulo. Una FK nullable permite omitir la relación. Las FK solo garantizan existencia de la referencia; no demuestran equivalencia lingüística. Los índices únicos también pueden imponer unicidad sin aparecer como restricciones UNIQUE en pg_constraint.

## acepcion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `palabra_id` | integer | Sí |
| `numero_acepcion` | integer | Sí |
| `definicion` | text | No |
| `contexto` | text | No |
| `categoria_gramatical` | text | No |
| `estado_validacion` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `acepcion_estado_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `acepcion_numero_check`: `CHECK ((numero_acepcion > 0))`.
- `acepcion_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `acepcion_palabra_id_numero_acepcion_key`: `UNIQUE (palabra_id, numero_acepcion)`.
- `acepcion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX acepcion_pkey ON public.acepcion USING btree (id)`.
- `CREATE UNIQUE INDEX acepcion_palabra_id_numero_acepcion_key ON public.acepcion USING btree (palabra_id, numero_acepcion)`.
- `CREATE INDEX idx_acepcion_palabra_estado ON public.acepcion USING btree (palabra_id, estado_validacion)`.
- `CREATE INDEX idx_acepcion_updated_at ON public.acepcion USING btree (updated_at, id)`.

## actividad_leccion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `leccion_id` | integer | Sí |
| `orden` | smallint | Sí |
| `codigo` | text | Sí |
| `tipo` | text | Sí |
| `habilidad_curricular` | text | Sí |
| `mecanica_gamificada` | text | Sí |
| `descripcion_estudiante` | text | Sí |
| `evidencia_aprendizaje` | text | No |
| `xp` | integer | Sí |
| `obligatoria` | boolean | Sí |
| `activa` | boolean | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `actividad_leccion_leccion_id_codigo_key`: `UNIQUE (leccion_id, codigo)`.
- `actividad_leccion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `actividad_leccion_pkey`: `PRIMARY KEY (id)`.
- `actividad_leccion_tipo_check`: `CHECK ((tipo = ANY (ARRAY['enganche'::text, 'vocabulario_contexto'::text, 'comprension_escucha'::text, 'comprension_lectora'::text, 'pronunciacion'::text, 'gramatica_aplicada'::text, 'produccion_oral'::text, 'produccion_escrita'::text, 'interaccion_tuki'::text, 'reto_final'::text, 'retroalimentacion'::text])))`.
- `actividad_leccion_xp_check`: `CHECK ((xp >= 0))`.

### Políticas de acceso

- lectura_publica_actividad_leccion. Operación: SELECT. Roles: anon, authenticated. USING: `activa`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX actividad_leccion_pkey ON public.actividad_leccion USING btree (id)`.
- `CREATE UNIQUE INDEX actividad_leccion_leccion_id_codigo_key ON public.actividad_leccion USING btree (leccion_id, codigo)`.
- `CREATE INDEX idx_actividad_leccion_leccion_orden ON public.actividad_leccion USING btree (leccion_id, orden)`.

## actividad_recurso

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `actividad_id` | bigint | Sí |
| `tipo_recurso` | text | Sí |
| `recurso_id` | bigint | Sí |
| `orden` | smallint | Sí |
| `rol` | text | Sí |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `actividad_recurso_actividad_id_fkey`: `FOREIGN KEY (actividad_id) REFERENCES actividad_leccion(id) ON DELETE CASCADE`.
- `actividad_recurso_pkey`: `PRIMARY KEY (actividad_id, tipo_recurso, recurso_id)`.
- `actividad_recurso_rol_check`: `CHECK ((rol = ANY (ARRAY['principal'::text, 'apoyo'::text, 'distractor'::text, 'contexto'::text])))`.
- `actividad_recurso_tipo_recurso_check`: `CHECK ((tipo_recurso = ANY (ARRAY['palabra'::text, 'expresion'::text, 'oracion'::text, 'regla_gramatical'::text, 'regla_pronunciacion'::text, 'cultura'::text])))`.

### Políticas de acceso

- lectura_publica_actividad_recurso. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX actividad_recurso_pkey ON public.actividad_recurso USING btree (actividad_id, tipo_recurso, recurso_id)`.
- `CREATE INDEX idx_actividad_recurso_actividad_orden ON public.actividad_recurso USING btree (actividad_id, orden)`.

## alineacion_curricular

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `leccion_id` | integer | Sí |
| `fuente_id` | integer | Sí |
| `unidad` | text | No |
| `tema` | text | Sí |
| `nivel_referencia` | text | No |
| `evidencia` | text | No |
| `estado_validacion` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `tipo_alineacion` | text | No |
| `grado_referencia` | text | No |
| `area_curricular` | text | No |
| `competencia` | text | No |
| `indicador_logro` | text | No |
| `contenido_curricular` | text | No |
| `eje_sear` | text | No |
| `referencia_documental` | text | No |
| `pagina_referencia` | text | No |
| `apta_revision_formal` | boolean | Sí |

### Claves e integridad

- `alineacion_curricular_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `alineacion_curricular_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `alineacion_curricular_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `alineacion_curricular_leccion_id_fuente_id_tema_key`: `UNIQUE (leccion_id, fuente_id, tema)`.
- `alineacion_curricular_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica alineacion curricular. Operación: SELECT. Roles: anon, authenticated. USING: `(estado_validacion <> 'rechazada'::text)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX alineacion_curricular_pkey ON public.alineacion_curricular USING btree (id)`.
- `CREATE UNIQUE INDEX alineacion_curricular_leccion_id_fuente_id_tema_key ON public.alineacion_curricular USING btree (leccion_id, fuente_id, tema)`.
- `CREATE INDEX idx_alineacion_curricular_fuente_id ON public.alineacion_curricular USING btree (fuente_id)`.

## categoria

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `nombre` | text | Sí |

### Claves e integridad

- `categoria_nombre_key`: `UNIQUE (nombre)`.
- `categoria_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX categoria_pkey ON public.categoria USING btree (id)`.
- `CREATE UNIQUE INDEX categoria_nombre_key ON public.categoria USING btree (nombre)`.

## cultura_contenido

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `titulo` | text | Sí |
| `contenido` | text | Sí |
| `rango_pagina_inicio` | integer | No |
| `rango_pagina_fin` | integer | No |
| `fuente_id` | integer | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `cultura_contenido_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `cultura_contenido_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX cultura_contenido_pkey ON public.cultura_contenido USING btree (id)`.
- `CREATE INDEX idx_cultura_contenido_fuente_id ON public.cultura_contenido USING btree (fuente_id)`.
- `CREATE INDEX idx_cultura_contenido_updated_at ON public.cultura_contenido USING btree (updated_at, id)`.

## ejemplo_regla_gramatical

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `regla_id` | bigint | Sí |
| `texto_idioma` | text | Sí |
| `traduccion_espanol` | text | No |
| `fuente_id` | integer | No |
| `estado_validacion` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `idioma_traduccion_id` | integer | No |

### Claves e integridad

- `ejemplo_regla_gramatical_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `ejemplo_regla_gramatical_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL`.
- `ejemplo_regla_gramatical_idioma_traduccion_id_fkey`: `FOREIGN KEY (idioma_traduccion_id) REFERENCES idioma(id) ON DELETE SET NULL`.
- `ejemplo_regla_gramatical_pkey`: `PRIMARY KEY (id)`.
- `ejemplo_regla_gramatical_regla_id_fkey`: `FOREIGN KEY (regla_id) REFERENCES regla_gramatical(id) ON DELETE CASCADE`.
- `ejemplo_regla_gramatical_regla_id_texto_idioma_key`: `UNIQUE (regla_id, texto_idioma)`.

### Políticas de acceso

- lectura_publica_ejemplo_regla_gramatical. Operación: SELECT. Roles: anon, authenticated. USING: `(estado_validacion <> 'rechazada'::text)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX ejemplo_regla_gramatical_pkey ON public.ejemplo_regla_gramatical USING btree (id)`.
- `CREATE UNIQUE INDEX ejemplo_regla_gramatical_regla_id_texto_idioma_key ON public.ejemplo_regla_gramatical USING btree (regla_id, texto_idioma)`.
- `CREATE INDEX idx_ejemplo_regla_gramatical_fuente_id ON public.ejemplo_regla_gramatical USING btree (fuente_id)`.
- `CREATE INDEX idx_ejemplo_regla_gramatical_idioma_traduccion_id ON public.ejemplo_regla_gramatical USING btree (idioma_traduccion_id)`.

## etapa_ruta_curricular

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `codigo` | text | Sí |
| `nombre` | text | Sí |
| `orden` | smallint | Sí |
| `modalidad` | text | Sí |
| `grado_origen` | smallint | No |
| `grado_destino` | smallint | No |
| `unidad_pedagogica_ciclo` | text | No |
| `descripcion` | text | Sí |
| `activa` | boolean | Sí |

### Claves e integridad

- `etapa_ruta_curricular_orden_key`: `UNIQUE (orden)`.
- `etapa_ruta_curricular_pkey`: `PRIMARY KEY (codigo)`.

### Políticas de acceso

- lectura_publica_etapa_ruta_curricular. Operación: SELECT. Roles: anon, authenticated. USING: `activa`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX etapa_ruta_curricular_pkey ON public.etapa_ruta_curricular USING btree (codigo)`.
- `CREATE UNIQUE INDEX etapa_ruta_curricular_orden_key ON public.etapa_ruta_curricular USING btree (orden)`.

## evidencia_curricular_leccion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `leccion_id` | integer | Sí |
| `etapa_codigo` | text | Sí |
| `fuente_id` | integer | Sí |
| `grado` | smallint | Sí |
| `asignatura_area` | text | Sí |
| `unidad_oficial` | text | No |
| `competencia_eje_transversal` | text | No |
| `competencia_grado` | text | No |
| `indicador_logro` | text | No |
| `contenido_oficial` | text | No |
| `criterio_evaluacion` | text | No |
| `actividad_aikukisna` | text | No |
| `evidencia_aprendizaje` | text | No |
| `pagina_seccion` | text | No |
| `tipo_correspondencia` | text | Sí |
| `aplica_sear` | boolean | Sí |
| `eje_sear` | text | No |
| `adecuacion_intercultural` | text | No |
| `estado_validacion` | text | Sí |
| `observacion` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `lengua_aplicacion_id` | integer | No |
| `naturaleza_aplicacion` | text | No |
| `es_texto_oficial_literal` | boolean | Sí |

### Claves e integridad

- `evidencia_curricular_leccion_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `evidencia_curricular_leccion_etapa_codigo_fkey`: `FOREIGN KEY (etapa_codigo) REFERENCES etapa_ruta_curricular(codigo)`.
- `evidencia_curricular_leccion_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `evidencia_curricular_leccion_grado_check`: `CHECK (((grado >= 1) AND (grado <= 11)))`.
- `evidencia_curricular_leccion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `evidencia_curricular_leccion_lengua_aplicacion_id_fkey`: `FOREIGN KEY (lengua_aplicacion_id) REFERENCES idioma(id)`.
- `evidencia_curricular_leccion_naturaleza_aplicacion_check`: `CHECK ((naturaleza_aplicacion = ANY (ARRAY['curriculo_nacional'::text, 'adaptacion_linguistica'::text, 'eib_sear'::text, 'puente_transicion'::text])))`.
- `evidencia_curricular_leccion_pkey`: `PRIMARY KEY (id)`.
- `evidencia_curricular_leccion_tipo_correspondencia_check`: `CHECK ((tipo_correspondencia = ANY (ARRAY['literal'::text, 'directa'::text, 'tematica'::text, 'prerrequisito'::text, 'puente'::text])))`.
- `evidencia_documentada_completa_chk`: `CHECK (((estado_validacion <> ALL (ARRAY['documentada'::text, 'validada'::text])) OR ((NULLIF(btrim(COALESCE(competencia_grado, ''::text)), ''::text) IS NOT NULL) AND (NULLIF(btrim(COALESCE(indicador_logro, ''::text)), ''::text) IS NOT NULL) AND (NULLIF(btrim(COALESCE(contenido_oficial, ''::text)), ''::text) IS NOT NULL) AND (NULLIF(btrim(COALESCE(criterio_evaluacion, ''::text)), ''::text) IS NOT NULL))))`.
- `evidencia_sear_completa_chk`: `CHECK (((NOT aplica_sear) OR ((NULLIF(btrim(COALESCE(eje_sear, ''::text)), ''::text) IS NOT NULL) AND (NULLIF(btrim(COALESCE(adecuacion_intercultural, ''::text)), ''::text) IS NOT NULL))))`.

### Políticas de acceso

- lectura_autenticada_evidencia_curricular. Operación: SELECT. Roles: authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX evidencia_curricular_leccion_pkey ON public.evidencia_curricular_leccion USING btree (id)`.
- `CREATE INDEX idx_evidencia_curricular_leccion_leccion ON public.evidencia_curricular_leccion USING btree (leccion_id)`.
- `CREATE INDEX idx_evidencia_curricular_leccion_etapa ON public.evidencia_curricular_leccion USING btree (etapa_codigo)`.
- `CREATE INDEX idx_evidencia_curricular_leccion_fuente ON public.evidencia_curricular_leccion USING btree (fuente_id)`.
- `CREATE INDEX idx_evidencia_curricular_lengua_aplicacion ON public.evidencia_curricular_leccion USING btree (lengua_aplicacion_id)`.

## expresion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `idioma_id` | integer | Sí |
| `texto` | text | Sí |
| `texto_normalizado` | text | Sí |
| `tipo` | text | Sí |
| `estado_validacion` | text | Sí |
| `fuente_id` | integer | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `texto_busqueda` | text | No |

### Claves e integridad

- `expresion_estado_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `expresion_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `expresion_idioma_id_fkey`: `FOREIGN KEY (idioma_id) REFERENCES idioma(id)`.
- `expresion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX expresion_pkey ON public.expresion USING btree (id)`.
- `CREATE INDEX idx_expresion_idioma_texto ON public.expresion USING btree (idioma_id, texto_normalizado)`.
- `CREATE INDEX idx_expresion_updated_at ON public.expresion USING btree (updated_at, id)`.
- `CREATE INDEX idx_expresion_fuente_id ON public.expresion USING btree (fuente_id)`.
- `CREATE INDEX idx_expresion_idioma_texto_busqueda ON public.expresion USING btree (idioma_id, texto_busqueda)`.

## expresion_contexto_cultural

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `expresion_id` | bigint | Sí |
| `cultura_id` | integer | Sí |
| `tipo_relacion` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `expresion_contexto_cultural_cultura_id_fkey`: `FOREIGN KEY (cultura_id) REFERENCES cultura_contenido(id) ON DELETE CASCADE`.
- `expresion_contexto_cultural_expresion_id_fkey`: `FOREIGN KEY (expresion_id) REFERENCES expresion(id) ON DELETE CASCADE`.
- `expresion_contexto_cultural_pkey`: `PRIMARY KEY (expresion_id)`.

### Políticas de acceso

- lectura_publica_expresion_contexto_cultural. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX expresion_contexto_cultural_pkey ON public.expresion_contexto_cultural USING btree (expresion_id)`.
- `CREATE INDEX idx_expresion_contexto_cultural_cultura_id ON public.expresion_contexto_cultural USING btree (cultura_id)`.

## fuente_documento

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `titulo` | text | Sí |
| `autor` | text | No |
| `anio` | integer | No |
| `institucion` | text | No |
| `url` | text | No |
| `licencia` | text | No |
| `nota_uso` | text | No |

### Claves e integridad

- `fuente_documento_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX fuente_documento_pkey ON public.fuente_documento USING btree (id)`.

## idioma

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `codigo` | text | Sí |
| `nombre` | text | Sí |

### Claves e integridad

- `idioma_codigo_key`: `UNIQUE (codigo)`.
- `idioma_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX idioma_pkey ON public.idioma USING btree (id)`.
- `CREATE UNIQUE INDEX idioma_codigo_key ON public.idioma USING btree (codigo)`.

## leccion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `titulo` | text | Sí |
| `capitulo_numero` | integer | No |
| `nivel` | integer | Sí |
| `categoria_id` | integer | No |
| `idioma_meta_id` | integer | Sí |

### Claves e integridad

- `leccion_categoria_id_fkey`: `FOREIGN KEY (categoria_id) REFERENCES categoria(id)`.
- `leccion_idioma_meta_id_fkey`: `FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id)`.
- `leccion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.
- solo superadmin borra lecciones. Operación: DELETE. Roles: public. USING: `(EXISTS ( SELECT 1    FROM super_administrador   WHERE (super_administrador.usuario_id = ( SELECT auth.uid() AS uid))))`. WITH CHECK: `no explícito`.
- solo superadmin crea lecciones. Operación: INSERT. Roles: public. USING: `no explícito`. WITH CHECK: `(EXISTS ( SELECT 1    FROM super_administrador   WHERE (super_administrador.usuario_id = ( SELECT auth.uid() AS uid))))`.
- solo superadmin edita lecciones. Operación: UPDATE. Roles: public. USING: `(EXISTS ( SELECT 1    FROM super_administrador   WHERE (super_administrador.usuario_id = ( SELECT auth.uid() AS uid))))`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_pkey ON public.leccion USING btree (id)`.
- `CREATE INDEX idx_leccion_categoria_id ON public.leccion USING btree (categoria_id)`.
- `CREATE INDEX idx_leccion_idioma_meta_id ON public.leccion USING btree (idioma_meta_id)`.

## leccion_cultura

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `cultura_id` | integer | Sí |
| `tipo_vinculo` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `nota` | text | No |

### Claves e integridad

- `leccion_cultura_cultura_id_fkey`: `FOREIGN KEY (cultura_id) REFERENCES cultura_contenido(id) ON DELETE CASCADE`.
- `leccion_cultura_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_cultura_pkey`: `PRIMARY KEY (leccion_id, cultura_id, tipo_vinculo)`.

### Políticas de acceso

- lectura_publica_leccion_cultura. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_cultura_pkey ON public.leccion_cultura USING btree (leccion_id, cultura_id, tipo_vinculo)`.
- `CREATE INDEX idx_leccion_cultura_cultura_id ON public.leccion_cultura USING btree (cultura_id)`.

## leccion_experiencia_gamificada

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `titulo_visible` | text | Sí |
| `subtitulo_visible` | text | No |
| `formato_principal` | text | Sí |
| `xp_base` | integer | Sí |
| `usa_tuki` | boolean | Sí |
| `usa_audio` | boolean | Sí |
| `usa_lectura` | boolean | Sí |
| `usa_escritura` | boolean | Sí |
| `usa_oralidad` | boolean | Sí |
| `tiene_reto_final` | boolean | Sí |
| `mostrar_info_curricular` | boolean | Sí |
| `activa` | boolean | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `mundo_codigo` | text | No |
| `orden_en_mundo` | integer | No |
| `recompensa_visible` | text | No |
| `mensaje_inicio_tuki` | text | No |
| `mensaje_fin_tuki` | text | No |

### Claves e integridad

- `leccion_experiencia_gamificada_formato_principal_check`: `CHECK ((formato_principal = ANY (ARRAY['mision'::text, 'historia'::text, 'reto'::text, 'dialogo'::text, 'exploracion'::text, 'jefe'::text])))`.
- `leccion_experiencia_gamificada_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_experiencia_gamificada_mundo_codigo_fkey`: `FOREIGN KEY (mundo_codigo) REFERENCES mundo_gamificado(codigo)`.
- `leccion_experiencia_gamificada_pkey`: `PRIMARY KEY (leccion_id)`.
- `leccion_experiencia_gamificada_xp_base_check`: `CHECK ((xp_base >= 0))`.

### Políticas de acceso

- lectura_publica_leccion_experiencia_gamificada. Operación: SELECT. Roles: anon, authenticated. USING: `activa`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_experiencia_gamificada_pkey ON public.leccion_experiencia_gamificada USING btree (leccion_id)`.
- `CREATE INDEX idx_leccion_experiencia_gamificada_mundo ON public.leccion_experiencia_gamificada USING btree (mundo_codigo)`.

## leccion_expresion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `expresion_id` | bigint | Sí |
| `tipo_vinculo` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_expresion_expresion_id_fkey`: `FOREIGN KEY (expresion_id) REFERENCES expresion(id) ON DELETE CASCADE`.
- `leccion_expresion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_expresion_pkey`: `PRIMARY KEY (leccion_id, expresion_id, tipo_vinculo)`.

### Políticas de acceso

- lectura_publica_leccion_expresion. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_expresion_pkey ON public.leccion_expresion USING btree (leccion_id, expresion_id, tipo_vinculo)`.
- `CREATE INDEX idx_leccion_expresion_expresion ON public.leccion_expresion USING btree (expresion_id)`.

## leccion_fuente

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `fuente_id` | integer | Sí |
| `tipo_vinculo` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_fuente_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `leccion_fuente_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_fuente_pkey`: `PRIMARY KEY (leccion_id, fuente_id, tipo_vinculo)`.

### Políticas de acceso

- lectura_publica_leccion_fuente. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_fuente_pkey ON public.leccion_fuente USING btree (leccion_id, fuente_id, tipo_vinculo)`.
- `CREATE INDEX idx_leccion_fuente_fuente_id ON public.leccion_fuente USING btree (fuente_id)`.

## leccion_oracion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `oracion_id` | integer | Sí |
| `tipo_vinculo` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_oracion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_oracion_oracion_id_fkey`: `FOREIGN KEY (oracion_id) REFERENCES oracion_ejemplo(id) ON DELETE CASCADE`.
- `leccion_oracion_pkey`: `PRIMARY KEY (leccion_id, oracion_id, tipo_vinculo)`.

### Políticas de acceso

- lectura_publica_leccion_oracion. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_oracion_pkey ON public.leccion_oracion USING btree (leccion_id, oracion_id, tipo_vinculo)`.
- `CREATE INDEX idx_leccion_oracion_oracion_id ON public.leccion_oracion USING btree (oracion_id)`.

## leccion_palabra

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `palabra_id` | integer | Sí |

### Claves e integridad

- `leccion_palabra_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_palabra_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `leccion_palabra_pkey`: `PRIMARY KEY (leccion_id, palabra_id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_palabra_pkey ON public.leccion_palabra USING btree (leccion_id, palabra_id)`.
- `CREATE INDEX idx_leccion_palabra_palabra_id ON public.leccion_palabra USING btree (palabra_id)`.

## leccion_regla_gramatical

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `regla_id` | bigint | Sí |
| `orden` | integer | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_regla_gramatical_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_regla_gramatical_pkey`: `PRIMARY KEY (leccion_id, regla_id)`.
- `leccion_regla_gramatical_regla_id_fkey`: `FOREIGN KEY (regla_id) REFERENCES regla_gramatical(id) ON DELETE CASCADE`.

### Políticas de acceso

- lectura_publica_leccion_regla_gramatical. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_regla_gramatical_pkey ON public.leccion_regla_gramatical USING btree (leccion_id, regla_id)`.
- `CREATE INDEX idx_leccion_regla_gramatical_regla_id ON public.leccion_regla_gramatical USING btree (regla_id)`.

## leccion_regla_pronunciacion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `regla_id` | bigint | Sí |
| `orden` | integer | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_regla_pronunciacion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_regla_pronunciacion_pkey`: `PRIMARY KEY (leccion_id, regla_id)`.
- `leccion_regla_pronunciacion_regla_id_fkey`: `FOREIGN KEY (regla_id) REFERENCES regla_pronunciacion(id) ON DELETE CASCADE`.

### Políticas de acceso

- lectura_publica_leccion_regla_pronunciacion. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_regla_pronunciacion_pkey ON public.leccion_regla_pronunciacion USING btree (leccion_id, regla_id)`.
- `CREATE INDEX idx_leccion_regla_pronunciacion_regla ON public.leccion_regla_pronunciacion USING btree (regla_id)`.

## leccion_ruta_curricular

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `leccion_id` | integer | Sí |
| `etapa_codigo` | text | No |
| `estado_mapeo` | text | Sí |
| `es_refuerzo` | boolean | Sí |
| `es_transicion` | boolean | Sí |
| `prerrequisito_descripcion` | text | No |
| `proposito_transicion` | text | No |
| `justificacion` | text | No |
| `fuente_primaria_id` | integer | No |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `leccion_ruta_curricular_estado_mapeo_check`: `CHECK ((estado_mapeo = ANY (ARRAY['pendiente_evidencia'::text, 'propuesta'::text, 'documentada'::text, 'validada'::text])))`.
- `leccion_ruta_curricular_etapa_codigo_fkey`: `FOREIGN KEY (etapa_codigo) REFERENCES etapa_ruta_curricular(codigo)`.
- `leccion_ruta_curricular_fuente_primaria_id_fkey`: `FOREIGN KEY (fuente_primaria_id) REFERENCES fuente_documento(id)`.
- `leccion_ruta_curricular_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `leccion_ruta_curricular_pkey`: `PRIMARY KEY (leccion_id)`.

### Políticas de acceso

- lectura_publica_leccion_ruta_curricular. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX leccion_ruta_curricular_pkey ON public.leccion_ruta_curricular USING btree (leccion_id)`.
- `CREATE INDEX idx_leccion_ruta_curricular_etapa ON public.leccion_ruta_curricular USING btree (etapa_codigo)`.
- `CREATE INDEX idx_leccion_ruta_curricular_fuente ON public.leccion_ruta_curricular USING btree (fuente_primaria_id)`.

## logro

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `nombre` | text | Sí |
| `descripcion` | text | Sí |
| `condicion_tipo` | text | Sí |
| `condicion_valor` | integer | Sí |
| `categoria_id` | integer | No |

### Claves e integridad

- `chk_condicion_tipo_valido`: `CHECK ((condicion_tipo = ANY (ARRAY['lecciones_completadas'::text, 'racha_maxima'::text, 'palabras_favoritas'::text, 'memorias_tuki'::text, 'leccion_categoria_completada'::text])))`.
- `logro_categoria_id_fkey`: `FOREIGN KEY (categoria_id) REFERENCES categoria(id)`.
- `logro_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX logro_pkey ON public.logro USING btree (id)`.
- `CREATE INDEX idx_logro_categoria_id ON public.logro USING btree (categoria_id)`.

## logro_desbloqueado

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `usuario_id` | uuid | Sí |
| `logro_id` | integer | Sí |
| `fecha` | timestamp with time zone | Sí |

### Claves e integridad

- `logro_desbloqueado_logro_id_fkey`: `FOREIGN KEY (logro_id) REFERENCES logro(id) ON DELETE CASCADE`.
- `logro_desbloqueado_pkey`: `PRIMARY KEY (usuario_id, logro_id)`.
- `logro_desbloqueado_usuario_id_fkey`: `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE`.

### Políticas de acceso

- usuario lee sus propios logros. Operación: SELECT. Roles: public. USING: `(( SELECT auth.uid() AS uid) = usuario_id)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX logro_desbloqueado_pkey ON public.logro_desbloqueado USING btree (usuario_id, logro_id)`.
- `CREATE INDEX idx_logro_desbloqueado_logro_id ON public.logro_desbloqueado USING btree (logro_id)`.

## memoria_tuki

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `usuario_id` | uuid | Sí |
| `tipo` | text | Sí |
| `resumen` | text | Sí |
| `fecha` | timestamp with time zone | Sí |

### Claves e integridad

- `chk_tipo_valido`: `CHECK ((tipo = ANY (ARRAY['aprendizaje'::text, 'conversacion'::text, 'preferencia'::text, 'recomendacion'::text])))`.
- `memoria_tuki_pkey`: `PRIMARY KEY (id)`.
- `memoria_tuki_usuario_id_fkey`: `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE`.

### Políticas de acceso

- usuario ve sus propias memorias de tuki. Operación: ALL. Roles: public. USING: `(( SELECT auth.uid() AS uid) = usuario_id)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX memoria_tuki_pkey ON public.memoria_tuki USING btree (id)`.
- `CREATE INDEX idx_memoria_tuki_usuario_id ON public.memoria_tuki USING btree (usuario_id)`.

## mundo_gamificado

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `codigo` | text | Sí |
| `nombre_visible` | text | Sí |
| `descripcion_visible` | text | Sí |
| `orden` | smallint | Sí |
| `icono_clave` | text | No |
| `xp_desbloqueo` | integer | Sí |
| `recompensa_final` | text | No |
| `activo` | boolean | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `mundo_gamificado_orden_key`: `UNIQUE (orden)`.
- `mundo_gamificado_pkey`: `PRIMARY KEY (codigo)`.
- `mundo_gamificado_xp_desbloqueo_check`: `CHECK ((xp_desbloqueo >= 0))`.

### Políticas de acceso

- lectura_publica_mundo_gamificado. Operación: SELECT. Roles: anon, authenticated. USING: `activo`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX mundo_gamificado_pkey ON public.mundo_gamificado USING btree (codigo)`.
- `CREATE UNIQUE INDEX mundo_gamificado_orden_key ON public.mundo_gamificado USING btree (orden)`.

## oracion_ejemplo

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `texto_origen` | text | Sí |
| `texto_destino` | text | Sí |
| `leccion_id` | integer | No |
| `fuente_id` | integer | Sí |
| `idioma_origen_id` | integer | Sí |
| `idioma_destino_id` | integer | Sí |
| `estado_validacion` | text | Sí |
| `nota_validacion` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `oracion_ejemplo_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `oracion_ejemplo_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `oracion_ejemplo_idioma_destino_id_fkey`: `FOREIGN KEY (idioma_destino_id) REFERENCES idioma(id)`.
- `oracion_ejemplo_idioma_origen_id_fkey`: `FOREIGN KEY (idioma_origen_id) REFERENCES idioma(id)`.
- `oracion_ejemplo_idiomas_distintos`: `CHECK ((idioma_origen_id <> idioma_destino_id))`.
- `oracion_ejemplo_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE SET NULL`.
- `oracion_ejemplo_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX oracion_ejemplo_pkey ON public.oracion_ejemplo USING btree (id)`.
- `CREATE INDEX idx_oracion_ejemplo_fuente_id ON public.oracion_ejemplo USING btree (fuente_id)`.
- `CREATE INDEX idx_oracion_ejemplo_leccion_id ON public.oracion_ejemplo USING btree (leccion_id)`.
- `CREATE INDEX oracion_ejemplo_busqueda_origen_idx ON public.oracion_ejemplo USING btree (idioma_origen_id, lower(btrim(texto_origen)))`.
- `CREATE INDEX oracion_ejemplo_busqueda_destino_idx ON public.oracion_ejemplo USING btree (idioma_destino_id, lower(btrim(texto_destino)))`.

## palabra

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `idioma_id` | integer | Sí |
| `texto` | text | Sí |
| `categoria_id` | integer | No |
| `fuente_id` | integer | Sí |
| `pronunciacion` | text | No |
| `pronunciacion_fonetica` | text | No |
| `pronunciacion_verificada` | boolean | Sí |
| `texto_normalizado` | text | Sí |
| `estado_validacion` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `palabra_canonica_id` | integer | No |
| `texto_busqueda` | text | No |
| `tipo_entrada` | text | Sí |

### Claves e integridad

- `palabra_categoria_id_fkey`: `FOREIGN KEY (categoria_id) REFERENCES categoria(id)`.
- `palabra_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `palabra_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `palabra_idioma_id_fkey`: `FOREIGN KEY (idioma_id) REFERENCES idioma(id)`.
- `palabra_palabra_canonica_id_fkey`: `FOREIGN KEY (palabra_canonica_id) REFERENCES palabra(id) ON DELETE SET NULL`.
- `palabra_pkey`: `PRIMARY KEY (id)`.
- `palabra_tipo_entrada_check`: `CHECK ((tipo_entrada = ANY (ARRAY['palabra'::text, 'locucion'::text, 'morfema_fragmento'::text, 'texto_lexicografico'::text, 'pendiente_clasificacion'::text])))`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX palabra_pkey ON public.palabra USING btree (id)`.
- `CREATE INDEX idx_palabra_texto_trgm ON public.palabra USING gin (texto gin_trgm_ops)`.
- `CREATE INDEX idx_palabra_categoria_id ON public.palabra USING btree (categoria_id)`.
- `CREATE INDEX idx_palabra_fuente_id ON public.palabra USING btree (fuente_id)`.
- `CREATE INDEX idx_palabra_idioma_id ON public.palabra USING btree (idioma_id)`.
- `CREATE INDEX idx_palabra_idioma_texto_normalizado ON public.palabra USING btree (idioma_id, texto_normalizado)`.
- `CREATE INDEX idx_palabra_updated_at ON public.palabra USING btree (updated_at, id)`.
- `CREATE INDEX idx_palabra_canonica_id ON public.palabra USING btree (palabra_canonica_id)`.
- `CREATE INDEX idx_palabra_idioma_texto_busqueda ON public.palabra USING btree (idioma_id, texto_busqueda)`.

## palabra_canonica

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `palabra_id` | integer | Sí |
| `palabra_canonica_id` | integer | Sí |
| `motivo` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `palabra_canonica_check`: `CHECK (((palabra_id > 0) AND (palabra_canonica_id > 0)))`.
- `palabra_canonica_palabra_canonica_id_fkey`: `FOREIGN KEY (palabra_canonica_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `palabra_canonica_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `palabra_canonica_pkey`: `PRIMARY KEY (palabra_id)`.

### Políticas de acceso

- lectura_publica_palabra_canonica. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX palabra_canonica_pkey ON public.palabra_canonica USING btree (palabra_id)`.
- `CREATE INDEX idx_palabra_canonica_canonica ON public.palabra_canonica USING btree (palabra_canonica_id)`.

## palabra_favorita

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `usuario_id` | uuid | Sí |
| `palabra_id` | integer | Sí |

### Claves e integridad

- `palabra_favorita_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `palabra_favorita_pkey`: `PRIMARY KEY (usuario_id, palabra_id)`.
- `palabra_favorita_usuario_id_fkey`: `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE`.

### Políticas de acceso

- usuario ve y edita sus propios favoritos. Operación: ALL. Roles: public. USING: `(( SELECT auth.uid() AS uid) = usuario_id)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX palabra_favorita_pkey ON public.palabra_favorita USING btree (usuario_id, palabra_id)`.
- `CREATE INDEX idx_palabra_favorita_palabra_id ON public.palabra_favorita USING btree (palabra_id)`.

## palabra_fuente

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `palabra_id` | integer | Sí |
| `fuente_id` | integer | Sí |
| `pagina_inicio` | integer | No |
| `pagina_fin` | integer | No |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `palabra_fuente_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `palabra_fuente_paginas_check`: `CHECK (((pagina_inicio IS NULL) OR (pagina_fin IS NULL) OR (pagina_fin >= pagina_inicio)))`.
- `palabra_fuente_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `palabra_fuente_pkey`: `PRIMARY KEY (palabra_id, fuente_id)`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX palabra_fuente_pkey ON public.palabra_fuente USING btree (palabra_id, fuente_id)`.
- `CREATE INDEX idx_palabra_fuente_fuente ON public.palabra_fuente USING btree (fuente_id)`.
- `CREATE INDEX idx_palabra_fuente_updated_at ON public.palabra_fuente USING btree (updated_at, palabra_id)`.

## progreso_leccion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `usuario_id` | uuid | Sí |
| `leccion_id` | integer | Sí |
| `estado` | text | Sí |
| `puntaje` | integer | No |
| `fecha_completado` | timestamp with time zone | No |

### Claves e integridad

- `chk_estado_valido`: `CHECK ((estado = ANY (ARRAY['no_iniciada'::text, 'en_progreso'::text, 'completada'::text])))`.
- `progreso_leccion_leccion_id_fkey`: `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE`.
- `progreso_leccion_pkey`: `PRIMARY KEY (usuario_id, leccion_id)`.
- `progreso_leccion_usuario_id_fkey`: `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE`.

### Políticas de acceso

- usuario ve y edita su propio progreso. Operación: ALL. Roles: public. USING: `(( SELECT auth.uid() AS uid) = usuario_id)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX progreso_leccion_pkey ON public.progreso_leccion USING btree (usuario_id, leccion_id)`.
- `CREATE INDEX idx_progreso_leccion_leccion_id ON public.progreso_leccion USING btree (leccion_id)`.

## regla_gramatical

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `idioma_id` | integer | Sí |
| `codigo` | text | Sí |
| `categoria` | text | Sí |
| `titulo` | text | Sí |
| `descripcion` | text | Sí |
| `patron` | text | No |
| `aplicacion` | text | No |
| `productiva` | boolean | Sí |
| `prioridad` | integer | Sí |
| `fuente_id` | integer | No |
| `estado_validacion` | text | Sí |
| `notas` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `regla_gramatical_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `regla_gramatical_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL`.
- `regla_gramatical_idioma_id_codigo_key`: `UNIQUE (idioma_id, codigo)`.
- `regla_gramatical_idioma_id_fkey`: `FOREIGN KEY (idioma_id) REFERENCES idioma(id) ON DELETE CASCADE`.
- `regla_gramatical_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_publica_regla_gramatical. Operación: SELECT. Roles: anon, authenticated. USING: `(estado_validacion <> 'rechazada'::text)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX regla_gramatical_pkey ON public.regla_gramatical USING btree (id)`.
- `CREATE UNIQUE INDEX regla_gramatical_idioma_id_codigo_key ON public.regla_gramatical USING btree (idioma_id, codigo)`.
- `CREATE INDEX idx_regla_gramatical_fuente_id ON public.regla_gramatical USING btree (fuente_id)`.
- `CREATE INDEX idx_regla_gramatical_updated_at ON public.regla_gramatical USING btree (updated_at, id)`.

## regla_pronunciacion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `idioma_id` | integer | Sí |
| `patron` | text | Sí |
| `tipo` | text | Sí |
| `descripcion` | text | Sí |
| `reemplazo_fonetico` | text | No |
| `ejemplo` | text | No |
| `fuente_id` | integer | No |
| `prioridad` | integer | Sí |
| `activa` | boolean | Sí |
| `created_at` | timestamp with time zone | Sí |
| `estado_validacion` | text | Sí |
| `notas` | text | No |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `regla_pronunciacion_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['documentada'::text, 'pendiente_fonetica'::text, 'validada'::text])))`.
- `regla_pronunciacion_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL`.
- `regla_pronunciacion_idioma_id_fkey`: `FOREIGN KEY (idioma_id) REFERENCES idioma(id) ON DELETE CASCADE`.
- `regla_pronunciacion_idioma_id_patron_tipo_key`: `UNIQUE (idioma_id, patron, tipo)`.
- `regla_pronunciacion_pkey`: `PRIMARY KEY (id)`.
- `regla_pronunciacion_tipo_check`: `CHECK ((tipo = ANY (ARRAY['grafema'::text, 'digrafo'::text, 'vocal_larga'::text, 'ortografia'::text, 'otro'::text])))`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX regla_pronunciacion_pkey ON public.regla_pronunciacion USING btree (id)`.
- `CREATE UNIQUE INDEX regla_pronunciacion_idioma_id_patron_tipo_key ON public.regla_pronunciacion USING btree (idioma_id, patron, tipo)`.
- `CREATE INDEX idx_regla_pronunciacion_fuente_id ON public.regla_pronunciacion USING btree (fuente_id)`.
- `CREATE INDEX idx_regla_pronunciacion_updated_at ON public.regla_pronunciacion USING btree (updated_at, id)`.

## revision_linguistica

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `tipo_entidad` | text | Sí |
| `entidad_id` | bigint | Sí |
| `clasificacion` | text | No |
| `estado` | text | Sí |
| `observacion` | text | No |
| `revisado_por` | uuid | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `revision_linguistica_estado_check`: `CHECK ((estado = ANY (ARRAY['pendiente'::text, 'en_revision'::text, 'aprobada'::text, 'rechazada'::text, 'requiere_consulta'::text])))`.
- `revision_linguistica_pkey`: `PRIMARY KEY (id)`.
- `revision_linguistica_revisado_por_fkey`: `FOREIGN KEY (revisado_por) REFERENCES usuario(id) ON DELETE SET NULL`.
- `revision_linguistica_tipo_entidad_check`: `CHECK ((tipo_entidad = ANY (ARRAY['palabra'::text, 'traduccion'::text, 'acepcion'::text, 'traduccion_acepcion'::text, 'expresion'::text, 'traduccion_expresion'::text, 'variante_palabra'::text])))`.

### Políticas de acceso

- superadmin gestiona revisiones linguisticas. Operación: ALL. Roles: authenticated. USING: `(EXISTS ( SELECT 1    FROM super_administrador sa   WHERE (sa.usuario_id = ( SELECT auth.uid() AS uid))))`. WITH CHECK: `((EXISTS ( SELECT 1    FROM super_administrador sa   WHERE (sa.usuario_id = ( SELECT auth.uid() AS uid)))) AND ((revisado_por IS NULL) OR (revisado_por = ( SELECT auth.uid() AS uid))))`.

### Índices

- `CREATE UNIQUE INDEX revision_linguistica_pkey ON public.revision_linguistica USING btree (id)`.
- `CREATE INDEX idx_revision_linguistica_entidad ON public.revision_linguistica USING btree (tipo_entidad, entidad_id)`.
- `CREATE INDEX idx_revision_linguistica_estado ON public.revision_linguistica USING btree (estado)`.
- `CREATE INDEX idx_revision_linguistica_revisado_por ON public.revision_linguistica USING btree (revisado_por)`.

## super_administrador

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `usuario_id` | uuid | Sí |

### Claves e integridad

- `super_administrador_pkey`: `PRIMARY KEY (usuario_id)`.
- `super_administrador_usuario_id_fkey`: `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE`.

### Políticas de acceso

- usuario solo verifica si el mismo es superadmin. Operación: SELECT. Roles: public. USING: `(usuario_id = ( SELECT auth.uid() AS uid))`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX super_administrador_pkey ON public.super_administrador USING btree (usuario_id)`.

## traduccion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | integer | Sí |
| `palabra_origen_id` | integer | Sí |
| `palabra_destino_id` | integer | Sí |
| `nota` | text | No |
| `estado_validacion` | text | Sí |
| `fuente_id` | integer | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `nivel_confianza` | numeric(4,3) | No |
| `es_preferida` | boolean | Sí |

### Claves e integridad

- `traduccion_estado_validacion_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `traduccion_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `traduccion_palabra_destino_id_fkey`: `FOREIGN KEY (palabra_destino_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `traduccion_palabra_origen_id_fkey`: `FOREIGN KEY (palabra_origen_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `traduccion_palabra_origen_id_palabra_destino_id_key`: `UNIQUE (palabra_origen_id, palabra_destino_id)`.
- `traduccion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura publica. Operación: SELECT. Roles: public. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX traduccion_pkey ON public.traduccion USING btree (id)`.
- `CREATE UNIQUE INDEX traduccion_palabra_origen_id_palabra_destino_id_key ON public.traduccion USING btree (palabra_origen_id, palabra_destino_id)`.
- `CREATE INDEX idx_traduccion_origen ON public.traduccion USING btree (palabra_origen_id)`.
- `CREATE INDEX idx_traduccion_palabra_destino_id ON public.traduccion USING btree (palabra_destino_id)`.
- `CREATE INDEX idx_traduccion_fuente_id ON public.traduccion USING btree (fuente_id)`.
- `CREATE INDEX idx_traduccion_updated_at ON public.traduccion USING btree (updated_at, id)`.

## traduccion_acepcion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `acepcion_origen_id` | bigint | Sí |
| `acepcion_destino_id` | bigint | Sí |
| `tipo` | text | Sí |
| `estado_validacion` | text | Sí |
| `nivel_confianza` | numeric(4,3) | No |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `traduccion_acepcion_acepcion_destino_id_fkey`: `FOREIGN KEY (acepcion_destino_id) REFERENCES acepcion(id) ON DELETE CASCADE`.
- `traduccion_acepcion_acepcion_origen_id_acepcion_destino_id__key`: `UNIQUE (acepcion_origen_id, acepcion_destino_id, tipo)`.
- `traduccion_acepcion_acepcion_origen_id_fkey`: `FOREIGN KEY (acepcion_origen_id) REFERENCES acepcion(id) ON DELETE CASCADE`.
- `traduccion_acepcion_confianza_check`: `CHECK (((nivel_confianza IS NULL) OR ((nivel_confianza >= (0)::numeric) AND (nivel_confianza <= (1)::numeric))))`.
- `traduccion_acepcion_distinta_check`: `CHECK ((acepcion_origen_id <> acepcion_destino_id))`.
- `traduccion_acepcion_estado_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `traduccion_acepcion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX traduccion_acepcion_pkey ON public.traduccion_acepcion USING btree (id)`.
- `CREATE UNIQUE INDEX traduccion_acepcion_acepcion_origen_id_acepcion_destino_id__key ON public.traduccion_acepcion USING btree (acepcion_origen_id, acepcion_destino_id, tipo)`.
- `CREATE INDEX idx_traduccion_acepcion_origen ON public.traduccion_acepcion USING btree (acepcion_origen_id)`.
- `CREATE INDEX idx_traduccion_acepcion_destino ON public.traduccion_acepcion USING btree (acepcion_destino_id)`.
- `CREATE INDEX idx_traduccion_acepcion_updated_at ON public.traduccion_acepcion USING btree (updated_at, id)`.

## traduccion_expresion

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `expresion_origen_id` | bigint | Sí |
| `expresion_destino_id` | bigint | Sí |
| `estado_validacion` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `traduccion_expresion_distinta_check`: `CHECK ((expresion_origen_id <> expresion_destino_id))`.
- `traduccion_expresion_estado_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `traduccion_expresion_expresion_destino_id_fkey`: `FOREIGN KEY (expresion_destino_id) REFERENCES expresion(id) ON DELETE CASCADE`.
- `traduccion_expresion_expresion_origen_id_expresion_destino__key`: `UNIQUE (expresion_origen_id, expresion_destino_id)`.
- `traduccion_expresion_expresion_origen_id_fkey`: `FOREIGN KEY (expresion_origen_id) REFERENCES expresion(id) ON DELETE CASCADE`.
- `traduccion_expresion_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX traduccion_expresion_pkey ON public.traduccion_expresion USING btree (id)`.
- `CREATE UNIQUE INDEX traduccion_expresion_expresion_origen_id_expresion_destino__key ON public.traduccion_expresion USING btree (expresion_origen_id, expresion_destino_id)`.
- `CREATE INDEX idx_traduccion_expresion_origen ON public.traduccion_expresion USING btree (expresion_origen_id)`.
- `CREATE INDEX idx_traduccion_expresion_updated_at ON public.traduccion_expresion USING btree (updated_at, id)`.
- `CREATE INDEX idx_traduccion_expresion_destino ON public.traduccion_expresion USING btree (expresion_destino_id)`.

## traduccion_fuente

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `traduccion_id` | integer | Sí |
| `fuente_id` | integer | Sí |
| `tipo_vinculo` | text | Sí |
| `nota` | text | No |
| `created_at` | timestamp with time zone | Sí |

### Claves e integridad

- `traduccion_fuente_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)`.
- `traduccion_fuente_pkey`: `PRIMARY KEY (traduccion_id, fuente_id, tipo_vinculo)`.
- `traduccion_fuente_traduccion_id_fkey`: `FOREIGN KEY (traduccion_id) REFERENCES traduccion(id) ON DELETE CASCADE`.

### Políticas de acceso

- lectura_publica_traduccion_fuente. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX traduccion_fuente_pkey ON public.traduccion_fuente USING btree (traduccion_id, fuente_id, tipo_vinculo)`.
- `CREATE INDEX idx_traduccion_fuente_fuente_id ON public.traduccion_fuente USING btree (fuente_id)`.

## tuki_respuesta_sistema

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `codigo` | text | Sí |
| `tipo` | text | Sí |
| `idioma_respuesta_id` | integer | No |
| `texto` | text | Sí |
| `prioridad` | integer | Sí |
| `activa` | boolean | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |

### Claves e integridad

- `tuki_respuesta_sistema_codigo_key`: `UNIQUE (codigo)`.
- `tuki_respuesta_sistema_idioma_respuesta_id_fkey`: `FOREIGN KEY (idioma_respuesta_id) REFERENCES idioma(id)`.
- `tuki_respuesta_sistema_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- lectura_publica_respuestas_tuki. Operación: SELECT. Roles: anon, authenticated. USING: `(activa = true)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX tuki_respuesta_sistema_pkey ON public.tuki_respuesta_sistema USING btree (id)`.
- `CREATE UNIQUE INDEX tuki_respuesta_sistema_codigo_key ON public.tuki_respuesta_sistema USING btree (codigo)`.
- `CREATE INDEX idx_tuki_respuesta_sistema_idioma_respuesta ON public.tuki_respuesta_sistema USING btree (idioma_respuesta_id)`.
- `CREATE INDEX idx_tuki_respuesta_sistema_updated_at ON public.tuki_respuesta_sistema USING btree (updated_at, id)`.

## usuario

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | uuid | Sí |
| `nombre_usuario` | text | No |
| `correo` | text | No |
| `edad` | integer | No |
| `pais` | text | No |
| `ciudad` | text | No |
| `idioma_meta_id` | integer | No |
| `xp` | integer | Sí |
| `racha_actual` | integer | Sí |
| `racha_maxima` | integer | Sí |
| `ultima_actividad` | date | No |
| `nombre` | text | No |
| `apellido` | text | No |

### Claves e integridad

- `usuario_id_fkey`: `FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE`.
- `usuario_idioma_meta_id_fkey`: `FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id)`.
- `usuario_pkey`: `PRIMARY KEY (id)`.

### Políticas de acceso

- usuario ve y edita su propio perfil. Operación: ALL. Roles: public. USING: `(( SELECT auth.uid() AS uid) = id)`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX usuario_pkey ON public.usuario USING btree (id)`.
- `CREATE INDEX idx_usuario_idioma_meta_id ON public.usuario USING btree (idioma_meta_id)`.
- `CREATE UNIQUE INDEX usuario_nombre_usuario_unico ON public.usuario USING btree (lower(nombre_usuario))`.

## variante_palabra

RLS habilitada: sí.

| Columna | Tipo PostgreSQL | Obligatoria |
| --- | --- | --- |
| `id` | bigint | Sí |
| `palabra_id` | integer | Sí |
| `texto` | text | Sí |
| `texto_normalizado` | text | Sí |
| `tipo` | text | Sí |
| `estado_validacion` | text | Sí |
| `created_at` | timestamp with time zone | Sí |
| `updated_at` | timestamp with time zone | Sí |
| `fuente_id` | integer | No |
| `nota` | text | No |
| `texto_busqueda` | text | No |

### Claves e integridad

- `variante_palabra_estado_check`: `CHECK ((estado_validacion = ANY (ARRAY['importada'::text, 'pendiente_revision'::text, 'documentada'::text, 'validada'::text, 'rechazada'::text])))`.
- `variante_palabra_fuente_id_fkey`: `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL`.
- `variante_palabra_palabra_id_fkey`: `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE`.
- `variante_palabra_palabra_id_texto_normalizado_tipo_key`: `UNIQUE (palabra_id, texto_normalizado, tipo)`.
- `variante_palabra_pkey`: `PRIMARY KEY (id)`.
- `variante_palabra_tipo_check`: `CHECK ((tipo = ANY (ARRAY['ortografica'::text, 'dialectal'::text, 'alternativa'::text, 'historica'::text, 'abreviacion'::text])))`.

### Políticas de acceso

- lectura_contenido_linguistico. Operación: SELECT. Roles: anon, authenticated. USING: `true`. WITH CHECK: `no explícito`.

### Índices

- `CREATE UNIQUE INDEX variante_palabra_pkey ON public.variante_palabra USING btree (id)`.
- `CREATE UNIQUE INDEX variante_palabra_palabra_id_texto_normalizado_tipo_key ON public.variante_palabra USING btree (palabra_id, texto_normalizado, tipo)`.
- `CREATE INDEX idx_variante_idioma_busqueda ON public.variante_palabra USING btree (texto_normalizado, palabra_id)`.
- `CREATE INDEX idx_variante_updated_at ON public.variante_palabra USING btree (updated_at, id)`.
- `CREATE INDEX idx_variante_texto_busqueda ON public.variante_palabra USING btree (texto_busqueda)`.
- `CREATE INDEX idx_variante_palabra_fuente_id ON public.variante_palabra USING btree (fuente_id)`.

## Vistas de lectura

| Vista | Opciones observadas |
| --- | --- |
| `vista_expresiones_estudiantiles` | security_invoker=true |
| `vista_diccionario_estudiantil` | security_invoker=true |
| `vista_cobertura_leccion` | security_invoker=true |
| `vista_huecos_contenido` | security_invoker=true |
| `vw_expresiones_estudiantiles` | security_invoker=true |
| `vw_traducciones_estudiantiles` | security_invoker=true |
| `vista_expresiones_respuestas` | security_invoker=true |
| `vista_contenido_offline_aikukisna` | security_invoker=true |
| `vista_diccionario_respuestas` | security_invoker=true |
| `vista_tuki_conocimiento` | security_invoker=true |
| `vista_auditoria_curricular_leccion` | security_invoker=true |
| `vista_expresiones_sin_salida` | security_invoker=true |
| `vista_modulo_alineacion_curricular` | security_invoker=true |
| `vista_auditoria_curricular_leccion_v2` | security_invoker=true |
| `vista_leccion_curricular_interna` | security_invoker=true |
| `vista_secuencia_leccion_estudiante` | security_invoker=true |
| `vista_leccion_estudiante` | security_invoker=true |
| `vista_auditoria_curricular_leccion_v3` | security_invoker=true |
| `vista_estado_produccion_lecciones` | security_invoker=true |
| `vista_catalogo_offline_lecciones` | security_invoker=true |
| `vista_actividad_recurso_resuelto` | security_invoker=true |
| `vista_auditoria_integridad_contenido` | security_invoker=true |

## Funciones SQL propias

No se incluyen funciones de extensiones. SECURITY DEFINER ejecuta con privilegios del propietario y requiere controles específicos; false indica el comportamiento invoker observado.

| Función | Argumentos | SECURITY DEFINER |
| --- | --- | --- |
| `obtener_correo_por_usuario` | p_nombre_usuario text | Sí |
| `desbloquear_logro` | p_logro_id integer | No |
| `miskito_a_fonetica` | input_text text | No |
| `actualizar_updated_at_linguistico` | Sin argumentos | No |
| `proteger_columnas_gamificacion` | Sin argumentos | No |
| `crear_usuario_nuevo` | Sin argumentos | Sí |
| `resolver_consulta_linguistica` | p_texto text, p_idioma_origen integer, p_idioma_destino integer | No |
| `responder_tuki` | p_pregunta text, p_idioma_origen integer, p_idioma_destino integer | No |
| `completar_leccion` | p_leccion_id integer, p_puntaje integer | No |
| `traducir_texto_seguro` | p_texto text, p_idioma_origen integer, p_idioma_destino integer | No |
| `buscar_rutas_traduccion_expresion` | p_texto text, p_idioma_origen integer, p_idioma_destino integer | No |
| `obtener_pronunciacion_segura` | p_texto text, p_idioma_id integer | No |
| `obtener_contexto_tuki_usuario` | Sin argumentos | No |
| `saludo_tuki_usuario` | Sin argumentos | No |
| `buscar_rutas_traduccion_palabra` | p_texto text, p_idioma_origen integer, p_idioma_destino integer | No |
| `consultar_diccionario_seguro` | p_texto text, p_idioma_origen integer, p_idioma_destino integer | No |
| `obtener_paquete_leccion` | p_leccion_id integer | No |
| `obtener_paquetes_lecciones_actualizados` | p_desde timestamp with time zone | No |
| `auditar_integridad_aikukisna` | Sin argumentos | No |

## Triggers públicos observados

| Tabla | Trigger | Evento | Acción |
| --- | --- | --- | --- |
| usuario | trg_proteger_gamificacion | UPDATE | `EXECUTE FUNCTION proteger_columnas_gamificacion()` |
| palabra | palabra_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| palabra_fuente | palabra_fuente_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| acepcion | acepcion_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| traduccion_acepcion | traduccion_acepcion_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| variante_palabra | variante_palabra_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| expresion | expresion_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| traduccion_expresion | traduccion_expresion_actualizar_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| cultura_contenido | trg_cultura_contenido_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |
| regla_pronunciacion | trg_regla_pronunciacion_updated_at | UPDATE | `EXECUTE FUNCTION actualizar_updated_at_linguistico()` |

## Relaciones por clave foránea

Esta lista es exhaustiva para las FK de las 44 tablas inspeccionadas. No incluye vínculos polimórficos sin FK.

| Tabla hija | Restricción referencial |
| --- | --- |
| `acepcion` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `actividad_leccion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `actividad_recurso` | `FOREIGN KEY (actividad_id) REFERENCES actividad_leccion(id) ON DELETE CASCADE` |
| `alineacion_curricular` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `alineacion_curricular` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `cultura_contenido` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `ejemplo_regla_gramatical` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL` |
| `ejemplo_regla_gramatical` | `FOREIGN KEY (idioma_traduccion_id) REFERENCES idioma(id) ON DELETE SET NULL` |
| `ejemplo_regla_gramatical` | `FOREIGN KEY (regla_id) REFERENCES regla_gramatical(id) ON DELETE CASCADE` |
| `evidencia_curricular_leccion` | `FOREIGN KEY (etapa_codigo) REFERENCES etapa_ruta_curricular(codigo)` |
| `evidencia_curricular_leccion` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `evidencia_curricular_leccion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `evidencia_curricular_leccion` | `FOREIGN KEY (lengua_aplicacion_id) REFERENCES idioma(id)` |
| `expresion` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `expresion` | `FOREIGN KEY (idioma_id) REFERENCES idioma(id)` |
| `expresion_contexto_cultural` | `FOREIGN KEY (cultura_id) REFERENCES cultura_contenido(id) ON DELETE CASCADE` |
| `expresion_contexto_cultural` | `FOREIGN KEY (expresion_id) REFERENCES expresion(id) ON DELETE CASCADE` |
| `leccion` | `FOREIGN KEY (categoria_id) REFERENCES categoria(id)` |
| `leccion` | `FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id)` |
| `leccion_cultura` | `FOREIGN KEY (cultura_id) REFERENCES cultura_contenido(id) ON DELETE CASCADE` |
| `leccion_cultura` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_experiencia_gamificada` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_experiencia_gamificada` | `FOREIGN KEY (mundo_codigo) REFERENCES mundo_gamificado(codigo)` |
| `leccion_expresion` | `FOREIGN KEY (expresion_id) REFERENCES expresion(id) ON DELETE CASCADE` |
| `leccion_expresion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_fuente` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `leccion_fuente` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_oracion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_oracion` | `FOREIGN KEY (oracion_id) REFERENCES oracion_ejemplo(id) ON DELETE CASCADE` |
| `leccion_palabra` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_palabra` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `leccion_regla_gramatical` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_regla_gramatical` | `FOREIGN KEY (regla_id) REFERENCES regla_gramatical(id) ON DELETE CASCADE` |
| `leccion_regla_pronunciacion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `leccion_regla_pronunciacion` | `FOREIGN KEY (regla_id) REFERENCES regla_pronunciacion(id) ON DELETE CASCADE` |
| `leccion_ruta_curricular` | `FOREIGN KEY (etapa_codigo) REFERENCES etapa_ruta_curricular(codigo)` |
| `leccion_ruta_curricular` | `FOREIGN KEY (fuente_primaria_id) REFERENCES fuente_documento(id)` |
| `leccion_ruta_curricular` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `logro` | `FOREIGN KEY (categoria_id) REFERENCES categoria(id)` |
| `logro_desbloqueado` | `FOREIGN KEY (logro_id) REFERENCES logro(id) ON DELETE CASCADE` |
| `logro_desbloqueado` | `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE` |
| `memoria_tuki` | `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE` |
| `oracion_ejemplo` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `oracion_ejemplo` | `FOREIGN KEY (idioma_destino_id) REFERENCES idioma(id)` |
| `oracion_ejemplo` | `FOREIGN KEY (idioma_origen_id) REFERENCES idioma(id)` |
| `oracion_ejemplo` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE SET NULL` |
| `palabra` | `FOREIGN KEY (categoria_id) REFERENCES categoria(id)` |
| `palabra` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `palabra` | `FOREIGN KEY (idioma_id) REFERENCES idioma(id)` |
| `palabra` | `FOREIGN KEY (palabra_canonica_id) REFERENCES palabra(id) ON DELETE SET NULL` |
| `palabra_canonica` | `FOREIGN KEY (palabra_canonica_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `palabra_canonica` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `palabra_favorita` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `palabra_favorita` | `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE` |
| `palabra_fuente` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `palabra_fuente` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `progreso_leccion` | `FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE` |
| `progreso_leccion` | `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE` |
| `regla_gramatical` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL` |
| `regla_gramatical` | `FOREIGN KEY (idioma_id) REFERENCES idioma(id) ON DELETE CASCADE` |
| `regla_pronunciacion` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL` |
| `regla_pronunciacion` | `FOREIGN KEY (idioma_id) REFERENCES idioma(id) ON DELETE CASCADE` |
| `revision_linguistica` | `FOREIGN KEY (revisado_por) REFERENCES usuario(id) ON DELETE SET NULL` |
| `super_administrador` | `FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE` |
| `traduccion` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `traduccion` | `FOREIGN KEY (palabra_destino_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `traduccion` | `FOREIGN KEY (palabra_origen_id) REFERENCES palabra(id) ON DELETE CASCADE` |
| `traduccion_acepcion` | `FOREIGN KEY (acepcion_destino_id) REFERENCES acepcion(id) ON DELETE CASCADE` |
| `traduccion_acepcion` | `FOREIGN KEY (acepcion_origen_id) REFERENCES acepcion(id) ON DELETE CASCADE` |
| `traduccion_expresion` | `FOREIGN KEY (expresion_destino_id) REFERENCES expresion(id) ON DELETE CASCADE` |
| `traduccion_expresion` | `FOREIGN KEY (expresion_origen_id) REFERENCES expresion(id) ON DELETE CASCADE` |
| `traduccion_fuente` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id)` |
| `traduccion_fuente` | `FOREIGN KEY (traduccion_id) REFERENCES traduccion(id) ON DELETE CASCADE` |
| `tuki_respuesta_sistema` | `FOREIGN KEY (idioma_respuesta_id) REFERENCES idioma(id)` |
| `usuario` | `FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE` |
| `usuario` | `FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id)` |
| `variante_palabra` | `FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id) ON DELETE SET NULL` |
| `variante_palabra` | `FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE` |

