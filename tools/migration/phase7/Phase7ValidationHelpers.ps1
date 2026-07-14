Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Resolve-ValidationProjectRoot {
    param([string]$ProjectRoot)

    if ([string]::IsNullOrWhiteSpace($ProjectRoot)) {
        $ProjectRoot = "D:\Minecraft Stuff\Mod Projects\professor-porkers-legendary-dungeons"
    }

    $resolved = [System.IO.Path]::GetFullPath($ProjectRoot)

    if (-not (Test-Path -LiteralPath (Join-Path $resolved ".git"))) {
        throw "Git project not found: $resolved"
    }

    return $resolved
}

function Invoke-ValidationProcess {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$WorkingDirectory,
        [string]$LogPath
    )

    $parent = Split-Path -Parent $LogPath
    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }

    Write-Host "Running: $FilePath $($Arguments -join ' ')" -ForegroundColor Cyan
    Write-Host "This command may take several minutes." -ForegroundColor DarkGray
    Write-Host "Output is being written to:" -ForegroundColor DarkGray
    Write-Host $LogPath -ForegroundColor DarkGray

    Push-Location $WorkingDirectory
    $previousPreference = $ErrorActionPreference

    try {
        # Windows PowerShell 5.1 converts native stderr into error records.
        # Gradle writes ordinary warnings to stderr, so use its exit code.
        $ErrorActionPreference = "Continue"

        & $FilePath @Arguments *> $LogPath
        $exitCode = $LASTEXITCODE
    }
    finally {
        $ErrorActionPreference = $previousPreference
        Pop-Location
    }

    if (Test-Path -LiteralPath $LogPath) {
        Get-Content -LiteralPath $LogPath | ForEach-Object {
            Write-Host $_
        }
    }

    return $exitCode
}

function Get-ValidationHead {
    param([string]$ProjectRoot)

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = "git.exe"
    $startInfo.Arguments = "rev-parse HEAD"
    $startInfo.WorkingDirectory = $ProjectRoot
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo
    $process.Start() | Out-Null
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        throw "Could not read Git HEAD. $stderr"
    }

    return $stdout.Trim()
}

function Get-ValidationDirectory {
    param([string]$ProjectRoot)

    $directory = Join-Path $ProjectRoot ".phase7-validation"
    if (-not (Test-Path -LiteralPath $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
    }
    return $directory
}

function Write-PassMarker {
    param(
        [string]$Path,
        [string]$Name,
        [string]$ProjectRoot
    )

    $content = @(
        "Result: PASS"
        "Check: $Name"
        "Commit: $(Get-ValidationHead $ProjectRoot)"
        "Recorded: $(Get-Date -Format o)"
    ) -join [Environment]::NewLine

    [System.IO.File]::WriteAllText(
        $Path,
        $content,
        (New-Object System.Text.UTF8Encoding($false))
    )
}
