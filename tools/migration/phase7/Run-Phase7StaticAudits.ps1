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
$failures = New-Object System.Collections.Generic.List[string]

function Require-File {
    param([string]$RelativePath)
    $full = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
        $failures.Add("Missing required file: $RelativePath")
    }
}

function Require-Text {
    param(
        [string]$RelativePath,
        [string]$Text
    )

    $full = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
        $failures.Add("Missing required file: $RelativePath")
        return
    }

    $content = [System.IO.File]::ReadAllText($full)
    if (-not $content.Contains($Text)) {
        $failures.Add("Missing required text in ${RelativePath}: $Text")
    }
}

if (Test-Path -LiteralPath (Join-Path $root "src")) {
    $failures.Add("The retired root src directory still exists.")
}

foreach ($required in @(
    "common\build.gradle.kts",
    "fabric\build.gradle.kts",
    "neoforge\build.gradle.kts",
    "common\src\main\java\porker\pp_legendarydungeons\LegendaryDungeons.java",
    "common\src\main\java\porker\pp_legendarydungeons\LegendaryDungeonsClient.java",
    "fabric\src\main\resources\fabric.mod.json",
    "neoforge\src\main\resources\META-INF\neoforge.mods.toml",
    "common\src\main\resources\pp_legendarydungeons.mixins.json"
)) {
    Require-File $required
}

Require-Text "settings.gradle.kts" 'include("common", "fabric", "neoforge")'
Require-Text "common\build.gradle.kts" 'common("fabric", "neoforge")'
Require-Text "fabric\build.gradle.kts" 'transformProductionFabric'
Require-Text "neoforge\build.gradle.kts" 'transformProductionNeoForge'
Require-Text "fabric\src\main\resources\fabric.mod.json" '"id": "pp_legendarydungeons"'
Require-Text "neoforge\src\main\resources\META-INF\neoforge.mods.toml" 'modId = "pp_legendarydungeons"'
Require-Text "neoforge\src\main\resources\META-INF\neoforge.mods.toml" 'config = "pp_legendarydungeons.mixins.json"'

$commonJavaRoot = Join-Path $root "common\src\main\java"
$commonJavaFiles = Get-ChildItem -LiteralPath $commonJavaRoot -Recurse -File -Filter "*.java"

$loaderImports = $commonJavaFiles |
    Select-String -Pattern '^\s*import\s+(net\.fabricmc|net\.neoforged)\.'

if ($loaderImports) {
    $failures.Add("Direct Fabric/NeoForge Java imports exist in common:`n$($loaderImports | Out-String)")
}

$oldEntrypointReferences = $commonJavaFiles |
    Select-String -SimpleMatch "ProfessorPorkersLegendaryDungeons"

if ($oldEntrypointReferences) {
    $failures.Add("Common still references the Fabric entrypoint:`n$($oldEntrypointReferences | Out-String)")
}

$expectedFabricJava = @(
    "ProfessorPorkersLegendaryDungeons.java",
    "ProfessorPorkersLegendaryDungeonsClient.java"
)

$fabricJavaRoot = Join-Path $root "fabric\src\main\java"
$fabricJava = @(
    Get-ChildItem -LiteralPath $fabricJavaRoot -Recurse -File -Filter "*.java" |
        Select-Object -ExpandProperty Name
)

foreach ($name in $fabricJava) {
    if ($expectedFabricJava -notcontains $name) {
        $failures.Add("Unexpected Java implementation in Fabric module: $name")
    }
}

foreach ($name in $expectedFabricJava) {
    if ($fabricJava -notcontains $name) {
        $failures.Add("Missing Fabric entrypoint: $name")
    }
}

$expectedNeoForgeJava = @(
    "ProfessorPorkersLegendaryDungeonsNeoForge.java",
    "ProfessorPorkersLegendaryDungeonsNeoForgeClient.java"
)

$neoJavaRoot = Join-Path $root "neoforge\src\main\java"
$neoJava = @(
    Get-ChildItem -LiteralPath $neoJavaRoot -Recurse -File -Filter "*.java" |
        Select-Object -ExpandProperty Name
)

foreach ($name in $neoJava) {
    if ($expectedNeoForgeJava -notcontains $name) {
        $failures.Add("Unexpected Java implementation in NeoForge module: $name")
    }
}

foreach ($name in $expectedNeoForgeJava) {
    if ($neoJava -notcontains $name) {
        $failures.Add("Missing NeoForge entrypoint: $name")
    }
}

if (Test-Path -LiteralPath (Join-Path $root "common\src\main\resources\fabric.mod.json")) {
    $failures.Add("Fabric metadata was duplicated into common.")
}

if (Test-Path -LiteralPath (Join-Path $root "common\src\main\resources\META-INF\neoforge.mods.toml")) {
    $failures.Add("NeoForge metadata was duplicated into common.")
}

if (Test-Path -LiteralPath (Join-Path $root "fabric\src\main\resources\pp_legendarydungeons.mixins.json")) {
    $failures.Add("Shared mixin configuration was duplicated into Fabric resources.")
}

if (Test-Path -LiteralPath (Join-Path $root "neoforge\src\main\resources\pp_legendarydungeons.mixins.json")) {
    $failures.Add("Shared mixin configuration was duplicated into NeoForge resources.")
}

$validationDirectory = Get-ValidationDirectory $root
$marker = Join-Path $validationDirectory "static-audit.pass"

if ($failures.Count -gt 0) {
    if (Test-Path -LiteralPath $marker) {
        Remove-Item -LiteralPath $marker -Force
    }

    Write-Host "Phase 7 static audit failed:" -ForegroundColor Red
    $failures | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    throw "Correct the static-audit failures before continuing."
}

Write-PassMarker $marker "Phase 7 static module-boundary audit" $root
Write-Host "Phase 7 static audit passed." -ForegroundColor Green
