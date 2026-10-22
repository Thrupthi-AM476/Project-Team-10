# GitHub issue creation script — Vehicle Parking System Team 10
# Run: powershell -File scripts/create-issues.ps1

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

Write-Output "Creating new GitHub issues..."

# 26 — Frontend setup
New-GHIssue `
    "Set up React + Vite frontend with routing and component structure" `
    "## Summary`nBootstrap the frontend: Vite 5, React 18, React Router 6, global CSS, reusable components, API client, AuthContext.`n`n## Acceptance Criteria`n- [ ] src/frontend/ initialised with package.json, vite.config.js, index.html`n- [ ] React Router 6 with all planned routes (landing, auth, customer, staff, admin)`n- [ ] Responsive CSS with design tokens (colors, spacing, typography)`n- [ ] Reusable components: Card, Field, Badge, LoadingSpinner, EmptyState, ErrorMessage, ConfirmDialog`n- [ ] API client with JWT headers and centralized error normalization`n- [ ] AuthContext: login/register/logout, persisted token and user object`n- [ ] ProtectedRoute and RoleRoute components for auth/role gating`n- [ ] .env.example with VITE_API_BASE_URL`n- [ ] npm run build produces clean bundle (no errors)`n`n## Definition of Done`nFrontend builds without errors; routing navigates between pages; auth context persists after page reload.`n`n## Story Points: 3 | Priority: P0 | Sprint: 1" `
    @("frontend","P0") @("Thrupthi-AM476") 1 | Out-Null

# 27 — Login and registration pages
New-GHIssue `
    "Implement login and registration pages with validation and error handling" `
    "## Summary`nBuild the two public-facing auth pages connected to the backend JWT API.`n`n## Acceptance Criteria`n- [ ] /register: fullName, email, phone (optional), password fields`n- [ ] Client-side password validation: min 8 chars, upper, lower, number, special (@`$!%*?&)`n- [ ] /login: email + password; loading state during request`n- [ ] After login, redirect to role-appropriate dashboard`n- [ ] Duplicate email → clear error message`n- [ ] Invalid credentials → clear error message (no stack trace)`n- [ ] Locked account → appropriate message`n- [ ] Passwords never stored in localStorage`n- [ ] Forms are keyboard-navigable and have proper labels`n`n## Definition of Done`nRegister and login work against live backend; errors display correctly; redirect fires on success.`n`n## Story Points: 3 | Priority: P0 | Sprint: 1" `
    @("frontend","security","P0") @("Thrupthi-AM476") 1 | Out-Null

# 28 — Customer dashboard and profile
New-GHIssue `
    "Build customer dashboard and user profile page" `
    "## Summary`nCustomer landing page after login: summary stats, quick links, and a profile view/edit page.`n`n## Acceptance Criteria`n- [ ] /dashboard: welcome message with user name`n- [ ] Vehicle count stat card`n- [ ] Active parking session card (shows ticket, slot, entry time) or empty state`n- [ ] Quick action links: vehicles, parking, reservations, profile`n- [ ] /profile: display id, fullName, email, phone, role, status, memberSince`n- [ ] Profile edit form: fullName and phone only (role/status are read-only)`n- [ ] Save success message; validation error displayed inline`n- [ ] No passwordHash ever displayed`n`n## Definition of Done`nDashboard loads real data from API; profile updates persist after refresh.`n`n## Story Points: 3 | Priority: P0 | Sprint: 1" `
    @("frontend","P0") @("Thrupthi-AM476") 1 | Out-Null

# 29 — Vehicle management UI
New-GHIssue `
    "Build vehicle registration, listing, and management UI" `
    "## Summary`nFrontend pages for customers to manage their vehicles and for staff to view all vehicles.`n`n## Acceptance Criteria`n- [ ] /vehicles: table of user's own vehicles (customers) or all vehicles (staff)`n- [ ] Add vehicle form: licensePlate (auto-uppercased), vehicleType, make, model, color`n- [ ] Client-side plate validation (2-20 alphanumeric/hyphen)`n- [ ] Edit vehicle inline or in modal`n- [ ] Deactivate vehicle with confirmation dialog`n- [ ] Inactive vehicles shown with visual distinction`n- [ ] Duplicate plate error shown inline`n- [ ] Empty state when no vehicles registered`n- [ ] Staff sees owner name column`n`n## Definition of Done`nCRUD operations work against live backend; ownership is never bypassed from the frontend.`n`n## Story Points: 5 | Priority: P0 | Sprint: 1" `
    @("frontend","P0") @("Thrupthi-AM476") 1 | Out-Null

# 30 — Parking availability and slot grid
New-GHIssue `
    "Build parking availability grid and real-time slot status display" `
    "## Summary`nVisualize all parking slots with live status, grouped by floor, with color coding.`n`n## Acceptance Criteria`n- [ ] /parking: grid of slot cards, color-coded (green=AVAILABLE, red=OCCUPIED, amber=RESERVED, grey=OUT_OF_SERVICE)`n- [ ] Each card shows slot number, type (REGULAR/DISABLED/EV), and floor`n- [ ] Summary stats: total, available, occupied, reserved, out-of-service counts`n- [ ] Refresh button reloads from API`n- [ ] CTA to park vehicle when slots are available (authenticated customers)`n- [ ] Works for unauthenticated users (public endpoint)`n`n## Definition of Done`nSlot grid renders all 20 seed slots; status updates after refresh.`n`n## Story Points: 3 | Priority: P0 | Sprint: 2" `
    @("frontend","P0") @("adik1611") 2 | Out-Null

# 31 — Parking entry and ticket UI
New-GHIssue `
    "Build parking entry workflow and entry-ticket display" `
    "## Summary`nLet authenticated customers park a vehicle: select from their registered vehicles, choose slot type, submit, and see the generated ticket.`n`n## Acceptance Criteria`n- [ ] /parking/session: vehicle selector (own active vehicles only)`n- [ ] Optional preferred slot type selector (REGULAR/DISABLED/EV)`n- [ ] Submit allocates slot and shows ticket: ticketId, slotNumber, slotType, entryTime`n- [ ] Parking full error displayed clearly`n- [ ] Already-parked vehicle error displayed`n- [ ] Active session displayed on return to page with link to exit`n- [ ] Navigate to /parking/exit from ticket view`n`n## Definition of Done`nEntry workflow creates a real session in DB; ticket details visible after allocation.`n`n## Story Points: 3 | Priority: P0 | Sprint: 2" `
    @("frontend","P0") @("Vikas75bit") 2 | Out-Null

# 32 — Exit, fee, and receipt UI
New-GHIssue `
    "Build vehicle exit workflow with fee display and receipt" `
    "## Summary`nExit form, fee breakdown, payment method selector, and receipt card after successful exit.`n`n## Acceptance Criteria`n- [ ] /parking/exit: shows active session if one exists; otherwise shows manual ticketId input`n- [ ] Payment method selector: Cash, Card, Digital Wallet`n- [ ] Submit processes exit and shows calculated fee`n- [ ] Receipt card: receiptId, ticketId, plate, slot, entry/exit times, duration, fee, paymentStatus`n- [ ] Note that payment is simulated for academic project`n- [ ] Graceful error if ticket not found or already completed`n- [ ] Back link to parking page after receipt`n`n## Definition of Done`nExit workflow persists in DB; fee calculated from active rate schedule; receipt displayed.`n`n## Story Points: 3 | Priority: P0 | Sprint: 2" `
    @("frontend","P0") @("Vikas75bit") 2 | Out-Null

Write-Output "Done creating issues 26-32."
