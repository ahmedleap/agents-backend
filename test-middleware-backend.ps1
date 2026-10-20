# Authenticated backend smoke test through the middleware auth flow.
# The middleware currently authenticates users but does not proxy the backend
# trading routes, so auth runs through port 8082 and API checks run on port 8080.
#
# Example:
#   .\test-middleware-backend.ps1
#   .\test-middleware-backend.ps1 -MiddlewareUrl http://10.14.131.55:8082/api/v1 -BackendUrl http://10.14.131.55:8080

param(
    [string]$MiddlewareUrl = "http://10.14.131.55:8082/api/v1",
    [string]$BackendUrl = "http://10.14.131.55:8080",
    [string]$Email = "",
    [string]$Password = "TestPass123!",
    [string]$Ssn = "123456789",
    [string]$DateOfBirth = "1990-01-15",
    [switch]$SkipSignup
)

$ErrorActionPreference = "Stop"
$script:Failures = 0

if ([string]::IsNullOrWhiteSpace($Email)) {
    $suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $Email = "backend-smoke-$suffix@example.test"
}

function Invoke-JsonRequest {
    param(
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $request = @{
        Uri = $Url
        Method = $Method
        Headers = $Headers
        ContentType = "application/json"
        UseBasicParsing = $true
        ErrorAction = "Stop"
    }

    if ($null -ne $Body) {
        $request.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }

    try {
        $response = Invoke-WebRequest @request
        $content = if ($response.Content -is [byte[]]) {
            [System.Text.Encoding]::UTF8.GetString($response.Content)
        }
        else {
            [string]$response.Content
        }
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try { $json = $content | ConvertFrom-Json } catch { $json = $content }
        }

        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Body = $json
            RawBody = $content
        }
    }
    catch {
        if ($null -eq $_.Exception.Response) {
            throw
        }

        $errorResponse = $_.Exception.Response
        $reader = New-Object System.IO.StreamReader($errorResponse.GetResponseStream())
        $content = $reader.ReadToEnd()
        $json = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try { $json = $content | ConvertFrom-Json } catch { $json = $content }
        }

        return [pscustomobject]@{
            StatusCode = [int]$errorResponse.StatusCode
            Body = $json
            RawBody = $content
        }
    }
}

function Test-Status {
    param(
        [string]$Name,
        [object]$Response,
        [int[]]$ExpectedStatus
    )

    if ($ExpectedStatus -contains $Response.StatusCode) {
        Write-Host "[PASS] $Name -> HTTP $($Response.StatusCode)" -ForegroundColor Green
        return $true
    }

    Write-Host "[FAIL] $Name -> HTTP $($Response.StatusCode); expected $($ExpectedStatus -join ', ')" -ForegroundColor Red
    $script:Failures++
    return $false
}

function Test-ClientEndpoint {
    param(
        [string]$Name,
        [string]$Url,
        [hashtable]$Headers
    )

    try {
        $response = Invoke-JsonRequest -Method GET -Url $Url -Headers $Headers
        [void](Test-Status -Name $Name -Response $response -ExpectedStatus @(200, 201, 204))
        return $response
    }
    catch {
        Write-Host "[FAIL] $Name -> connection error: $($_.Exception.Message)" -ForegroundColor Red
        $script:Failures++
        return $null
    }
}

Write-Host "Middleware: $MiddlewareUrl"
Write-Host "Backend:    $BackendUrl"
Write-Host "Test email: $Email"
Write-Host ""

try {
    $health = Invoke-JsonRequest -Method GET -Url "$BackendUrl/actuator/health"
    if (Test-Status -Name "Backend health without token" -Response $health -ExpectedStatus @(200)) {
        if ($health.RawBody -match '"status"\s*:\s*"UP"') {
            Write-Host "[PASS] Backend health status is UP" -ForegroundColor Green
        }
        else {
            Write-Host "[FAIL] Backend health response did not contain status UP" -ForegroundColor Red
            $script:Failures++
        }
    }

    $middlewareHealth = Invoke-JsonRequest -Method GET -Url "$MiddlewareUrl/health"
    [void](Test-Status -Name "Middleware health" -Response $middlewareHealth -ExpectedStatus @(200))

    if (-not $SkipSignup) {
        $signup = Invoke-JsonRequest -Method POST -Url "$MiddlewareUrl/auth/signup" -Body @{
            email = $Email
            password = $Password
            firstName = "Backend"
            lastName = "SmokeTest"
            dateOfBirth = $DateOfBirth
            ssn = $Ssn
        }
        [void](Test-Status -Name "Middleware signup" -Response $signup -ExpectedStatus @(201))
    }
    else {
        Write-Host "[INFO] Signup skipped; logging in with the supplied existing client." -ForegroundColor Yellow
    }

    $login = Invoke-JsonRequest -Method POST -Url "$MiddlewareUrl/auth/login" -Body @{
        email = $Email
        password = $Password
    }
    if (-not (Test-Status -Name "Middleware login" -Response $login -ExpectedStatus @(200))) {
        throw "Login did not return HTTP 200."
    }

    $accessToken = $login.Body.accessToken
    $clientId = $login.Body.user.clientId
    if ([string]::IsNullOrWhiteSpace($accessToken) -or [string]::IsNullOrWhiteSpace($clientId)) {
        throw "Login response did not contain an access token and client ID."
    }

    Write-Host "[PASS] Login returned an access token and client ID" -ForegroundColor Green
    Write-Host "Client ID: $clientId" -ForegroundColor Gray
    $headers = @{ Authorization = "Bearer $accessToken" }

    $accountCreate = Invoke-JsonRequest -Method POST -Url "$BackendUrl/api/accounts" -Headers $headers -Body @{
        clientId = $clientId
        name = "Smoke Test Account"
        initialCashBalance = 10000
    }
    if (-not (Test-Status -Name "Create client account" -Response $accountCreate -ExpectedStatus @(201))) {
        throw "Account creation failed."
    }

    $accountId = $accountCreate.Body.accountId
    if ([string]::IsNullOrWhiteSpace($accountId)) {
        throw "Account creation response did not contain accountId."
    }
    Write-Host "Account ID: $accountId" -ForegroundColor Gray

    # These endpoints are expected to return 2xx even when their collections are empty.
    $accountsResponse = Test-ClientEndpoint -Name "Client accounts" -Url "$BackendUrl/api/accounts" -Headers $headers
    [void](Test-ClientEndpoint -Name "Account detail" -Url "$BackendUrl/api/accounts/$accountId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Account summary" -Url "$BackendUrl/api/accounts/$accountId/summary" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Account performance" -Url "$BackendUrl/api/accounts/$accountId/performance?period=1M" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Account transactions" -Url "$BackendUrl/api/accounts/$accountId/transactions?limit=10" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client watchlist" -Url "$BackendUrl/api/watchlists/$clientId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client portfolio" -Url "$BackendUrl/api/portfolio/v1/$clientId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Account holdings" -Url "$BackendUrl/api/holding/v1/$accountId" -Headers $headers)
    $instrumentsResponse = Test-ClientEndpoint -Name "Client instruments" -Url "$BackendUrl/api/instruments" -Headers $headers
    $quotesResponse = Test-ClientEndpoint -Name "Client instruments with quotes" -Url "$BackendUrl/api/instruments/with-quotes" -Headers $headers
    [void](Test-ClientEndpoint -Name "Client dashboard" -Url "$BackendUrl/dashboard?clientId=$clientId&period=30D" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client user profile" -Url "$BackendUrl/users/me" -Headers $headers)

    $instrumentId = $instrumentsResponse.Body.results[0].instrumentId
    if ([string]::IsNullOrWhiteSpace($instrumentId)) {
        throw "Instrument response did not contain an instrumentId; cannot place the order test."
    }

    $quotedInstrument = @($quotesResponse.Body) | Where-Object {
        $_.instrumentId -eq $instrumentId -and $null -ne $_.ask
    } | Select-Object -First 1
    if ($null -eq $quotedInstrument) {
        throw "No usable ask price was returned; cannot place the limit order test."
    }

    $orderCreate = Invoke-JsonRequest -Method POST -Url "$BackendUrl/api/v1/orders" -Headers $headers -Body @{
        accountId = $accountId
        instrumentId = $instrumentId
        quantity = 1
        orderType = "BUY"
    }
    if (-not (Test-Status -Name "Place client market order" -Response $orderCreate -ExpectedStatus @(201))) {
        throw "Order creation failed."
    }

    $createdOrderId = $orderCreate.Body.orderId
    if ([string]::IsNullOrWhiteSpace($createdOrderId)) {
        throw "Order creation response did not contain orderId."
    }
    Write-Host "Order ID: $createdOrderId" -ForegroundColor Gray

    $limitOrderCreate = Invoke-JsonRequest -Method POST -Url "$BackendUrl/api/v1/orders" -Headers $headers -Body @{
        accountId = $accountId
        instrumentId = $instrumentId
        quantity = 1
        price = [decimal]$quotedInstrument.ask
        orderType = "BUY"
    }
    if (-not (Test-Status -Name "Place client limit order" -Response $limitOrderCreate -ExpectedStatus @(201))) {
        throw "Limit order creation failed."
    }

    $limitOrderId = $limitOrderCreate.Body.orderId
    if ([string]::IsNullOrWhiteSpace($limitOrderId)) {
        throw "Limit order response did not contain orderId."
    }
    Write-Host "Limit order ID: $limitOrderId" -ForegroundColor Gray

    [void](Test-ClientEndpoint -Name "Client order history" -Url "$BackendUrl/api/v1/orders?accountId=$accountId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client pending orders" -Url "$BackendUrl/api/v1/orders/pending?accountId=$accountId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client order detail" -Url "$BackendUrl/api/v1/orders/$createdOrderId" -Headers $headers)
    [void](Test-ClientEndpoint -Name "Client limit order detail" -Url "$BackendUrl/api/v1/orders/$limitOrderId" -Headers $headers)

    # A normal client token must not access staff-only routes.
    $admin = Invoke-JsonRequest -Method GET -Url "$BackendUrl/api/v1/admin/clients" -Headers $headers
    [void](Test-Status -Name "Admin route rejected for client" -Response $admin -ExpectedStatus @(403))

    $analytics = Invoke-JsonRequest -Method GET -Url "$BackendUrl/api/v1/analytics/platform/metrics" -Headers $headers
    [void](Test-Status -Name "Analyst route rejected for client" -Response $analytics -ExpectedStatus @(403))

}
catch {
    Write-Host "[FAIL] Smoke test stopped: $($_.Exception.Message)" -ForegroundColor Red
    $script:Failures++
}

Write-Host ""
if ($script:Failures -eq 0) {
    Write-Host "[PASS] Middleware/backend smoke test completed." -ForegroundColor Green
    exit 0
}

Write-Host "[FAIL] Smoke test completed with $script:Failures failure(s)." -ForegroundColor Red
exit 1
