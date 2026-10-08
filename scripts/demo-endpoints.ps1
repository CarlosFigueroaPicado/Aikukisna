param(
    [string]$OutputDirectory = ".\docs\aikukisna\evidencias"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\local.properties")) {
    throw "Ejecuta este script desde la raíz del proyecto y configura local.properties."
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

New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$headers = @{ apikey = $apiKey; Authorization = "Bearer $apiKey" }

$endpoints = [ordered]@{
    palabra = "$baseUrl/rest/v1/palabra?select=id,texto,idioma_id&limit=2&order=id.asc"
    leccion = "$baseUrl/rest/v1/leccion?select=id,titulo,nivel,idioma_meta_id&limit=2&order=id.asc"
    cultura = "$baseUrl/rest/v1/cultura_contenido?select=id,titulo&limit=2&order=id.asc"
}

$resumen = [ordered]@{
    ejecutadoEn = (Get-Date).ToUniversalTime().ToString("o")
    proyecto = "Aikukisna"
    resultados = @()
}

foreach ($entry in $endpoints.GetEnumerator()) {
    Write-Host "Consultando $($entry.Key)..." -ForegroundColor Cyan
    $data = Invoke-RestMethod -Uri $entry.Value -Headers $headers -Method Get
    $json = $data | ConvertTo-Json -Depth 10
    $json | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory "$($entry.Key).json")
    $resumen.resultados += [ordered]@{
        endpoint = if ($entry.Key -eq "cultura") { "/rest/v1/cultura_contenido" } else { "/rest/v1/$($entry.Key)" }
        registros = @($data).Count
        estado = "OK"
    }
}

$resumen | ConvertTo-Json -Depth 10 | Set-Content -Encoding UTF8 (Join-Path $OutputDirectory "resumen.json")
Write-Host "Evidencia generada en $OutputDirectory" -ForegroundColor Green
