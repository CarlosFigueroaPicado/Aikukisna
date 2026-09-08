param(
    [string]$ProjectRef
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\local.properties")) {
    throw "No se encontró local.properties."
}

$properties = @{}
Get-Content ".\local.properties" | ForEach-Object {
    if ($_ -match '^([^#=]+)=(.*)$') {
        $properties[$matches[1].Trim()] = $matches[2].Trim()
    }
}

$baseUrl = $properties["SUPABASE_URL"]
$apiKey = $properties["SUPABASE_ANON_KEY"]
if ([string]::IsNullOrWhiteSpace($baseUrl) -or [string]::IsNullOrWhiteSpace($apiKey)) {
    throw "SUPABASE_URL y SUPABASE_ANON_KEY son obligatorios."
}

Write-Host "Comprobando URL de Supabase: $baseUrl" -ForegroundColor Cyan
$headers = @{ apikey = $apiKey; Authorization = "Bearer $apiKey" }
$checks = @(
    @{ Name = "tabla palabra"; Url = "$baseUrl/rest/v1/palabra?select=id&limit=1" },
    @{ Name = "tabla leccion"; Url = "$baseUrl/rest/v1/leccion?select=id&limit=1" },
    @{ Name = "tabla cultura_contenido"; Url = "$baseUrl/rest/v1/cultura_contenido?select=id&limit=1" }
)

foreach ($check in $checks) {
    try {
        Invoke-RestMethod -Uri $check.Url -Headers $headers -Method Get | Out-Null
        Write-Host "OK: $($check.Name)" -ForegroundColor Green
    } catch {
        Write-Host "FALLO: $($check.Name) - $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

if (Get-Command supabase -ErrorAction SilentlyContinue) {
    Write-Host "Supabase CLI disponible. Verifica migraciones con 'supabase db diff' antes de publicar." -ForegroundColor Yellow
} else {
    Write-Warning "Supabase CLI no está instalado; no se puede validar ni desplegar el esquema SQL desde este equipo."
}
