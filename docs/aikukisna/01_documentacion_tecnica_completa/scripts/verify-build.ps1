$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME -or -not (Test-Path $env:JAVA_HOME)) {
    $androidStudioJdk = "C:\Program Files\Android\Android Studio\jbr"
    if (Test-Path $androidStudioJdk) {
        $env:JAVA_HOME = $androidStudioJdk
    } else {
        throw "Configura JAVA_HOME con un JDK 17 antes de ejecutar este script."
    }
}

if (-not (Test-Path ".\gradlew.bat")) {
    throw "Ejecuta el script desde la raíz del proyecto Aikukisna."
}

Write-Host "Ejecutando pruebas unitarias..." -ForegroundColor Cyan
& .\gradlew.bat :app:testDebugUnitTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Generando APK debug..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apk = Resolve-Path ".\app\build\outputs\apk\debug\app-debug.apk"
Write-Host "APK generado: $apk" -ForegroundColor Green
