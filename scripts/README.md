# Scripts de Aikukisna

Los scripts se ejecutan desde la raíz del proyecto. No contienen claves: leen la configuración desde `local.properties`.

## Compilar y probar

```powershell
.\scripts\verify-build.ps1
```

## Probar endpoints públicos

```powershell
.\scripts\test-endpoints.ps1
```

## Generar evidencia demostrable

Este script consulta Supabase y guarda las respuestas JSON en `docs/aikukisna/evidencias/`:

```powershell
.\scripts\demo-endpoints.ps1
```

## Qué se puede demostrar

- `verify-build.ps1`: pruebas unitarias y generación del APK.
- `test-endpoints.ps1`: impresión de respuestas reales en consola.
- `demo-endpoints.ps1`: respuestas reales guardadas como evidencia documental.

## Despliegue

```powershell
.\scripts\verify-supabase.ps1
.\scripts\deploy-release.ps1
```

`verify-supabase.ps1` comprueba que el proyecto configurado responde en las tablas usadas por la app. `deploy-release.ps1` ejecuta pruebas y genera el APK release. La publicación en Google Play requiere firma, cuenta de Play Console y credenciales externas.

## Base de datos

El repositorio no contiene migraciones SQL ni Edge Functions. El esquema actual vive en Supabase. Para versionarlo se debe exportar desde el proyecto real y guardar las migraciones en `supabase/migrations/`; no se agrega un esquema inventado porque podría eliminar o modificar datos existentes.

Para consultar el perfil autenticado se debe proporcionar el UUID del usuario y un JWT de sesión:

```powershell
.\scripts\test-endpoints.ps1 -UserId "<USER_UUID>" -AccessToken "<JWT_DE_SUPABASE_AUTH>"
```

Los scripts imprimen respuestas JSON resumidas y nunca imprimen la clave Supabase.

## Verificar variables de entorno

```powershell
.\scripts\verify-environment.ps1
```

La plantilla está en `local.properties.example`. El proyecto Android utiliza `local.properties`, no un archivo `.env`.

## Preparar el corpus de traducción

El corpus se genera exclusivamente desde la réplica local de Supabase:

```powershell
python .\scripts\preparar_corpus_traduccion.py
```

Los pares con estado `documentada` o `validada` se distribuyen de forma reproducible entre entrenamiento, validación y prueba. Los registros `importada` quedan en `pendientes_validacion.jsonl` y no entran al entrenamiento hasta que sean revisados. Una pareja bilingüe y su dirección inversa siempre quedan en la misma partición para evitar contaminación de la evaluación.

Las aprobaciones humanas se registran por referencia en `modelos/traduccion/validaciones/aprobadas.jsonl`. El estado original procedente de Supabase se conserva para auditoría.

El entrenamiento del modelo maestro requiere un entorno aislado con GPU:

```powershell
python -m pip install -r .\scripts\requirements-traduccion.txt
python .\scripts\entrenar_traductor_byt5.py --origen 2 --destino 1
```

El modelo no debe incorporarse a Android solo por terminar el entrenamiento. Primero debe superar la evaluación reservada y una revisión lingüística externa; después se cuantiza o destila a un modelo móvil.

## Validar plantillas gramaticales

```powershell
python .\scripts\preparar_plantillas_gramaticales.py
```

El archivo resultante conserva el patrón y los ejemplos documentados, pero deja vacíos el patrón ejecutable y sus restricciones. Esos campos deben ser completados por el validador lingüístico antes de permitir que el motor genere una oración nueva.
