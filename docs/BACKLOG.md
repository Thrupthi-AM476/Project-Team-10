# Issue Backlog & Sprint Plan — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656  
**Official timeline:** Sprint 1 (Oct 19-23) · Sprint 2 (Oct 26-30)  
**Project board:** https://github.com/users/Thrupthi-AM476/projects/2

---

## SRS Requirement Mapping

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

## Sprint 1 — Foundation (Oct 19-23)

**Goal:** Working foundation: auth, vehicle management, frontend, database, CI, README, open PR.  
**Milestone:** Sprint 1 (Oct 19-23) — due October 23, 2026  
**Sprint start:** October 19, 2026 · **Sprint end:** October 23, 2026

| # | Issue | Assignee | SP | SRS Req |
|---|---|---|---|---|
| 20 | User registration, login and profile management | Thrupthi-AM476 | 8 | VPS-SR-002/003/004 |
| 3 | Vehicle registration | Thrupthi-AM476 | 5 | — |
| 32 | Set up React + Vite frontend | Thrupthi-AM476 | 3 | — |
| 33 | Login and registration pages | Thrupthi-AM476 | 3 | — |
| 34 | Customer dashboard and profile page | Thrupthi-AM476 | 3 | — |
| 35 | Vehicle registration, listing, management UI | Thrupthi-AM476 | 5 | — |
| 42 | Verify database schema and seed data | gaganreddy4656 | 2 | — |
| 43 | GitHub Actions CI pipeline | gaganreddy4656 | 2 | — |
| 45 | README and setup documentation | gaganreddy4656 | 2 | — |
| 49 | Merge feature branch and open PR | Thrupthi-AM476 | 1 | — |

**Sprint 1 total: 34 SP across 10 issues**

| Teammate | SP | Notes |
|---|---|---|
| Thrupthi-AM476 | 28 | **OVERLOAD FLAGGED** — 82% of sprint; consider reassigning #34 (3 SP) |
| gaganreddy4656 | 6 | |
| adik1611 | 0 | No Sprint 1 work — starts in Sprint 2 |
| Vikas75bit | 0 | No Sprint 1 work — starts in Sprint 2 |

---

## Sprint 2 — Completion (Oct 26-30)

**Goal:** Parking workflows, admin/staff dashboards, testing, security, documentation, demo.  
**Milestone:** Sprint 2 (Oct 26-30) — due October 30, 2026  
**Sprint start:** October 26, 2026 · **Sprint end:** October 30, 2026

| # | Issue | Assignee | SP | SRS Req |
|---|---|---|---|---|
| 10 | Parking slot management and availability | adik1611 | 5 | VPS-F-004/005 |
| 11 | Parking allocation and entry ticket generation | Vikas75bit | 5 | VPS-F-001/002/003/006 |
| 14 | Vehicle exit and parking fee calculation | Vikas75bit | 5 | VPS-F-007/010 |
| 15 | Payment status and receipt generation | Vikas75bit | 3 | VPS-F-008/009 |
| 16 | Parking rate configuration and reservations | adik1611 | 8 | VPS-F-011/012/014 |
| 18 | Admin dashboard and report generation | gaganreddy4656 | 5 | VPS-F-013/015 |
| 19 | Functional, security and non-functional testing | gaganreddy4656 | 8 | VPS-SR-003 |
| 36 | Parking availability grid UI | adik1611 | 3 | VPS-F-004 |
| 37 | Parking entry ticket UI | Vikas75bit | 3 | VPS-F-003 |
| 38 | Vehicle exit workflow and receipt UI | Vikas75bit | 3 | VPS-F-009 |
| 39 | Reservations page UI | adik1611 | 5 | VPS-F-011 |
| 40 | Staff dashboard for slot management | adik1611 | 5 | VPS-F-005 |
| 41 | Admin dashboard UI | gaganreddy4656 | 5 | VPS-F-013 |
| 44 | Frontend-backend API integration verification | Thrupthi-AM476 | 3 | — |
| 46 | Security audit: RBAC and safe error responses | gaganreddy4656 | 3 | VPS-SR-003 |
| 47 | Sprint reviews, retrospectives, contributions | gaganreddy4656 | 2 | — |
| 48 | Responsive layout and accessibility | Thrupthi-AM476 | 3 | VPS-NF-005 |

**Sprint 2 total: 76 SP across 17 issues**

| Teammate | SP | Notes |
|---|---|---|
| adik1611 | 26 | |
| Vikas75bit | 19 | |
| gaganreddy4656 | 23 | Heavy — consider splitting #19 (8 SP) |
| Thrupthi-AM476 | 6 | |

---

## Overall Story-Point Summary

| Teammate | Sprint 1 | Sprint 2 | Total |
|---|---|---|---|
| Thrupthi-AM476 | 28 | 6 | 34 |
| adik1611 | 0 | 26 | 26 |
| Vikas75bit | 0 | 19 | 19 |
| gaganreddy4656 | 6 | 23 | 29 |
| **Sprint total** | **34** | **76** | **110** |

---

## Story-Point Estimation Rationale

| # | SP | Rationale |
|---|---|---|
| 20 | 8 | JWT auth, BCrypt, account lock, profile, 21 tests — cross-component complexity |
| 3 | 5 | Ownership enforcement, plate normalization, full CRUD, 22 tests |
| 10 | 5 | Slot CRUD, public availability, 20 seed slots, staff RBAC |
| 11 | 5 | Entry flow, ownership check, ticket generation, full-lot handling |
| 14 | 5 | Exit flow, fee calc (grace period, rate lookup), slot release |
| 15 | 3 | Mock payment entity, receipt, duplicate prevention — limited scope |
| 16 | 8 | Two subsystems: rate CRUD + full reservation lifecycle with conflict check |
| 18 | 5 | Dashboard stats, date-filtered report, revenue calc |
| 19 | 8 | 71 tests across 8 classes; test coordination and security boundary testing |
| 32 | 3 | Vite setup, routing, CSS, component library |
| 33 | 3 | Login/register forms with validation and error handling |
| 34 | 3 | Dashboard stats cards, profile view/edit |
| 35 | 5 | Vehicle CRUD UI with ownership, confirmation dialogs, plate normalization |
| 36 | 3 | Color-coded slot grid with real-time refresh |
| 37 | 3 | Entry form with vehicle selector and ticket display |
| 38 | 3 | Exit form, fee display, receipt card |
| 39 | 5 | Reservation form with datetime pickers, conflict handling, cancel |
| 40 | 5 | Staff slot table, add/edit/disable, vehicle overview |
| 41 | 5 | Admin stat cards, rate table, date-filtered report |
| 42 | 2 | Verify schema matches entities, seed data present |
| 43 | 2 | CI workflow YAML, Java/Node setup |
| 44 | 3 | Verify all API paths, CORS, error handling, auth flow |
| 45 | 2 | README with setup, API docs, demo walkthrough |
| 46 | 3 | RBAC boundary checks, safe error response verification |
| 47 | 2 | Sprint review and retrospective documentation |
| 48 | 3 | Responsive breakpoints, accessibility labels, keyboard nav |
| 49 | 1 | Open PR, link issues, verify CI |

---

## Duplicate Issues

| # | Title | Canonical issue | Reason closed |
|---|---|---|---|
| 26 | Set up React + Vite frontend | #32 | Identical title and scope; created by failed script |
| 27 | Build customer dashboard | #34 | Identical; created by failed script |
| 28 | Build vehicle management UI | #35 | Identical; created by failed script |
| 29 | Build parking availability grid | #36 | Identical; created by failed script |
| 30 | Build parking entry UI | #37 | Identical; created by failed script |
| 31 | Build vehicle exit UI | #38 | Identical; created by failed script |

---

## GitHub Projects V2 Board — Status

**Project URL:** https://github.com/users/Thrupthi-AM476/projects/2

### What was done via API (verified)
- Milestones created with correct titles and due dates ✅
- All 27 open issues assigned to correct milestone (Sprint 1 or Sprint 2) ✅
- All issues have labels, assignees, and bodies with story points ✅
- Sprint 3 milestone closed and renamed ✅
- 6 duplicate issues closed with `duplicate` label ✅

### What could NOT be done (token scope limitation)
The GitHub token has scopes `repo`, `gist`, `workflow` but **NOT `read:project`** or `project`. GitHub Projects V2 uses a separate GraphQL API that requires the `project` scope. The following could NOT be done:

- Issues have NOT been added as items to the Projects V2 board ❌
- Story Points custom field NOT set on board ❌
- Sprint/Iteration field NOT configured on board ❌
- Status field NOT set on board ❌
- Sprint Start/End date fields NOT created on board ❌

### Manual steps to complete the board

**Step 1:** Add `project` scope to your token at https://github.com/settings/tokens

**Step 2:** Open the project at https://github.com/users/Thrupthi-AM476/projects/2

**Step 3:** Click `+ Add items` → add all 27 open issues (#3, #10, #11, #14, #15, #16, #18, #19, #20, #32-#49)

**Step 4:** Create custom fields:
- **Story Points** (Number) — values from the SP column in the tables above
- **Sprint** (Single select: "Sprint 1", "Sprint 2")
- **Sprint Start** (Date) — Sprint 1: 2026-10-19, Sprint 2: 2026-10-26
- **Sprint End** (Date) — Sprint 1: 2026-10-23, Sprint 2: 2026-10-30
- **Status** (Single select: "Todo", "In Progress", "Done")
- **Priority** (Single select: "P0", "P1", "P2")

**Step 5:** Set values for each issue using the tables above.

**Step 6:** Create board views:
1. **Backlog** — group by Priority
2. **Sprint Board** — group by Status, filter by current sprint
3. **By Assignee** — group by Assignee
4. **Sprint Review** — group by Sprint

**Note on GitHub Iteration field:** GitHub's native Iteration field uses fixed-duration intervals and may not support a Monday-Friday sprint with a weekend gap. If you use it, configure it as closely as possible and use the Sprint Start/End date fields for exact dates. The limitation should be documented.
