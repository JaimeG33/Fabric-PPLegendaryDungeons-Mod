Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$script:Phase8Branch = "architectury-multiloader-migration"
$script:Phase8BaseCommit = "01edde6c0c5f4dc0fda9c3fa96d8d3a859ee34cc"

function Resolve-Phase8ValidationRoot {
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

function ConvertTo-Phase8ValidationArgumentString {
    param([string[]]$Arguments)

    $quoted = foreach ($argument in $Arguments) {
        if ($null -eq $argument) {
            '""'
            continue
        }

        if ($argument -notmatch '[\s"]') {
            $argument
            continue
        }

        '"' + ($argument -replace '(\\*)"', '$1$1\"' -replace '(\\+)$', '$1$1') + '"'
    }

    return ($quoted -join " ")
}

function Invoke-Phase8ValidationNative {
    param(
        [string]$FilePath,
        [string[]]$Arguments,
        [string]$WorkingDirectory
    )

    $startInfo = New-Object System.Diagnostics.ProcessStartInfo
    $startInfo.FileName = $FilePath
    $startInfo.Arguments = ConvertTo-Phase8ValidationArgumentString $Arguments
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.CreateNoWindow = $true

    $process = New-Object System.Diagnostics.Process
    $process.StartInfo = $startInfo

    if (-not $process.Start()) {
        throw "Could not start native process: $FilePath"
    }

    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $process.WaitForExit()

    return [pscustomobject]@{
        ExitCode = $process.ExitCode
        StandardOutput = $stdoutTask.Result
        StandardError = $stderrTask.Result
    }
}

function Invoke-Phase8ValidationGit {
    param(
        [string]$ProjectRoot,
        [string[]]$Arguments
    )

    $result = Invoke-Phase8ValidationNative `
        -FilePath "git.exe" `
        -Arguments $Arguments `
        -WorkingDirectory $ProjectRoot

    if ($result.ExitCode -ne 0) {
        throw "git $($Arguments -join ' ') failed with exit code $($result.ExitCode).`n$($result.StandardError)"
    }

    return $result.StandardOutput.Trim()
}

function Assert-Phase8ValidationState {
    param(
        [string]$ProjectRoot,
        [switch]$RequireClean
    )

    $branch = Invoke-Phase8ValidationGit $ProjectRoot @("branch", "--show-current")
    if ($branch -ne $script:Phase8Branch) {
        throw "Expected branch '$($script:Phase8Branch)', found '$branch'."
    }

    $ancestorResult = Invoke-Phase8ValidationNative `
        -FilePath "git.exe" `
        -Arguments @("merge-base", "--is-ancestor", $script:Phase8BaseCommit, "HEAD") `
        -WorkingDirectory $ProjectRoot

    if ($ancestorResult.ExitCode -ne 0) {
        throw "Phase 7 completion commit is not an ancestor of HEAD."
    }

    if ($RequireClean) {
        $status = Invoke-Phase8ValidationGit $ProjectRoot @("status", "--porcelain")
        if (-not [string]::IsNullOrWhiteSpace($status)) {
            throw "Working tree must be clean before validation.`n$status"
        }
    }
}

function Get-Phase8ValidationDirectory {
    param([string]$ProjectRoot)

    $directory = Join-Path $ProjectRoot ".phase8-validation"
    if (-not (Test-Path -LiteralPath $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
    }

    return $directory
}

function Invoke-Phase8GradleToLog {
    param(
        [string]$ProjectRoot,
        [string[]]$Arguments,
        [string]$LogPath
    )

    $wrapper = Join-Path $ProjectRoot "gradlew.bat"
    $previousPreference = $ErrorActionPreference

    Push-Location $ProjectRoot
    try {
        $ErrorActionPreference = "Continue"
        & $wrapper @Arguments *> $LogPath
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

function Write-Phase8PassMarker {
    param(
        [string]$Path,
        [string]$CheckName,
        [string]$ProjectRoot
    )

    $head = Invoke-Phase8ValidationGit $ProjectRoot @("rev-parse", "HEAD")
    $content = @(
        "Result=PASS"
        "Check=$CheckName"
        "Commit=$head"
        "Recorded=$(Get-Date -Format o)"
    ) -join [Environment]::NewLine

    [System.IO.File]::WriteAllText(
        $Path,
        $content,
        (New-Object System.Text.UTF8Encoding($false))
    )
}
