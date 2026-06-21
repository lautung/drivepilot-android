[CmdletBinding()]
param(
    [switch]$IncludeDeviceTests,
    [switch]$IncludeWebE2E
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$androidRoot = Join-Path $repoRoot "apps/android"
$adminWebRoot = Join-Path $repoRoot "apps/admin-web"
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

Invoke-CheckedCommand `
    -Label "Admin Web locked dependency install" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("install", "--frozen-lockfile")

Invoke-CheckedCommand `
    -Label "Admin Web lint" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("lint")

Invoke-CheckedCommand `
    -Label "Admin Web format check" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("format:check")

Invoke-CheckedCommand `
    -Label "Admin Web typecheck, tests, API drift, and build" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("typecheck")

Invoke-CheckedCommand `
    -Label "Admin Web unit and component tests" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("test", "--run")

Invoke-CheckedCommand `
    -Label "Admin Web OpenAPI type drift" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("api:check")

Invoke-CheckedCommand `
    -Label "Admin Web production build" `
    -WorkingDirectory $adminWebRoot `
    -Executable "pnpm" `
    -CommandArgs @("build")

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

if ($IncludeWebE2E) {
    Invoke-CheckedCommand `
        -Label "Start backend dependencies for Admin Web E2E" `
        -WorkingDirectory $repoRoot `
        -Executable "docker" `
        -CommandArgs @("compose", "-f", $composeFile, "up", "--build", "-d")

    Invoke-CheckedCommand `
        -Label "Admin Web Playwright E2E" `
        -WorkingDirectory $adminWebRoot `
        -Executable "pnpm" `
        -CommandArgs @("test:e2e")
}

Write-Host "All requested checks passed."
