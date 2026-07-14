param(
    [string]$ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons"
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

$failures = New-Object System.Collections.Generic.List[string]

function Require-Phase8File {
    param([string]$RelativePath)

    $fullPath = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        $failures.Add("Missing required file: $RelativePath")
    }
}

function Require-Phase8Text {
    param(
        [string]$RelativePath,
        [string]$ExpectedText
    )

    $fullPath = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        $failures.Add("Missing required file: $RelativePath")
        return
    }

    $content = [System.IO.File]::ReadAllText($fullPath)
    if (-not $content.Contains($ExpectedText)) {
        $failures.Add("Missing expected text in ${RelativePath}: $ExpectedText")
    }
}

function Require-Phase8Regex {
    param(
        [string]$RelativePath,
        [string]$Pattern,
        [string]$Description
    )

    $fullPath = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $fullPath -PathType Leaf)) {
        $failures.Add("Missing required file: $RelativePath")
        return
    }

    $content = [System.IO.File]::ReadAllText($fullPath)
    if (-not [System.Text.RegularExpressions.Regex]::IsMatch($content, $Pattern)) {
        $failures.Add("Missing $Description in $RelativePath")
    }
}

$neoBuild = "neoforge\build.gradle.kts"
$neoMetadata = "neoforge\src\main\resources\META-INF\neoforge.mods.toml"
$neoMain = "neoforge\src\main\java\porker\pp_legendarydungeons\neoforge\ProfessorPorkersLegendaryDungeonsNeoForge.java"
$neoClient = "neoforge\src\main\java\porker\pp_legendarydungeons\neoforge\ProfessorPorkersLegendaryDungeonsNeoForgeClient.java"

foreach ($requiredPath in @(
    $neoBuild,
    $neoMetadata,
    $neoMain,
    $neoClient,
    "common\build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties"
)) {
    Require-Phase8File $requiredPath
}

if (Test-Path -LiteralPath (Join-Path $root "src")) {
    $failures.Add("The retired root src directory exists.")
}

Require-Phase8Text $neoBuild 'neoForge("net.neoforged:neoforge:${property("neoforge_version")}")'
Require-Phase8Text $neoBuild 'dev.architectury:architectury-neoforge'
Require-Phase8Text $neoBuild 'com.cobblemon:neoforge'
Require-Phase8Text $neoBuild 'kotlinforforge-neoforge'
Require-Phase8Text $neoBuild 'io.wispforest:endec:0.1.8'
Require-Phase8Text $neoBuild 'io.wispforest.endec:gson:0.1.5'
Require-Phase8Text $neoBuild 'io.wispforest.endec:netty:0.1.4'
Require-Phase8Text $neoBuild 'blue.endless:jankson:1.2.3'
Require-Phase8Text $neoBuild 'accessories-neoforge'
Require-Phase8Text $neoBuild 'cobblemon-mega-showdown'
Require-Phase8Text $neoBuild 'transformProductionNeoForge'

Require-Phase8Regex $neoMetadata '(?m)^\s*modLoader\s*=\s*"javafml"\s*$' "javafml loader metadata"
Require-Phase8Regex $neoMetadata '(?m)^\s*modId\s*=\s*"pp_legendarydungeons"\s*$' "stable mod ID"
Require-Phase8Regex `
    $neoMetadata `
    '(?m)^\s*\[features\.pp_legendarydungeons\]\s*$' `
    "single-table Java feature metadata"
Require-Phase8Regex $neoMetadata '(?m)^\s*config\s*=\s*"pp_legendarydungeons\.mixins\.json"\s*$' "shared mixin configuration"
Require-Phase8Regex $neoMetadata '(?ms)\[\[dependencies\.pp_legendarydungeons\]\].*?modId\s*=\s*"minecraft"' "Minecraft dependency"
Require-Phase8Regex $neoMetadata '(?ms)\[\[dependencies\.pp_legendarydungeons\]\].*?modId\s*=\s*"neoforge"' "NeoForge dependency"
Require-Phase8Regex $neoMetadata '(?ms)\[\[dependencies\.pp_legendarydungeons\]\].*?modId\s*=\s*"architectury"' "Architectury dependency"
Require-Phase8Regex $neoMetadata '(?ms)\[\[dependencies\.pp_legendarydungeons\]\].*?modId\s*=\s*"cobblemon"' "Cobblemon dependency"
Require-Phase8Regex $neoMetadata '(?ms)\[\[dependencies\.pp_legendarydungeons\]\].*?modId\s*=\s*"mega_showdown"' "Mega Showdown dependency"

Require-Phase8Text $neoMain "@Mod(LegendaryDungeons.MOD_ID)"
Require-Phase8Text $neoMain "LegendaryDungeons.init();"
Require-Phase8Text $neoClient "Dist.CLIENT"
Require-Phase8Text $neoClient "LegendaryDungeonsClient.init();"

$commonJavaRoot = Join-Path $root "common\src\main\java"
$loaderImports = @(
    Get-ChildItem -LiteralPath $commonJavaRoot -Recurse -File -Filter "*.java" |
        Select-String -Pattern '^\s*import\s+(net\.fabricmc|net\.neoforged)\.'
)

if ($loaderImports.Count -gt 0) {
    $failures.Add("Direct loader Java imports exist in common:`n$($loaderImports | Out-String)")
}

$validationDirectory = Get-Phase8ValidationDirectory $root
$staticMarker = Join-Path $validationDirectory "preflight-static.pass"
$buildMarker = Join-Path $validationDirectory "neoforge-build.pass"
$buildLog = Join-Path $validationDirectory "phase8-neoforge-build.log"
$reportPath = Join-Path $validationDirectory "phase8-preflight-report.txt"

foreach ($marker in @($staticMarker, $buildMarker)) {
    if (Test-Path -LiteralPath $marker) {
        Remove-Item -LiteralPath $marker -Force
    }
}

if ($failures.Count -gt 0) {
    $report = @(
        "Result=FAIL"
        "Recorded=$(Get-Date -Format o)"
        ""
        ($failures -join [Environment]::NewLine)
    ) -join [Environment]::NewLine

    [System.IO.File]::WriteAllText(
        $reportPath,
        $report,
        (New-Object System.Text.UTF8Encoding($false))
    )

    Write-Host "Phase 8 static preflight failed:" -ForegroundColor Red
    $failures | ForEach-Object {
        Write-Host " - $_" -ForegroundColor Red
    }
    throw "Correct the preflight failures before launching NeoForge."
}

Write-Phase8PassMarker $staticMarker "Phase 8 NeoForge static preflight" $root

$metadataContent = [System.IO.File]::ReadAllText((Join-Path $root $neoMetadata))
$dependencyMatches = [System.Text.RegularExpressions.Regex]::Matches(
    $metadataContent,
    '(?m)^\s*modId\s*=\s*"([^"]+)"\s*$'
)
$dependencyIds = @(
    $dependencyMatches |
        ForEach-Object { $_.Groups[1].Value } |
        Select-Object -Unique
)

Write-Host "Static preflight passed." -ForegroundColor Green
Write-Host "NeoForge metadata mod IDs: $($dependencyIds -join ', ')" -ForegroundColor Cyan
Write-Host ""
Write-Host "Building common and NeoForge. This may take several minutes." -ForegroundColor Yellow

$gradleArguments = @(
    ":common:build",
    ":neoforge:clean",
    ":neoforge:build",
    "--no-daemon",
    "--no-parallel",
    "--max-workers=2",
    "--console=plain",
    "--warning-mode",
    "all"
)

$exitCode = Invoke-Phase8GradleToLog `
    -ProjectRoot $root `
    -Arguments $gradleArguments `
    -LogPath $buildLog

if ($exitCode -ne 0) {
    throw "NeoForge preflight build failed with exit code $exitCode. Review: $buildLog"
}

$buildContent = [System.IO.File]::ReadAllText($buildLog)
if (-not $buildContent.Contains("BUILD SUCCESSFUL")) {
    throw "Gradle returned success but BUILD SUCCESSFUL was not found in: $buildLog"
}

Write-Phase8PassMarker $buildMarker "Phase 8 common and NeoForge build" $root

$report = @(
    "Result=PASS"
    "Recorded=$(Get-Date -Format o)"
    "Commit=$(Invoke-Phase8ValidationGit $root @('rev-parse', 'HEAD'))"
    "MetadataModIds=$($dependencyIds -join ',')"
    "BuildLog=$buildLog"
) -join [Environment]::NewLine

[System.IO.File]::WriteAllText(
    $reportPath,
    $report,
    (New-Object System.Text.UTF8Encoding($false))
)

Write-Host ""
Write-Host "Phase 8 NeoForge preflight and build passed." -ForegroundColor Green
Write-Host "Report: $reportPath" -ForegroundColor Cyan
