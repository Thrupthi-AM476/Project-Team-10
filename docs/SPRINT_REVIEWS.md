# Sprint Reviews and Retrospectives — Vehicle Parking System (Team 10)

**Repository:** https://github.com/Thrupthi-AM476/Project-Team-10  
**Team:** Thrupthi-AM476 · adik1611 · Vikas75bit · gaganreddy4656

---

## Sprint 1 — Core Application Foundation

**Goal:** Establish backend core (auth, vehicles, database, security) and frontend project scaffolding.

**Planned issues:** #20, #3, #42, #26, #27, #28, #29 (32 story points)

**Completed:**
- ✅ #20 — User registration, login, profile management (backend + tests)
- ✅ #3 — Vehicle registration with ownership enforcement (backend + tests)
- ✅ #42 — Database schema, seed data, H2 test config
- ✅ #26 — React + Vite frontend: components, API client, AuthContext, routing
- ✅ #27 — Login and registration pages
- ✅ #28 — Customer dashboard and profile page
- ✅ #29 — Vehicle management UI

**Key decisions made:**
- Role is always hardcoded to CUSTOMER on registration (no privilege escalation possible)
- H2 in-memory database used for tests; MySQL for production
- BCrypt password hashing with Spring Security's `PasswordEncoder`
- JWT with 24-hour expiry; no refresh token (academic scope)

**Blockers:** None — all Sprint 1 items delivered

---

## Sprint 2 — Parking Workflows

**Goal:** Complete parking entry/exit/fee, slot management, reservations, and payment; connect frontend.

**Planned issues:** #10, #11, #14, #15, #16, #25, #30, #31, #32, #37, #38, #39, #40 (49 story points)

**Completed:**
- ✅ #10 — Parking slot management endpoints (create/update/deactivate for staff)
- ✅ #11 — Parking entry with ownership enforcement, ticket generation
- ✅ #14 — Exit workflow: fee calculation (grace period, rate schedule), slot release
- ✅ #15 — Payment entity (mock SUCCESS); receipt in exit response
- ✅ #16 — Rate schedule CRUD (admin); reservations with conflict prevention
- ✅ #25 — Security fix: public slot availability endpoint, efficient report query
- ✅ #30 — Parking availability grid (color-coded, real-time)
- ✅ #31 — Parking entry/ticket UI
- ✅ #32 — Exit workflow and receipt UI
- ✅ #37 — Reservation create/list/cancel UI
- ✅ #38 — Staff dashboard with slot management
- ✅ #39 — Reservation form with datetime pickers
- ✅ #40 — Staff vehicle overview

**In Progress / Carry-forward:**
- 🔄 #40 — Staff dashboard refinements (vehicle listing table pagination)

**Blockers:**
- npm install required `strict-ssl=false` workaround due to corporate certificate (documented)

---

## Sprint 3 — Completion and Quality

**Goal:** Admin dashboard, remaining pages, full test coverage, CI, documentation, and demo readiness.

**Planned issues:** #18, #19, #35, #41, #43, #44, #45, #46, #47, #48, #49 (30 story points)

**Completed:**
- ✅ #18 — Admin dashboard: real stats, revenue, rate config, date-filtered reports
- ✅ #19 — Full test suite: 71 tests, 0 failures (AuthService×9, VehicleService×13, ParkingService×12, AuthController×7, UserController×5, VehicleController×9, ParkingController×8, AdminController×8)
- ✅ #35 — Admin dashboard UI with stat cards, rate table, report table
- ✅ #43 — GitHub Actions CI (backend test + frontend build)
- ✅ #44 — Frontend-backend integration verified; CORS configured; error handling
- ✅ #45 — README updated with full setup, API docs, demo walkthrough
- ✅ #46 — Security audit: all RBAC boundaries and safe error messages verified
- ✅ #41 — Admin dashboard UI (stat cards, reports)
- ✅ #48 — Responsive layout and basic accessibility verified

**In Progress:**
- 🔄 #47 — Sprint review documentation (this file; ongoing)
- 🔄 #49 — PR for final merge (see PR section below)

---

## Retrospective — Overall Project

### What Went Well
- Backend architecture was clean and complete from the start
- JWT + Spring Security integration worked without issues
- H2 test database allowed 71 automated tests without MySQL dependency
- React + Vite frontend builds in under 2 seconds
- Ownership enforcement was comprehensive (vehicles, parking sessions, history)
- GitHub Actions CI catches regressions automatically

### What Could Be Improved
- Issue bodies were initially empty — should be written at sprint start, not retroactively
- Frontend npm install needed a `strict-ssl` workaround; team should document network constraints earlier
- No frontend unit tests (Vitest/React Testing Library) — deferred due to time
- Payment simulation should be more obviously labeled in the UI

### Action Items for Future Projects
- Write acceptance criteria for every issue before starting a sprint
- Set up test coverage reporting (JaCoCo) from the beginning
- Use feature flags for incomplete features rather than leaving them as stubs
- Assign PR reviewers explicitly, not just after the fact

---

## Individual Contribution Summary

Based on actual Git commit history and GitHub issue assignments:

| Teammate | Issues Owned | PRs Opened | Commits | Focus Area |
|---|---|---|---|---|
| Thrupthi-AM476 | #20, #3, #26-29, #32-35, #44, #48, #49 | PR#24, PR#current | Main contributor | Auth, vehicles, full frontend |
| adik1611 | #10, #16, #36-40 | — | TBD | Slots, reservations, staff UI |
| Vikas75bit | #11, #14, #15, #37-38 | — | TBD | Entry/exit, fee, payment, receipt |
| gaganreddy4656 | #18, #19, #41-43, #45-47 | — | TBD | Admin, testing, CI, docs |

**Note:** Contribution counts above reflect planned assignments. Actual commit counts should be verified using:
```bash
git shortlog -sn --no-merges
```

---

## Test Results (Sprint 3 Final)

```
mvn test (from src/backend)
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
