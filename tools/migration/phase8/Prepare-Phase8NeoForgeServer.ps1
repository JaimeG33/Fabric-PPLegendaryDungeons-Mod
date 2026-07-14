param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons",
    [switch]$AcceptEula
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$helperPath = Join-Path $PSScriptRoot "Phase8ValidationHelpers.ps1"
if (-not (Test-Path -LiteralPath $helperPath -PathType Leaf)) {
    throw "Phase 8 validation helper is missing: $helperPath"
}
. $helperPath

if (-not $AcceptEula) {
    throw "Rerun with -AcceptEula only after accepting the Minecraft EULA."
}

$root = Resolve-Phase8ValidationRoot $ProjectRoot
Assert-Phase8ValidationState $root

$validationDirectory = Get-Phase8ValidationDirectory $root
$preflightMarker = Join-Path $validationDirectory "neoforge-build.pass"

if (-not (Test-Path -LiteralPath $preflightMarker -PathType Leaf)) {
    throw "Run Run-Phase8NeoForgePreflight.ps1 successfully before preparing the server."
}

$runDirectory = Join-Path $root "neoforge\run"
if (-not (Test-Path -LiteralPath $runDirectory)) {
    New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null
}

$eulaPath = Join-Path $runDirectory "eula.txt"
$propertiesPath = Join-Path $runDirectory "server.properties"

[System.IO.File]::WriteAllText(
    $eulaPath,
    "eula=true`r`n",
    (New-Object System.Text.UTF8Encoding($false))
)

$properties = @{}
if (Test-Path -LiteralPath $propertiesPath) {
    foreach ($line in Get-Content -LiteralPath $propertiesPath) {
        if ($line -match '^\s*([^#][^=]*)=(.*)$') {
            $properties[$matches[1].Trim()] = $matches[2]
        }
    }
}

$properties["level-name"] = "phase8_neoforge_validation_world"
$properties["motd"] = "Phase 8 NeoForge Validation"
$properties["online-mode"] = "false"
$properties["server-port"] = "25566"
$properties["allow-flight"] = "true"

$orderedKeys = @(
    "level-name",
    "motd",
    "online-mode",
    "server-port",
    "allow-flight"
)

$lines = New-Object System.Collections.Generic.List[string]
foreach ($key in $orderedKeys) {
    $lines.Add("$key=$($properties[$key])")
    $properties.Remove($key)
}

foreach ($key in ($properties.Keys | Sort-Object)) {
    $lines.Add("$key=$($properties[$key])")
}

[System.IO.File]::WriteAllText(
    $propertiesPath,
    ($lines -join "`r`n") + "`r`n",
    (New-Object System.Text.UTF8Encoding($false))
)

Write-Host "NeoForge validation server prepared." -ForegroundColor Green
Write-Host "Runtime directory: $runDirectory" -ForegroundColor Cyan
Write-Host "Connect address: localhost:25566" -ForegroundColor Cyan
