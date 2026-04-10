param(
    [string]$PostgresContainer = "englishforu-postgres",
    [string]$DbName = "GestionUserPI",
    [string]$DbUser = "postgres",
    [string]$SeedFile = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($SeedFile)) {
    $SeedFile = Join-Path $PSScriptRoot "demo-seed-data.sql"
}

if (-not (Test-Path $SeedFile)) {
    throw "Seed file not found: $SeedFile"
}

Write-Host "[INFO] Seeding demo data from $SeedFile" -ForegroundColor Cyan

$containerRunning = docker ps --format "{{.Names}}" | Where-Object { $_ -eq $PostgresContainer }
if (-not $containerRunning) {
    throw "Postgres container '$PostgresContainer' is not running. Start Docker stack first."
}

Get-Content -Path $SeedFile -Raw |
    docker exec -i $PostgresContainer psql -v ON_ERROR_STOP=1 -U $DbUser -d $DbName

if ($LASTEXITCODE -ne 0) {
    throw "Seeding failed with exit code $LASTEXITCODE"
}

Write-Host "[INFO] Demo seed applied. Summary counts:" -ForegroundColor Cyan

docker exec $PostgresContainer psql -U $DbUser -d $DbName -c "
SELECT
    (SELECT COUNT(*) FROM users WHERE email LIKE '%@demo.englishforu.local') AS demo_users,
    (SELECT COUNT(*) FROM discussion_posts WHERE author_email LIKE '%@demo.englishforu.local') AS demo_posts,
    (SELECT COUNT(*) FROM discussion_comments WHERE author_email LIKE '%@demo.englishforu.local') AS demo_discussion_comments,
    (SELECT COUNT(*) FROM reports WHERE title LIKE 'Demo:%') AS demo_reports,
    (SELECT COUNT(*) FROM report_comments WHERE report_id IN (SELECT id FROM reports WHERE title LIKE 'Demo:%')) AS demo_report_comments,
    (SELECT COUNT(*) FROM notifications WHERE title LIKE 'Demo:%' OR recipient_user_id IN (SELECT id FROM users WHERE email LIKE '%@demo.englishforu.local')) AS demo_notifications;
"

if ($LASTEXITCODE -ne 0) {
    throw "Count query failed with exit code $LASTEXITCODE"
}

Write-Host "[PASS] Demo data is ready." -ForegroundColor Green
