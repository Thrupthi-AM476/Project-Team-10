# GitHub issue creation script part 2
# Run: powershell -File scripts/create-issues-2.ps1

$credOutput = echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1
$lines = $credOutput -split "`n"
$T = ($lines | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "Bearer $T"; "Accept" = "application/vnd.github+json"; "Content-Type" = "application/json" }
$R = "Thrupthi-AM476/Project-Team-10"

function New-GHIssue($title, $body, $labels, $assignees, $milestone) {
    $payload = @{ title=$title; body=$body; labels=$labels; assignees=$assignees; milestone=$milestone } | ConvertTo-Json -Depth 6
    $result = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues" -Headers $H -Method POST -Body $payload
    Write-Output "  Created #$($result.number): $title"
    Start-Sleep -Milliseconds 600
    return $result.number
}

Write-Output "Creating issues 33-42..."

# 33 — Reservations UI
New-GHIssue `
    "Build reservations page: create, list, and cancel advance bookings" `
    "## Summary`nFrontend for advance slot reservations: date/time picker, vehicle selector, list view, and cancel action.`n`n## Acceptance Criteria`n- [ ] /reservations: list of user's reservations with status badges`n- [ ] Create reservation: vehicle selector, start/end datetime pickers, optional slotType`n- [ ] endTime must be after startTime (client-side validation)`n- [ ] Conflict shown as server error displayed inline`n- [ ] Cancel reservation with confirmation dialog`n- [ ] Status badges: PENDING (default), CONFIRMED (green), CANCELLED (red), EXPIRED (amber), COMPLETED (blue)`n- [ ] Admin sees all reservations`n`n## Definition of Done`nReservation created/cancelled in DB; list reflects current state.`n`n## Story Points: 5 | Priority: P1 | Sprint: 2" `
    @("frontend","P1") @("adik1611") 2 | Out-Null

# 34 — Staff dashboard
New-GHIssue `
    "Build staff dashboard for slot management and vehicle oversight" `
    "## Summary`nDashboard for PARKING_ATTENDANT role: manage slots, view all vehicles, trigger reservation expiry.`n`n## Acceptance Criteria`n- [ ] /staff: stats row (total slots, available, occupied, vehicle count)`n- [ ] Slot table with edit (type, floor, status) and disable (OUT_OF_SERVICE) actions`n- [ ] Add slot form: slotNumber, slotType, floor`n- [ ] All vehicles table: plate, type, owner, status`n- [ ] Confirmation dialog before disabling a slot`n- [ ] Role-gated: PARKING_ATTENDANT, ADMINISTRATOR, MAINTENANCE_TECHNICIAN`n`n## Definition of Done`nSlot create/edit/disable persists in DB; only accessible to staff roles.`n`n## Story Points: 5 | Priority: P1 | Sprint: 2" `
    @("frontend","P1") @("adik1611") 2 | Out-Null

# 35 — Admin dashboard UI
New-GHIssue `
    "Build administrator dashboard with stats, reports, and rate configuration" `
    "## Summary`nAdmin page showing system statistics, revenue, parking history report, and rate schedule management.`n`n## Acceptance Criteria`n- [ ] /admin: 8 stat cards from real API data`n- [ ] Rate schedule table: name, vehicle type, rate/hr, grace period, status`n- [ ] Add/edit rate form; ratePerHour > 0 validated client-side`n- [ ] Date-filtered parking history report table`n- [ ] Revenue displayed in INR`n- [ ] Report table: ticket, plate, slot, entry/exit, duration, fee, payment status`n- [ ] Role-gated: ADMINISTRATOR only`n`n## Definition of Done`nAll data loads from real API; rate edits persist; report respects date filters.`n`n## Story Points: 5 | Priority: P1 | Sprint: 3" `
    @("frontend","P1") @("gaganreddy4656") 3 | Out-Null

# 36 — Backend: database schema and seed data
New-GHIssue `
    "Verify database schema, relationships, constraints, and seed data" `
    "## Summary`nEnsure src/database/schema.sql matches all JPA entities, has correct FK relationships, unique constraints, and meaningful seed data.`n`n## Acceptance Criteria`n- [ ] All 8 tables present: users, vehicles, parking_slots, parking_records, payments, rate_schedules, reservations, audit_logs`n- [ ] FK constraints correct (cascade rules verified)`n- [ ] Unique constraints: email, license_plate, slot_number, ticket_id, parking_record_id (payments)`n- [ ] 20 seed slots (15 REGULAR, 3 DISABLED, 2 EV)`n- [ ] 4 seed rate schedules (CAR=30, MOTORCYCLE=15, TRUCK=60, EV=20 INR/hr)`n- [ ] Default admin user seed (admin@vps.com, bcrypt hash)`n- [ ] ddl-auto=validate in application.properties ensures schema matches entities`n- [ ] application-test.properties uses H2 with create-drop (no MySQL for tests)`n`n## Story Points: 2 | Priority: P0 | Sprint: 1" `
    @("database","backend","P0") @("gaganreddy4656") 1 | Out-Null

# 37 — GitHub Actions CI pipeline
New-GHIssue `
    "Set up GitHub Actions CI for backend tests and frontend build" `
    "## Summary`nAutomate test execution and build verification on every push and pull request.`n`n## Acceptance Criteria`n- [ ] .github/workflows/ci.yml present`n- [ ] backend-tests job: setup-java@v4 (Java 21), mvn test, mvn package -DskipTests`n- [ ] frontend-build job: setup-node@v4 (Node 20), npm install, npm run build`n- [ ] Both jobs run on push to main and feat/** branches, and on pull_request to main`n- [ ] CI passes on current codebase (71 backend tests, frontend build success)`n`n## Story Points: 2 | Priority: P1 | Sprint: 3" `
    @("testing","documentation","P1") @("gaganreddy4656") 3 | Out-Null

# 38 — Integration: connect frontend to backend
New-GHIssue `
    "Verify and fix all frontend-to-backend API integration points" `
    "## Summary`nEnsure every frontend API call matches the real backend route, request DTO, response DTO, and authorization requirement.`n`n## Acceptance Criteria`n- [ ] CORS configured for http://localhost:3000 in SecurityConfig`n- [ ] All API paths in client.js match controller @RequestMapping paths exactly`n- [ ] Auth token attached as Bearer header on all authenticated calls`n- [ ] 401/403 responses redirect to login or show error (not crash)`n- [ ] Request DTOs match backend @Valid constraints (e.g. password pattern)`n- [ ] Response DTOs parsed correctly (camelCase JSON field names)`n- [ ] Loading states shown during API calls`n- [ ] Error messages from server displayed to user`n`n## Story Points: 3 | Priority: P0 | Sprint: 3" `
    @("integration","frontend","backend","P0") @("Thrupthi-AM476") 3 | Out-Null

Write-Output "Done creating issues 33-38."
