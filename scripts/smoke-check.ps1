param(
    [string]$FrontendUrl = "http://localhost:4200",
    [string]$GatewayUrl = "http://localhost:8090",
    [string]$UserServiceUrl = "http://localhost:8081",
    [string]$DiscussionServiceUrl = "http://localhost:8088",
    [string]$EurekaUrl = "http://localhost:8761"
)

$ErrorActionPreference = "Stop"

function Invoke-Check {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [int[]]$AllowedStatus,
        [string]$Body = "",
        [string]$ContentType = "application/json"
    )

    $statusCode = 0

    try {
        if ($Method -eq "POST") {
            $response = Invoke-WebRequest -Uri $Url -Method POST -Body $Body -ContentType $ContentType -TimeoutSec 20
        } else {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -TimeoutSec 20
        }
        $statusCode = [int]$response.StatusCode
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode.value__
        } else {
            Write-Host ("[FAIL] {0} -> connection error" -f $Name) -ForegroundColor Red
            return $false
        }
    }

    if ($AllowedStatus -contains $statusCode) {
        Write-Host ("[PASS] {0} -> HTTP {1}" -f $Name, $statusCode) -ForegroundColor Green
        return $true
    }

    Write-Host ("[FAIL] {0} -> HTTP {1}, expected {2}" -f $Name, $statusCode, ($AllowedStatus -join ",")) -ForegroundColor Red
    return $false
}

$checks = @(
    @{ Name = "Frontend"; Method = "GET"; Url = $FrontendUrl; Allowed = @(200) },
    @{ Name = "Eureka Health"; Method = "GET"; Url = "$EurekaUrl/actuator/health"; Allowed = @(200) },
    @{ Name = "User Service Health"; Method = "GET"; Url = "$UserServiceUrl/actuator/health"; Allowed = @(200) },
    @{ Name = "Discussion Service Health"; Method = "GET"; Url = "$DiscussionServiceUrl/actuator/health"; Allowed = @(200) },
    @{ Name = "Gateway Health"; Method = "GET"; Url = "$GatewayUrl/actuator/health"; Allowed = @(200) },
    @{ Name = "Gateway Login Invalid Payload"; Method = "POST"; Url = "$GatewayUrl/api/auth/login"; Allowed = @(400,401); Body = "{}" },
    @{ Name = "Reports Mine Unauthenticated"; Method = "GET"; Url = "$GatewayUrl/api/reports/mine"; Allowed = @(401,403) },
    @{ Name = "Helpdesk Reports Unauthenticated"; Method = "GET"; Url = "$GatewayUrl/api/helpdesk/reports"; Allowed = @(401,403) },
    @{ Name = "Notifications Unauthenticated"; Method = "GET"; Url = "$GatewayUrl/api/notifications"; Allowed = @(401,403) },
    @{ Name = "Discussion Feed Unauthenticated"; Method = "GET"; Url = "$GatewayUrl/api/discussions/feed"; Allowed = @(401,403) }
)

$failed = 0

foreach ($check in $checks) {
    $bodyValue = ""
    if ($check.ContainsKey("Body")) {
        $bodyValue = [string]$check.Body
    }

    $ok = Invoke-Check -Name $check.Name -Method $check.Method -Url $check.Url -AllowedStatus $check.Allowed -Body $bodyValue
    if (-not $ok) {
        $failed++
    }
}

if ($failed -gt 0) {
    Write-Host ("Smoke check failed: {0} check(s)" -f $failed) -ForegroundColor Red
    exit 1
}

Write-Host "Smoke check passed" -ForegroundColor Green
exit 0
