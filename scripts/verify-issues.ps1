# Verify final issue states after closing
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

$cred = echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1
$T = (($cred -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "token $T"; "Accept" = "application/vnd.github+json" }
$R = "Thrupthi-AM476/Project-Team-10"

Write-Output "=== VERIFICATION: Issue states ==="
foreach ($n in @(32, 33, 34, 35, 44, 48, 49)) {
    try {
        $i = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/$n" -Headers $H
        $ms = if ($i.milestone) { $i.milestone.title } else { "no milestone" }
        Write-Output "#$($i.number) [$($i.state)] [$ms] $($i.title)"
        Start-Sleep -Milliseconds 600
    } catch {
        Write-Output "#$n ERROR: $($_.Exception.Message)"
        Start-Sleep -Seconds 1
    }
}

Write-Output "`n=== PR #56 state ==="
try {
    $pr = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/pulls/56" -Headers $H
    Write-Output "PR#$($pr.number) [$($pr.state)] $($pr.title)"
    Write-Output "  Head: $($pr.head.ref) -> Base: $($pr.base.ref)"
    Write-Output "  Mergeable: $($pr.mergeable_state)"
    Write-Output "  URL: $($pr.html_url)"
} catch {
    Write-Output "PR#56 ERROR: $($_.Exception.Message)"
}
