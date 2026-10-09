# Flujo de revisión lingüística humana

Los siguientes conjuntos permanecen sin cambios automáticos:

- 52 grupos duplicados.
- 48 traducciones dentro del mismo idioma.
- 13,746 candidatos a múltiples acepciones.
- 37,433 candidatos a expresión.

Se preparó una migración independiente para `revision_linguistica`. La tabla registra la entidad, clasificación, estado, observación, persona revisora y fechas. No contiene disparadores que modifiquen el corpus.

Solo usuarios presentes en `super_administrador` pueden consultar, registrar o actualizar revisiones mediante RLS. Cada inserción o actualización exige que `revisado_por` sea el `auth.uid()` actual.

Flujo previsto:

1. Importar o seleccionar un candidato del reporte de auditoría.
2. Un perfil autorizado revisa las fuentes asociadas.
3. Registrar una observación con estado `pendiente` o `en_revision`.
4. La persona validadora decide `aprobada` o `rechazada`.
5. Una migración posterior, revisada por separado, puede aplicar decisiones aprobadas al corpus.

La migración está preparada, pero no se aplicó porque el historial remoto todavía no está reconciliado.
