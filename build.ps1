# ==============================================================================
# Phone Book - Automated Build & Publish Script
# Author: muddy
# ==============================================================================

$ErrorActionPreference = "Stop"

# Define explicit JDK tool paths using your exact JDK directory
$jdkBin = "C:\Program Files\Java\jdk-27.0.1.0.3\bin"
$javacPath    = Join-Path $jdkBin "javac.exe"
$jarPath      = Join-Path $jdkBin "jar.exe"
$jpackagePath = Join-Path $jdkBin "jpackage.exe"

Write-Host "==================================================" -ForegroundColor Cyan

foreach ($requiredFile in @("mysql-connector-j-26.10.0.jar", "phonebook.png", "schema.sql")) {
    if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "Required application file is missing: $requiredFile"
    }
}
Write-Host " Starting Phone Book Build & Package Process" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

# 1. Clean previous build artifacts
Write-Host "`n[1/4] Cleaning previous build folders..." -ForegroundColor Yellow
if (Test-Path "out") { Remove-Item -Recurse -Force "out" }
# Preserve previously built installers in dist; jpackage writes the new build there.
if (Test-Path "input") { Remove-Item -Recurse -Force "input" }

New-Item -ItemType Directory -Force "out" | Out-Null
New-Item -ItemType Directory -Force "dist" | Out-Null
New-Item -ItemType Directory -Force "input" | Out-Null

# 2. Compile Java Source Files
Write-Host "[2/4] Compiling Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path . -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

if (-not $javaFiles) {
    Write-Error "No .java files found in the current directory!"
    exit 1
}

# The source uses java.sql and loads the MySQL driver reflectively, so the
# connector is packaged for runtime but is not needed on javac's classpath.
& $javacPath -d "out" $javaFiles
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE."
}

# 3. Package into JAR
Write-Host "[3/4] Packaging application into JAR..." -ForegroundColor Yellow
# Copy MySQL driver to input folder for jpackage runtime inclusion
Copy-Item "mysql-connector-j-26.10.0.jar" "input/"

# Package the runtime resources so the installed app does not depend on its
# current working directory for the logo or database schema.
Copy-Item -LiteralPath "phonebook.png", "schema.sql" -Destination "out/"

# Create Manifest file. MainDashboard owns the application's main() method.
$manifestDir = "out/META-INF"
New-Item -ItemType Directory -Force $manifestDir | Out-Null
$manifestPath = "$manifestDir/MANIFEST.MF"

$manifestContent = @"
Manifest-Version: 1.0
Main-Class: MainDashboard
Class-Path: mysql-connector-j-26.10.0.jar
"@
Set-Content -Path $manifestPath -Value $manifestContent -Encoding ASCII

# Create main application jar
& $jarPath --create --file="input/phonebook.jar" --manifest="$manifestPath" -C "out" .
if ($LASTEXITCODE -ne 0) {
    throw "JAR packaging failed with exit code $LASTEXITCODE."
}

# 4. Generate Windows .EXE Installer using jpackage
Write-Host "[4/4] Generating Windows .exe installer with jpackage..." -ForegroundColor Yellow

# Give jpackage and WiX a fresh temp location for each run. WiX can fail while
# linking the MSI if its shared Windows temp directory is locked or cleaned up.
$tempId = [guid]::NewGuid().ToString("N")
$packageTemp = Join-Path $env:TEMP ("PhoneBook-jpackage-" + $tempId)
$wixTemp = Join-Path $env:TEMP ("PhoneBook-wix-" + $tempId)
New-Item -ItemType Directory -Path $packageTemp | Out-Null
New-Item -ItemType Directory -Path $wixTemp | Out-Null
$previousTemp = $env:TEMP
$previousTmp = $env:TMP
$env:TEMP = $wixTemp
$env:TMP = $wixTemp

$iconParam = @()
if (Test-Path "phonebook.ico") {
    $iconParam = @("--icon", "phonebook.ico")
} elseif (Test-Path "phonebook.png") {
    Write-Host "Tip: Windows jpackage prefers a .ico file for app icons. Using phonebook.png." -ForegroundColor DarkYellow
    $iconParam = @("--icon", "phonebook.png")
}

& $jpackagePath `
  --type exe `
  --temp $packageTemp `
  --name "Phone Book" `
  --app-version "1.0.3" `
  --vendor "muddy" `
  --input "input" `
  --main-jar "phonebook.jar" `
  --main-class "MainDashboard" `
  @iconParam `
  --dest "dist" `
  --win-dir-chooser `
  --win-shortcut `
  --win-menu `
  --description "Phone Book Management System by muddy"
$jpackageExitCode = $LASTEXITCODE
$env:TEMP = $previousTemp
$env:TMP = $previousTmp
if ($jpackageExitCode -ne 0) {
    throw "jpackage failed with exit code $jpackageExitCode. Temporary packaging files were kept at: $packageTemp and $wixTemp"
}

Remove-Item -LiteralPath $packageTemp -Recurse -Force
Remove-Item -LiteralPath $wixTemp -Recurse -Force

Write-Host "`n==================================================" -ForegroundColor Green
Write-Host " Build Successful! Your installer is ready in:" -ForegroundColor Green
Write-Host " $(Get-Location)\dist" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Cyan
