param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$OutputPath = Join-Path $ProjectRoot "phase6-log-review.log"
$CandidateLogs = @(
    (Join-Path $ProjectRoot "phase6-static-audit.log"),
    (Join-Path $ProjectRoot "phase6-clean-build.log"),
    (Join-Path $ProjectRoot "phase6-artifact-audit.log"),
    (Join-Path $ProjectRoot "phase6-run-client.log"),
    (Join-Path $ProjectRoot "phase6-run-server.log")
)

$Patterns = @(
    "Exception",
    "ERROR",
    "already registered",
    "ClassCastException",
    "Failed to encode",
    "Failed to decode",
    "NoClassDefFoundError",
    "Could not execute entrypoint",
    "Missing registry",
    "Unknown custom packet payload",
    "disconnect",
    "WARN"
)

$existingLogs = $CandidateLogs | Where-Object { Test-Path -LiteralPath $_ -PathType Leaf }

$review = New-Object System.Collections.Generic.List[string]
$review.Add("Phase 6 log review")
$review.Add("Generated: $(Get-Date -Format o)")
$review.Add("Project: $ProjectRoot")
$review.Add("")

if (-not $existingLogs) {
    $review.Add("No Phase 6 logs were found.")
    $review.Add("Expected possible paths:")
    foreach ($path in $CandidateLogs) {
        $review.Add("- $path")
    }
    $review | Set-Content -LiteralPath $OutputPath -Encoding UTF8
    Write-Warning "No logs found. Review written to: $OutputPath"
    exit 0
}

$review.Add("Logs included:")
foreach ($path in $existingLogs) {
    $review.Add("- $path")
}

foreach ($pattern in $Patterns) {
    $review.Add("")
    $review.Add(("=" * 80))
    $review.Add("PATTERN: $pattern")
    $review.Add(("=" * 80))

    $matches = Select-String -LiteralPath $existingLogs -Pattern $pattern -CaseSensitive:$false
    if ($matches) {
        foreach ($match in $matches) {
            $review.Add(("{0}:{1}: {2}" -f $match.Path, $match.LineNumber, $match.Line.Trim()))
        }
    } else {
        $review.Add("No matches.")
    }
}

$review.Add("")
$review.Add(("=" * 80))
$review.Add("MANUAL CLASSIFICATION")
$review.Add(("=" * 80))
$review.Add("Classify each meaningful match as one of:")
$review.Add("- Migration regression")
$review.Add("- Existing project warning")
$review.Add("- Missing optional development dependency")
$review.Add("- Third-party warning")
$review.Add("- Harmless shutdown message")
$review.Add("- Release blocker")
$review.Add("")
$review.Add("Do not ignore an exception solely because the game remained open.")

$review | Set-Content -LiteralPath $OutputPath -Encoding UTF8
Write-Host "Phase 6 log review written to: $OutputPath" -ForegroundColor Green
