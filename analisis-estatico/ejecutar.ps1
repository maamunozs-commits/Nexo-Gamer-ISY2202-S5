param([string]$Destino = 'resultado-semgrep.json')
$ErrorActionPreference = 'Stop'
$proyecto = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
$imagen = (Get-Content -LiteralPath (Join-Path $PSScriptRoot 'imagen.txt') -Raw).Trim()
docker run --rm -v "${proyecto}:/src:ro" $imagen semgrep scan --config /src/analisis-estatico/reglas.yml --metrics off --disable-version-check --no-git-ignore --error --json /src/src/main | Set-Content -LiteralPath $Destino -Encoding utf8NoBOM
exit $LASTEXITCODE
