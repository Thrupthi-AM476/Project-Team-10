# Fix milestones and issue sprint assignments
# Sprint 1: Oct 19-23 (milestone 1) -> due 2026-10-23
# Sprint 2: Oct 26-30 (milestone 2) -> due 2026-10-30
# Sprint 3: CLOSE IT (milestone 3)

$env:GH_TOKEN = (((echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1) -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$R = "Thrupthi-AM476/Project-Team-10"

# 1. Close Sprint 3 milestone
gh api --method PATCH repos/$R/milestones/3 -f state=closed -f title="Sprint 3 (removed - merged into Sprint 2)" 2>&1 | Select-Object "title","state"
Write-Output "Closed Sprint 3 milestone"

# 2. Set due date on Sprint 1
gh api --method PATCH repos/$R/milestones/1 -f title="Sprint 1 (Oct 19-23)" -f due_on="2026-10-23T23:59:59Z" -f description="Working product: auth, vehicles, parking entry/exit, frontend, CI, README, PR merge." 2>&1 | Select-Object "title","due_on"
Write-Output "Updated Sprint 1 milestone"

# 3. Set due date on Sprint 2
gh api --method PATCH repos/$R/milestones/2 -f title="Sprint 2 (Oct 26-30)" -f due_on="2026-10-30T23:59:59Z" -f description="Reservations, admin dashboard, staff dashboard, full test suite, security audit, sprint docs, responsive design." 2>&1 | Select-Object "title","due_on"
Write-Output "Updated Sprint 2 milestone"

# 4. Move Sprint 3 issues to Sprint 2 (milestone 2)
$sprint3Issues = @(18,19,41,43,44,45,46,47,48,49)
foreach ($num in $sprint3Issues) {
    gh api --method PATCH "repos/$R/issues/$num" -f milestone=2 2>&1 | Select-Object "number" | Out-Null
    Write-Output "  Moved #$num to Sprint 2"
}

# 5. Move issues from Sprint 2 to Sprint 1 (needed for working product)
$toSprint1 = @(10,11,14,15,25,36,37,38)
foreach ($num in $toSprint1) {
    gh api --method PATCH "repos/$R/issues/$num" -f milestone=1 2>&1 | Select-Object "number" | Out-Null
    Write-Output "  Moved #$num to Sprint 1"
}

Write-Output "All milestone and issue updates complete."
