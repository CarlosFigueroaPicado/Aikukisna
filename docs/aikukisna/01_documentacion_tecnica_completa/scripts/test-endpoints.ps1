param(
    [string]$UserId,
    [string]$AccessToken
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path ".\local.properties")) {
    throw "No se encontró local.properties en la raíz del proyecto."
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
    throw "SUPABASE_URL y SUPABASE_ANON_KEY deben existir en local.properties."
}

$headers = @{
    apikey = $apiKey
    Authorization = "Bearer $apiKey"
}

function Invoke-Endpoint([string]$Name, [string]$Url, [hashtable]$RequestHeaders = $headers) {
    Write-Host "`n=== $Name ===" -ForegroundColor Cyan
    try {
        $response = Invoke-RestMethod -Uri $Url -Headers $RequestHeaders -Method Get
        $response | ConvertTo-Json -Depth 8
    } catch {
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Invoke-Endpoint "Palabras" "$baseUrl/rest/v1/palabra?select=id,texto,idioma_id&limit=2&order=id.asc"
Invoke-Endpoint "Lecciones" "$baseUrl/rest/v1/leccion?select=id,titulo,nivel,idioma_meta_id&limit=2&order=id.asc"
Invoke-Endpoint "Cultura" "$baseUrl/rest/v1/cultura_contenido?select=id,titulo&limit=2&order=id.asc"

if (-not [string]::IsNullOrWhiteSpace($UserId)) {
    if ([string]::IsNullOrWhiteSpace($AccessToken)) {
        throw "AccessToken es obligatorio cuando se proporciona UserId."
    }
    $authHeaders = @{
        apikey = $apiKey
        Authorization = "Bearer $AccessToken"
    }
    Invoke-Endpoint "Perfil autenticado" "$baseUrl/rest/v1/usuario?select=id,nombre,nombre_usuario,idioma_meta:idioma_meta_id(*)&id=eq.$UserId" $authHeaders
}
