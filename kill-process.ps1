# ==============================================================================
# Phone Book - Standalone Process & Lock Killer Script
# Author: muddy
# ==============================================================================

$ErrorActionPreference = "SilentlyContinue"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " Stopping all Phone Book and Java background locks..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Terminate any running Phone Book GUI app instances or background Java tasks
Write-Host "`n[1/2] Terminating active processes..." -ForegroundColor Yellow
$targetProcesses = Get-Process | Where-Object { 
    $_.ProcessName -match "java" -or 
    $_.ProcessName -match "javaw" -or 
    $_.CommandLine -match "phonebook" -or 
    $_.MainWindowTitle -match "Phone Book" 
}

if ($targetProcesses) {
    foreach ($proc in $targetProcesses) {
        Write-Host "Stopping process: ID $($proc.Id) - $($proc.ProcessName) ($($proc.MainWindowTitle))" -ForegroundColor DarkYellow
        Stop-Process -Id $proc.Id -Force
    }
    Start-Sleep -Seconds 2
    Write-Host "All matching processes terminated successfully." -ForegroundColor Green
} else {
    Write-Host "No active Phone Book or blocking Java processes found." -ForegroundColor Green
}

# 2. Clear out old build and input staging locks
Write-Host "`n[2/2] Clearing temporary build locks..." -ForegroundColor Yellow
$lockFolders = @("out", "input")
foreach ($folder in $lockFolders) {
    if (Test-Path $folder) {
        Remove-Item -Recurse -Force $folder
        Write-Host "Cleared folder lock: $folder" -ForegroundColor Green
    }
}

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host " Cleanup complete! You can now run your installer." -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan