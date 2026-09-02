# ==============================================================================
# Smart Campus Parking and Traffic Management System - PowerShell Build & Run
# ==============================================================================

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "     SMART CAMPUS PARKING AND TRAFFIC MANAGEMENT SYSTEM" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

if (-not (Test-Path -Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Compiling Java source files..." -ForegroundColor Yellow
javac -cp "lib/mysql-connector-j-8.3.0.jar;src" -d bin src/*.java

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[OK] Compilation successful." -ForegroundColor Green
Write-Host ""
Write-Host "[2/2] Launching Application..." -ForegroundColor Yellow
Write-Host ""

java -cp "bin;lib/mysql-connector-j-8.3.0.jar" Main
