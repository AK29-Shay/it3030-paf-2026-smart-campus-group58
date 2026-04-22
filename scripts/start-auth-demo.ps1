$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$backendRoot = Join-Path $repoRoot "backend\smartcampus"
$sourceRoot = Join-Path $backendRoot "src\main\java"
$outRoot = Join-Path $backendRoot "out"
$stdoutLog = Join-Path $backendRoot "auth-demo.log"
$stderrLog = Join-Path $backendRoot "auth-demo.err.log"
$port = 8080

function Get-RequiredCommand {
    param([string]$Name)

    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $command) {
        throw "Required command '$Name' was not found on PATH."
    }
    return $command
}

function Get-AuthDemoProcess {
    $connection = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $connection) {
        return $null
    }

    $process = Get-CimInstance Win32_Process -Filter "ProcessId = $($connection.OwningProcess)" -ErrorAction SilentlyContinue
    if (-not $process) {
        return $null
    }

    if ($process.CommandLine -like "*com.project.smartcampus.SmartcampusApplication*") {
        return $process
    }

    throw "Port $port is already in use by another process ($($process.Name), PID $($process.ProcessId)). Stop that process or change the configured server port first."
}

function Clear-OutputDirectory {
    if (-not (Test-Path $outRoot)) {
        New-Item -ItemType Directory -Path $outRoot | Out-Null
        return
    }

    Get-ChildItem $outRoot -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force
}

Get-RequiredCommand java | Out-Null
Get-RequiredCommand javac | Out-Null

$existing = Get-AuthDemoProcess
if ($existing) {
    Write-Host "Stopping existing Smart Campus auth demo (PID $($existing.ProcessId))..."
    Stop-Process -Id $existing.ProcessId -Force
    Start-Sleep -Seconds 1
}

$javaFiles = Get-ChildItem $sourceRoot -Recurse -Filter *.java | Where-Object { $_.Length -gt 0 } | Select-Object -ExpandProperty FullName
if (-not $javaFiles) {
    throw "No non-empty Java source files were found under $sourceRoot."
}

Clear-OutputDirectory

Write-Host "Compiling Smart Campus auth demo..."
& javac -encoding UTF-8 -d $outRoot $javaFiles

Write-Host "Starting Smart Campus auth demo on http://localhost:$port ..."
$process = Start-Process java `
    -ArgumentList @("-cp", $outRoot, "com.project.smartcampus.SmartcampusApplication") `
    -WorkingDirectory $repoRoot `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -PassThru

Start-Sleep -Seconds 2

if ($process.HasExited) {
    $stderr = if (Test-Path $stderrLog) { Get-Content $stderrLog -Raw } else { "" }
    throw "Smart Campus auth demo exited immediately. $stderr"
}

$health = Invoke-RestMethod -Uri "http://localhost:$port/actuator/health" -Method Get
if ($health.status -ne "UP") {
    throw "Auth demo started but health check did not report UP."
}

Write-Host "Smart Campus auth demo is running."
Write-Host "PID: $($process.Id)"
Write-Host "Health: http://localhost:$port/actuator/health"
Write-Host "Seed user: student@example.com / ChangeMe123!"
Write-Host "Seed admin: admin@example.com / ChangeMe123!"
