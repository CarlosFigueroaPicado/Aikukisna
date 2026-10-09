# Configuración de seguridad de Supabase Auth

Fecha de comprobación: 2026-09-21.

## Protección contra contraseñas filtradas

Estado remoto comprobado en **Authentication → Attack Protection**: `DISABLED`.

Para habilitarla:

1. Abrir el proyecto Aikukisna en Supabase Dashboard.
2. Ir a **Authentication → Sign In / Providers → Email** mediante el enlace **Configure in email provider**.
3. Activar **Prevent use of leaked passwords**.
4. Guardar los cambios.

Configuración recomendada:

- Activar la protección de contraseñas filtradas.
- Mantener un mínimo de ocho caracteres, coherente con el registro Android.
- Mantener los requisitos actuales de mayúscula, número y símbolo en la aplicación.

Impacto: las cuentas existentes conservan su sesión, pero Supabase puede rechazar una contraseña conocida como filtrada durante alta o cambio de contraseña. Esta capacidad depende del plan contratado.

Verificación: volver a **Authentication → Attack Protection** y confirmar que el indicador deje de mostrar `DISABLED`; posteriormente probar un registro controlado con una contraseña conocida como insegura. No se modifican tablas internas de Auth.

## Edge Function `login-usuario`

La función es un endpoint previo a la autenticación, por lo que `verify_jwt` queda desactivado. El handler compensa esta configuración comprobando la clave pública del proyecto recibida en `apikey` o en el encabezado heredado `Authorization`, acepta únicamente `POST`, limita la longitud de los campos y delega la verificación de contraseña a Supabase Auth.

La consulta `nombre_usuario → correo` se ejecuta únicamente dentro de la Edge Function con la credencial de servidor. El correo no se incluye en el JSON de respuesta y Android no consulta la RPC heredada. Los intentos con cuenta inexistente también pasan por `signInWithPassword` usando un correo neutro para conservar la misma forma de respuesta.
