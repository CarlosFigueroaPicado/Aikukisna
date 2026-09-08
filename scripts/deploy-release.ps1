param(
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    $androidStudioJdk = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path $androidStudioJdk) {
        $env:JAVA_HOME = $androidStudioJdk
    } else {
        throw "Configura JAVA_HOME con JDK 17 antes del despliegue."
    }
}

if (-not (Test-Path ".\local.properties")) {
    throw "No se encontró local.properties."
}

$releaseKeystore = Select-String -Path ".\local.properties" -Pattern '^KEYSTORE_FILE=' -Quiet
if (-not $releaseKeystore) {
    Write-Warning "No hay KEYSTORE_FILE configurado. Se intentará compilar el release sin firma personalizada."
}

if (-not $SkipTests) {
    Write-Host "Ejecutando pruebas unitarias..." -ForegroundColor Cyan
    & .\gradlew.bat :app:testDebugUnitTest
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

Write-Host "Generando APK release..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleRelease
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$outputs = Get-ChildItem ".\app\build\outputs\apk\release" -Filter *.apk -File -ErrorAction SilentlyContinue
if (-not $outputs) {
    throw "Gradle terminó, pero no se encontró ningún APK release."
}

$outputs | Select-Object FullName,Length | Format-Table -AutoSize
Write-Host "Build release generado. Antes de publicar, verificar firma, RLS, Edge Functions y pruebas en dispositivo." -ForegroundColor Green
