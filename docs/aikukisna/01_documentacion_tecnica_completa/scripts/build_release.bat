@echo off
REM ============================================================
REM AIKUKISNA — build_release.bat
REM Compila un Android App Bundle (.aab) en modo release.
REM Requiere: gradlew.bat en la raíz del proyecto, y el keystore
REM de firma configurado (ver .gitignore: *.jks/*.keystore nunca
REM se suben al repositorio, deben existir localmente).
REM ============================================================

echo Compilando Aikukisna en modo release...
cd /d "%~dp0.."

call gradlew.bat bundleRelease

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: la compilacion fallo. Revisa que el keystore de firma
    echo este configurado en local.properties o en el archivo de firma
    echo correspondiente antes de reintentar.
    exit /b %ERRORLEVEL%
)

echo.
echo Listo. El archivo .aab queda en:
echo   app\build\outputs\bundle\release\app-release.aab
