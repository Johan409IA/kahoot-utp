$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsRoot = Join-Path $projectRoot '.tools'
$javaRoot = Join-Path $toolsRoot 'java'
New-Item -ItemType Directory -Path $toolsRoot -Force | Out-Null

if (-not (Test-Path -LiteralPath (Join-Path $javaRoot 'bin/java.exe'))) {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $uri = 'https://api.adoptium.net/v3/assets/latest/25/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse'
    $release = @(Invoke-RestMethod -Uri $uri)[0]
    $package = $release.binary.package
    $archive = Join-Path $toolsRoot 'java25.zip'
    Write-Host ('Descargando Eclipse Temurin ' + $release.release_name + '...')
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $package.link -OutFile $archive -UseBasicParsing
    if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $package.checksum.ToLowerInvariant()) {
        throw 'La suma SHA-256 del JDK no coincide. No se instalará el archivo.'
    }
    $extraction = Join-Path $toolsRoot 'java-extracted'
    Expand-Archive -LiteralPath $archive -DestinationPath $extraction -Force
    $jdk = @(Get-ChildItem -LiteralPath $extraction -Directory | Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin/java.exe') })
    if ($jdk.Count -ne 1) { throw 'No se encontró un único JDK en el archivo descargado.' }
    # Validate both absolute targets before moving the extracted directory.
    $allowedRoot = [IO.Path]::GetFullPath($toolsRoot) + [IO.Path]::DirectorySeparatorChar
    if (-not $jdk[0].FullName.StartsWith($allowedRoot, [StringComparison]::OrdinalIgnoreCase) -or
        -not [IO.Path]::GetFullPath($javaRoot).StartsWith($allowedRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'La ruta del JDK debe permanecer dentro de .tools.'
    }
    Move-Item -LiteralPath $jdk[0].FullName -Destination $javaRoot
}
& (Join-Path $javaRoot 'bin/java.exe') -version
if ($LASTEXITCODE -ne 0) { throw 'No se pudo ejecutar Java 25.' }
Write-Host 'Java local listo. mvnw.cmd lo seleccionará automáticamente.'
