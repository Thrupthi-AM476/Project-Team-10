# Sprint Reviews and Retrospectives — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656  
**Official timeline:** Sprint 1 (Oct 19-23) · Sprint 2 (Oct 26-30)

---

## Sprint 1 — Working Product (Oct 19-23)

**Goal:** A demonstrable working parking system: register, login, manage vehicles, view parking availability, park a vehicle, exit and pay, view receipt. Includes CI pipeline, README, and an open PR for team review.

**Planned issues (18 open, 63 SP):**

| # | Issue | Assignee | SP | Status |
|---|---|---|---|---|
| 20 | User registration, login, profile | Thrupthi-AM476 | 8 | Code implemented on feature branch |
| 3 | Vehicle registration | Thrupthi-AM476 | 5 | Code implemented on feature branch |
| 42 | Database schema and seed data | gaganreddy4656 | 2 | Schema in src/database/schema.sql |
| 10 | Parking slot management | adik1611 | 5 | Code implemented on feature branch |
| 11 | Parking allocation and entry | Vikas75bit | 5 | Code implemented on feature branch |
| 14 | Vehicle exit and fee calculation | Vikas75bit | 5 | Code implemented on feature branch |
| 15 | Payment and receipt | Vikas75bit | 3 | Code implemented on feature branch |
| 25 | Fix slot availability and rate endpoints | — | 2 | Code implemented on feature branch |
| 32 | Frontend setup | Thrupthi-AM476 | 3 | Code implemented on feature branch |
| 33 | Login and registration pages | Thrupthi-AM476 | 3 | Code implemented on feature branch |
| 34 | Customer dashboard | Thrupthi-AM476 | 3 | Code implemented on feature branch |
| 35 | Vehicle management UI | Thrupthi-AM476 | 5 | Code implemented on feature branch |
| 36 | Parking availability grid | adik1611 | 3 | Code implemented on feature branch |
| 37 | Parking entry UI | Vikas75bit | 3 | Code implemented on feature branch |
| 38 | Exit workflow UI | Vikas75bit | 3 | Code implemented on feature branch |
| 43 | GitHub Actions CI | gaganreddy4656 | 2 | CI configured in .github/workflows/ci.yml |
| 45 | README and setup docs | gaganreddy4656 | 2 | README updated on feature branch |
| 49 | Merge PR for review | Thrupthi-AM476 | 1 | PR #50 open, awaiting review |

**Implementation status:** All code exists on branch `feat/fullstack-vps-development`. PR #50 is open. Backend tests: 71 passing, 0 failures. Frontend builds cleanly.

**Sprint 1 deliverables checklist (per teacher's requirements):**

| Deliverable | Status | Evidence |
|---|---|---|
| Working product | Implemented | 71 tests pass; frontend builds; code on feature branch |
| GitHub Actions for PRs/commits | Configured | .github/workflows/ci.yml (backend test + frontend build) |
| PRs raised by assigned members | Open | PR #50 from feat/fullstack-vps-development to main |
| Repository-owner reviews | **PENDING** | PR #50 awaiting review by Thrupthi-AM476 |
| 1-minute demonstration video | **NOT DONE** | Must be recorded and uploaded to repo |
| Deviation documentation | **NOT DONE** | Must document any deviations from SRS/SAD/Test Plan |

**Remaining Sprint 1 work:**
- [ ] Repository-owner must review and approve PR #50
- [ ] Record a 1-minute demo video and upload to the repository
- [ ] Document any deviations from the submitted SRS/SAD/Test Plan (e.g., payment is simulated, no ALPR)
- [ ] Add issues to the GitHub Projects V2 board and set Story Points/Sprint/Status fields (requires `project` token scope — see BACKLOG.md)

**Workload flag:** Thrupthi-AM476 has 28 SP (44% of sprint total). This is high. The existing implementation is already on the feature branch, so the sprint work is review and coordination, not new coding. If the team agrees, consider reassigning #34 (3 SP) to another member.

---

## Sprint 2 — Completion (Oct 26-30) — PLANNED, NOT STARTED

**Goal:** Reservations, admin dashboard, staff dashboard, comprehensive testing, security audit, responsive design, and sprint documentation.

**Planned issues (10 open, 47 SP):**

| # | Issue | Assignee | SP | Status |
|---|---|---|---|---|
| 16 | Rate configuration and reservations | adik1611 | 8 | Not started |
| 18 | Admin dashboard and reports | gaganreddy4656 | 5 | Not started |
| 19 | Full test suite | gaganreddy4656 | 8 | Not started |
| 39 | Reservations page UI | adik1611 | 5 | Not started |
| 40 | Staff dashboard | adik1611 | 5 | Not started |
| 41 | Admin dashboard UI | gaganreddy4656 | 5 | Not started |
| 44 | Frontend-backend integration verification | Thrupthi-AM476 | 3 | Not started |
| 46 | Security audit | gaganreddy4656 | 3 | Not started |
| 47 | Sprint reviews and retrospectives | gaganreddy4656 | 2 | Not started |
| 48 | Responsive layout and accessibility | Thrupthi-AM476 | 3 | Not started |

**Note:** Some of this code already exists on the feature branch from initial development. Sprint 2 work involves reviewing, testing, hardening, and formally completing these features — not building from scratch.

**Workload flag:** Vikas75bit has 0 SP planned. gaganreddy4656 has 23 SP (49%). Consider reassigning #46 (security audit, 3 SP) to Vikas75bit.

---

## Sprint 1 Retrospective (to be completed after Sprint 1 ends)

### What went well
- [To be filled in after Sprint 1 review meeting]

### What could be improved
- [To be filled in after Sprint 1 review meeting]

### Action items
- [To be filled in after Sprint 1 review meeting]

---

## Test Results (Current State)

```
cd src/backend && mvn test
Tests run: 71, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests | Coverage Focus |
|---|---|---|
| AuthServiceTest | 9 | Registration, login, account lock, role enforcement |
| VehicleServiceTest | 13 | CRUD, ownership, duplicate plate |
| ParkingServiceTest | 12 | Entry/exit ownership, fee calc, full parking |
| AuthControllerTest | 7 | HTTP status codes, role elevation blocked |
| UserControllerTest | 5 | Profile GET/PUT, no passwordHash |
| VehicleControllerTest | 9 | Ownership, plate normalization |
| ParkingControllerTest | 8 | Entry/exit/receipt, active session |
| AdminControllerTest | 8 | Dashboard, rates CRUD, report, 403 enforcement |

Frontend build: `npm run build` → 52 modules, 217KB JS (64KB gzipped), clean exit.
