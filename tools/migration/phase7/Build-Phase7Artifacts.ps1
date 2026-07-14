param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$helper = Join-Path $PSScriptRoot "Phase7ValidationHelpers.ps1"
if (-not (Test-Path -LiteralPath $helper)) {
    throw "Validation helper is missing: $helper"
}
. $helper

$root = Resolve-ValidationProjectRoot $ProjectRoot
$validationDirectory = Get-ValidationDirectory $root
$log = Join-Path $validationDirectory "phase7-all-platform-build.log"
$marker = Join-Path $validationDirectory "build.pass"

if (Test-Path -LiteralPath $marker) {
    Remove-Item -LiteralPath $marker -Force
}

$gradleWrapper = Join-Path $root "gradlew.bat"

$arguments = @(
    ":common:clean",
    ":fabric:clean",
    ":neoforge:clean",
    ":common:build",
    ":fabric:build",
    ":neoforge:build",
    "--no-daemon",
    "--no-parallel",
    "--max-workers=2",
    "--console=plain",
    "--warning-mode",
    "all"
)

$exitCode = Invoke-ValidationProcess `
    -FilePath $gradleWrapper `
    -Arguments $arguments `
    -WorkingDirectory $root `
    -LogPath $log

if ($exitCode -ne 0) {
    throw "Phase 7 build failed with exit code $exitCode. Review: $log"
}

$logContent = [System.IO.File]::ReadAllText($log)
if (-not $logContent.Contains("BUILD SUCCESSFUL")) {
    throw "Gradle returned success but BUILD SUCCESSFUL was not found in: $log"
}

Write-PassMarker $marker "Common, Fabric, and NeoForge builds" $root
Write-Host "Common, Fabric, and NeoForge builds passed." -ForegroundColor Green
