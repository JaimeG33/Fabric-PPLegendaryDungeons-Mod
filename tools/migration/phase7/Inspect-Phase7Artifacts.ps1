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

Add-Type -AssemblyName System.IO.Compression.FileSystem

$root = Resolve-ValidationProjectRoot $ProjectRoot
$validationDirectory = Get-ValidationDirectory $root
$marker = Join-Path $validationDirectory "artifacts.pass"

if (Test-Path -LiteralPath $marker) {
    Remove-Item -LiteralPath $marker -Force
}

function Find-ReleaseJar {
    param(
        [string]$Directory,
        [string]$PatternDescription,
        [string]$NameRegex
    )

    if (-not (Test-Path -LiteralPath $Directory)) {
        throw "Build output directory is missing: $Directory"
    }

    $matches = @(
        Get-ChildItem -LiteralPath $Directory -File -Filter "*.jar" |
            Where-Object { $_.Name -match $NameRegex }
    )

    if ($matches.Count -ne 1) {
        throw "Expected exactly one $PatternDescription JAR in $Directory; found $($matches.Count).`n$($matches.Name -join [Environment]::NewLine)"
    }

    return $matches[0]
}

function Inspect-Jar {
    param(
        [System.IO.FileInfo]$Jar,
        [string[]]$RequiredEntries,
        [string[]]$RequiredPrefixes,
        [string[]]$ForbiddenEntries,
        [string]$Platform
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($Jar.FullName)

    try {
        $entryNames = @($archive.Entries | Select-Object -ExpandProperty FullName)

        $duplicates = @(
            $entryNames |
                Group-Object |
                Where-Object { $_.Count -gt 1 }
        )

        if ($duplicates.Count -gt 0) {
            throw "$Platform JAR contains duplicate ZIP entries:`n$($duplicates.Name -join [Environment]::NewLine)"
        }

        foreach ($entry in $RequiredEntries) {
            if ($entryNames -notcontains $entry) {
                throw "$Platform JAR is missing required entry: $entry"
            }
        }

        foreach ($prefix in $RequiredPrefixes) {
            $prefixMatches = @(
                $entryNames | Where-Object { $_.StartsWith($prefix) }
            )

            if ($prefixMatches.Count -eq 0) {
                throw "$Platform JAR is missing required content prefix: $prefix"
            }
        }

        foreach ($entry in $ForbiddenEntries) {
            if ($entryNames -contains $entry) {
                throw "$Platform JAR contains forbidden entry: $entry"
            }
        }

        return [pscustomobject]@{
            Platform = $Platform
            Jar = $Jar.FullName
            Entries = $entryNames.Count
            SizeBytes = $Jar.Length
        }
    }
    finally {
        $archive.Dispose()
    }
}

$fabricJar = Find-ReleaseJar `
    -Directory (Join-Path $root "fabric\build\libs") `
    -PatternDescription "Fabric release" `
    -NameRegex 'fabric-mc1\.21\.1-cob1\.7\.3\.jar$'

$neoForgeJar = Find-ReleaseJar `
    -Directory (Join-Path $root "neoforge\build\libs") `
    -PatternDescription "NeoForge release" `
    -NameRegex 'neoforge-mc1\.21\.1-cob1\.7\.3\.jar$'

$sharedRequired = @(
    "porker/pp_legendarydungeons/LegendaryDungeons.class",
    "porker/pp_legendarydungeons/LegendaryDungeonsClient.class",
    "porker/pp_legendarydungeons/setup/ModBlocks.class",
    "pp_legendarydungeons.mixins.json",
    "assets/pp_legendarydungeons/icon.png"
)

$fabricResult = Inspect-Jar `
    -Jar $fabricJar `
    -Platform "Fabric" `
    -RequiredEntries ($sharedRequired + @(
        "fabric.mod.json",
        "porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.class",
        "porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeonsClient.class"
    )) `
    -RequiredPrefixes @(
        "assets/pp_legendarydungeons/",
        "data/pp_legendarydungeons/"
    ) `
    -ForbiddenEntries @(
        "META-INF/neoforge.mods.toml",
        "porker/pp_legendarydungeons/neoforge/ProfessorPorkersLegendaryDungeonsNeoForge.class",
        "porker/pp_legendarydungeons/neoforge/ProfessorPorkersLegendaryDungeonsNeoForgeClient.class"
    )

$neoForgeResult = Inspect-Jar `
    -Jar $neoForgeJar `
    -Platform "NeoForge" `
    -RequiredEntries ($sharedRequired + @(
        "META-INF/neoforge.mods.toml",
        "porker/pp_legendarydungeons/neoforge/ProfessorPorkersLegendaryDungeonsNeoForge.class",
        "porker/pp_legendarydungeons/neoforge/ProfessorPorkersLegendaryDungeonsNeoForgeClient.class"
    )) `
    -RequiredPrefixes @(
        "assets/pp_legendarydungeons/",
        "data/pp_legendarydungeons/"
    ) `
    -ForbiddenEntries @(
        "fabric.mod.json",
        "porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeons.class",
        "porker/pp_legendarydungeons/ProfessorPorkersLegendaryDungeonsClient.class"
    )

$report = @(
    "# Phase 7 artifact inspection"
    ""
    "Commit: $(Get-ValidationHead $root)"
    "Recorded: $(Get-Date -Format o)"
    ""
    "| Platform | JAR | Entries | Size bytes |"
    "|---|---|---:|---:|"
    "| $($fabricResult.Platform) | ``$($fabricResult.Jar)`` | $($fabricResult.Entries) | $($fabricResult.SizeBytes) |"
    "| $($neoForgeResult.Platform) | ``$($neoForgeResult.Jar)`` | $($neoForgeResult.Entries) | $($neoForgeResult.SizeBytes) |"
    ""
    "Result: PASS"
) -join [Environment]::NewLine

$reportPath = Join-Path $validationDirectory "artifact-inspection.md"
[System.IO.File]::WriteAllText(
    $reportPath,
    $report,
    (New-Object System.Text.UTF8Encoding($false))
)

Write-PassMarker $marker "Fabric and NeoForge artifact inspection" $root
Write-Host "Fabric and NeoForge artifact inspection passed." -ForegroundColor Green
Write-Host "Report: $reportPath" -ForegroundColor Cyan
