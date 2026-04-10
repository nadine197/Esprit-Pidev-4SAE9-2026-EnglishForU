param(
    [switch]$SkipFrontendTests,
    [switch]$SkipBackendTests,
    [switch]$SkipSmokeCheck
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$frontendDir = Join-Path $repoRoot "frontend"
$backendDir = Join-Path $repoRoot "backend"
$scriptsDir = Join-Path $repoRoot "scripts"

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Action
    )

    Write-Host "`n[STEP] $Name" -ForegroundColor Cyan
    & $Action

    if ($LASTEXITCODE -ne 0) {
        throw "$Name failed with exit code $LASTEXITCODE"
    }

    Write-Host "[PASS] $Name" -ForegroundColor Green
}

function Invoke-MavenTests {
    param(
        [string]$ModuleName,
        [string]$ModulePath
    )

    $fullPath = (Resolve-Path $ModulePath).Path

    if ($env:JAVA_HOME -and (Get-Command java -ErrorAction SilentlyContinue)) {
        $mvnwCmd = Join-Path $fullPath "mvnw.cmd"
        if (-not (Test-Path $mvnwCmd)) {
            throw "mvnw.cmd not found for module: $ModuleName"
        }

        Invoke-Step "$ModuleName tests (local Java)" {
            Push-Location $fullPath
            try {
                & $mvnwCmd -B test
            }
            finally {
                Pop-Location
            }
        }
        return
    }

    Invoke-Step "$ModuleName tests (Docker Maven)" {
        docker run --rm -v "${fullPath}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-17 mvn -B test
    }
}

Write-Host "[INFO] Demo validation started" -ForegroundColor Cyan

Invoke-Step "Frontend build" {
    Push-Location $frontendDir
    try {
        npm run build
    }
    finally {
        Pop-Location
    }
}

if (-not $SkipFrontendTests) {
    Invoke-Step "Frontend unit tests" {
        Push-Location $frontendDir
        try {
            npm run test -- --watch=false --browsers=ChromeHeadless
        }
        finally {
            Pop-Location
        }
    }
}

if (-not $SkipBackendTests) {
    Invoke-MavenTests -ModuleName "eureka-server" -ModulePath (Join-Path $backendDir "eureka-server")
    Invoke-MavenTests -ModuleName "Gateway" -ModulePath (Join-Path $backendDir "Gateway")
    Invoke-MavenTests -ModuleName "user-management" -ModulePath (Join-Path $backendDir "user-management")
    Invoke-MavenTests -ModuleName "discussion-service" -ModulePath (Join-Path $backendDir "discussion-service")
}

if (-not $SkipSmokeCheck) {
    Invoke-Step "API smoke checks" {
        & powershell -ExecutionPolicy Bypass -File (Join-Path $scriptsDir "smoke-check.ps1")
    }
}

Write-Host "`n[PASS] Demo validation complete." -ForegroundColor Green
