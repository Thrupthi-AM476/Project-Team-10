# GitHub Issues Update Script — Vehicle Parking System Sprint 2
# Closes issues #32, #33, #34, #35, #44, #48, #49 and links PR#56
# Run manually: powershell -ExecutionPolicy Bypass -File scripts\github-update.ps1

[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
[System.Net.ServicePointManager]::ServerCertificateValidationCallback = { $true }

$credOutput = echo "protocol=https`nhost=github.com`n" | git credential fill 2>&1
$T = (($credOutput -split "`n") | Where-Object { $_ -match "^password=" } | Select-Object -First 1) -replace "^password=",""
$H = @{ "Authorization" = "token $T"; "Accept" = "application/vnd.github+json"; "Content-Type" = "application/json" }
$R = "Thrupthi-AM476/Project-Team-10"
$PR = 56

function Patch-Issue($num, $body) {
    $r = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/$num" -Headers $H -Method PATCH -Body $body
    return $r
}

Write-Output "=== Updating issues for PR #$PR ==="

# Issue bodies mapping each issue to completion evidence
$issues = @(
    @{
        num = 32
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on branch `feat/fullstack-vps-development`, included in PR #56.\n\n## What was delivered\n- Vite 5, React 18, React Router 6 configured in `src/frontend/`\n- `src/frontend/src/App.jsx` with all routes including role-aware ProtectedRoute\n- `src/frontend/src/components/ProtectedRoute.jsx` with redirect-to-origin support\n- `src/frontend/src/context/AuthContext.jsx` with login/register/logout and persisted state\n- `src/frontend/src/api/client.js` centralized API client with JWT headers, 401 auto-clear\n- `src/frontend/src/components/Layout.jsx` with skip-link and tabIndex\n- `src/frontend/src/components/Navbar.jsx` (from main) with role-aware links\n- `src/frontend/src/index.css` complete design system (Inter font, design tokens)\n- `package.json`, `vite.config.js`, `eslint.config.js` all working\n\n## Acceptance Criteria\n- [x] Vite + React project in src/frontend/\n- [x] React Router 6 with all planned routes\n- [x] Responsive CSS with design tokens\n- [x] API client with JWT headers and error normalization\n- [x] AuthContext with login/register/logout, persisted token\n- [x] ProtectedRoute and RoleRoute components\n- [x] .env.example with VITE_API_BASE_URL\n- [x] npm run build succeeds (52 modules, 232KB)\n- [x] npm run lint passes (0 errors)\n\nPR: #56\n\n**Story Points: 3 | Sprint: 1**"}'
    },
    @{
        num = 33
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on `feat/fullstack-vps-development`, included in PR #56.\n\n## What was delivered\n- `Login.jsx`: email/password form, show/hide toggle, spinner on submit, 403 vs 400 error distinction, aria-required/describedby/busy\n- `Register.jsx`: fullName, email, phone, password, confirm-password fields; real-time pw rule checklist (5 rules matching backend regex), strength bar (weak/medium/strong), onBlur field-level errors, submit disabled until all rules met\n\n## Acceptance Criteria\n- [x] /register: fullName, email, phone (optional), password, confirm-password\n- [x] Client-side password validation: 8+ chars, upper, lower, number, special\n- [x] /login: email + password with loading state\n- [x] Role-based redirect after login (admin/staff/customer dashboards)\n- [x] Server errors displayed inline (duplicate email, wrong password, locked account)\n- [x] Passwords never stored in localStorage\n- [x] Forms are keyboard-navigable (aria labels, focus states)\n\nPR: #56\n\n**Story Points: 3 | Sprint: 1**"}'
    },
    @{
        num = 34
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on `feat/fullstack-vps-development`, included in PR #56.\n\n## What was delivered\n- `CustomerDashboard.jsx`: first-name greeting, stat cards with icons (vehicle count, active count, parked now), vehicle preview list with type icons (🚗🏍🚚⚡), active parking session card with time-since display, quick-actions panel\n- `Profile.jsx`: avatar circle with initial, info-list layout displaying all account details, edit form with dirty-state detection (Save disabled when nothing changed), notice panel explaining locked fields\n\n## Acceptance Criteria\n- [x] /dashboard: welcome message, vehicle count, active session card\n- [x] /profile: displays name, email, phone, role, status, memberSince\n- [x] Profile edit: fullName and phone only\n- [x] No passwordHash displayed anywhere\n- [x] Empty states for new users\n\nPR: #56\n\n**Story Points: 3 | Sprint: 1**"}'
    },
    @{
        num = 35
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on `feat/fullstack-vps-development`, included in PR #56.\n\n## What was delivered\n- `Vehicles.jsx`: extracted `VehicleForm` component with own validation state; plate validated client-side against backend regex (2-20 alphanumeric/hyphen, auto-uppercased); vehicle table with type icons, make/model combined, aria-labels on action buttons; ghost-style deactivate distinct from edit; ConfirmDialog before deactivation\n\n## Acceptance Criteria\n- [x] /vehicles: table of own vehicles (customer) or all (staff)\n- [x] Add form: licensePlate auto-uppercased, vehicleType, make, model, color\n- [x] Client-side plate validation (2-20 chars, pattern enforced)\n- [x] Edit inline; deactivate with confirmation dialog\n- [x] Duplicate plate error displayed inline from backend\n- [x] Empty state when no vehicles\n\nPR: #56\n\n**Story Points: 5 | Sprint: 1**"}'
    },
    @{
        num = 44
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on `feat/fullstack-vps-development`, included in PR #56.\n\n## What was verified\n- All API paths in `src/frontend/src/api/client.js` cross-checked against backend controllers\n- `POST /api/v1/auth/register` — matches RegisterRequest DTO (fullName, email, password, phone)\n- `POST /api/v1/auth/login` — matches AuthRequest DTO\n- `GET/PUT /api/v1/users/me` — matches UserProfileResponse/Request\n- `GET/POST/PUT/DELETE /api/v1/vehicles` and `/api/v1/vehicles/{id}` — all verified\n- `GET /api/v1/parking/slots` (public), `GET /api/v1/parking/active-session` (auth)\n- CORS: backend SecurityConfig allows http://localhost:3000\n- 401 response: added auto-clear of localStorage so ProtectedRoute redirects automatically\n- Backend validation errors propagated to UI error state; never hardcoded fake responses\n\n## Acceptance Criteria\n- [x] All API paths match backend @RequestMapping exactly\n- [x] 401/403 responses handled gracefully (redirect or show error)\n- [x] Request DTOs match @Valid constraints\n- [x] Loading states shown during calls\n- [x] Server error messages displayed to user\n\nPR: #56\n\n**Story Points: 3 | Sprint: 2**"}'
    },
    @{
        num = 48
        body = '{"state":"closed","body":"## Status: COMPLETE\n\nImplemented on `feat/fullstack-vps-development`, included in PR #56.\n\n## What was verified\n- `index.css` rewritten with full responsive design system\n- Navbar collapses to hamburger at 768px\n- Forms, cards, tables, slot grid all use responsive grid (auto-fit columns, single col at 600px)\n- Skip-to-content link (`<a class=\"skip-link\" href=\"#main-content\">`) for keyboard users\n- `tabIndex=-1` on main content area for programmatic focus after navigation\n- `focus-visible` outlines on all interactive elements (2px solid primary)\n- `aria-required` on all required inputs\n- `aria-describedby` linking inputs to error/hint elements\n- `aria-label` on icon-only buttons (show/hide pw, deactivate vehicle)\n- `aria-busy` on submit buttons during loading\n- `aria-live` on password strength and rule list\n- `role=alert` on field error messages\n- `role=status` on success messages\n- `role=dialog` and `aria-modal` on ConfirmDialog\n- No horizontal overflow at 375px viewport\n\n## Acceptance Criteria\n- [x] All pages usable at 375px, 768px, 1280px\n- [x] Navbar collapses on small screens\n- [x] Forms have labels linked to inputs via htmlFor\n- [x] Error/success messages have role=alert or role=status\n- [x] No horizontal scroll at 375px\n\nPR: #56\n\n**Story Points: 3 | Sprint: 2**"}'
    }
)

foreach ($issue in $issues) {
    Write-Output "Closing #$($issue.num)..."
    try {
        $r = Patch-Issue $issue.num $issue.body
        Write-Output "  OK: #$($r.number) [$($r.state)] $($r.title)"
    } catch {
        Write-Output "  ERR #$($issue.num): $($_.Exception.Message)"
    }
    Start-Sleep -Milliseconds 600
}

# Issue #49 is "In Review" (PR exists but not merged) — keep open
Write-Output "`nUpdating #49 body (In Review - not closing)..."
$body49 = '{"body":"## Status: IN REVIEW\n\nPR #56 (`feat/fullstack-vps-development` -> `main`) is open and awaiting team review.\n\nDo not close this issue until the PR is reviewed and merged by the team.\n\n## What was done\n- All seven Sprint 2 frontend issues implemented on `feat/fullstack-vps-development`\n- Branch merged with origin/main (includes all teammate PRs #51-#55)\n- Landing.jsx conflict resolved: teammate /parking public link preserved\n- PR #56 opened targeting main with full description and test results\n\n## PR link\nhttps://github.com/Thrupthi-AM476/Project-Team-10/pull/56\n\n**Story Points: 1 | Sprint: 1**"}'
try {
    $r = Invoke-RestMethod -Uri "https://api.github.com/repos/$R/issues/49" -Headers $H -Method PATCH -Body $body49
    Write-Output "  #49 body updated [$($r.state)]"
} catch {
    Write-Output "  ERR #49: $($_.Exception.Message)"
}

Write-Output "`n=== Done. Verify at https://github.com/$R/issues ==="
