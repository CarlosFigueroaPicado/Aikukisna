param([string[]]$GradleArguments = @())

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

Write-Host "Sincronizando configuración y dependencias Gradle..." -ForegroundColor Cyan
& .\gradlew.bat @GradleArguments :app:dependencies --configuration debugRuntimeClasspath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Limpiando proyecto..." -ForegroundColor Cyan
if (Test-Path ".\build\test-captures") {
    $captureBackup = Join-Path ".\docs\evidencia_funcional" ("capturas-" + (Get-Date -Format "yyyyMMdd-HHmmss-fff"))
    New-Item -ItemType Directory -Path $captureBackup -Force | Out-Null
    Copy-Item -LiteralPath ".\build\test-captures" -Destination $captureBackup -Recurse -ErrorAction Stop
}
& .\gradlew.bat @GradleArguments clean
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Ejecutando pruebas unitarias..." -ForegroundColor Cyan
& .\gradlew.bat @GradleArguments :app:testDebugUnitTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Generando APK debug..." -ForegroundColor Cyan
& .\gradlew.bat @GradleArguments :app:assembleDebug
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$apk = Resolve-Path ".\app\build\outputs\apk\debug\app-debug.apk"
Write-Host "APK generado: $apk" -ForegroundColor Green
