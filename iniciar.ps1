$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

function Nueva-Clave {
    $bytes = [byte[]]::new(32)
    $generador = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generador.GetBytes($bytes) }
    finally { $generador.Dispose() }
    [Convert]::ToBase64String($bytes)
}

if (-not (Test-Path -LiteralPath '.env')) {
    @(
        "CLAVE_BD=$(Nueva-Clave)"
        "CLAVE_RAIZ=$(Nueva-Clave)"
        "CLAVE_JWT=$(Nueva-Clave)"
    ) | Set-Content -LiteralPath '.env' -Encoding ascii
}

docker compose up -d --build
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
