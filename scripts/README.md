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
