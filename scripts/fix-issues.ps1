# Fix all GitHub issues: update bodies with story points, correct sprint assignments
# Run: powershell -ExecutionPolicy Bypass -File scripts\fix-issues.ps1

$credOutput = echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1
$T = (($credOutput -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "token $T"; "Accept" = "application/vnd.github+json"; "Content-Type" = "application/json" }
$R = "Thrupthi-AM476/Project-Team-10"

function Update-Issue($num, $data) {
    $json = $data | ConvertTo-Json -Compress -Depth 4
    $r = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/$num" -Headers $H -Method PATCH -Body $json
    Write-Host "  Updated #$num: $($r.title)" -ForegroundColor Green
    Start-Sleep -Milliseconds 500
}

Write-Host "=== Fixing issue bodies and sprint assignments ===" -ForegroundColor Cyan

# ----------------------------------------------------------------
# ORIGINAL 9 ISSUES — correct bodies and story points
# ----------------------------------------------------------------

# #20 User registration, login and profile management
# Correct SP: 8 (complex: JWT, BCrypt, account lock, profile, tests)
Update-Issue 20 @{
    body = "## Summary`nSecure user registration, JWT login, account locking, and authenticated profile management.`n`n## What is implemented`n- POST /api/v1/auth/register — CUSTOMER role enforced; password policy (8+ chars, upper/lower/number/special); BCrypt hashing`n- POST /api/v1/auth/login — JWT token; failed-attempt counter; account locked after 5 failures`n- GET /api/v1/users/me — own profile, no passwordHash in response`n- PUT /api/v1/users/me — update fullName and phone only`n- Audit log for login success/failure/lock events`n`n## Acceptance Criteria`n- [ ] Registration: fullName, email, password required; phone optional`n- [ ] Duplicate email returns 400 with safe message`n- [ ] Role field in request body is silently ignored; role always = CUSTOMER`n- [ ] Passwords stored as BCrypt hash; never returned anywhere`n- [ ] Successful login returns JWT; failed attempts increment`n- [ ] Account locked after 5 consecutive failures; returns 403`n- [ ] Locked or inactive accounts return 403 on login`n- [ ] GET /me returns profile without passwordHash`n- [ ] PUT /me rejects changes to role, isActive, failedAttempts, id`n- [ ] All error messages are safe (no stack traces, SQL details)`n`n## Tests`nAuthServiceTest (9 tests) + AuthControllerTest (7 tests) + UserControllerTest (5 tests)`n`n## Story Points: 8 | Priority: P0 | Sprint: 1 | Assignee: Thrupthi-AM476"
    milestone = 1
}

# #3 Vehicle registration
# Correct SP: 5 (ownership enforcement, plate normalization, full CRUD)
Update-Issue 3 @{
    body = "## Summary`nRegister, view, update, and deactivate vehicles with ownership enforcement and plate normalization.`n`n## What is implemented`n- POST /api/v1/vehicles — register; plate normalized to uppercase; duplicate returns 400`n- GET /api/v1/vehicles — own for CUSTOMER; all for staff`n- GET /api/v1/vehicles/{id} — CUSTOMER sees own only (403 otherwise)`n- GET /api/v1/vehicles/search?plate=... — ownership enforced`n- PUT /api/v1/vehicles/{id} — owner or ADMINISTRATOR`n- DELETE /api/v1/vehicles/{id} — soft deactivate; owner, ADMINISTRATOR, or PARKING_ATTENDANT`n`n## Acceptance Criteria`n- [ ] License plate pattern: 2-20 alphanumeric or hyphen characters`n- [ ] Plate normalized to uppercase on save and lookup`n- [ ] Duplicate plate returns 400`n- [ ] Customer cannot view or modify another user's vehicle (403)`n- [ ] Deactivated vehicles excluded from active listings`n- [ ] vehicleType: CAR, MOTORCYCLE, TRUCK, EV`n`n## Tests`nVehicleServiceTest (13 tests) + VehicleControllerTest (9 tests)`n`n## Story Points: 5 | Priority: P0 | Sprint: 1 | Assignee: Thrupthi-AM476"
    milestone = 1
}

# #10 Parking slot management and availability
# Correct SP: 5 (CRUD + public availability + 20 seed slots)
Update-Issue 10 @{
    body = "## Summary`nManage parking slots (staff CRUD) and expose public real-time slot availability.`n`n## What is implemented`n- GET /api/v1/parking/slots — public; all slots with status, type, floor`n- GET /api/v1/parking/slots/available — public; AVAILABLE slots only`n- POST /api/v1/parking/slots — ADMINISTRATOR or PARKING_ATTENDANT only`n- PUT /api/v1/parking/slots/{id} — update type, floor, status`n- DELETE /api/v1/parking/slots/{id} — marks OUT_OF_SERVICE`n- Seed: 20 slots (15 REGULAR, 3 DISABLED, 2 EV) in schema.sql`n`n## Acceptance Criteria`n- [ ] Slot types: REGULAR, DISABLED, EV`n- [ ] Slot statuses: AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE`n- [ ] Duplicate slot number returns 400`n- [ ] Customer cannot create/modify slots (403)`n- [ ] Frontend: color-coded grid with refresh`n`n## Tests`nParkingControllerTest covers entry/exit/availability`n`n## Story Points: 5 | Priority: P0 | Sprint: 2 | Assignee: adik1611"
    milestone = 2
}

# #11 Parking allocation and entry ticket generation
# Correct SP: 5 (entry flow, ownership, ticket, duplicate session)
Update-Issue 11 @{
    body = "## Summary`nProcess vehicle entry: validate ownership, allocate available slot, generate unique ticket, record session.`n`n## What is implemented`n- POST /api/v1/parking/entry — JWT required; licensePlate + optional slotType`n- Customer can only park their own vehicle (403 otherwise)`n- Duplicate active session for same vehicle → 403`n- Allocates first AVAILABLE slot; respects preferred slotType when specified`n- Returns: ticketId (TKT-YYYYMMDD-NNNNN), slotNumber, slotType, entryTime`n- Parking full (no AVAILABLE slot) → 403 with safe message`n- Slot marked OCCUPIED; audit log entry created`n- GET /api/v1/parking/active-session — authenticated user's current session`n`n## Acceptance Criteria`n- [ ] ticketId is unique and follows TKT-YYYYMMDD-NNNNN format`n- [ ] Entry blocked if vehicle already has ACTIVE session`n- [ ] Entry blocked if parking is full`n- [ ] Customer cannot park another user's vehicle`n- [ ] Frontend: vehicle selector; ticket display after allocation`n`n## Tests`nParkingControllerTest (entry tests) + ParkingServiceTest (ownership, duplicate, full)`n`n## Story Points: 5 | Priority: P0 | Sprint: 2 | Assignee: Vikas75bit"
    milestone = 2
}

# #14 Vehicle exit and parking fee calculation
# Correct SP: 5 (exit, fee calc with grace period, rate lookup, slot release)
Update-Issue 14 @{
    body = "## Summary`nRecord vehicle exit, calculate fee from rate schedule and elapsed time, release slot.`n`n## What is implemented`n- POST /api/v1/parking/exit — ticketId + paymentMethod`n- Customer can only exit their own session (403 otherwise)`n- Ticket not found → 400; already-completed → 403`n- Fee = max(0, durationMinutes - gracePeriod) * ratePerHour / 60, rounded HALF_UP to 2dp`n- Duration within grace period (default 15 min) → zero fee`n- Slot released to AVAILABLE; audit log entry`n- GET /api/v1/parking/history — own history for CUSTOMER; all for staff`n- GET /api/v1/parking/history/vehicle/{id} — ownership enforced`n`n## Acceptance Criteria`n- [ ] Fee calculation uses active rate for vehicle type`n- [ ] Grace period applied before billing starts`n- [ ] Slot status AVAILABLE after exit`n- [ ] Customer blocked from exiting another user's session`n- [ ] Frontend: exit form, fee breakdown`n`n## Tests`nParkingServiceTest (fee, grace period, ownership, double-exit)`n`n## Story Points: 5 | Priority: P0 | Sprint: 2 | Assignee: Vikas75bit"
    milestone = 2
}

# #15 Payment status and receipt generation
# Correct SP: 3 (mock payment entity, receipt, duplicate prevention)
Update-Issue 15 @{
    body = "## Summary`nTrack payment status per parking session and produce a receipt on successful payment.`n`n## What is implemented`n- Payment entity: amount, currency (INR), method (CASH/CARD/DIGITAL_WALLET), status, transactionRef, paidAt`n- One payment per parking record (DB unique constraint — prevents duplicates)`n- Mock: always returns SUCCESS; no card numbers, CVV, or PINs ever stored`n- Receipt returned in exit response: receiptId (RCP-<ticketId>), all session details`n- Re-exit on completed ticket rejected (→ 403)`n`n## Acceptance Criteria`n- [ ] Payment statuses: PENDING, SUCCESS, FAILED`n- [ ] Duplicate payment attempt (same record) prevented by DB constraint`n- [ ] Receipt includes: receiptId, ticketId, plate, slot, times, duration, fee, paymentStatus`n- [ ] UI clearly states payment is simulated for academic project`n- [ ] No card data anywhere in DB or logs`n`n## Story Points: 3 | Priority: P1 | Sprint: 2 | Assignee: Vikas75bit"
    milestone = 2
}

# #16 Parking rate configuration and reservations
# Correct SP: 8 (two distinct subsystems: rate CRUD + full reservation lifecycle)
Update-Issue 16 @{
    body = "## Summary`nAllow administrators to configure parking rates, and customers to create advance reservations.`n`n## What is implemented`n**Rates (VPS-F-014)**`n- GET/POST/PUT /api/v1/admin/rates — ADMINISTRATOR only`n- Rate fields: name, vehicleType, ratePerHour (>0), gracePeriodMinutes (>=0), active`n- Fee calculation reads active rate for vehicle type`n- Seed: 4 rates (CAR=30, MOTORCYCLE=15, TRUCK=60, EV=20 INR/hr, grace=15min)`n`n**Reservations (VPS-F-011, VPS-F-012)**`n- POST /api/v1/reservations — own vehicle only; startTime in future; endTime > startTime`n- Conflict check: no overlapping CONFIRMED reservations on same slot`n- GET /api/v1/reservations — own for CUSTOMER; all for ADMINISTRATOR`n- DELETE /api/v1/reservations/{id} — cancel; customer own or admin`n- POST /api/v1/reservations/expire — staff/admin only; marks overdue as EXPIRED`n`n## Acceptance Criteria`n- [ ] Rate per hour > 0 validated`n- [ ] Customer cannot create/modify rates`n- [ ] Reservation conflict returns 403 with safe message`n- [ ] Customer cannot cancel another user's reservation`n`n## Story Points: 8 | Priority: P1 | Sprint: 2 | Assignee: adik1611"
    milestone = 2
}

# #18 Admin dashboard and report generation
# Correct SP: 5 (dashboard stats + date-filtered report + rate config already separate in #16)
Update-Issue 18 @{
    body = "## Summary`nProvide administrators with real-time system statistics and a date-filtered parking history report.`n`n## What is implemented`n- GET /api/v1/admin/dashboard — ADMINISTRATOR only`n- Stats: totalSlots, available/occupied/reserved/outOfService slots, activeVehicles, totalUsersRegistered, totalCompletedTransactions, revenueToday, revenueThisMonth`n- All stats derived from real DB queries (no hardcoded values)`n- GET /api/v1/admin/reports?from=...&to=... — ISO 8601 datetime params; defaults to last 30 days`n- Non-administrators receive 403`n`n## Acceptance Criteria`n- [ ] Revenue calculated from feeAmount on COMPLETED parking records`n- [ ] Dashboard loads from real data`n- [ ] Reports can be filtered by date range`n- [ ] Frontend: stat cards, revenue display, report table with pagination`n`n## Tests`nAdminControllerTest (dashboard, rates, reports — 8 tests)`n`n## Story Points: 5 | Priority: P1 | Sprint: 3 | Assignee: gaganreddy4656"
    milestone = 3
}

# #19 Functional, security, and non-functional testing
# Correct SP: 8 (comprehensive test suite coordination)
Update-Issue 19 @{
    body = "## Summary`nEnsure the application has meaningful automated test coverage across all layers.`n`n## What is implemented`n**Unit tests (service layer, Mockito):**`n- AuthServiceTest: 9 tests (registration, duplicate, role enforcement, concurrent duplicate, login, lock)`n- VehicleServiceTest: 13 tests (CRUD, ownership, duplicate plate)`n- ParkingServiceTest: 12 tests (entry/exit ownership, fee calc, grace period, full lot, slot CRUD)`n`n**Integration tests (Spring Boot Test, H2 in-memory):**`n- AuthControllerTest: 7 tests (register, duplicate email, role elevation blocked, weak password, login, locked)`n- UserControllerTest: 5 tests (profile GET/PUT, unauthenticated, no passwordHash)`n- VehicleControllerTest: 9 tests (CRUD, duplicate, ownership, plate normalization)`n- ParkingControllerTest: 8 tests (entry, duplicate session, exit+receipt, history, active session, public slots)`n- AdminControllerTest: 8 tests (dashboard, rates CRUD, report, role enforcement)`n`n## Run command`n\`\`\`bash`ncd src/backend && mvn test`n\`\`\``n`n## Test results: 71 tests, 0 failures, 0 errors`n`n## Acceptance Criteria`n- [ ] H2 in-memory; no MySQL required for tests`n- [ ] Role elevation blocked in registration test`n- [ ] Ownership boundaries tested for vehicles, sessions, history`n- [ ] Admin-only endpoints return 403 for customer`n- [ ] Password hash never appears in any response`n`n## Story Points: 8 | Priority: P0 | Sprint: 3 | Assignee: gaganreddy4656"
    milestone = 3
}

Write-Host "=== All 9 original issues updated ===" -ForegroundColor Cyan
