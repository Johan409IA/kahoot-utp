param(
    [switch]$DatabaseOnly,
    [Alias('Stop')][switch]$StopDatabase
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$localRoot = Join-Path $projectRoot '.local'
$dataRoot = Join-Path $localRoot 'postgres-data'
$postgresBin = Join-Path $projectRoot '.tools/postgresql-dist/pgsql/bin'
$pgCtl = Join-Path $postgresBin 'pg_ctl.exe'
$credentialsFile = Join-Path $localRoot 'database-credentials.xml'

if ($StopDatabase) {
    if (Test-Path -LiteralPath (Join-Path $dataRoot 'postmaster.pid')) {
        & $pgCtl -D $dataRoot -m fast -w stop
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo detener PostgreSQL local.' }
    }
    return
}
if (-not (Test-Path -LiteralPath (Join-Path $projectRoot '.tools/java/bin/java.exe'))) { & (Join-Path $PSScriptRoot 'setup-java.ps1') }
if (-not (Test-Path -LiteralPath $pgCtl)) { & (Join-Path $PSScriptRoot 'setup-postgres.ps1') }
New-Item -ItemType Directory -Path $localRoot -Force | Out-Null

if (-not (Test-Path -LiteralPath $credentialsFile)) {
    if (Test-Path -LiteralPath (Join-Path $dataRoot 'PG_VERSION')) { throw 'La base existe pero faltan sus credenciales locales. No se reinicializará.' }
    $bytes = New-Object byte[] 32
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    $random.GetBytes($bytes)
    $random.Dispose()
    $password = [Convert]::ToBase64String($bytes)
    $securePassword = ConvertTo-SecureString $password -AsPlainText -Force
    $credential = New-Object Management.Automation.PSCredential('adaptcode', $securePassword)
    # Export-Clixml encrypts the password with Windows DPAPI for this Windows account.
    $credential | Export-Clixml -LiteralPath $credentialsFile
} else { $credential = Import-Clixml -LiteralPath $credentialsFile }

if (-not (Test-Path -LiteralPath (Join-Path $dataRoot 'PG_VERSION'))) {
    $passwordFile = Join-Path $localRoot 'initdb-password.tmp'
    try {
        [IO.File]::WriteAllText($passwordFile, $credential.GetNetworkCredential().Password, (New-Object Text.UTF8Encoding($false)))
        & (Join-Path $postgresBin 'initdb.exe') -D $dataRoot -U $credential.UserName --encoding=UTF8 --locale=C --auth=scram-sha-256 --pwfile=$passwordFile
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo inicializar la base local.' }
    } finally {
        if (Test-Path -LiteralPath $passwordFile) { Remove-Item -LiteralPath $passwordFile -Force }
    }
}

& $pgCtl -D $dataRoot status *> $null
if ($LASTEXITCODE -ne 0) {
    & $pgCtl -D $dataRoot -l (Join-Path $localRoot 'postgres.log') -o '-p 5433 -h 127.0.0.1' -w start
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL; revisa .local/postgres.log.' }
}

$env:PGPASSWORD = $credential.GetNetworkCredential().Password
try {
    $databaseExists = & (Join-Path $postgresBin 'psql.exe') -X -h 127.0.0.1 -p 5433 -U $credential.UserName -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = 'adaptcode_utp'"
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar a PostgreSQL local.' }
    if ($databaseExists -ne '1') {
        & (Join-Path $postgresBin 'createdb.exe') -h 127.0.0.1 -p 5433 -U $credential.UserName --encoding=UTF8 adaptcode_utp
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear adaptcode_utp.' }
    }
} finally { Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue }
Write-Host 'Base local disponible en 127.0.0.1:5433/adaptcode_utp.'
if ($DatabaseOnly) { return }

$env:ADAPTCODE_DB_URL = 'jdbc:postgresql://127.0.0.1:5433/adaptcode_utp'
$env:ADAPTCODE_DB_USERNAME = $credential.UserName
$env:ADAPTCODE_DB_PASSWORD = $credential.GetNetworkCredential().Password
try {
    Push-Location -LiteralPath $projectRoot
    # A project-local socket directory avoids Windows MSIX TEMP path failures in Java NIO.
    & (Join-Path $projectRoot 'mvnw.cmd') -B -ntp spring-boot:run '-Dspring-boot.run.profiles=local,demo' '-Dspring-boot.run.jvmArguments=-Djdk.net.unixdomain.tmpdir=.local'
    if ($LASTEXITCODE -ne 0) { throw 'El backend terminó con un error.' }
} finally {
    Pop-Location
    Remove-Item Env:ADAPTCODE_DB_PASSWORD -ErrorAction SilentlyContinue
}
