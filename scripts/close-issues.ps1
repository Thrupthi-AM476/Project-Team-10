# Close Sprint 2 frontend issues — run via: powershell -ExecutionPolicy Bypass -File scripts\close-issues.ps1
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

$cred = echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1
$T = (($cred -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "token $T"; "Accept" = "application/vnd.github+json"; "Content-Type" = "application/json" }
$R = "Thrupthi-AM476/Project-Team-10"

$toClose = @(33, 34, 35, 44, 48)

foreach ($n in $toClose) {
    try {
        Write-Output "Closing #$n..."
        $result = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/$n" -Headers $H -Method PATCH -Body '{"state":"closed"}'
        Write-Output "  Done: #$($result.number) [$($result.state)]"
        Start-Sleep -Seconds 1
    } catch {
        Write-Output "  Failed #$n : $($_.Exception.Message)"
        Start-Sleep -Seconds 2
    }
}

# Update #49 body without closing
try {
    Write-Output "Updating #49 (In Review)..."
    $b49 = '{"body":"## Status: IN REVIEW\n\nPR #56 is open and awaiting team review before merge.\n\nPR: https://github.com/Thrupthi-AM476/Project-Team-10/pull/56\n\n**Story Points: 1 | Sprint: 1**"}'
    $result = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/49" -Headers $H -Method PATCH -Body $b49
    Write-Output "  Done: #49 [$($result.state)]"
} catch {
    Write-Output "  Failed #49: $($_.Exception.Message)"
}

Write-Output "Done."
