$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\local.properties")) {
    throw "No existe local.properties. Copia local.properties.example y completa sus valores."
}

$required = @(
    "SUPABASE_URL",
    "SUPABASE_ANON_KEY",
    "GEMINI_API_KEY",
    "ELEVENLABS_API_KEY",
    "GOOGLE_WEB_CLIENT_ID"
)

$properties = @{}
Get-Content ".\local.properties" | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$missing = @($required | Where-Object {
    [string]::IsNullOrWhiteSpace($properties[$_])
})

if ($missing.Count -gt 0) {
    Write-Host "Faltan variables obligatorias: $($missing -join ', ')" -ForegroundColor Red
    exit 1
}

$release = @("KEYSTORE_FILE", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD")
$missingRelease = @($release | Where-Object {
    [string]::IsNullOrWhiteSpace($properties[$_])
})

Write-Host "Variables de desarrollo completas: OK" -ForegroundColor Green
if ($missingRelease.Count -gt 0) {
    Write-Warning "Variables de firma release incompletas: $($missingRelease -join ', ')"
} else {
    Write-Host "Variables de firma release completas: OK" -ForegroundColor Green
}

Write-Host "No se imprimieron valores sensibles." -ForegroundColor Cyan
