param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$GradleWrapper = Join-Path $ProjectRoot "gradlew.bat"
$BuildLog = Join-Path $ProjectRoot "phase6-clean-build.log"
$ArtifactLog = Join-Path $ProjectRoot "phase6-artifact-audit.log"
$LibsDirectory = Join-Path $ProjectRoot "build\libs"

if (-not (Test-Path -LiteralPath $GradleWrapper -PathType Leaf)) {
    throw "Gradle wrapper was not found: $GradleWrapper"
}

Push-Location $ProjectRoot
try {
    Write-Host "Stopping stale Gradle daemons..."
    & $GradleWrapper --stop
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle --stop failed with exit code $LASTEXITCODE."
    }

    Write-Host "Running clean build with all warnings..."
    & $GradleWrapper clean build --warning-mode all 2>&1 |
        Tee-Object -FilePath $BuildLog

    if ($LASTEXITCODE -ne 0) {
        throw "Phase 6 clean build failed. Review: $BuildLog"
    }

    if (-not (Test-Path -LiteralPath $LibsDirectory -PathType Container)) {
        throw "Build succeeded but build/libs was not found: $LibsDirectory"
    }

    $allJars = Get-ChildItem -LiteralPath $LibsDirectory -Filter "*.jar" -File |
        Sort-Object Name

    $mainJar = $allJars |
        Where-Object { $_.Name -notmatch "sources|dev|shadow" } |
        Select-Object -First 1

    $sourcesJar = $allJars |
        Where-Object { $_.Name -match "sources" } |
        Select-Object -First 1

    $audit = New-Object System.Collections.Generic.List[string]
    $audit.Add("Phase 6 artifact audit")
    $audit.Add("Generated: $(Get-Date -Format o)")
    $audit.Add("Project: $ProjectRoot")
    $audit.Add("")
    $audit.Add("JARs under build/libs:")
    foreach ($jarFile in $allJars) {
        $audit.Add("- $($jarFile.FullName) [$($jarFile.Length) bytes]")
    }

    if (-not $mainJar) {
        $audit.Add("")
        $audit.Add("ERROR: No main remapped JAR was found.")
        $audit | Set-Content -LiteralPath $ArtifactLog -Encoding UTF8
        throw "No main JAR was found. Review: $ArtifactLog"
    }

    if (-not $sourcesJar) {
        $audit.Add("")
        $audit.Add("WARNING: No sources JAR was found.")
    }

    $jarCommand = Get-Command jar -ErrorAction SilentlyContinue
    if (-not $jarCommand) {
        $audit.Add("")
        $audit.Add("ERROR: The Java 'jar' command is not available on PATH.")
        $audit | Set-Content -LiteralPath $ArtifactLog -Encoding UTF8
        throw "The Java jar command is unavailable. Review: $ArtifactLog"
    }

    $entries = & jar tf $mainJar.FullName 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Could not list the main JAR contents: $($mainJar.FullName)"
    }

    $requiredPatterns = @(
        "^fabric\.mod\.json$",
        "^pp_legendarydungeons\.mixins\.json$",
        "^porker/pp_legendarydungeons/LegendaryDungeons\.class$",
        "^porker/pp_legendarydungeons/LegendaryDungeonsClient\.class$",
        "^assets/pp_legendarydungeons/",
        "^data/pp_legendarydungeons/"
    )

    $forbiddenPatterns = @(
        "DungeonBlockBreakMixin\.class$",
        "DungeonBlockPlacementMixin\.class$"
    )

    $audit.Add("")
    $audit.Add("Required content checks:")
    foreach ($pattern in $requiredPatterns) {
        $found = $entries | Select-String -Pattern $pattern | Select-Object -First 1
        if ($found) {
            $audit.Add("PASS: $pattern -> $($found.Line)")
        } else {
            $audit.Add("FAIL: $pattern")
        }
    }

    $audit.Add("")
    $audit.Add("Forbidden removed-class checks:")
    foreach ($pattern in $forbiddenPatterns) {
        $found = $entries | Select-String -Pattern $pattern | Select-Object -First 1
        if ($found) {
            $audit.Add("FAIL: $pattern -> $($found.Line)")
        } else {
            $audit.Add("PASS: $pattern absent")
        }
    }

    $audit | Set-Content -LiteralPath $ArtifactLog -Encoding UTF8

    Write-Host ""
    Write-Host "Build completed successfully." -ForegroundColor Green
    Write-Host "Build log:    $BuildLog"
    Write-Host "Artifact log: $ArtifactLog"
    Write-Host "Main JAR:     $($mainJar.FullName)"
    if ($sourcesJar) {
        Write-Host "Sources JAR:  $($sourcesJar.FullName)"
    }

    $failedChecks = $audit | Where-Object { $_ -like "FAIL:*" }
    if ($failedChecks) {
        Write-Warning "One or more artifact checks failed. Review $ArtifactLog before continuing."
    }
}
finally {
    Pop-Location
}
