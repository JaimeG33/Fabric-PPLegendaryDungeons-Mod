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
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outputRoot = Join-Path $env:USERPROFILE "Desktop\phase8-neoforge-diagnostics-$stamp"
$zipPath = "$outputRoot.zip"

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null

function Copy-Phase8DiagnosticFile {
    param([string]$RelativePath)

    $source = Join-Path $root $RelativePath
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        return
    }

    $destination = Join-Path $outputRoot $RelativePath
    $parent = Split-Path -Parent $destination

    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }

    Copy-Item -LiteralPath $source -Destination $destination -Force
}

function Write-Phase8GitDiagnostic {
    param(
        [string[]]$Arguments,
        [string]$OutputName
    )

    $result = Invoke-Phase8ValidationNative `
        -FilePath "git.exe" `
        -Arguments $Arguments `
        -WorkingDirectory $root

    $content = @(
        "Command=git $($Arguments -join ' ')"
        "ExitCode=$($result.ExitCode)"
        ""
        $result.StandardOutput
        $result.StandardError
    ) -join [Environment]::NewLine

    [System.IO.File]::WriteAllText(
        (Join-Path $outputRoot $OutputName),
        $content,
        (New-Object System.Text.UTF8Encoding($false))
    )
}

foreach ($relativePath in @(
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "common\build.gradle.kts",
    "neoforge\build.gradle.kts",
    "neoforge\gradle.properties",
    "neoforge\src\main\resources\META-INF\neoforge.mods.toml",
    "neoforge\src\main\java\porker\pp_legendarydungeons\neoforge\ProfessorPorkersLegendaryDungeonsNeoForge.java",
    "neoforge\src\main\java\porker\pp_legendarydungeons\neoforge\ProfessorPorkersLegendaryDungeonsNeoForgeClient.java",
    "neoforge\run\logs\latest.log",
    "neoforge\run\logs\debug.log",
    "run\logs\latest.log",
    "run\logs\debug.log",
    ".phase8-validation\phase8-neoforge-build.log",
    ".phase8-validation\phase8-preflight-report.txt"
)) {
    Copy-Phase8DiagnosticFile $relativePath
}

foreach ($crashRoot in @(
    (Join-Path $root "neoforge\run\crash-reports"),
    (Join-Path $root "run\crash-reports")
)) {
    if (Test-Path -LiteralPath $crashRoot) {
        Get-ChildItem -LiteralPath $crashRoot -File -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 10 |
            ForEach-Object {
                $destination = Join-Path $outputRoot ("crash-reports\" + $_.Name)
                $parent = Split-Path -Parent $destination
                if (-not (Test-Path -LiteralPath $parent)) {
                    New-Item -ItemType Directory -Path $parent -Force | Out-Null
                }
                Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
            }
    }
}

$daemonRoot = Join-Path $env:USERPROFILE ".gradle\daemon"
if (Test-Path -LiteralPath $daemonRoot) {
    $latestDaemon = Get-ChildItem -LiteralPath $daemonRoot -Recurse -File -Filter "daemon-*.out.log" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

    if ($null -ne $latestDaemon) {
        Copy-Item -LiteralPath $latestDaemon.FullName `
            -Destination (Join-Path $outputRoot "latest-gradle-daemon.log") `
            -Force

        Get-Content -LiteralPath $latestDaemon.FullName -Tail 800 |
            Set-Content -LiteralPath (Join-Path $outputRoot "latest-gradle-daemon-tail-800.txt") `
                -Encoding UTF8
    }
}

Write-Phase8GitDiagnostic @("status", "--short") "git-status-short.txt"
Write-Phase8GitDiagnostic @("branch", "--show-current") "git-branch.txt"
Write-Phase8GitDiagnostic @("rev-parse", "HEAD") "git-head.txt"
Write-Phase8GitDiagnostic @("log", "-8", "--oneline", "--decorate") "git-recent-commits.txt"

$jarDirectories = @(
    (Join-Path $root "neoforge\build\libs"),
    (Join-Path $root "common\build\libs")
)

$jarLines = New-Object System.Collections.Generic.List[string]
foreach ($directory in $jarDirectories) {
    $jarLines.Add("Directory=$directory")

    if (Test-Path -LiteralPath $directory) {
        Get-ChildItem -LiteralPath $directory -File |
            Sort-Object Name |
            ForEach-Object {
                $jarLines.Add(
                    "$($_.Name) | $($_.Length) bytes | $($_.LastWriteTime.ToString('o'))"
                )
            }
    }
    else {
        $jarLines.Add("MISSING")
    }

    $jarLines.Add("")
}
$jarLines | Set-Content -LiteralPath (Join-Path $outputRoot "build-artifacts.txt") -Encoding UTF8

$os = Get-CimInstance Win32_OperatingSystem
$computer = Get-CimInstance Win32_ComputerSystem
$javaProcesses = Get-Process java, javaw -ErrorAction SilentlyContinue |
    Select-Object Id, ProcessName, CPU, WorkingSet64, PrivateMemorySize64, StartTime

@(
    "Collected=$(Get-Date -Format o)"
    "Computer=$env:COMPUTERNAME"
    "TotalPhysicalMemoryGB=$([math]::Round($computer.TotalPhysicalMemory / 1GB, 2))"
    "FreePhysicalMemoryGB=$([math]::Round(($os.FreePhysicalMemory * 1KB) / 1GB, 2))"
    ""
    "JavaProcesses:"
    ($javaProcesses | Format-Table -AutoSize | Out-String)
) | Set-Content -LiteralPath (Join-Path $outputRoot "system-memory-and-java.txt") -Encoding UTF8

@(
    "This bundle was generated read-only."
    "It did not edit source files, Git history, Gradle files, or dependencies."
    "ProjectRoot=$root"
) | Set-Content -LiteralPath (Join-Path $outputRoot "README.txt") -Encoding UTF8

Compress-Archive -LiteralPath $outputRoot -DestinationPath $zipPath -Force

Write-Host ""
Write-Host "Phase 8 diagnostic bundle created:" -ForegroundColor Green
Write-Host $zipPath -ForegroundColor Cyan
Write-Host "Upload that ZIP before applying runtime fixes." -ForegroundColor Yellow
