# GitHub Issue Backlog & Sprint Plan — Vehicle Parking System (Team 10)

This document defines the complete issue backlog, sprint assignments, and story-point estimates. Use the GitHub CLI (`gh issue create`) or the GitHub web UI to create these issues in the repository.

Labels: `frontend`, `backend`, `testing`, `documentation`, `security`, `bug`, `enhancement`

---

## Existing Issues (preserve)

| # | Title | Status |
|---|---|---|
| 3 | Vehicle registration | Open / In PR #24 |
| 10 | Parking slot management and availability | Open |
| 11 | Parking allocation and entry ticket generation | Open |
| 14 | Vehicle exit and parking fee calculation | Open |
| 15 | Payment status and receipt generation | Open |
| 16 | Parking rate configuration and reservations | Open |
| 18 | Admin dashboard and report generation | Open |
| 19 | Functional, security, and non-functional requirement testing | Open |
| 20 | User registration, login, and profile management | Open / In PR #24 |

---

## Sprint 1 — Foundation (Backend Core + Auth)

| Issue | Title | Labels | Points | Owner |
|---|---|---|---|---|
| 20 | User registration, login, and profile management | backend, security | 8 | Thrupthi |
| 3 | Vehicle registration | backend, security | 5 | Thrupthi |
| 21 | Database schema and JPA entity setup | backend | 3 | Team |
| 22 | Spring Security JWT configuration | backend, security | 5 | Team |
| 23 | Global exception handler and API response wrapper | backend | 2 | Team |
| 24 | Parking slot entity, repository, and seed data | backend | 2 | Team |

**Sprint 1 Goal:** Core authentication, vehicle management, and database foundation working with automated tests.

---

## Sprint 2 — Core Parking Features

| Issue | Title | Labels | Points | Owner |
|---|---|---|---|---|
| 10 | Parking slot management and availability | backend | 5 | Team |
| 11 | Parking allocation and entry ticket generation | backend | 8 | Team |
| 14 | Vehicle exit and parking fee calculation | backend | 5 | Team |
| 15 | Payment status and receipt generation | backend | 3 | Team |
| 16 | Parking rate configuration and reservations | backend | 8 | Team |
| 18 | Admin dashboard and report generation | backend | 5 | Team |
| 25 | Parking entry/exit ownership enforcement and RBAC | backend, security | 3 | Thrupthi |
| 26 | Rate schedule CRUD endpoints (VPS-F-014) | backend | 3 | Team |

**Sprint 2 Goal:** All core parking workflows (entry, exit, fee, payment, reservations, admin) functional with authorization.

---

## Sprint 3 — Frontend & Integration

| Issue | Title | Labels | Points | Owner |
|---|---|---|---|---|
| 27 | Frontend project setup (Vite + React + Router) | frontend | 3 | Thrupthi |
| 28 | API client with JWT auth and centralized error handling | frontend | 3 | Thrupthi |
| 29 | Landing page with branding and navigation | frontend | 2 | Thrupthi |
| 30 | Registration and login pages with validation | frontend, security | 5 | Thrupthi |
| 31 | Customer dashboard with vehicles and active session | frontend | 5 | Thrupthi |
| 32 | Vehicle management UI (add, edit, deactivate) | frontend | 5 | Thrupthi |
| 33 | Parking availability grid view | frontend | 3 | Thrupthi |
| 34 | Parking entry ticket view | frontend | 3 | Thrupthi |
| 35 | Exit, payment, and receipt view | frontend | 3 | Thrupthi |
| 36 | Reservation creation and listing UI | frontend | 5 | Thrupthi |
| 37 | User profile view and edit | frontend | 2 | Thrupthi |
| 38 | Staff dashboard — slot management | frontend | 5 | Thrupthi |
| 39 | Admin dashboard — stats, reports, rate config | frontend | 8 | Thrupthi |
| 40 | Role-aware navigation and protected routes | frontend, security | 3 | Thrupthi |
| 41 | CORS configuration and frontend-backend integration | backend, frontend | 2 | Team |

**Sprint 3 Goal:** Complete React frontend connected to backend with all screens and role-based navigation.

---

## Sprint 4 — Testing, CI, and Delivery

| Issue | Title | Labels | Points | Owner |
|---|---|---|---|---|
| 19 | Functional, security, and NFR testing | testing | 8 | Team |
| 42 | Backend unit tests for parking, admin, and reservation services | testing | 5 | Thrupthi |
| 43 | Backend integration tests for parking and admin endpoints | testing | 5 | Thrupthi |
| 44 | Authorization and ownership test coverage | testing, security | 5 | Thrupthi |
| 45 | GitHub Actions CI — backend test and frontend build | testing | 2 | Team |
| 46 | README, setup instructions, and API documentation | documentation | 3 | Thrupthi |
| 47 | Sprint review and retrospective documentation | documentation | 2 | Team |
| 48 | Responsive design and accessibility review | frontend, testing | 3 | Thrupthi |
| 49 | Final integration testing and demo preparation | testing | 3 | Team |

**Sprint 4 Goal:** Full test coverage, CI pipeline, documentation complete, demo-ready application.

---

## Story-Point Summary

| Sprint | Total Points | Issues |
|---|---|---|
| Sprint 1 | 25 | 6 |
| Sprint 2 | 37 | 7 |
| Sprint 3 | 59 | 15 |
| Sprint 4 | 34 | 8 |
| **Total** | **155** | **36** |

## Assignments

Work is distributed across team members. Issues marked "Team" are shared or to be assigned by the team lead. Issues marked with a name have an agreed owner.

---

## GitHub Project Board Configuration

**Project:** Vehicle Parking System – Team 10

### Views to configure
1. **Product Backlog** — all issues, grouped by label
2. **Sprint Board** — grouped by Status (Todo, In Progress, In Review, Done)
3. **By Assignee** — grouped by assignee
4. **Sprint Planning** — grouped by Sprint field
5. **Review/Testing** — issues with `testing` or `In Review` status

### Fields
- **Status:** Todo, In Progress, In Review, Done
- **Sprint:** Sprint 1, Sprint 2, Sprint 3, Sprint 4
- **Story Points:** numeric (Fibonacci: 1, 2, 3, 5, 8, 13)

### Labels
`frontend`, `backend`, `testing`, `documentation`, `security`, `bug`, `enhancement`

---

## Commands to Create Issues (if gh CLI is available)

```bash
# Example — create an issue
gh issue create --title "Frontend project setup (Vite + React + Router)" \
  --label "frontend" \
  --body "- [ ] Initialize Vite + React project
- [ ] Configure React Router
- [ ] Set up project structure
- [ ] Create .env.example"

# Create labels
gh label create frontend --color 1D76DB
gh label create backend --color 0E8A16
gh label create testing --color C5DEF5
gh label create documentation --color 0075CA
gh label create security --color D93F0B
gh label create bug --color D73A4A
gh label create enhancement --color A2EEEF
```
