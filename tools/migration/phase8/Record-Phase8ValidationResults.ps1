param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons",
    [switch]$ClientTitleScreenPass,
    [switch]$FreshWorldPass,
    [switch]$ClientControllerPass,
    [switch]$ClientPersistencePass,
    [switch]$DedicatedServerPass,
    [switch]$ConnectedClientPass,
    [switch]$ConnectedNetworkingPass,
    [switch]$ServerRestartPersistencePass,
    [switch]$DependencyFailureMessagePass
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$helperPath = Join-Path $PSScriptRoot "Phase8ValidationHelpers.ps1"
if (-not (Test-Path -LiteralPath $helperPath -PathType Leaf)) {
    throw "Phase 8 validation helper is missing: $helperPath"
}
. $helperPath

$root = Resolve-Phase8ValidationRoot $ProjectRoot
Assert-Phase8ValidationState $root -RequireClean

$validationDirectory = Get-Phase8ValidationDirectory $root
foreach ($markerName in @(
    "preflight-static.pass",
    "neoforge-build.pass"
)) {
    $markerPath = Join-Path $validationDirectory $markerName
    if (-not (Test-Path -LiteralPath $markerPath -PathType Leaf)) {
        throw "Required Phase 8 validation marker is missing: $markerPath"
    }
}

$requiredSwitches = @(
    [pscustomobject]@{ Name = "ClientTitleScreenPass"; Value = $ClientTitleScreenPass },
    [pscustomobject]@{ Name = "FreshWorldPass"; Value = $FreshWorldPass },
    [pscustomobject]@{ Name = "ClientControllerPass"; Value = $ClientControllerPass },
    [pscustomobject]@{ Name = "ClientPersistencePass"; Value = $ClientPersistencePass },
    [pscustomobject]@{ Name = "DedicatedServerPass"; Value = $DedicatedServerPass },
    [pscustomobject]@{ Name = "ConnectedClientPass"; Value = $ConnectedClientPass },
    [pscustomobject]@{ Name = "ConnectedNetworkingPass"; Value = $ConnectedNetworkingPass },
    [pscustomobject]@{ Name = "ServerRestartPersistencePass"; Value = $ServerRestartPersistencePass },
    [pscustomobject]@{ Name = "DependencyFailureMessagePass"; Value = $DependencyFailureMessagePass }
)

$missing = @(
    $requiredSwitches |
        Where-Object { -not $_.Value } |
        Select-Object -ExpandProperty Name
)

if ($missing.Count -gt 0) {
    throw "All Phase 8 checks must pass before recording results. Missing: $($missing -join ', ')"
}

$head = Invoke-Phase8ValidationGit $root @("rev-parse", "HEAD")
$resultsPath = Join-Path $root "docs\migration\PHASE_08_VALIDATION_RESULTS.md"

$content = @"
# Phase 8 Validation Results

## Result

**PASS**

- Validated commit: ``$head``
- Recorded: ``$(Get-Date -Format o)``

## Automated checks

| Check | Result |
|---|---|
| NeoForge module and metadata static preflight | PASS |
| Common and NeoForge clean build | PASS |
| Required development dependency declarations | PASS |

## NeoForge client checks

| Check | Result |
|---|---|
| Client reaches title screen | PASS |
| Fresh world loads | PASS |
| Parent and zone controllers register and open | PASS |
| Controller state survives world close/reopen | PASS |

## NeoForge dedicated-server checks

| Check | Result |
|---|---|
| Dedicated server reaches ready state | PASS |
| NeoForge client connects | PASS |
| Controller networking works while connected | PASS |
| Controller state survives disconnect and server restart | PASS |
| No physical-client class loads on dedicated server | PASS |

## Dependency error check

A disposable NeoForge runtime with at least one required dependency omitted
showed a clear loader-level missing-dependency message rather than an
unexplained class-loading crash.

## Phase boundary

Phase 8 proves NeoForge bootstrap, basic controller behavior, networking, and
persistence. Full feature-by-feature Fabric/NeoForge parity remains Phase 9.
"@

[System.IO.File]::WriteAllText(
    $resultsPath,
    $content,
    (New-Object System.Text.UTF8Encoding($false))
)

Write-Host "Phase 8 validation results recorded:" -ForegroundColor Green
Write-Host $resultsPath -ForegroundColor Cyan
Write-Host "Commit and push this result before running Step 8D finalization." -ForegroundColor Yellow
