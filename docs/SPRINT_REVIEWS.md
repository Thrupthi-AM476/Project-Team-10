# Sprint Reviews and Retrospectives — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656  
**Official timeline:** Sprint 1 (Oct 19-23) · Sprint 2 (Oct 26-30)  
**Project board:** https://github.com/users/Thrupthi-AM476/projects/2

---

## Sprint 1 — Foundation (Oct 19-23)

**Goal:** Working foundation — auth, vehicle management, frontend, database, CI, README, open PR.

**Planned issues (10 open, 34 SP):**

| # | Issue | Assignee | SP | Status |
|---|---|---|---|---|
| 20 | User registration, login, profile | Thrupthi-AM476 | 8 | Code on feature branch |
| 3 | Vehicle registration | Thrupthi-AM476 | 5 | Code on feature branch |
| 32 | Frontend setup (Vite + React) | Thrupthi-AM476 | 3 | Code on feature branch |
| 33 | Login and registration pages | Thrupthi-AM476 | 3 | Code on feature branch |
| 34 | Customer dashboard and profile | Thrupthi-AM476 | 3 | Code on feature branch |
| 35 | Vehicle management UI | Thrupthi-AM476 | 5 | Code on feature branch |
| 42 | Database schema and seed data | gaganreddy4656 | 2 | Schema in src/database/schema.sql |
| 43 | GitHub Actions CI | gaganreddy4656 | 2 | CI in .github/workflows/ci.yml |
| 45 | README and setup docs | gaganreddy4656 | 2 | README on feature branch |
| 49 | Merge PR for review | Thrupthi-AM476 | 1 | PR #50 open |

**Sprint 1 deliverables checklist (per teacher's requirements):**

| Deliverable | Status | Evidence |
|---|---|---|
| Working product (foundation) | Implemented | 71 tests pass; frontend builds; code on branch |
| GitHub Actions for PRs/commits | Configured | .github/workflows/ci.yml |
| PRs raised by assigned members | Open | PR #50 from feat/fullstack-vps-development |
| Repository-owner reviews | **PENDING** | PR #50 awaiting review |
| 1-minute demo video | **NOT DONE** | Must record and upload |
| Deviation documentation | **NOT DONE** | Must document deviations from SRS/SAD/Test Plan |

**Workload flag:** Thrupthi-AM476 has 28 of 34 SP (82%). This is a significant overload. The existing implementation is already on the feature branch, so the sprint work is primarily review and coordination. Consider reassigning #34 (3 SP, customer dashboard) to adik1611 or Vikas75bit if they have capacity.

---

## Sprint 2 — Completion (Oct 26-30) — PLANNED, NOT STARTED

**Goal:** Parking workflows, admin/staff dashboards, testing, security, documentation, demo, development freeze.

**Planned issues (17 open, 76 SP):**

| # | Issue | Assignee | SP |
|---|---|---|---|
| 10 | Parking slot management and availability | adik1611 | 5 |
| 11 | Parking allocation and entry ticket | Vikas75bit | 5 |
| 14 | Vehicle exit and fee calculation | Vikas75bit | 5 |
| 15 | Payment status and receipt | Vikas75bit | 3 |
| 16 | Rate configuration and reservations | adik1611 | 8 |
| 18 | Admin dashboard and reports | gaganreddy4656 | 5 |
| 19 | Full test suite | gaganreddy4656 | 8 |
| 36 | Parking availability grid UI | adik1611 | 3 |
| 37 | Parking entry ticket UI | Vikas75bit | 3 |
| 38 | Exit workflow and receipt UI | Vikas75bit | 3 |
| 39 | Reservations page UI | adik1611 | 5 |
| 40 | Staff dashboard | adik1611 | 5 |
| 41 | Admin dashboard UI | gaganreddy4656 | 5 |
| 44 | Frontend-backend integration | Thrupthi-AM476 | 3 |
| 46 | Security audit | gaganreddy4656 | 3 |
| 47 | Sprint reviews and docs | gaganreddy4656 | 2 |
| 48 | Responsive layout and a11y | Thrupthi-AM476 | 3 |

**Note:** Some code already exists on the feature branch from initial development. Sprint 2 work involves reviewing, testing, hardening, and formally completing these features.

**Workload flag:** Vikas75bit has 0 SP in Sprint 1 and 19 SP in Sprint 2. gaganreddy4656 has 23 SP in Sprint 2 (30%). Consider reassigning #46 (security audit, 3 SP) to Vikas75bit for better balance.

---

## Sprint 1 Retrospective (to be completed after Oct 23)

### What went well
- [To be filled after sprint review]

### What could be improved
- [To be filled after sprint review]

### Action items
- [To be filled after sprint review]

---

## Test Results (Current)

```
mvn test — 71 tests, 0 failures, 0 errors, BUILD SUCCESS
npm run build — 52 modules, 217KB JS (64KB gzip), clean exit
```
