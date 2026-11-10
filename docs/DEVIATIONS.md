# Deviations from SRS and SAD — Vehicle Parking System (Team 10)

| Field | Details |
|---|---|
| **Project** | Vehicle Parking System (VPS) |
| **Team** | Team 10 — Thrupthi-AM476, adik1611, Vikas75bit, gaganreddy4656 |
| **Version** | 1.0 (draft for team review) |
| **Date** | 10-11-2026 |
| **Compared documents** | SRS (`docs/SRS/`), SAD (`docs/SAD/`), STP (`docs/STP/`) |
| **Compared against** | Code on `main` (Spring Boot backend, React frontend, MySQL schema) |

## 1. Purpose

The SRS and SAD were written before development began and describe a full parking-lot installation (cameras, barriers, terminals, a payment gateway). The delivered product is a web application built within the sprint timeline. This document records where the delivered system differs from those documents, so the differences can be explained at the demo and viva.

## 2. Summary

| Type | Count |
|---|---|
| Features not implemented | 6 |
| Features simplified or changed | 13 |
| Not verified by testing (D-11, covers 3 requirements) | 1 |

## 3. Deviations from the SRS

| ID | SRS Ref | Planned (SRS) | Delivered | Status | Reason |
|---|---|---|---|---|---|
| D-01 | VPS-F-001, F-006 | Capture plate by ALPR camera or manual input; keep the barrier closed with a "Full" message when no slots are free | Manual plate entry only (`captureMethod` defaults to `MANUAL`). Entry is blocked with an error message when the lot is full. No camera or barrier control | Simplified | *No camera or barrier hardware was available; the SRS allows manual fallback, so manual entry was treated as the core path.* |
| D-02 | VPS-F-002, F-003 | Print or display the ticket | Ticket is displayed on screen only; no printing | Simplified | *No printer hardware in scope; on-screen ticket satisfies "display".* |
| D-03 | VPS-F-008, NF-003 | Accept cash, card or wallet payment through a payment gateway; PCI-DSS compliance | Payment is simulated: the method is recorded and the payment is always marked SUCCESS. No gateway, no card data stored. Failed-payment and gateway-outage flows are not implemented | Simplified | *A real payment gateway is out of scope for an academic project and needs merchant credentials; simulated payment shows the workflow.* |
| D-04 | VPS-F-009 | Print or send a digital receipt | A receipt ID is generated and returned in the exit response. No printing or email/SMS | Simplified | *No email or SMS service was integrated within the sprint timeline.* |
| D-05 | VPS-F-012 | Automatically release a reserved slot if the customer does not arrive within the grace period | Expiry is run by a manual endpoint (`POST /api/v1/reservations/expire`, admin/attendant only). No scheduled job | Changed | *A scheduler was deferred to keep Sprint 2 within scope; manual trigger demonstrates the logic.* |
| D-06 | VPS-F-015 | Export occupancy and revenue reports as CSV or PDF | Report data is returned as JSON (`GET /api/v1/admin/reports`) and shown in the admin dashboard. No CSV or PDF export | Not implemented | *Export formats were deprioritised against core parking workflows.* |
| D-07 | VPS-F-011 | Reserve through the web portal or a mobile interface | Web application only (responsive layout); no separate mobile app | Simplified | *A native mobile app was out of scope; the responsive web UI covers mobile browsers.* |
| D-08 | VPS-SR-001 | TLS 1.2+ mandatory for all communication | The application serves plain HTTP on port 8080; no TLS configuration in the project | Not implemented | *TLS is normally terminated at deployment (reverse proxy or hosting platform), which was not part of the course deliverable.* |
| D-09 | VPS-SR-005 | AES-256 encrypted storage of plate and transaction records | Stored as plain text in MySQL. Plate search relies on a plain, indexed `license_plate` column | Not implemented | *Encrypting the plate column would break lookup and uniqueness checks; deferred to a later hardening phase.* |
| D-10 | VPS-NF-004 | Audit/log retention (5 years per RTM) | Security events are written to the `audit_logs` table. No retention policy or archival configured | Simplified | *Retention needs operational infrastructure that a student deployment does not have.* |
| D-11 | VPS-NF-001, NF-002, NF-005 | 90th percentile of 5 s or less; 99.9% availability; WCAG 2.1 accessibility | Not load-tested, not measured, no formal accessibility audit (TC-026, TC-027, TC-030 are marked Not Executed) | Not verified | *Load, uptime and accessibility testing need tooling and test windows beyond the testing week.* |
| D-12 | VPS-SR-004 | Lock account after 5 consecutive failed logins | Implemented. The lock has no expiry and no unlock endpoint; it stays until changed in the database | Simplified | *An unlock flow was not specified in the SRS, so only the lock itself was built.* |

## 4. Deviations from the SAD

| ID | SAD Ref | Planned (SAD) | Delivered | Status | Reason |
|---|---|---|---|---|---|
| D-13 | 3.4, 3.5 | Separate components: API Gateway, Auth Service, Slot Manager, Fee Calculator, Payment Service, ALPR/Barrier adapter | A single Spring Boot application with controller, service and repository layers. The components exist as service classes. No gateway and no hardware adapter | Simplified | *A single deployable is simpler to build, test and run for a four-person team.* |
| D-14 | 3.6 | React touch-optimised kiosk UI; admin portal on React with Tailwind CSS | One React and Vite web app for customers, staff and admin. Tailwind CSS is not used | Changed | *One shared frontend avoided duplicate work; plain CSS was already in use.* |
| D-15 | 3.6, 3.9 | Mutual TLS for the payment gateway; AES-256 for sensitive fields | Not present (see D-03, D-08, D-09) | Not implemented | *See D-03, D-08 and D-09.* |
| D-16 | 3.6, 4.4 | Structured JSON logs shipped to ELK; metric alerts | Audit events stored in the database; standard console logging. No ELK, metrics or alerts | Not implemented | *ELK needs separate servers, which were not available.* |
| D-17 | 3.6 | Docker containers on a cloud VM or on-premises server | Run locally with MySQL, Maven and npm. No Dockerfile; CI runs through GitHub Actions | Not implemented | *Deployment was outside the mini-project scope.* |
| D-18 | 4.3 | Endpoints `/api/v1/entry` and `/api/v1/exit`, with terminal IDs in the request | `/api/v1/parking/entry` and `/api/v1/parking/exit`, with JWT authentication. Entry request has `licensePlate`, `slotType` and `captureMethod`; no terminal ID | Changed | *Endpoints were grouped by resource; terminal IDs apply only to physical terminals.* |
| D-19 | 4.3, 4.4 | Error body `{ "error", "message", "timestamp" }`; codes such as 409 Parking Full, 402 Payment Failed, 503 Terminal Offline | Responses use `{ "success", "message", "data" }`. A full lot raises an illegal-state error, which the handler returns as HTTP 403. 402 and 503 are not used | Changed | *A single wrapper was adopted across all endpoints for consistency; the status mapping was kept generic.* |
| D-20 | 2.3 (SRS) | Four roles: customer, parking attendant, administrator, maintenance technician | `MAINTENANCE_TECHNICIAN` exists in the user model, but no endpoint or screen uses it | Simplified | *No maintenance workflow was in the sprint scope.* |

## 5. Implemented broadly as specified

Slot assignment and ticket generation (VPS-F-002, F-003), real-time availability and slot categories (F-004, F-005), fee calculation and slot release (F-007, F-010), reservations (F-011), admin dashboard and rate configuration (F-013, F-014), password policy (SR-002), role-based access control (SR-003) and account lockout (SR-004). See the Test Case Document for the test cases mapped to each.

## 6. Impact on testing

Test cases TC-013, TC-014, TC-017, TC-026 to TC-031 and TC-035 are marked "Not Executed". Most of these relate to the deviations above: D-03 (payment failure and gateway), D-08 (TLS), D-09 (encryption), D-10 (retention) and D-11 (performance, availability, accessibility). Each should either be executed or recorded here as out of scope.
