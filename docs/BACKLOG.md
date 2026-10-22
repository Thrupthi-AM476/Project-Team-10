# Complete Issue Backlog & Sprint Plan — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656  
**Last updated:** October 2026

---

## Issue Inventory

### Open Issues: 27 | Closed Issues: 9 (6 duplicates + PR#24 + 2 others)

| # | Title | Assignee | Labels | SP | Priority | Milestone | Status |
|---|---|---|---|---|---|---|---|
| 3 | Vehicle registration | Thrupthi-AM476 | backend, security, P0 | 5 | P0 | Sprint 1 | Open |
| 10 | Parking slot management and availability | adik1611 | backend, frontend, P0 | 5 | P0 | Sprint 2 | Open |
| 11 | Parking allocation and entry ticket generation | Vikas75bit | backend, frontend, P0 | 5 | P0 | Sprint 2 | Open |
| 14 | Vehicle exit and parking fee calculation | Vikas75bit | backend, frontend, P0 | 5 | P0 | Sprint 2 | Open |
| 15 | Payment status and receipt generation | Vikas75bit | backend, frontend, P1 | 3 | P1 | Sprint 2 | Open |
| 16 | Parking rate configuration and reservations | adik1611 | backend, frontend, P1 | 8 | P1 | Sprint 2 | Open |
| 18 | Admin dashboard and report generation | gaganreddy4656 | backend, frontend, P1 | 5 | P1 | Sprint 3 | Open |
| 19 | Functional, security and non-functional testing | gaganreddy4656 | testing, security, P0 | 8 | P0 | Sprint 3 | Open |
| 20 | User registration, login and profile management | Thrupthi-AM476 | backend, security, P0 | 8 | P0 | Sprint 1 | Open |
| 25 | Fix parking availability and rate schedule mgmt | — | backend, bug, P0 | 2 | P0 | Sprint 2 | Open |
| 32 | Set up React + Vite frontend | Thrupthi-AM476 | frontend, P0 | 3 | P0 | Sprint 1 | Open |
| 33 | Login and registration pages | Thrupthi-AM476 | frontend, security, P0 | 3 | P0 | Sprint 1 | Open |
| 34 | Customer dashboard and profile page | Thrupthi-AM476 | frontend, P0 | 3 | P0 | Sprint 1 | Open |
| 35 | Vehicle registration, listing, management UI | Thrupthi-AM476 | frontend, P0 | 5 | P0 | Sprint 1 | Open |
| 36 | Parking availability grid | adik1611 | frontend, P0 | 3 | P0 | Sprint 2 | Open |
| 37 | Parking entry ticket UI | Vikas75bit | frontend, P0 | 3 | P0 | Sprint 2 | Open |
| 38 | Vehicle exit workflow and receipt UI | Vikas75bit | frontend, P0 | 3 | P0 | Sprint 2 | Open |
| 39 | Reservations page | adik1611 | frontend, P1 | 5 | P1 | Sprint 2 | Open |
| 40 | Staff dashboard | adik1611 | frontend, P1 | 5 | P1 | Sprint 2 | Open |
| 41 | Admin dashboard UI | gaganreddy4656 | frontend, P1 | 5 | P1 | Sprint 3 | Open |
| 42 | Verify database schema and seed data | gaganreddy4656 | database, backend, P0 | 2 | P0 | Sprint 1 | Open |
| 43 | GitHub Actions CI pipeline | gaganreddy4656 | testing, documentation, P1 | 2 | P1 | Sprint 3 | Open |
| 44 | Frontend-backend API integration | Thrupthi-AM476 | integration, frontend, backend, P0 | 3 | P0 | Sprint 3 | Open |
| 45 | README and setup documentation | gaganreddy4656 | documentation, P1 | 2 | P1 | Sprint 3 | Open |
| 46 | Security audit: RBAC and safe error responses | gaganreddy4656 | security, testing, P0 | 3 | P0 | Sprint 3 | Open |
| 47 | Sprint reviews, retrospectives, contributions | gaganreddy4656 | documentation, P2 | 2 | P2 | Sprint 3 | Open |
| 48 | Responsive layout and accessibility | Thrupthi-AM476 | frontend, accessibility, P2 | 3 | P2 | Sprint 3 | Open |
| 49 | Merge feature branch and open PR | Thrupthi-AM476 | integration, P0 | 1 | P0 | Sprint 3 | Open |

---

## Sprint Plan

### Sprint 1 — Foundation
**Goal:** Core backend working (auth, vehicles, DB), frontend setup and initial screens.

| # | Issue | Assignee | SP |
|---|---|---|---|
| 20 | User registration, login, profile management | Thrupthi-AM476 | 8 |
| 3 | Vehicle registration | Thrupthi-AM476 | 5 |
| 42 | Database schema and seed data | gaganreddy4656 | 2 |
| 32 | Frontend setup (Vite + React + Router) | Thrupthi-AM476 | 3 |
| 33 | Login and registration pages | Thrupthi-AM476 | 3 |
| 34 | Customer dashboard and profile page | Thrupthi-AM476 | 3 |
| 35 | Vehicle management UI | Thrupthi-AM476 | 5 |

**Sprint 1 Total: 29 SP**

| Teammate | Issues | SP |
|---|---|---|
| Thrupthi-AM476 | #20, #3, #32, #33, #34, #35 | 27 |
| gaganreddy4656 | #42 | 2 |
| adik1611 | — | 0 |
| Vikas75bit | — | 0 |

---

### Sprint 2 — Core Parking Workflows
**Goal:** All parking operations working end-to-end with frontend.

| # | Issue | Assignee | SP |
|---|---|---|---|
| 10 | Parking slot management and availability | adik1611 | 5 |
| 11 | Parking allocation and entry ticket | Vikas75bit | 5 |
| 14 | Vehicle exit and fee calculation | Vikas75bit | 5 |
| 15 | Payment status and receipt | Vikas75bit | 3 |
| 16 | Rate configuration and reservations | adik1611 | 8 |
| 25 | Fix: slot availability + rate endpoints | (any) | 2 |
| 36 | Parking availability grid UI | adik1611 | 3 |
| 37 | Parking entry ticket UI | Vikas75bit | 3 |
| 38 | Exit workflow and receipt UI | Vikas75bit | 3 |
| 39 | Reservations page UI | adik1611 | 5 |
| 40 | Staff dashboard UI | adik1611 | 5 |

**Sprint 2 Total: 47 SP**

| Teammate | Issues | SP |
|---|---|---|
| Thrupthi-AM476 | — | 0 |
| adik1611 | #10, #16, #36, #39, #40 | 26 |
| Vikas75bit | #11, #14, #15, #37, #38 | 19 |
| gaganreddy4656 | — | 0 |

---

### Sprint 3 — Completion, Testing & Quality
**Goal:** Admin dashboard, all remaining pages, full test coverage, CI, docs, PR merged.

| # | Issue | Assignee | SP |
|---|---|---|---|
| 18 | Admin dashboard and reports | gaganreddy4656 | 5 |
| 19 | Full test suite (71 tests) | gaganreddy4656 | 8 |
| 41 | Admin dashboard UI | gaganreddy4656 | 5 |
| 43 | GitHub Actions CI | gaganreddy4656 | 2 |
| 44 | Frontend-backend integration verification | Thrupthi-AM476 | 3 |
| 45 | README and setup documentation | gaganreddy4656 | 2 |
| 46 | Security audit | gaganreddy4656 | 3 |
| 47 | Sprint reviews and retrospectives | gaganreddy4656 | 2 |
| 48 | Responsive layout and accessibility | Thrupthi-AM476 | 3 |
| 49 | Merge PR and review | Thrupthi-AM476 | 1 |

**Sprint 3 Total: 34 SP**

| Teammate | Issues | SP |
|---|---|---|
| Thrupthi-AM476 | #44, #48, #49 | 7 |
| gaganreddy4656 | #18, #19, #41, #43, #45, #46, #47 | 27 |
| adik1611 | — | 0 |
| Vikas75bit | — | 0 |

---

## Overall Story-Point Summary

| Teammate | Sprint 1 | Sprint 2 | Sprint 3 | Total |
|---|---|---|---|---|
| Thrupthi-AM476 | 27 | 0 | 7 | 34 |
| adik1611 | 0 | 26 | 0 | 26 |
| Vikas75bit | 0 | 19 | 0 | 19 |
| gaganreddy4656 | 2 | 0 | 27 | 29 |
| **Total** | **29** | **47** | **34** | **110** |

**Note:** Thrupthi is the primary implementer and has done the bulk of the work on the feature branch. The sprint totals reflect the actual division of labor.

---

## Before-and-After: Original 9 Issues

| # | Original SP | Corrected SP | Reason for change |
|---|---|---|---|
| #20 | 5 | 8 | Complex: JWT, BCrypt, account lock, profile, 21 tests — five is too low |
| #3 | 3 | 5 | Ownership enforcement, plate normalization, 22 tests — three is too low |
| #10 | 5 | 5 | Appropriate — slot CRUD + public availability |
| #11 | 5 | 5 | Appropriate — entry flow with ownership and ticket generation |
| #14 | 5 | 5 | Appropriate — exit + fee calculation with grace period |
| #15 | 5 | 3 | Smaller scope: mock payment entity + receipt only — five is too high |
| #16 | 5 | 8 | Two distinct subsystems: rates + full reservation lifecycle — five is too low |
| #18 | 5 | 5 | Appropriate — dashboard + report |
| #19 | 5 | 8 | Comprehensive test coordination: 71 tests across 8 classes — five is too low |

---

## GitHub Projects Board — Manual Setup Instructions

The GitHub token lacks the `read:project` scope, so Projects V2 fields cannot be set programmatically. Follow these steps:

### Step 1: Add token scope
Go to **https://github.com/settings/tokens** → your token → add `project` scope → Save.

### Step 2: Open the board
Go to **https://github.com/users/Thrupthi-AM476/projects** (or the project link from your repo's Projects tab).

### Step 3: Add all open issues to the board
Click **+ Add items** → search for issues by number → add all 27 open issues.

### Step 4: Add an Iteration field (if not present)
Settings → Fields → **+ New field** → type: Iteration → name: Sprint  
Configure:
- Sprint 1: 2 weeks
- Sprint 2: 2 weeks  
- Sprint 3: 2 weeks

### Step 5: Add a Story Points field
Settings → Fields → **+ New field** → type: Number → name: Story Points

### Step 6: Set values for each issue
Use the table above. Set Status, Sprint (Iteration), Story Points, and Priority for each issue.

### Step 7: Create useful board views
1. **Backlog** — group by Priority; filter by Status = Backlog/Todo
2. **Sprint Board** — group by Status; filter by current sprint
3. **By Assignee** — group by Assignee
4. **Sprint Review** — group by Sprint; show completed vs open
