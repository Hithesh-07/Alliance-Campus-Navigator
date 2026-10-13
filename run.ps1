# ==============================================================================
# CampusNav - Alliance University Campus Navigation System
# PowerShell Launch Script
# ==============================================================================

$ErrorActionPreference = "Stop"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  CampusNav - Alliance University Campus Navigation System (Desktop)" -ForegroundColor White
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Verify Java compiler & runtime
try {
    $javaOutput = java -version 2>&1
    Write-Host "[✓] Java environment detected." -ForegroundColor Green
} catch {
    Write-Host "[✗] Java (JDK 21+) is not available in system PATH." -ForegroundColor Red
    Write-Host "    Please install JDK 21 or newer and verify 'javac' and 'java' commands." -ForegroundColor Yellow
    exit 1
}

# 2. Compile source files if needed
Write-Host "Checking build and compiling sources..." -ForegroundColor DarkGray
if (!(Test-Path out)) {
    New-Item -ItemType Directory -Path out | Out-Null
}

$sourceFiles = Get-ChildItem -Recurse -Path src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d out $sourceFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[✗] Compilation failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit 1
}

Write-Host "[✓] Compilation successful." -ForegroundColor Green
Write-Host "Launching desktop window..." -ForegroundColor Cyan

# 3. Launch application
java -cp out Main
