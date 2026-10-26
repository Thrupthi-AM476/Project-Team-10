# GitHub issue creation script part 3
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

Write-Output "Creating issues 39-44..."

# 39 — README and setup documentation
New-GHIssue `
    "Write complete README with setup instructions, API docs, and demo walkthrough" `
    "## Summary`nUpdate the README to reflect the actual implementation with prerequisites, environment setup, run commands, test commands, and a demo scenario.`n`n## Acceptance Criteria`n- [ ] Project description and technology stack`n- [ ] Repository structure diagram`n- [ ] Database schema summary (8 tables)`n- [ ] Backend API endpoint table with methods, paths, auth requirements`n- [ ] Prerequisites: Java 21+, Maven, MySQL 8.x, Node 20+, npm`n- [ ] Backend setup: MySQL setup, application.properties, mvn spring-boot:run`n- [ ] Frontend setup: cp .env.example .env, npm install, npm run`n- [ ] Test execution: mvn test (71 tests passing)`n- [ ] Demo walkthrough: 8-step end-to-end scenario`n- [ ] Security notes: simulated payment, no real card data`n- [ ] Known limitations`n`n## Story Points: 2 | Priority: P1 | Sprint: 3" `
    @("documentation","P1") @("gaganreddy4656") 3 | Out-Null

# 40 — Security audit and RBAC verification
New-GHIssue `
    "Security audit: verify RBAC boundaries and safe error responses across all endpoints" `
    "## Summary`nSystematic check that every endpoint enforces the correct role, ownership is never bypassed, and no sensitive data leaks.`n`n## Acceptance Criteria`n- [ ] Customer cannot access /api/v1/admin/** (403)`n- [ ] Customer cannot view/update/delete another user's vehicle`n- [ ] Customer cannot park or exit a session for another user's vehicle`n- [ ] Customer cannot view another user's parking history`n- [ ] Role field removed from RegisterRequest (privilege escalation blocked)`n- [ ] passwordHash never appears in any API response`n- [ ] Error messages contain no stack traces, SQL details, or internal state`n- [ ] Unauthenticated requests to protected endpoints return 403`n- [ ] Account lock message is safe (no internal reason leaked)`n- [ ] Test coverage for all the above (see #19)`n`n## Story Points: 3 | Priority: P0 | Sprint: 3" `
    @("security","testing","P0") @("gaganreddy4656") 3 | Out-Null

# 41 — Sprint retrospective and contribution tracking document
New-GHIssue `
    "Document sprint reviews, retrospectives, and individual contribution tracking" `
    "## Summary`nCreate Agile evidence documentation: sprint goals vs actuals, retrospective notes, and Git-based contribution summary per team member.`n`n## Acceptance Criteria`n- [ ] Sprint 1 review: planned vs completed issues, blockers, key decisions`n- [ ] Sprint 2 review: parking workflow completions, integration status`n- [ ] Sprint 3 review: final state, known limitations, demo readiness`n- [ ] Retrospective format: What went well / What to improve / Action items`n- [ ] Contribution table: teammate, issues owned, PRs opened, PRs reviewed, commits`n- [ ] All entries reflect actual Git history and GitHub activity (no fabrication)`n- [ ] Document stored in docs/SPRINT_REVIEWS.md`n`n## Story Points: 2 | Priority: P2 | Sprint: 3" `
    @("documentation","P2") @("gaganreddy4656") 3 | Out-Null

# 42 — Demo preparation: seed data and walkthrough script
New-GHIssue `
    "Prepare demo: seed data, end-to-end walkthrough, and screenshots" `
    "## Summary`nEnsure the application can be fully demonstrated from a fresh database using documented steps and realistic seed data.`n`n## Acceptance Criteria`n- [ ] schema.sql idempotent (CREATE IF NOT EXISTS, no errors on re-run)`n- [ ] Default admin credential documented (admin@vps.com / Admin@123 — change on first use)`n- [ ] Demo script: register, login, add vehicle, park, exit, receipt, admin dashboard`n- [ ] Each step references the exact URL and expected outcome`n- [ ] At least 3 UI screenshots included in docs/screenshots/`n- [ ] Known limitation: payment is simulated; clearly stated in UI and docs`n- [ ] Application runs with: mvn spring-boot:run + npm run`n`n## Story Points: 2 | Priority: P1 | Sprint: 3" `
    @("documentation","P1") @("Thrupthi-AM476") 3 | Out-Null

# 43 — Responsive design and accessibility basics
New-GHIssue `
    "Verify responsive layout and accessibility basics across all pages" `
    "## Summary`nEnsure the UI works on desktop, tablet, and mobile viewports, and meets basic accessibility requirements.`n`n## Acceptance Criteria`n- [ ] All pages usable at 375px (mobile), 768px (tablet), 1280px (desktop)`n- [ ] Navigation collapses to hamburger menu on small screens`n- [ ] Forms have <label> elements linked to inputs via htmlFor`n- [ ] Buttons and links have sufficient color contrast (WCAG AA target)`n- [ ] Error and success messages have role=alert or role=status`n- [ ] Confirmation dialogs have role=dialog and aria-modal`n- [ ] No horizontal scroll on any page at 375px`n`n## Story Points: 3 | Priority: P2 | Sprint: 3" `
    @("frontend","P2") @("Thrupthi-AM476") 3 | Out-Null

# 44 — Merge and integrate all feature branches
New-GHIssue `
    "Merge feat/fullstack-vps-development into main and open PR for review" `
    "## Summary`nIntegrate all work from the feature branch into main with a full PR description, linked issues, and verified CI.`n`n## Acceptance Criteria`n- [ ] feat/fullstack-vps-development rebased on main (or merged cleanly)`n- [ ] PR description lists all implemented features, test results, and linked issues`n- [ ] CI passes: 71 backend tests + frontend build`n- [ ] No merge conflicts`n- [ ] PR reviewed by at least one other team member`n- [ ] PR merged to main`n`n## Story Points: 1 | Priority: P0 | Sprint: 3" `
    @("integration","P0") @("Thrupthi-AM476") 3 | Out-Null

Write-Output "Done creating issues 39-44."
