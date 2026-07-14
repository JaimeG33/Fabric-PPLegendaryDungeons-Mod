param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons",
    [switch]$AcceptEula
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$helper = Join-Path $PSScriptRoot "Phase7ValidationHelpers.ps1"
if (-not (Test-Path -LiteralPath $helper)) {
    throw "Validation helper is missing: $helper"
}
. $helper

if (-not $AcceptEula) {
    throw "Rerun with -AcceptEula only if you accept the Minecraft EULA."
}

$root = Resolve-ValidationProjectRoot $ProjectRoot
$runDirectory = Join-Path $root "fabric\run"

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

$properties["level-name"] = "phase7_validation_world"
$properties["motd"] = "Phase 7 Fabric Validation"
$properties["online-mode"] = "false"
$properties["server-port"] = "25565"

$orderedKeys = @(
    "level-name",
    "motd",
    "online-mode",
    "server-port"
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

Write-Host "Fabric validation server prepared." -ForegroundColor Green
Write-Host "Runtime directory: $runDirectory" -ForegroundColor Cyan
