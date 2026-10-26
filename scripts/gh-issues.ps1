# Creates all new GitHub issues for VPS Team 10
param()

$credOutput = (echo "protocol=https`nhost=github.com`n") | git credential fill 2>&1
$T = (($credOutput -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "Bearer $T"; "Accept" = "application/vnd.github+json"; "Content-Type" = "application/json" }
$R = "Thrupthi-AM476/Project-Team-10"

function Post-Issue([hashtable]$data) {
    $json = $data | ConvertTo-Json -Compress -Depth 6
    try {
        $r = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues" `
            -Headers $H -Method POST -Body $json
        Write-Host "  OK  #$($r.number): $($r.title)" -ForegroundColor Green
        Start-Sleep -Milliseconds 400
    } catch {
        Write-Host "  ERR $($data.title): $_" -ForegroundColor Red
    }
}

$issues = @(
    @{
        title = "Set up React + Vite frontend with routing and component structure"
        body  = "## Summary`nBootstrap the React frontend with Vite 5, React Router 6, global CSS, reusable components, and API client.`n`n## Acceptance Criteria`n- [ ] src/frontend initialised with package.json, vite.config.js, index.html`n- [ ] React Router 6 routes for all pages`n- [ ] Responsive CSS with design tokens`n- [ ] Reusable: Card, Field, Badge, LoadingSpinner, EmptyState, ErrorMessage, ConfirmDialog`n- [ ] Centralized API client with JWT headers`n- [ ] AuthContext: login/register/logout, persisted token`n- [ ] ProtectedRoute and RoleRoute`n- [ ] .env.example with VITE_API_BASE_URL`n- [ ] npm run build succeeds`n`n**Story Points: 3 | Priority: P0 | Sprint: 1**"
        labels = @("frontend","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 1
    },
    @{
        title = "Implement login and registration pages with validation"
        body  = "## Summary`nBuild public auth pages connected to the backend JWT API.`n`n## Acceptance Criteria`n- [ ] /register: fullName, email, phone (optional), password`n- [ ] Client-side password validation (8+ chars, upper, lower, number, special)`n- [ ] /login: email + password; loading state during request`n- [ ] After login, redirect to role-appropriate dashboard`n- [ ] Server errors displayed inline (duplicate email, wrong password, locked account)`n- [ ] Passwords never stored in localStorage`n- [ ] Forms are keyboard-navigable`n`n**Story Points: 3 | Priority: P0 | Sprint: 1**"
        labels = @("frontend","security","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 1
    },
    @{
        title = "Build customer dashboard and user profile page"
        body  = "## Summary`nCustomer landing page after login: summary stats and quick links. Profile view and edit.`n`n## Acceptance Criteria`n- [ ] /dashboard: welcome, vehicle count, active session card, quick actions`n- [ ] /profile: displays id, name, email, phone, role, status, memberSince`n- [ ] Profile edit: fullName and phone only`n- [ ] No passwordHash displayed anywhere`n- [ ] Empty states for new users`n`n**Story Points: 3 | Priority: P0 | Sprint: 1**"
        labels = @("frontend","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 1
    },
    @{
        title = "Build vehicle registration, listing, and management UI"
        body  = "## Summary`nFrontend pages for customers to manage vehicles and staff to view all.`n`n## Acceptance Criteria`n- [ ] /vehicles: table with own vehicles (customers) or all (staff)`n- [ ] Add form: licensePlate (auto-uppercased), vehicleType, make, model, color`n- [ ] Client-side plate validation`n- [ ] Edit inline; deactivate with confirmation dialog`n- [ ] Duplicate plate error shown inline`n- [ ] Empty state when no vehicles`n`n**Story Points: 5 | Priority: P0 | Sprint: 1**"
        labels = @("frontend","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 1
    },
    @{
        title = "Build parking availability grid and real-time slot status display"
        body  = "## Summary`nColor-coded slot grid showing live availability status.`n`n## Acceptance Criteria`n- [ ] /parking: grid of slot cards color-coded by status`n- [ ] Each card: slot number, type (REGULAR/DISABLED/EV), floor`n- [ ] Summary stats: total, available, occupied, reserved, out-of-service`n- [ ] Refresh button reloads from API`n- [ ] Park vehicle CTA for authenticated customers`n- [ ] Works unauthenticated (public endpoint)`n`n**Story Points: 3 | Priority: P0 | Sprint: 2**"
        labels = @("frontend","P0")
        assignees = @("adik1611")
        milestone = 2
    },
    @{
        title = "Build parking entry workflow and entry-ticket display"
        body  = "## Summary`nLet authenticated customers park: select vehicle, choose slot type, get ticket.`n`n## Acceptance Criteria`n- [ ] /parking/session: own active-vehicle selector`n- [ ] Optional slot type preference`n- [ ] Shows ticket: ticketId, slotNumber, slotType, entryTime`n- [ ] Parking full and already-parked errors shown clearly`n- [ ] Navigate to /parking/exit from ticket view`n`n**Story Points: 3 | Priority: P0 | Sprint: 2**"
        labels = @("frontend","P0")
        assignees = @("Vikas75bit")
        milestone = 2
    },
    @{
        title = "Build vehicle exit workflow with fee display and receipt"
        body  = "## Summary`nExit form, fee breakdown, payment selector, and receipt card.`n`n## Acceptance Criteria`n- [ ] /parking/exit: shows active session or manual ticket input`n- [ ] Payment method selector`n- [ ] Receipt: receiptId, plate, slot, times, duration, fee, paymentStatus`n- [ ] Note that payment is simulated`n- [ ] Error if ticket not found or completed`n`n**Story Points: 3 | Priority: P0 | Sprint: 2**"
        labels = @("frontend","P0")
        assignees = @("Vikas75bit")
        milestone = 2
    },
    @{
        title = "Build reservations page: create, list, and cancel advance bookings"
        body  = "## Summary`nFrontend for advance slot reservations.`n`n## Acceptance Criteria`n- [ ] /reservations: list with status badges`n- [ ] Create: vehicle selector, start/end datetime pickers, optional slotType`n- [ ] Client-side endTime > startTime validation`n- [ ] Cancel with confirmation dialog`n- [ ] Admin sees all reservations`n`n**Story Points: 5 | Priority: P1 | Sprint: 2**"
        labels = @("frontend","P1")
        assignees = @("adik1611")
        milestone = 2
    },
    @{
        title = "Build staff dashboard for slot management and vehicle oversight"
        body  = "## Summary`nPARKING_ATTENDANT dashboard: manage slots, view all vehicles.`n`n## Acceptance Criteria`n- [ ] /staff: stats row, slot table with edit/disable, add slot form`n- [ ] All vehicles table: plate, type, owner, status`n- [ ] Confirmation before disabling slot`n- [ ] Role-gated: PARKING_ATTENDANT and ADMINISTRATOR`n`n**Story Points: 5 | Priority: P1 | Sprint: 2**"
        labels = @("frontend","P1")
        assignees = @("adik1611")
        milestone = 2
    },
    @{
        title = "Build administrator dashboard with stats, reports, and rate configuration"
        body  = "## Summary`nAdmin page: system stats, revenue, parking report, rate schedule management.`n`n## Acceptance Criteria`n- [ ] /admin: 8 stat cards from real API`n- [ ] Rate schedule table with add/edit`n- [ ] Date-filtered parking history report`n- [ ] Revenue in INR`n- [ ] ADMINISTRATOR role only`n`n**Story Points: 5 | Priority: P1 | Sprint: 3**"
        labels = @("frontend","P1")
        assignees = @("gaganreddy4656")
        milestone = 3
    },
    @{
        title = "Verify database schema, constraints, and seed data completeness"
        body  = "## Summary`nEnsure schema.sql matches JPA entities with correct constraints and seed data.`n`n## Acceptance Criteria`n- [ ] All 8 tables present with correct FKs and unique constraints`n- [ ] 20 seed parking slots and 4 rate schedules`n- [ ] Default admin user seed`n- [ ] ddl-auto=validate in production config`n- [ ] H2 create-drop for tests`n`n**Story Points: 2 | Priority: P0 | Sprint: 1**"
        labels = @("database","backend","P0")
        assignees = @("gaganreddy4656")
        milestone = 1
    },
    @{
        title = "Set up GitHub Actions CI for backend tests and frontend build"
        body  = "## Summary`nAutomate test and build verification on every push and PR.`n`n## Acceptance Criteria`n- [ ] .github/workflows/ci.yml present`n- [ ] backend-tests: Java 21, mvn test, mvn package -DskipTests`n- [ ] frontend-build: Node 20, npm install, npm run build`n- [ ] Runs on push to main and feat/** and on PR to main`n- [ ] CI passes on current codebase`n`n**Story Points: 2 | Priority: P1 | Sprint: 3**"
        labels = @("testing","documentation","P1")
        assignees = @("gaganreddy4656")
        milestone = 3
    },
    @{
        title = "Verify frontend-backend API integration and fix any mismatches"
        body  = "## Summary`nEnsure every frontend API call matches the real backend route, DTO, and auth requirement.`n`n## Acceptance Criteria`n- [ ] CORS configured for localhost:3000`n- [ ] All paths in client.js match controller @RequestMapping exactly`n- [ ] 401/403 redirect to login or show error`n- [ ] Request DTOs match @Valid constraints`n- [ ] Loading states shown during calls`n- [ ] Server error messages displayed to user`n`n**Story Points: 3 | Priority: P0 | Sprint: 3**"
        labels = @("integration","frontend","backend","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 3
    },
    @{
        title = "Write complete README with setup instructions and demo walkthrough"
        body  = "## Summary`nUpdate README with full setup, API reference, and 8-step demo scenario.`n`n## Acceptance Criteria`n- [ ] Tech stack, repo structure, database schema`n- [ ] Backend and frontend setup commands`n- [ ] mvn test result (71 tests passing)`n- [ ] 8-step end-to-end demo walkthrough`n- [ ] Known limitations (simulated payment)`n`n**Story Points: 2 | Priority: P1 | Sprint: 3**"
        labels = @("documentation","P1")
        assignees = @("gaganreddy4656")
        milestone = 3
    },
    @{
        title = "Security audit: verify RBAC, ownership enforcement, and safe error responses"
        body  = "## Summary`nSystematic verification that every endpoint enforces role and ownership, and no sensitive data leaks.`n`n## Acceptance Criteria`n- [ ] Customers blocked from /admin/** (403)`n- [ ] Customers cannot access another user's vehicle, session, or history`n- [ ] Role elevation via registration blocked`n- [ ] passwordHash never in any response`n- [ ] No stack traces or SQL in error messages`n- [ ] Unauthenticated requests return 403`n`n**Story Points: 3 | Priority: P0 | Sprint: 3**"
        labels = @("security","testing","P0")
        assignees = @("gaganreddy4656")
        milestone = 3
    },
    @{
        title = "Document sprint reviews, retrospectives, and individual contributions"
        body  = "## Summary`nCreate Agile evidence: sprint goals vs actuals, retrospectives, and contribution summary.`n`n## Acceptance Criteria`n- [ ] Sprint 1/2/3 review: planned vs completed issues, blockers`n- [ ] Retrospective: went well / improve / actions for each sprint`n- [ ] Contribution table: teammate, issues owned, PRs, commits (from actual Git history)`n- [ ] Stored in docs/SPRINT_REVIEWS.md`n`n**Story Points: 2 | Priority: P2 | Sprint: 3**"
        labels = @("documentation","P2")
        assignees = @("gaganreddy4656")
        milestone = 3
    },
    @{
        title = "Verify responsive layout and basic accessibility across all pages"
        body  = "## Summary`nEnsure UI works on mobile/tablet/desktop and meets basic accessibility requirements.`n`n## Acceptance Criteria`n- [ ] All pages usable at 375px, 768px, 1280px`n- [ ] Navbar collapses on small screens`n- [ ] Forms have labels linked to inputs`n- [ ] Error/success messages have role=alert or role=status`n- [ ] No horizontal scroll at 375px`n`n**Story Points: 3 | Priority: P2 | Sprint: 3**"
        labels = @("frontend","accessibility","P2")
        assignees = @("Thrupthi-AM476")
        milestone = 3
    },
    @{
        title = "Merge feature branch and open PR for team review"
        body  = "## Summary`nIntegrate feat/fullstack-vps-development into main with full PR description and CI verification.`n`n## Acceptance Criteria`n- [ ] Branch rebased or merged cleanly`n- [ ] PR description links all implemented features and issues`n- [ ] CI passes: 71 tests + frontend build`n- [ ] Reviewed by at least one teammate`n- [ ] Merged to main`n`n**Story Points: 1 | Priority: P0 | Sprint: 3**"
        labels = @("integration","P0")
        assignees = @("Thrupthi-AM476")
        milestone = 3
    }
)

Write-Host "Creating $($issues.Count) issues..." -ForegroundColor Cyan
foreach ($issue in $issues) {
    Post-Issue $issue
}
Write-Host "Done." -ForegroundColor Cyan
