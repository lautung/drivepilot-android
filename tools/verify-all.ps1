[CmdletBinding()]
param(
    [switch]$IncludeDeviceTests
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repoRoot "apps/android"
$backendRoot = Join-Path $repoRoot "services/backend"
$composeFile = Join-Path $repoRoot "infra/docker-compose.yml"

function Invoke-CheckedCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Label,
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory,
        [Parameter(Mandatory = $true)]
        [string]$Executable,
        [Parameter(Mandatory = $true)]
        [string[]]$CommandArgs
    )

    Write-Host "==> $Label"
    Push-Location -LiteralPath $WorkingDirectory
    try {
        & $Executable @CommandArgs
        if ($LASTEXITCODE -ne 0) {
            throw "$Label failed with exit code $LASTEXITCODE."
        }
    } finally {
        Pop-Location
    }
}

Invoke-CheckedCommand `
    -Label "Android unit tests, lint, and Debug build" `
    -WorkingDirectory $androidRoot `
    -Executable ".\gradlew.bat" `
    -CommandArgs @("testDebugUnitTest", "lintDebug", "assembleDebug", "--console=plain")

if ($IncludeDeviceTests) {
    $devices = & adb devices
    if ($LASTEXITCODE -ne 0) {
        throw "adb devices failed with exit code $LASTEXITCODE."
    }
    $availableDevices = @($devices | Select-Object -Skip 1 | Where-Object { $_ -match "\sdevice$" })
    if ($availableDevices.Count -eq 0) {
        throw "No running Android device was found. Start or create an AVD before using -IncludeDeviceTests."
    }

    Invoke-CheckedCommand `
        -Label "Android connected device tests" `
        -WorkingDirectory $androidRoot `
        -Executable ".\gradlew.bat" `
        -CommandArgs @("connectedDebugAndroidTest", "--console=plain")
}

Invoke-CheckedCommand `
    -Label "Backend tests and bootJar" `
    -WorkingDirectory $backendRoot `
    -Executable ".\gradlew.bat" `
    -CommandArgs @("test", "bootJar", "--console=plain")

Invoke-CheckedCommand `
    -Label "Docker Compose configuration" `
    -WorkingDirectory $repoRoot `
    -Executable "docker" `
    -CommandArgs @("compose", "-f", $composeFile, "config", "--quiet")

Write-Host "All requested checks passed."
