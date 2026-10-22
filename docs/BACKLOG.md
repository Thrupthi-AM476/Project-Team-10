# Issue Backlog & Sprint Plan — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656  
**Official timeline:** Sprint 1 (Oct 19-23) · Sprint 2 (Oct 26-30)

---

## SRS Requirement Mapping

Issues are mapped to genuine functional requirements from the submitted SRS:

| SRS Req ID | Description | Issue(s) |
|---|---|---|
| VPS-F-001 | License plate capture at entry | #11, #37 |
| VPS-F-002 | Slot assignment and occupancy | #11 |
| VPS-F-003 | Ticket generation | #11, #37 |
| VPS-F-004 | Real-time slot availability | #10, #36 |
| VPS-F-005 | Slot categorisation | #10 |
| VPS-F-006 | Block entry when full | #11 |
| VPS-F-007 | Parking fee calculation | #14, #38 |
| VPS-F-008 | Payment processing | #15, #38 |
| VPS-F-009 | Receipt generation | #15, #38 |
| VPS-F-010 | Slot release on exit | #14 |
| VPS-F-011 | Advance reservation | #16, #39 |
| VPS-F-012 | Auto-release expired reservation | #16 |
| VPS-F-013 | Admin occupancy dashboard | #18, #41 |
| VPS-F-014 | Rate schedule configuration | #16 |
| VPS-F-015 | Report export | #18 |
| VPS-SR-002 | Password complexity | #20 |
| VPS-SR-003 | RBAC enforcement | #20, #46 |
| VPS-SR-004 | Account lockout (5 attempts) | #20 |

---

## Sprint 1 — Working Product (Oct 19-23)

**Goal:** A demonstrable working parking system with auth, vehicle management, parking entry/exit, fee calculation, receipt, frontend UI, CI pipeline, README, and an open PR for review.

**Milestone:** Sprint 1 (Oct 19-23) — due October 23, 2026

| # | Issue | Assignee | Labels | SP | SRS Req |
|---|---|---|---|---|---|
| 20 | User registration, login and profile management | Thrupthi-AM476 | backend, security, P0 | 8 | VPS-SR-002/003/004 |
| 3 | Vehicle registration | Thrupthi-AM476 | backend, security, P0 | 5 | — |
| 42 | Verify database schema and seed data | gaganreddy4656 | database, backend, P0 | 2 | — |
| 10 | Parking slot management and availability | adik1611 | backend, frontend, P0 | 5 | VPS-F-004/005 |
| 11 | Parking allocation and entry ticket generation | Vikas75bit | backend, frontend, P0 | 5 | VPS-F-001/002/003/006 |
| 14 | Vehicle exit and parking fee calculation | Vikas75bit | backend, frontend, P0 | 5 | VPS-F-007/010 |
| 15 | Payment status and receipt generation | Vikas75bit | backend, frontend, P1 | 3 | VPS-F-008/009 |
| 25 | Fix parking availability and rate schedule mgmt | — | backend, bug, P0 | 2 | — |
| 32 | Set up React + Vite frontend | Thrupthi-AM476 | frontend, P0 | 3 | — |
| 33 | Login and registration pages | Thrupthi-AM476 | frontend, security, P0 | 3 | — |
| 34 | Customer dashboard and profile page | Thrupthi-AM476 | frontend, P0 | 3 | — |
| 35 | Vehicle registration, listing, management UI | Thrupthi-AM476 | frontend, P0 | 5 | — |
| 36 | Parking availability grid UI | adik1611 | frontend, P0 | 3 | VPS-F-004 |
| 37 | Parking entry ticket UI | Vikas75bit | frontend, P0 | 3 | VPS-F-003 |
| 38 | Vehicle exit workflow and receipt UI | Vikas75bit | frontend, P0 | 3 | VPS-F-009 |
| 43 | GitHub Actions CI pipeline | gaganreddy4656 | testing, documentation, P1 | 2 | — |
| 45 | README and setup documentation | gaganreddy4656 | documentation, P1 | 2 | — |
| 49 | Merge feature branch and open PR | Thrupthi-AM476 | integration, P0 | 1 | — |

**Sprint 1 total: 63 SP across 18 issues**

| Teammate | Issues | SP | Notes |
|---|---|---|---|
| Thrupthi-AM476 | #20, #3, #32, #33, #34, #35, #49 | 28 | **OVERLOAD FLAGGED** — 28 SP in 5 days is high |
| adik1611 | #10, #36 | 8 | |
| Vikas75bit | #11, #14, #15, #37, #38 | 19 | |
| gaganreddy4656 | #42, #43, #45 | 6 | |
| Unassigned | #25 | 2 | |

**Workload concern:** Thrupthi-AM476 carries 28 SP (44% of sprint). Consider reassigning #34 (3 SP) or #35 (5 SP) to adik1611 or Vikas75bit if they have capacity. The existing implementation is already on the feature branch, so the sprint work is primarily review, testing, and PR coordination rather than new implementation.

---

## Sprint 2 — Completion (Oct 26-30)

**Goal:** Reservations, admin dashboard, staff dashboard, comprehensive testing, security audit, sprint documentation, responsive design.

**Milestone:** Sprint 2 (Oct 26-30) — due October 30, 2026

| # | Issue | Assignee | Labels | SP | SRS Req |
|---|---|---|---|---|---|
| 16 | Parking rate configuration and reservations | adik1611 | backend, frontend, P1 | 8 | VPS-F-011/012/014 |
| 18 | Admin dashboard and report generation | gaganreddy4656 | backend, frontend, P1 | 5 | VPS-F-013/015 |
| 19 | Functional, security and non-functional testing | gaganreddy4656 | testing, security, P0 | 8 | VPS-SR-003 |
| 39 | Reservations page UI | adik1611 | frontend, P1 | 5 | VPS-F-011 |
| 40 | Staff dashboard for slot management | adik1611 | frontend, P1 | 5 | VPS-F-005 |
| 41 | Admin dashboard UI | gaganreddy4656 | frontend, P1 | 5 | VPS-F-013 |
| 44 | Frontend-backend API integration verification | Thrupthi-AM476 | integration, P0 | 3 | — |
| 46 | Security audit: RBAC and safe error responses | gaganreddy4656 | security, testing, P0 | 3 | VPS-SR-003 |
| 47 | Sprint reviews, retrospectives, contributions | gaganreddy4656 | documentation, P2 | 2 | — |
| 48 | Responsive layout and accessibility | Thrupthi-AM476 | frontend, accessibility, P2 | 3 | VPS-NF-005 |

**Sprint 2 total: 47 SP across 10 issues**

| Teammate | Issues | SP | Notes |
|---|---|---|---|
| Thrupthi-AM476 | #44, #48 | 6 | |
| adik1611 | #16, #39, #40 | 18 | |
| Vikas75bit | — | 0 | **UNDERUTILIZED** — consider assigning #46 (3 SP) |
| gaganreddy4656 | #18, #19, #41, #46, #47 | 23 | Heavy load — consider splitting #19 |

**Workload concern:** Vikas75bit has 0 SP planned. gaganreddy4656 has 23 SP (49% of sprint). Consider reassigning #46 (security audit, 3 SP) from gaganreddy4656 to Vikas75bit for balance.

---

## Overall Story-Point Summary

| Teammate | Sprint 1 | Sprint 2 | Total |
|---|---|---|---|
| Thrupthi-AM476 | 28 | 6 | 34 |
| adik1611 | 8 | 18 | 26 |
| Vikas75bit | 19 | 0 | 19 |
| gaganreddy4656 | 6 | 23 | 29 |
| Unassigned | 2 | 0 | 2 |
| **Sprint total** | **63** | **47** | **110** |

---

## Before-and-After: Original 9 Issues

| # | Original SP | Corrected SP | Sprint | Reason for SP change |
|---|---|---|---|---|
| #20 | 5 | 8 | 1 | JWT + BCrypt + account lock + profile = complex (21 tests) |
| #3 | 3 | 5 | 1 | Ownership enforcement + plate normalization (22 tests) |
| #10 | 5 | 5 | 1 | Appropriate |
| #11 | 5 | 5 | 1 | Appropriate |
| #14 | 5 | 5 | 1 | Appropriate |
| #15 | 5 | 3 | 1 | Mock-only payment; smaller than estimated |
| #16 | 5 | 8 | 2 | Two subsystems: rates + full reservation lifecycle |
| #18 | 5 | 5 | 2 | Appropriate |
| #19 | 5 | 8 | 2 | 71 tests across 8 classes; test coordination is real work |

---

## GitHub Projects Board — Why Changes Aren't Visible

### The problem

The GitHub token used for API calls has scopes `repo`, `gist`, `workflow` but **NOT `read:project`** or `project`. This means:

1. **Milestones were set successfully** — milestones are a repo-level feature accessible with `repo` scope. You can see them at https://github.com/Thrupthi-AM476/Project-Team-10/milestones

2. **Issues were NOT added to the Projects V2 board** — Projects V2 is a separate API that requires the `project` scope. No issues were added to any project board.

3. **Custom fields (Story Points, Iteration, Status) were never set** — these are Projects V2 custom fields, not issue properties. They only exist when an issue is added as an item to a project board.

### The fix

**Step 1:** Add the `project` scope to your GitHub token:
- Go to https://github.com/settings/tokens
- Edit your personal access token
- Check the `project` scope (or `read:project` + `write:project`)
- Save

**Step 2:** Run this to refresh gh CLI auth:
```
gh auth refresh -s project
```

**Step 3:** Add all open issues to your Projects V2 board:
- Open your project at https://github.com/users/Thrupthi-AM476/projects
- Click `+ Add items` → add issues #3, #10, #11, #14, #15, #16, #18, #19, #20, #25, #32-#49

**Step 4:** Create custom fields on the board:
- **Story Points** (Number field)
- **Sprint** (Iteration field with Sprint 1: Oct 19-23 and Sprint 2: Oct 26-30)
- **Priority** (Single select: P0, P1, P2)
- **Status** (Single select: Todo, In Progress, In Review, Done)

**Step 5:** Set values for each issue using the table above (Sprint column = Iteration, SP column = Story Points).

**Step 6:** Create board views:
1. **Backlog** — group by Priority
2. **Sprint Board** — group by Status, filter by current sprint
3. **By Assignee** — group by Assignee
4. **Sprint Review** — group by Sprint

---

## Closed Issues

| # | Title | Reason |
|---|---|---|
| 24 | feat: secure auth, profile mgmt, vehicle ownership | PR merged |
| 26-31 | Frontend issues (6 duplicates) | Created in error by failed script; superseded by #32-#38 |
