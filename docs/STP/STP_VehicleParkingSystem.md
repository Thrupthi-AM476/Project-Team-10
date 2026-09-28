# Software Test Plan (STP)

**Project:** Vehicle Parking System
**Version:** 1.0
**Date:** 28-09-2026
**Status:** Draft

---

## Revision History

| Version | Date | Change Summary |
|---|---|---|
| 1.0 | 28-09-2026 | Initial STP draft |

## Approvals

| Role | Name | Signature / Date |
|---|---|---|
| QA Lead | | |
| Dev Lead | | |
| Course Coordinator | | |

---

## Table of Contents

1. Introduction
2. Test Items
3. Features to be Tested
4. Features Not to be Tested
5. Test Approach / Strategy
6. Test Environment
7. Test Schedule
8. Test Deliverables
9. Roles and Responsibilities
10. Risks and Mitigation
11. Assumptions & Dependencies
12. Suspension & Resumption Criteria
13. Test Case Management & Traceability
14. Test Metrics & Reporting

---

## 1. Introduction

### Purpose

This document defines the test plan for the Vehicle Parking System v1.0. It outlines test objectives, scope, strategy, resources, schedule, and responsibilities to ensure all functional and non-functional requirements defined in the SRS are verified before release.

### Scope

Testing covers all VPS features: vehicle entry/exit, slot management, fee calculation, payment processing, reservation, and the admin portal. Hardware firmware, core payment gateway internals, and ALPR camera vendor software are excluded.

### References

- VPS SRS v1.0 — `docs/SRS/SRS_VehicleParkingSystem.md`
- VPS SAD v1.0 — `docs/SAD/SAD_VehicleParkingSystem.md`
- PCI-DSS v4.0 Standards
- WCAG 2.1 AA Guidelines

### Definitions

| Term | Definition |
|---|---|
| VPS | Vehicle Parking System |
| SRS | Software Requirements Specification |
| RTM | Requirements Traceability Matrix |
| UAT | User Acceptance Testing |
| TC | Test Case |
| LP | License Plate |
| ALPR | Automatic License Plate Recognition |

---

## 2. Test Items

- Vehicle Entry module (LP capture, ticket generation, barrier control)
- Vehicle Exit module (ticket scan, fee display, barrier control)
- Slot Management module (real-time occupancy, categorisation)
- Fee Calculator module (duration-based and rate-schedule pricing)
- Payment Service module (card, cash, digital wallet)
- Reservation module (advance booking, grace period release)
- Admin Web Portal (dashboard, rate config, reports, user management)
- Auth Service (login, RBAC, account lockout)
- Logging & Monitoring module

---

## 3. Features to be Tested

Features mapped to SRS requirement IDs:

| Feature | Req ID(s) |
|---|---|
| License plate capture at entry (ALPR + manual) | VPS-F-001 |
| Slot assignment and occupancy marking | VPS-F-002 |
| Ticket generation at entry | VPS-F-003 |
| Real-time slot availability display | VPS-F-004 |
| Slot categorisation (regular, disabled, EV) | VPS-F-005 |
| Entry blocked when parking is full | VPS-F-006 |
| Parking fee calculation | VPS-F-007 |
| Payment processing at exit | VPS-F-008 |
| Receipt generation and delivery | VPS-F-009 |
| Slot marked available on exit | VPS-F-010 |
| Advance slot reservation | VPS-F-011 |
| Auto-release of expired reservation | VPS-F-012 |
| Admin occupancy dashboard | VPS-F-013 |
| Rate schedule configuration | VPS-F-014 |
| Report export (CSV/PDF) | VPS-F-015 |
| Transaction response time ≤ 5s | VPS-NF-001 |
| 99.9% system availability | VPS-NF-002 |
| PCI-DSS compliance | VPS-NF-003 |
| 5-year audit log retention | VPS-NF-004 |
| WCAG 2.1 AA accessibility | VPS-NF-005 |
| TLS 1.2+ on all connections | VPS-SR-001 |
| Password complexity policy | VPS-SR-002 |
| RBAC enforcement | VPS-SR-003 |
| Account lockout after 5 failures | VPS-SR-004 |
| AES-256 encryption at rest | VPS-SR-005 |

---

## 4. Features Not to be Tested

- Core payment gateway internal processing (vendor responsibility)
- ALPR camera firmware and image processing algorithms (vendor responsibility)
- Physical barrier hardware and motor control firmware
- Bank core settlement systems beyond the API interface
- Operating system and network infrastructure

---

## 5. Test Approach / Strategy

### Test Levels

| Level | Description |
|---|---|
| Unit Testing | Individual service/module functions tested in isolation (Fee Calculator, Slot Manager logic) |
| Integration Testing | Inter-service communication tested (Entry Terminal ↔ API Gateway ↔ Slot Manager ↔ DB) |
| System Testing | End-to-end VPS flows tested against all SRS requirements |
| Acceptance Testing (UAT) | Stakeholder sign-off testing against acceptance criteria in SRS |

### Test Types

| Type | Description |
|---|---|
| Functional Testing | Verify all 15 FRs behave as specified |
| Regression Testing | Re-run test suite after every bug fix or new build |
| Performance Testing | Verify response time ≤ 5s at 90th percentile under load |
| Security Testing | Validate TLS, RBAC, account lockout, encryption, PCI-DSS |
| Usability Testing | Verify terminal UI clarity, accessibility (WCAG 2.1 AA) |
| Boundary Testing | Test edge cases: zero balance, max duration, full capacity |

### Entry Criteria

- Stable build delivered by development team
- Test environment configured and verified
- Test data (dummy vehicles, slots, accounts) available
- SRS and SAD reviewed and approved

### Exit Criteria

- 100% of planned test cases executed
- 0 critical or high defects open
- All acceptance criteria in SRS satisfied
- RTM shows full coverage of requirements

---

### 5.1 Security Validation

- Validate PIN/password masking — no credentials in logs or UI
- TLS 1.2+ verification on all endpoints using network scan tools
- PCI-DSS compliance checks — no plaintext PAN in DB or logs
- Input fuzzing on LP entry field, ticket ID, and payment fields
- Penetration testing of authentication and admin portal flows
- RBAC boundary testing — attempt cross-role access and verify rejection
- Account lockout verification after 5 failed login attempts

---

## 6. Test Environment

### Hardware

- Entry terminal kiosk with simulated barrier, LP camera, and ticket printer
- Exit terminal kiosk with simulated barrier, card reader, and receipt printer
- Admin workstation (standard browser)
- Load test server for JMeter-based performance testing

### Software

| Component | Version |
|---|---|
| VPS Application | v1.0 (build under test) |
| Database | MySQL 8.x |
| Payment Gateway | Sandbox / mock environment |
| ALPR Service | Mock service with pre-defined LP responses |
| OS | Ubuntu 22.04 LTS (server), Windows 11 (admin workstation) |

### Tools

| Tool | Purpose |
|---|---|
| Selenium | UI automation for terminal and admin portal |
| Postman / Newman | API testing for `/entry`, `/exit`, admin endpoints |
| JMeter | Performance and load testing |
| OWASP ZAP | Security / penetration testing |
| Jira | Defect tracking and test management |
| JUnit 5 | Unit testing (Java backend services) |

### Test Data

- 50 dummy vehicle records with various license plates
- 3 parking facility configurations (small, medium, large capacity)
- Multiple rate schedules (standard, peak, flat-rate)
- Test payment cards (sandbox credentials for all payment methods)
- Admin, attendant, and customer user accounts

---

## 7. Test Schedule

| Milestone | Target Date |
|---|---|
| Test case design complete | 05-10-2026 |
| Test environment setup verified | 07-10-2026 |
| Unit & integration test execution | 08-10-2026 to 12-10-2026 |
| System test execution | 13-10-2026 to 20-10-2026 |
| Security & performance testing | 21-10-2026 to 23-10-2026 |
| UAT | 24-10-2026 to 27-10-2026 |
| Test summary report | 28-10-2026 |

---

## 8. Test Deliverables

- Test Plan (this document)
- Test Cases (manual and automated)
- Test Scripts (Selenium, Postman collections, JMeter plans)
- Test Data sets
- Test Execution Logs
- Defect Reports (Jira)
- Final Test Summary Report

---

## 9. Roles and Responsibilities

| Role | Responsibility |
|---|---|
| QA Lead | Prepare test plan, coordinate execution, sign off |
| Test Engineer | Design and execute test cases, log defects |
| Developer | Support defect triage and fixes |
| Product Owner / Instructor | Review results, approve UAT sign-off |

---

## 10. Risks and Mitigation

| Risk | Mitigation |
|---|---|
| Delay in stable build delivery | Request early smoke builds; run smoke test immediately on receipt |
| Test environment downtime | Maintain backup environment on cloud VM |
| ALPR vendor mock not available | Build internal LP mock service using pre-defined inputs |
| Payment gateway sandbox instability | Use fully offline mock payment service as fallback |
| Incomplete test data | Prepare and version-control all test data sets in advance |

---

## 11. Assumptions & Dependencies

- Payment gateway sandbox will be stable and available throughout testing
- ALPR mock service will be ready before system test execution begins
- Test data (vehicles, accounts, rate schedules) will be prepared before execution
- Hardware drivers and terminal simulators will be provided before integration testing

---

## 12. Suspension & Resumption Criteria

**Suspend testing if:**
- Test environment is unavailable for more than 4 consecutive hours
- A build blocks more than 30% of planned test cases
- A critical security vulnerability is discovered during testing

**Resume testing if:**
- Blocking defects are resolved and verified fixed
- Test environment is stabilised
- A new build is delivered with confirmed fixes

---

## 13. Test Case Management & Traceability

RTM ensures every SRS requirement maps to at least one test case.

| Req ID | Requirement Short | Test Case(s) |
|---|---|---|
| VPS-F-001 | Capture license plate | TC-ENT-01, TC-ENT-01B (manual fallback) |
| VPS-F-002 | Assign and mark slot occupied | TC-ENT-02 |
| VPS-F-003 | Generate entry ticket | TC-ENT-03 |
| VPS-F-004 | Real-time slot availability display | TC-SLOT-01 |
| VPS-F-005 | Slot categorisation | TC-SLOT-02 |
| VPS-F-006 | Block entry when full | TC-SLOT-03 |
| VPS-F-007 | Calculate parking fee | TC-EXIT-01, TC-EXIT-01B (peak rate) |
| VPS-F-008 | Accept payment at exit | TC-EXIT-02 (card), TC-EXIT-02B (cash), TC-EXIT-02C (wallet) |
| VPS-F-009 | Print/send receipt | TC-EXIT-03 |
| VPS-F-010 | Mark slot available on exit | TC-EXIT-04 |
| VPS-F-011 | Advance reservation | TC-RES-01 |
| VPS-F-012 | Auto-release expired reservation | TC-RES-02 |
| VPS-F-013 | Admin dashboard | TC-ADM-01 |
| VPS-F-014 | Rate schedule configuration | TC-ADM-02 |
| VPS-F-015 | Export reports | TC-ADM-03 |
| VPS-NF-001 | Response time ≤ 5s | TC-PERF-01 |
| VPS-NF-002 | 99.9% availability | TC-PERF-02 |
| VPS-NF-003 | PCI-DSS compliance | TC-SEC-01 |
| VPS-NF-004 | 5-year log retention | TC-OPS-01 |
| VPS-NF-005 | WCAG 2.1 AA accessibility | TC-UX-01 |
| VPS-SR-001 | TLS 1.2+ | TC-SEC-01 |
| VPS-SR-002 | Password complexity | TC-SEC-02 |
| VPS-SR-003 | RBAC enforcement | TC-SEC-03 |
| VPS-SR-004 | Account lockout | TC-SEC-04 |
| VPS-SR-005 | AES-256 at rest | TC-SEC-05 |

---

## 14. Test Metrics & Reporting

### Metrics Collected

- % test cases executed
- % test cases passed / failed
- Defect density (defects per module)
- Defect severity distribution (critical / high / medium / low)
- Defect aging (average days open)
- Requirement coverage (% requirements with passing test cases)

### Reports

- Daily test execution status update
- Weekly defect summary
- Final Test Summary Report (post-UAT)
