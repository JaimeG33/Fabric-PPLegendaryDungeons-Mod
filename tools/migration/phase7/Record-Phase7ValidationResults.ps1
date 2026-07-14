param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons",
    [switch]$IntegratedClientPass,
    [switch]$DedicatedServerPass,
    [switch]$ConnectedClientNetworkingPass,
    [switch]$RestartPersistencePass
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

foreach ($marker in @(
    "static-audit.pass",
    "build.pass",
    "artifacts.pass"
)) {
    $path = Join-Path $validationDirectory $marker
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required automated validation marker is missing: $path"
    }
}

if (-not $IntegratedClientPass) {
    throw "Integrated Fabric client/world/controller test must pass."
}
if (-not $DedicatedServerPass) {
    throw "Fabric dedicated-server startup test must pass."
}
if (-not $ConnectedClientNetworkingPass) {
    throw "Connected Fabric client networking test must pass."
}
if (-not $RestartPersistencePass) {
    throw "Fabric restart and persistence test must pass."
}

$resultsPath = Join-Path $root "docs\migration\PHASE_07_VALIDATION_RESULTS.md"
$head = Get-ValidationHead $root
$recorded = Get-Date -Format o

$content = @"
# Phase 7 Validation Results

## Result

**PASS**

- Validated commit: ``$head``
- Recorded: ``$recorded``

## Automated checks

| Check | Result |
|---|---|
| Common/platform static-boundary audit | PASS |
| Clean common, Fabric, and NeoForge builds | PASS |
| Fabric release-JAR inspection | PASS |
| NeoForge release-JAR inspection | PASS |
| Duplicate-entry and loader-metadata audit | PASS |

## Fabric runtime checks

| Check | Result |
|---|---|
| Split Fabric client reaches title screen | PASS |
| Fresh or validation world loads | PASS |
| Parent controller block and editor open | PASS |
| Zone controller block and editor open | PASS |
| Fabric dedicated server starts | PASS |
| Client connects to dedicated server | PASS |
| Parent/zone editor networking works while connected | PASS |
| Saved controller state survives disconnect and server restart | PASS |

## NeoForge boundary for Phase 7

The NeoForge module compiles and produces an inspected loader-specific JAR.
NeoForge client and dedicated-server runtime startup are intentionally deferred
to Phase 8, which owns NeoForge bootstrap and runtime dependency validation.

## Deferred non-blocking issue

At GUI scale `4` or some `Auto` scale/resolution combinations,
`DungeonRuleZoneScreen` is taller than the visible area and its Save/Preview
controls can be cut off below the screen. The controls remain functional and
the layout displays correctly at lower GUI scales. This UI issue is deferred
until after the loader migration.
"@

[System.IO.File]::WriteAllText(
    $resultsPath,
    $content,
    (New-Object System.Text.UTF8Encoding($false))
)

Write-Host "Phase 7 validation results recorded:" -ForegroundColor Green
Write-Host $resultsPath -ForegroundColor Cyan
Write-Host "Commit and push this result before running Step 7E3." -ForegroundColor Yellow
