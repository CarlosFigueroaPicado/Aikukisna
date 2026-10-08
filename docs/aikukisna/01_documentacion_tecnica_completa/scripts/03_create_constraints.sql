-- ============================================================
-- AIKUKISNA — 03_create_constraints.sql
-- Responsabilidad única: llaves foráneas, restricciones UNIQUE
-- y CHECK. Requiere que 02_create_tables.sql ya se haya ejecutado.
-- ============================================================

-- ---------- Llaves foráneas ----------

ALTER TABLE leccion
    ADD CONSTRAINT fk_leccion_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    ADD CONSTRAINT fk_leccion_idioma_meta
        FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id);

ALTER TABLE palabra
    ADD CONSTRAINT fk_palabra_idioma
        FOREIGN KEY (idioma_id) REFERENCES idioma(id),
    ADD CONSTRAINT fk_palabra_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id),
    ADD CONSTRAINT fk_palabra_fuente
        FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id);

ALTER TABLE traduccion
    ADD CONSTRAINT fk_traduccion_origen
        FOREIGN KEY (palabra_origen_id) REFERENCES palabra(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_traduccion_destino
        FOREIGN KEY (palabra_destino_id) REFERENCES palabra(id) ON DELETE CASCADE;

ALTER TABLE leccion_palabra
    ADD CONSTRAINT fk_leccion_palabra_leccion
        FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_leccion_palabra_palabra
        FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE;

ALTER TABLE oracion_ejemplo
    ADD CONSTRAINT fk_oracion_leccion
        FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_oracion_fuente
        FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id);

ALTER TABLE cultura_contenido
    ADD CONSTRAINT fk_cultura_fuente
        FOREIGN KEY (fuente_id) REFERENCES fuente_documento(id);

ALTER TABLE logro
    ADD CONSTRAINT fk_logro_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria(id);

ALTER TABLE usuario
    ADD CONSTRAINT fk_usuario_auth
        FOREIGN KEY (id) REFERENCES auth.users(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_usuario_idioma_meta
        FOREIGN KEY (idioma_meta_id) REFERENCES idioma(id);

ALTER TABLE progreso_leccion
    ADD CONSTRAINT fk_progreso_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_progreso_leccion
        FOREIGN KEY (leccion_id) REFERENCES leccion(id) ON DELETE CASCADE;

ALTER TABLE palabra_favorita
    ADD CONSTRAINT fk_favorita_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_favorita_palabra
        FOREIGN KEY (palabra_id) REFERENCES palabra(id) ON DELETE CASCADE;

ALTER TABLE logro_desbloqueado
    ADD CONSTRAINT fk_desbloqueado_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_desbloqueado_logro
        FOREIGN KEY (logro_id) REFERENCES logro(id) ON DELETE CASCADE;

ALTER TABLE memoria_tuki
    ADD CONSTRAINT fk_memoria_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

ALTER TABLE super_administrador
    ADD CONSTRAINT fk_superadmin_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE;

-- ---------- Restricciones UNIQUE ----------
-- Evitan duplicados reales encontrados durante la carga de datos
-- (362 pares de traducción repetidos antes de aplicar esta restricción).

ALTER TABLE idioma
    ADD CONSTRAINT uq_idioma_codigo UNIQUE (codigo);

ALTER TABLE categoria
    ADD CONSTRAINT uq_categoria_nombre UNIQUE (nombre);

ALTER TABLE traduccion
    ADD CONSTRAINT uq_traduccion_par UNIQUE (palabra_origen_id, palabra_destino_id);

-- ---------- Restricciones CHECK ----------
-- Enumeraciones pequeñas y fijas: CHECK en vez de tabla de catálogo
-- aparte (evita un JOIN innecesario para 3-5 valores sin atributos propios).

ALTER TABLE progreso_leccion
    ADD CONSTRAINT chk_estado_valido
        CHECK (estado IN ('no_iniciada', 'en_progreso', 'completada'));

ALTER TABLE memoria_tuki
    ADD CONSTRAINT chk_tipo_valido
        CHECK (tipo IN ('aprendizaje', 'conversacion', 'preferencia', 'recomendacion'));

ALTER TABLE logro
    ADD CONSTRAINT chk_condicion_tipo_valido
        CHECK (condicion_tipo IN (
            'lecciones_completadas', 'racha_maxima', 'palabras_favoritas',
            'memorias_tuki', 'leccion_categoria_completada'
        ));
