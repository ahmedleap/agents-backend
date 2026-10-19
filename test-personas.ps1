# End-to-end persona smoke test for Windows PowerShell 5.1.
# Requirements: psql on PATH, a running backend, and middleware reachable at MiddlewareUrl.
# Client tokens are obtained from middleware. AnalystToken and AdminToken must be supplied
# until middleware implements staff login/token issuance.

[CmdletBinding()]
param(
    [string]$ApiUrl = "http://localhost:8080",
    [string]$MiddlewareUrl = "http://10.14.131.55:8082/api/v1",
    [Parameter(Mandatory = $true)]
    [string]$DatabaseUrl,
    [string]$ClientEmail = "",
    [string]$ClientPassword = "Password123!",
    [string]$AdminEmail = "john.admin@platform.com",
    [string]$AdminPassword = "",
    [string]$AnalystEmail = "sarah.analyst@platform.com",
    [string]$AnalystPassword = "",
    [string]$AdminToken = "",
    [string]$AnalystToken = "",
    [switch]$SkipStaff
)

$ErrorActionPreference = "Stop"
$ApiUrl = $ApiUrl.TrimEnd('/')
$MiddlewareUrl = $MiddlewareUrl.TrimEnd('/')

function Fail-Test([string]$Message) {
    Write-Host "[FAIL] $Message" -ForegroundColor Red
    exit 1
}

function Invoke-JsonApi {
    param(
        [ValidateSet("GET", "POST", "PUT", "PATCH", "DELETE")]
        [string]$Method,
        [string]$Url,
        [string]$Token = "",
        [object]$Body = $null
    )

    $headers = @{}
    if (-not [string]::IsNullOrWhiteSpace($Token)) {
        $headers["Authorization"] = "Bearer $Token"
    }

    $request = @{
        Uri = $Url
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $request["ContentType"] = "application/json"
        $request["Body"] = ($Body | ConvertTo-Json -Depth 8 -Compress)
    }

    try {
        $response = Invoke-WebRequest @request
        $parsed = $null
        if (-not [string]::IsNullOrWhiteSpace($response.Content)) {
            try { $parsed = $response.Content | ConvertFrom-Json } catch { $parsed = $response.Content }
        }
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Body = $parsed }
    }
    catch {
        $webResponse = $_.Exception.Response
        if ($null -eq $webResponse) {
            throw
        }
        $status = [int]$webResponse.StatusCode
        $reader = New-Object System.IO.StreamReader($webResponse.GetResponseStream())
        $content = $reader.ReadToEnd()
        $reader.Dispose()
        $parsed = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try { $parsed = $content | ConvertFrom-Json } catch { $parsed = $content }
        }
        return [pscustomobject]@{ Status = $status; Body = $parsed }
    }
}

function Assert-Status($Response, [int[]]$Expected, [string]$Name) {
    if ($Expected -notcontains $Response.Status) {
        $body = if ($null -eq $Response.Body) { "" } else { $Response.Body | ConvertTo-Json -Depth 5 -Compress }
        Fail-Test "$Name returned HTTP $($Response.Status), expected $($Expected -join ','). Body: $body"
    }
    Write-Host "[PASS] $Name -> HTTP $($Response.Status)" -ForegroundColor Green
}

function Get-RequiredProperty($Object, [string]$Property, [string]$Name) {
    $value = $Object.$Property
    if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) {
        Fail-Test "$Name did not contain required property '$Property'."
    }
    return $value
}

function Invoke-Backend {
    param([string]$Method, [string]$Path, [string]$Token, [object]$Body = $null)
    return Invoke-JsonApi -Method $Method -Url ($ApiUrl + $Path) -Token $Token -Body $Body
}

function Get-StaffToken {
    param([string]$Email, [string]$Password, [string]$ExpectedRole)

    if ([string]::IsNullOrWhiteSpace($Email) -or [string]::IsNullOrWhiteSpace($Password)) {
        Fail-Test "Supply -$($ExpectedRole.Substring(0, 1) + $ExpectedRole.Substring(1).ToLowerInvariant())Email and password, or provide -$($ExpectedRole.Substring(0, 1) + $ExpectedRole.Substring(1).ToLowerInvariant())Token."
    }

    Write-Host "Logging in $ExpectedRole through middleware: $Email" -ForegroundColor Cyan
    $response = Invoke-JsonApi -Method POST -Url "$MiddlewareUrl/auth/admin/login" -Body @{
        email = $Email
        password = $Password
    }
    Assert-Status $response 200 "$ExpectedRole middleware login"

    $role = Get-RequiredProperty $response.Body.user "role" "$ExpectedRole middleware login"
    if ($role.ToString().ToUpperInvariant() -ne $ExpectedRole) {
        Fail-Test "$ExpectedRole middleware login returned role '$role'."
    }

    return Get-RequiredProperty $response.Body "accessToken" "$ExpectedRole middleware login"
}

if ([string]::IsNullOrWhiteSpace($ClientEmail)) {
    $ClientEmail = "persona.client.$([guid]::NewGuid().ToString('N'))@example.test"
}
$clientPhone = "+1202" + (Get-Random -Minimum 1000000 -Maximum 9999999).ToString()

Write-Host "Creating or locating client through middleware: $ClientEmail" -ForegroundColor Cyan
$login = Invoke-JsonApi -Method POST -Url "$MiddlewareUrl/auth/login" -Body @{ email = $ClientEmail; password = $ClientPassword }
if ($login.Status -ne 200) {
    $signup = Invoke-JsonApi -Method POST -Url "$MiddlewareUrl/auth/signup" -Body @{
        email = $ClientEmail
        password = $ClientPassword
        firstName = "Persona"
        lastName = "Client"
        dateOfBirth = "1990-01-15"
        ssn = "123456789"
        phoneNumber = $clientPhone
        country = "US"
    }
    Assert-Status $signup @(201, 409) "middleware client signup"
    $login = Invoke-JsonApi -Method POST -Url "$MiddlewareUrl/auth/login" -Body @{ email = $ClientEmail; password = $ClientPassword }
}
Assert-Status $login 200 "middleware client login"
$clientToken = Get-RequiredProperty $login.Body "accessToken" "middleware client login"
$clientId = Get-RequiredProperty $login.Body.user "clientId" "middleware client login"

$accountOne = [guid]::NewGuid()
$accountTwo = [guid]::NewGuid()
$instrumentOne = [guid]::NewGuid()
$instrumentTwo = [guid]::NewGuid()
$holdingOne = [guid]::NewGuid()
$orderOne = [guid]::NewGuid()
$tickerOne = "P" + ([guid]::NewGuid().ToString('N').Substring(0, 8)).ToUpperInvariant()
$tickerTwo = "Q" + ([guid]::NewGuid().ToString('N').Substring(0, 8)).ToUpperInvariant()

$sql = @"
BEGIN;
INSERT INTO accounts (account_id, client_id, name, cash_balance, status, open_date)
VALUES ('$accountOne', '$clientId', 'Persona Trading', 100000.00, 'ACTIVE', CURRENT_TIMESTAMP),
       ('$accountTwo', '$clientId', 'Persona Retirement', 50000.00, 'ACTIVE', CURRENT_TIMESTAMP);
INSERT INTO instruments (instrument_id, ticker, name, asset_class, industry, bid, ask, price_updated_at)
VALUES ('$instrumentOne', '$tickerOne', 'Persona Technology', 'STOCK', 'Technology', 99.00, 101.00, CURRENT_TIMESTAMP),
       ('$instrumentTwo', '$tickerTwo', 'Persona Healthcare', 'STOCK', 'Healthcare', 49.00, 51.00, CURRENT_TIMESTAMP);
INSERT INTO watchlists (watchlist_id, client_id, instrument_id, added_at)
VALUES (gen_random_uuid(), '$clientId', '$instrumentOne', CURRENT_TIMESTAMP);
INSERT INTO holdings (holding_id, account_id, instrument_id, quantity, average_cost_basis)
VALUES ('$holdingOne', '$accountOne', '$instrumentOne', 10.000000, 90.0000);
INSERT INTO transactions (transaction_id, account_id, txn_type, amount, created_at)
VALUES (gen_random_uuid(), '$accountOne', 'DEPOSIT', 100000.00, CURRENT_TIMESTAMP);
INSERT INTO orders (order_id, account_id, instrument_id, order_type, quantity, limit_price, status, created_at)
VALUES ('$orderOne', '$accountOne', '$instrumentOne', 'BUY', 1.000000, 100.0000, 'PENDING', CURRENT_TIMESTAMP);
COMMIT;
"@

$sqlPath = Join-Path $env:TEMP ("persona-fixture-" + [guid]::NewGuid().ToString('N') + ".sql")
$sql | Set-Content -Path $sqlPath -Encoding ASCII
try {
    & psql $DatabaseUrl -v ON_ERROR_STOP=1 -f $sqlPath
    if ($LASTEXITCODE -ne 0) { Fail-Test "Fixture SQL failed with exit code $LASTEXITCODE." }
}
finally {
    Remove-Item -LiteralPath $sqlPath -Force -ErrorAction SilentlyContinue
}
Write-Host "[PASS] Fixture inserted for client $clientId" -ForegroundColor Green

# CLIENT: personal resources work, staff resources are denied.
Assert-Status (Invoke-Backend GET "/users/me" $clientToken) 200 "client current user"
Assert-Status (Invoke-Backend GET "/dashboard?clientId=$clientId&period=30D" $clientToken) 200 "client dashboard"
Assert-Status (Invoke-Backend GET "/api/accounts" $clientToken) 200 "client account list"
Assert-Status (Invoke-Backend GET "/api/accounts/$accountOne" $clientToken) 200 "client account detail"
Assert-Status (Invoke-Backend GET "/api/accounts/$accountOne/summary" $clientToken) 200 "client account summary"
Assert-Status (Invoke-Backend GET "/api/accounts/$accountOne/performance?period=1Y" $clientToken) 200 "client account performance"
Assert-Status (Invoke-Backend GET "/api/accounts/$accountOne/transactions?limit=10" $clientToken) 200 "client transactions"
Assert-Status (Invoke-Backend GET "/api/holding/v1/$accountOne" $clientToken) 200 "client holdings"
Assert-Status (Invoke-Backend GET "/api/holding/v1/$accountOne/$holdingOne" $clientToken) 200 "client holding detail"
Assert-Status (Invoke-Backend GET "/api/portfolio/v1/$clientId" $clientToken) 200 "client portfolio"
Assert-Status (Invoke-Backend GET "/api/portfolio/v1/$clientId/$accountOne" $clientToken) 200 "client account portfolio"
Assert-Status (Invoke-Backend GET "/api/portfolio/v1/allocation/$clientId" $clientToken) 200 "client portfolio allocation"
Assert-Status (Invoke-Backend GET "/api/portfolio/v1/allocation/$clientId/$accountOne" $clientToken) 200 "client account allocation"
Assert-Status (Invoke-Backend GET "/api/watchlists/$clientId" $clientToken) 200 "client watchlist"
Assert-Status (Invoke-Backend GET "/api/v1/orders/pending?accountId=$accountOne" $clientToken) 200 "client pending orders"
Assert-Status (Invoke-Backend GET "/api/v1/orders?accountId=$accountOne" $clientToken) 200 "client order history"
Assert-Status (Invoke-Backend GET "/api/v1/orders/$orderOne" $clientToken) 200 "client order detail"
Assert-Status (Invoke-Backend GET "/api/instruments/search?q=$tickerOne" $clientToken) 200 "client instrument search"
Assert-Status (Invoke-Backend GET "/api/instruments/$tickerOne" $clientToken) 200 "client instrument detail"
Assert-Status (Invoke-Backend GET "/api/instruments/$tickerOne/quote" $clientToken) 200 "client instrument quote"
Assert-Status (Invoke-Backend GET "/api/instruments/$tickerOne/history?period=1Y" $clientToken) 200 "client instrument history"
Assert-Status (Invoke-Backend GET "/api/v1/analytics/health" $clientToken) 403 "client analytics denial"
Assert-Status (Invoke-Backend POST "/api/admins" $clientToken @{ firstName = "Denied"; lastName = "Client"; email = "denied-$clientId@example.test"; passwordHash = "unused"; role = "ADMIN" }) 403 "client admin denial"

if (-not $SkipStaff) {
    if ([string]::IsNullOrWhiteSpace($AnalystToken)) {
        $AnalystToken = Get-StaffToken -Email $AnalystEmail -Password $AnalystPassword -ExpectedRole "ANALYST"
    }
    if ([string]::IsNullOrWhiteSpace($AdminToken)) {
        $AdminToken = Get-StaffToken -Email $AdminEmail -Password $AdminPassword -ExpectedRole "ADMIN"
    }

    # ANALYST: analytics and read-only market/order access work; client mutations do not.
    $analyticsPaths = @(
        "/api/v1/analytics/platform/metrics",
        "/api/v1/analytics/instruments/top-traded?limit=5",
        "/api/v1/analytics/assets/by-class",
        "/api/v1/analytics/clients/by-risk-tolerance",
        "/api/v1/analytics/clients/by-portfolio-size",
        "/api/v1/analytics/dashboard/total-volume",
        "/api/v1/analytics/dashboard/total-trades",
        "/api/v1/analytics/dashboard/active-clients",
        "/api/v1/analytics/dashboard/volume-trend?startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/dashboard/instrument-trend?startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/dashboard/most-active-instruments?limit=5",
        "/api/v1/analytics/dashboard/instrument-analysis?ticker=$tickerOne&startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/dashboard/segment-trends?segmentType=RISK_TOLERANCE&startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/dashboard/fulfillment-trends?startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/dashboard/asset-class-trends?startDate=2026-01-01&endDate=2026-01-31",
        "/api/v1/analytics/health"
    )
    foreach ($path in $analyticsPaths) {
        Assert-Status (Invoke-Backend GET $path $AnalystToken) 200 "analyst $path"
    }
    Assert-Status (Invoke-Backend GET "/api/instruments/search?q=$tickerOne" $AnalystToken) 200 "analyst market data"
    Assert-Status (Invoke-Backend GET "/api/v1/orders/$orderOne" $AnalystToken) 200 "analyst order read"
    Assert-Status (Invoke-Backend GET "/api/accounts" $AnalystToken) 403 "analyst client-resource denial"
    Assert-Status (Invoke-Backend POST "/api/v1/orders" $AnalystToken @{ accountId = $accountOne; instrumentId = $instrumentOne; orderType = "BUY"; quantity = 1 }) 403 "analyst order-create denial"

    # ADMIN: admin operations and approved read access work; analytics remain analyst-only.
    $createdAdminEmail = "created.admin.$([guid]::NewGuid().ToString('N'))@example.test"
    Assert-Status (Invoke-Backend POST "/api/admins" $AdminToken @{ firstName = "Smoke"; lastName = "Admin"; email = $createdAdminEmail; passwordHash = "unused"; role = "ADMIN" }) 201 "admin create-admin"
    Assert-Status (Invoke-Backend GET "/api/v1/orders/$orderOne" $AdminToken) 200 "admin order read"
    Assert-Status (Invoke-Backend GET "/api/instruments/search?q=$tickerOne" $AdminToken) 200 "admin market data"
    Assert-Status (Invoke-Backend GET "/api/v1/analytics/health" $AdminToken) 403 "admin analytics denial"
    Assert-Status (Invoke-Backend GET "/api/accounts" $AdminToken) 403 "admin client-resource denial"
}

Write-Host "[PASS] Persona smoke test completed." -ForegroundColor Green
Write-Host "[INFO] Client: $clientId" -ForegroundColor Gray
