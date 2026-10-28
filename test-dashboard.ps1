# Dashboard endpoint smoke test.
# Start the backend first with: mvn spring-boot:run
# Run the fixture SQL in pgAdmin, then run this script.

param(
    [string]$ApiUrl = "http://localhost:8080",
    [string]$DatabaseUrl = "",
    [string]$ClientId = "",
    [ValidateSet("30D", "90D", "180D", "365D")]
    [string]$Period = "30D"
)

$ErrorActionPreference = "Stop"

function Fail-Test([string]$Message) {
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    exit 1
}

if ([string]::IsNullOrWhiteSpace($ClientId) -and -not [string]::IsNullOrWhiteSpace($DatabaseUrl)) {
    $query = "SELECT client_id FROM clients WHERE email LIKE 'dashboard-fixture.%@example.test' ORDER BY join_date DESC LIMIT 1;"
    $ClientId = ((& psql $DatabaseUrl -Atc $query | Select-Object -First 1).ToString()).Trim()
}

if ([string]::IsNullOrWhiteSpace($ClientId)) {
    Fail-Test "Provide -ClientId from the SQL script output, or provide -DatabaseUrl so the latest fixture client can be discovered."
}

try {
    [void][Guid]::Parse($ClientId)
}
catch {
    Fail-Test "ClientId is not a valid UUID: $ClientId"
}

$endpoint = "$ApiUrl/api/dashboard?clientId=$ClientId&period=$Period"
Write-Host "Testing $endpoint" -ForegroundColor Cyan

try {
    $dashboard = Invoke-RestMethod -Uri $endpoint -Method Get
}
catch {
    Fail-Test "Dashboard request failed: $($_.Exception.Message)"
}

$accounts = @($dashboard.accounts)
$marketMovers = @($dashboard.marketMovers)
$watchlist = @($dashboard.watchlist)
$recentTrades = @($dashboard.recentTrades)
$portfolioHistory = @($dashboard.portfolioHistory)

if ($accounts.Count -ne 2) {
    Fail-Test "Expected exactly 2 active accounts, received $($accounts.Count)."
}

if (@($accounts | Where-Object { $_.status -ne "ACTIVE" }).Count -ne 0) {
    Fail-Test "The accounts section contains a non-active account."
}

if ($marketMovers.Count -lt 2) {
    Fail-Test "Expected at least 2 market movers, received $($marketMovers.Count)."
}

if ($watchlist.Count -ne 2) {
    Fail-Test "Expected 2 watchlist securities, received $($watchlist.Count)."
}

if ($recentTrades.Count -ne 5) {
    Fail-Test "Expected the five most recent trades, received $($recentTrades.Count)."
}

if ($portfolioHistory.Count -lt 1) {
    Fail-Test "Expected portfolio history for period $Period, but received no points."
}

$statuses = @($recentTrades | ForEach-Object { $_.status } | Select-Object -Unique)
Write-Host "[PASS] Active accounts: $($accounts.Count)" -ForegroundColor Green
Write-Host "[PASS] Market movers: $($marketMovers.Count)" -ForegroundColor Green
Write-Host "[PASS] Watchlist entries: $($watchlist.Count)" -ForegroundColor Green
Write-Host "[PASS] Recent trades returned: $($recentTrades.Count)" -ForegroundColor Green
Write-Host "[PASS] Portfolio history points: $($portfolioHistory.Count)" -ForegroundColor Green
Write-Host "[INFO] Recent trade statuses: $($statuses -join ', ')" -ForegroundColor Gray
Write-Host "[PASS] Dashboard smoke test completed." -ForegroundColor Green