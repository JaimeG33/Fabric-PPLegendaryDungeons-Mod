param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$LogPath = Join-Path $ProjectRoot "phase6-static-audit.log"
$JavaRoot = Join-Path $ProjectRoot "src\main\java"
$MainRoot = Join-Path $ProjectRoot "src\main"

if (-not (Test-Path -LiteralPath $JavaRoot -PathType Container)) {
    throw "Java source root was not found: $JavaRoot"
}

$lines = New-Object System.Collections.Generic.List[string]

function Add-Line([string]$Text = "") {
    $lines.Add($Text)
    Write-Host $Text
}

function Add-Section([string]$Title) {
    Add-Line ""
    Add-Line ("=" * 80)
    Add-Line $Title
    Add-Line ("=" * 80)
}

function Search-Files {
    param(
        [string[]]$Paths,
        [string]$Pattern,
        [string]$Expected
    )

    Add-Line "Pattern:  $Pattern"
    Add-Line "Expected: $Expected"

    $existingPaths = $Paths | Where-Object { Test-Path -LiteralPath $_ }
    if (-not $existingPaths) {
        Add-Line "RESULT: Paths not found."
        return
    }

    $matches = Get-ChildItem -LiteralPath $existingPaths -Recurse -File -ErrorAction SilentlyContinue |
        Select-String -Pattern $Pattern -AllMatches

    if ($matches) {
        Add-Line "RESULT: MATCHES FOUND"
        foreach ($match in $matches) {
            Add-Line ("{0}:{1}: {2}" -f $match.Path, $match.LineNumber, $match.Line.Trim())
        }
    } else {
        Add-Line "RESULT: No matches."
    }
}

Add-Line "Phase 6 static source audit"
Add-Line "Generated: $(Get-Date -Format o)"
Add-Line "Project:   $ProjectRoot"

Add-Section "Source-control state"
$branch = (& git -C $ProjectRoot branch --show-current 2>&1 | Out-String).Trim()
$head = (& git -C $ProjectRoot rev-parse HEAD 2>&1 | Out-String).Trim()
$status = (& git -C $ProjectRoot status --short 2>&1 | Out-String).Trim()
Add-Line "Branch: $branch"
Add-Line "Head:   $head"
Add-Line "Working tree status before/including Phase 6 patch:"
Add-Line $status

Add-Section "Obsolete Fabric networking"
Search-Files -Paths @($JavaRoot) `
    -Pattern "PayloadTypeRegistry|ServerPlayNetworking|ClientPlayNetworking|Unpooled" `
    -Expected "No matches"

Add-Section "Old lifecycle, tick, interaction, trade, and loot hooks"
Search-Files -Paths @($JavaRoot) `
    -Pattern "ServerLifecycleEvents|ServerTickEvents|UseBlockCallback|UseItemCallback|TradeOfferHelper|LootTableEvents" `
    -Expected "No matches"

Add-Section "Removed break/place mixins"
Search-Files -Paths @($MainRoot) `
    -Pattern "DungeonBlockBreakMixin|DungeonBlockPlacementMixin" `
    -Expected "No matches"

Add-Section "Direct registry calls in migrated registry owners"
$registryFiles = @(
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\setup\ModBlocks.java"),
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\setup\ModBlockEntities.java")
)
Search-Files -Paths $registryFiles `
    -Pattern "Registry\.register|BuiltInRegistries" `
    -Expected "No matches"

Add-Section "Remaining Fabric Java imports"
Search-Files -Paths @($JavaRoot) `
    -Pattern "import net\.fabricmc" `
    -Expected "Only the two thin Fabric entrypoints"

Add-Section "All client imports"
Search-Files -Paths @($JavaRoot) `
    -Pattern "import net\.minecraft\.client" `
    -Expected "Only client networking/screens or other explicitly client-only classes"

Add-Section "Forbidden client imports in shared/server classes"
$serverSafeFiles = @(
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\LegendaryDungeons.java"),
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\dungeon_rules\network\DungeonRuleNetworking.java"),
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\server\ServerTickScheduler.java"),
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\setup\ModBlocks.java"),
    (Join-Path $JavaRoot "porker\pp_legendarydungeons\setup\ModBlockEntities.java")
)
Search-Files -Paths $serverSafeFiles `
    -Pattern "import net\.minecraft\.client" `
    -Expected "No matches"

Add-Section "Stable packet IDs"
Search-Files -Paths @($JavaRoot) `
    -Pattern "open_dungeon_rule_editor|update_dungeon_rule_block" `
    -Expected "Both IDs appear under the pp_legendarydungeons namespace"

Add-Line ""
Add-Line "Audit written to: $LogPath"
Add-Line "Review every MATCHES FOUND section against its expected result."
$lines | Set-Content -LiteralPath $LogPath -Encoding UTF8
