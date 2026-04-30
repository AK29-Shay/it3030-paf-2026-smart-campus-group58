$ErrorActionPreference = "Stop"

$Base = if ($env:BASE) {
    $env:BASE
} elseif ($env:API_BASE) {
    $env:API_BASE
} else {
    "http://localhost:8080"
}

$Divider = "============================================================"

function Format-JsonOrRaw {
    param([string]$Text)

    try {
        $pretty = $Text | python -c "import json,sys; print(json.dumps(json.load(sys.stdin), indent=2))" 2>$null
        if ($LASTEXITCODE -eq 0) {
            return ($pretty -join "`n")
        }
    } catch {
    }

    try {
        $payload = ConvertFrom-Json -InputObject $Text
        return (ConvertTo-Json -InputObject $payload -Depth 20)
    } catch {
        return $Text
    }
}

function Redact-Token {
    param([string]$Text)

    try {
        $payload = $Text | ConvertFrom-Json
        if ($payload.PSObject.Properties.Name -contains "token") {
            $payload.token = "<redacted>"
        }
        return ($payload | ConvertTo-Json -Depth 20)
    } catch {
        return $Text
    }
}

function Redact-Command {
    param([string]$Text)

    return ($Text -replace "Bearer [A-Za-z0-9._-]+", "Bearer <redacted>")
}

function Invoke-CurlJson {
    param(
        [string]$Label,
        [string]$DisplayCommand,
        [string[]]$Arguments,
        [string]$LiteralOutput
    )

    Write-Output ""
    Write-Output ">>> $Label"
    Write-Output (Redact-Command $DisplayCommand)

    if ($PSBoundParameters.ContainsKey("LiteralOutput")) {
        $output = $LiteralOutput
    } else {
        $output = (& curl.exe @Arguments) -join "`n"
        if ($LASTEXITCODE -ne 0) {
            throw "curl.exe failed with exit code $LASTEXITCODE"
        }
    }

    Write-Output (Format-JsonOrRaw $output)
}

function Login {
    param(
        [string]$Email,
        [string]$Role
    )

    $body = @{
        email = $Email
        password = "ChangeMe123!"
        role = $Role
    } | ConvertTo-Json -Compress

    $response = Invoke-RestMethod -Uri "$Base/api/auth/login" -Method Post -ContentType "application/json" -Body $body
    return ($response | ConvertTo-Json -Depth 20)
}

Write-Output ""
Write-Output $Divider
Write-Output "Smart Campus API Evidence"
Write-Output (Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Write-Output $Divider

$AdminLogin = Login "admin@example.com" "ADMIN"
$UserLogin = Login "student@example.com" "USER"
$TechLogin = Login "technician@example.com" "TECHNICIAN"

$AdminToken = ($AdminLogin | ConvertFrom-Json).token
$UserToken = ($UserLogin | ConvertFrom-Json).token
$TechToken = ($TechLogin | ConvertFrom-Json).token

if (-not $AdminToken -or -not $UserToken -or -not $TechToken) {
    Write-Output "Failed to obtain one or more JWT tokens."
    Write-Output (Redact-Token $AdminLogin)
    Write-Output (Redact-Token $UserLogin)
    Write-Output (Redact-Token $TechLogin)
    exit 1
}

$AdminAuth = "Authorization: Bearer $AdminToken"
$UserAuth = "Authorization: Bearer $UserToken"
$TechAuth = "Authorization: Bearer $TechToken"

Invoke-CurlJson "GET /" "curl.exe -s $Base/" @("-s", "$Base/")
Invoke-CurlJson "POST /api/auth/login (ADMIN)" "curl.exe -s -X POST $Base/api/auth/login -H 'Content-Type: application/json' -d '{admin credentials}'" @() -LiteralOutput (Redact-Token $AdminLogin)
Invoke-CurlJson "GET /api/auth/me (USER)" "curl.exe -s $Base/api/auth/me -H '$UserAuth'" @("-s", "$Base/api/auth/me", "-H", $UserAuth)
Invoke-CurlJson "GET /api/resources" "curl.exe -s $Base/api/resources -H '$UserAuth'" @("-s", "$Base/api/resources", "-H", $UserAuth)
Invoke-CurlJson "GET /api/bookings (ADMIN)" "curl.exe -s $Base/api/bookings -H '$AdminAuth'" @("-s", "$Base/api/bookings", "-H", $AdminAuth)
Invoke-CurlJson "GET /api/bookings/my-bookings (USER)" "curl.exe -s $Base/api/bookings/my-bookings -H '$UserAuth'" @("-s", "$Base/api/bookings/my-bookings", "-H", $UserAuth)
Invoke-CurlJson "GET /api/tickets (ADMIN)" "curl.exe -s $Base/api/tickets -H '$AdminAuth'" @("-s", "$Base/api/tickets", "-H", $AdminAuth)
Invoke-CurlJson "GET /api/tickets/technician/{id} (TECHNICIAN)" "curl.exe -s $Base/api/tickets/technician/3 -H '$TechAuth'" @("-s", "$Base/api/tickets/technician/3", "-H", $TechAuth)
Invoke-CurlJson "GET /api/notifications/unread/count (USER)" "curl.exe -s $Base/api/notifications/unread/count -H '$UserAuth'" @("-s", "$Base/api/notifications/unread/count", "-H", $UserAuth)
Invoke-CurlJson "GET /api/admin/command-center (ADMIN)" "curl.exe -s $Base/api/admin/command-center -H '$AdminAuth'" @("-s", "$Base/api/admin/command-center", "-H", $AdminAuth)

Write-Output ""
Write-Output $Divider
Write-Output "API evidence complete."
Write-Output $Divider
