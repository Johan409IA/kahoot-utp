$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsRoot = Join-Path $projectRoot '.tools'
$postgresRoot = Join-Path $toolsRoot 'postgresql-dist/pgsql'
$pgCtl = Join-Path $postgresRoot 'bin/pg_ctl.exe'
if (-not (Test-Path -LiteralPath $pgCtl)) {
    New-Item -ItemType Directory -Path $toolsRoot -Force | Out-Null
    $archive = Join-Path $toolsRoot 'postgresql.zip'
    if (-not (Test-Path -LiteralPath $archive)) {
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        $ProgressPreference = 'SilentlyContinue'
        Write-Host 'Descargando PostgreSQL desde EnterpriseDB...'
        Invoke-WebRequest -Uri 'https://get.enterprisedb.com/postgresql/postgresql-18.6-5-windows-x64-binaries.zip' -OutFile $archive -UseBasicParsing
    }
    Expand-Archive -LiteralPath $archive -DestinationPath (Join-Path $toolsRoot 'postgresql-dist') -Force
}
& (Join-Path $postgresRoot 'bin/psql.exe') --version
if ($LASTEXITCODE -ne 0) { throw 'No se pudo ejecutar PostgreSQL.' }
Write-Host 'PostgreSQL local listo; no se ha instalado un servicio global.'
