# Software Requirements Specification (SRS)

**Project:** Vehicle Parking System
**Version:** 1.0
**Authors:** Thrupthi G D (PE51UG24AM476)
**Date:** 06-09-2026
**Status:** Draft

---

## Revision History

| Version | Date | Author | Change Summary | Approval |
|---|---|---|---|---|
| 1.0 | 06-09-2026 | Thrupthi G D | Initial SRS draft | Pending |

## Table of Contents

1. Introduction
2. Overall Description
3. External Interface Requirements
4. System Features (Detailed)
5. Non-Functional Requirements (Detailed)
6. Quality Attributes & Acceptance Tests
7. UML Use-Case Diagram
8. Requirements Traceability Matrix (RTM)

---

## 1. Introduction

### 1.1 Purpose

This document is a Software Requirements Specification (SRS) for a Vehicle Parking System. It defines functional and non-functional requirements, external interfaces, and verification criteria for use by developers, QA engineers, and the course assessor. The system automates the management of parking slots, vehicle entry/exit, fee calculation, and administrative monitoring.

### 1.2 Scope

Covers all user interactions at parking entry/exit terminals, real-time slot management, vehicle tracking, fee calculation, receipt generation, and administrative reporting. Excludes payment gateway internal processing beyond API integration, physical barrier hardware firmware, and city-level traffic management systems.

### 1.3 Audience

Developers, QA Engineers, System Integrators, Maintenance Technicians, and Assessment Evaluators.

### 1.4 Definitions

| Term | Definition |
|---|---|
| VPS | Vehicle Parking System |
| LP | License Plate |
| ALPR | Automatic License Plate Recognition |
| OTP | One-Time Password |
| API | Application Programming Interface |
| UI | User Interface |
| TLS | Transport Layer Security |
| RBAC | Role-Based Access Control |
| SRS | Software Requirements Specification |
| RTM | Requirements Traceability Matrix |

---

## 2. Overall Description

### 2.1 Product Perspective

The Vehicle Parking System is a self-service software platform that manages parking facilities. It interacts with entry/exit barrier hardware, a central database for slot tracking, a payment gateway for fee collection, and an admin dashboard for monitoring. The system operates in real time and provides both customer-facing terminals and a web-based admin console.

### 2.2 Major Product Functions

- Vehicle entry registration (license plate capture, ticket generation)
- Real-time parking slot availability display
- Slot assignment and reservation
- Vehicle exit processing and fee calculation
- Receipt and ticket printing
- Payment processing (cash / card / digital wallet)
- Admin dashboard: reports, occupancy overview, revenue tracking
- Alerts for full capacity, unauthorized vehicles, and system faults
- Offline queuing for transactions during network outages

### 2.3 User Roles and Characteristics

| Role | Description |
|---|---|
| Driver / Customer | General public; expects quick entry/exit, clear instructions, and accurate billing |
| Parking Attendant | On-site staff; handles manual overrides and assists customers |
| System Administrator | Manages slot configuration, user roles, system settings, and reports |
| Maintenance Technician | Diagnoses hardware and software faults; accesses diagnostic logs |

### 2.4 Operating Environment

Web-based admin portal (modern browsers), embedded terminal software at entry/exit points, networked via secure WAN/LAN, power backed by UPS, operating in outdoor environmental conditions (temperature/humidity rated hardware).

### 2.5 Constraints

- PCI-DSS compliance for payment card data handling
- ALPR camera integration is vendor-specific
- Hardware barrier APIs vary by vendor and must be abstracted
- System must operate in degraded (offline) mode during network outages
- WCAG 2.1 AA accessibility compliance for the customer-facing UI

---

## 3. External Interface Requirements

### 3.1 User Interfaces

- **Entry Terminal:** Touch-screen kiosk with license plate input, ticket dispensing, slot availability display. High-contrast mode and audio prompts for accessibility.
- **Exit Terminal:** Touch-screen kiosk for ticket scan/entry, payment processing, and receipt printing.
- **Admin Web Portal:** Responsive web dashboard for slot management, occupancy reports, revenue analytics, and user management.

### 3.2 Hardware Interfaces

- ALPR camera module (vendor API integration)
- Entry/exit barrier gate controller (vendor-specific serial/network interface)
- Ticket printer (ESC/POS protocol)
- Receipt printer
- Card payment terminal (PCI-compliant POS device)
- Status LEDs and operator panel at entry/exit booths

### 3.3 Software Interfaces

- **Payment Gateway API:** JSON over TLS for card/digital payment authorisation and settlement
- **ALPR Service API:** Image-to-text license plate recognition results
- **Monitoring & Logging API:** Health checks, uptime alerts, and audit log shipping
- **SMS/Email Notification Service:** Entry/exit confirmations and reservation reminders

### 3.4 Communications

- TLS 1.2+ enforced for all network communication
- Mutual TLS for payment gateway connections
- Retry/backoff strategy for failed API calls
- Offline transaction queuing with secure signing when network is unavailable

---

## 4. System Features (Detailed)

> The system contains **15 functional requirements**, **5 non-functional requirements**, **2 security objectives**, and **5 security requirements** as required.

---

### 4.1 Vehicle Entry & Ticket Generation

**Description:** When a vehicle arrives, the system captures the license plate, assigns an available slot, generates a unique ticket, and opens the barrier.

| Req ID | Requirement | Type | Priority | Source / Stakeholder | Acceptance Criteria / Test Case Ref | Comments / Dependencies |
|---|---|---|---|---|---|---|
| VPS-F-001 | The system shall capture the vehicle license plate at entry using ALPR or manual input. | Functional | High | Operations | AC: License plate stored with timestamp on entry. Test: TC-ENT-01 | Requires ALPR camera or manual fallback |
| VPS-F-002 | The system shall assign an available parking slot to the vehicle upon entry and mark it as occupied. | Functional | High | Operations | AC: Slot status changes to "Occupied"; ticket reflects slot number. Test: TC-ENT-02 | Requires real-time slot DB |
| VPS-F-003 | The system shall generate a unique ticket with entry timestamp, slot number, and license plate, and print or display it to the customer. | Functional | High | Customer | AC: Ticket generated within 3 seconds of entry; unique ID confirmed. Test: TC-ENT-03 | Depends on VPS-F-001, VPS-F-002 |

---

### 4.2 Slot Management & Availability Display

**Description:** The system tracks slot occupancy in real time and displays availability to customers and administrators.

| Req ID | Requirement | Type | Priority | Source / Stakeholder | Acceptance Criteria / Test Case Ref | Comments / Dependencies |
|---|---|---|---|---|---|---|
| VPS-F-004 | The system shall display real-time slot availability (total, occupied, available) on the entry terminal and admin portal. | Functional | High | Customer / Admin | AC: Display updates within 5 seconds of any slot status change. Test: TC-SLOT-01 | Requires DB polling or event-driven updates |
| VPS-F-005 | The system shall support slot categorisation (e.g., regular, disabled, EV charging). | Functional | Medium | Operations | AC: Slot types configurable by admin; correct type assigned during booking. Test: TC-SLOT-02 | Admin configuration required |
| VPS-F-006 | The system shall prevent entry when all slots are occupied and display a "Parking Full" notice. | Functional | High | Operations / Customer | AC: Barrier remains closed and "Full" message displayed when occupancy = 100%. Test: TC-SLOT-03 | Depends on VPS-F-002 |

---

### 4.3 Vehicle Exit & Fee Calculation

**Description:** On exit, the system validates the ticket, calculates the parking fee based on duration, processes payment, and opens the barrier.

| Req ID | Requirement | Type | Priority | Source / Stakeholder | Acceptance Criteria / Test Case Ref | Comments / Dependencies |
|---|---|---|---|---|---|---|
| VPS-F-007 | The system shall calculate the parking fee based on entry timestamp, exit timestamp, and the applicable rate schedule. | Functional | High | Business | AC: Fee = correct rate × duration; verified against test data. Test: TC-EXIT-01 | Rate schedule configurable by admin |
| VPS-F-008 | The system shall accept payment via cash, card, or digital wallet before opening the exit barrier. | Functional | High | Customer / Business | AC: Payment confirmed before barrier opens; failed payment prevents exit. Test: TC-EXIT-02 | Requires payment gateway integration |
| VPS-F-009 | The system shall print or send a digital receipt upon successful payment at exit. | Functional | Medium | Customer | AC: Receipt contains ticket ID, duration, amount paid, and timestamp. Test: TC-EXIT-03 | Depends on VPS-F-007, VPS-F-008 |
| VPS-F-010 | The system shall mark the parking slot as available upon confirmed vehicle exit. | Functional | High | Operations | AC: Slot status reverts to "Available" within 5 seconds of exit confirmation. Test: TC-EXIT-04 | Depends on VPS-F-002 |

---

### 4.4 Reservation

**Description:** Customers can pre-book a parking slot via the web portal or mobile interface.

| Req ID | Requirement | Type | Priority | Source / Stakeholder | Acceptance Criteria / Test Case Ref | Comments / Dependencies |
|---|---|---|---|---|---|---|
| VPS-F-011 | The system shall allow registered customers to reserve a parking slot in advance for a specified time window. | Functional | Medium | Customer | AC: Reservation confirmed with slot number and booking ID; slot held for 15 minutes past arrival time. Test: TC-RES-01 | Requires customer account |
| VPS-F-012 | The system shall automatically release a reserved slot if the customer does not arrive within the grace period. | Functional | Medium | Operations | AC: Slot released and marked "Available" after grace period expiry. Test: TC-RES-02 | Depends on VPS-F-011 |

---

### 4.5 Admin & Reporting

**Description:** Administrators manage slots, rates, users, and access system reports.

| Req ID | Requirement | Type | Priority | Source / Stakeholder | Acceptance Criteria / Test Case Ref | Comments / Dependencies |
|---|---|---|---|---|---|---|
| VPS-F-013 | The system shall provide an admin dashboard displaying current occupancy, daily revenue, and transaction history. | Functional | High | Admin | AC: Dashboard loads within 3 seconds; data matches DB records. Test: TC-ADM-01 | Admin login required |
| VPS-F-014 | The system shall allow administrators to configure parking rate schedules (hourly, flat rate, peak pricing). | Functional | High | Business / Admin | AC: Rate changes apply to all new transactions immediately after save. Test: TC-ADM-02 | — |
| VPS-F-015 | The system shall generate exportable reports (CSV/PDF) for occupancy and revenue for selected date ranges. | Functional | Medium | Admin / Management | AC: Report exported in < 10 seconds; data matches on-screen dashboard values. Test: TC-ADM-03 | Depends on VPS-F-013 |

---

## 5. Non-Functional Requirements (Detailed)

| Req ID | Requirement | Category | Priority | Acceptance Criteria / Measurement |
|---|---|---|---|---|
| VPS-NF-001 | Transaction processing (entry to ticket generation, exit to barrier open) shall complete within 5 seconds for 90% of transactions under normal load. | Performance | High | 90th percentile ≤ 5s in load tests. Test: TC-PERF-01 |
| VPS-NF-002 | The system shall maintain 99.9% availability per month; scheduled maintenance windows excluded. | Reliability | High | Uptime monitoring reports ≥ 99.9% per month. Test: Ops reports |
| VPS-NF-003 | All payment card data must comply with PCI-DSS; card numbers must not be stored in plaintext. | Security / Compliance | High | PCI-DSS audit checklist pass. Test: TC-SEC-01 |
| VPS-NF-004 | The system shall retain transaction logs and audit events with timestamps for a minimum of 5 years. | Audit / Data Retention | High | Automated archival verified; retrieval tested. Test: TC-OPS-01 |
| VPS-NF-005 | Customer-facing terminals and web portal shall comply with WCAG 2.1 AA accessibility standards. | Usability / Accessibility | Medium | Accessibility audit pass. Test: TC-UX-01 |

---

### 5.1 Security

#### 5.1.1 Security Objectives

1. **Protect cardholder and personal data** — Ensure all payment and personal data is encrypted in transit and at rest, preventing unauthorised disclosure or misuse.
2. **Prevent unauthorised system access** — Ensure only authenticated and authorised users (admins, attendants) can access management functions, and that all access is logged and auditable.

#### 5.1.2 Security Requirements

| Req ID | Requirement | Type | Priority | Acceptance Criteria / Test Case Ref |
|---|---|---|---|---|
| VPS-SR-001 | TLS 1.2+ shall be mandatory for all network communications between system components. | Security | High | TLS version verified via network scan. Test: TC-SEC-01 |
| VPS-SR-002 | All admin and attendant accounts shall be protected by password authentication with a minimum complexity policy (8+ characters, mixed case, number, symbol). | Security | High | Login with non-compliant password rejected. Test: TC-SEC-02 |
| VPS-SR-003 | The system shall implement Role-Based Access Control (RBAC); customers, attendants, and admins shall only access functions permitted for their role. | Security | High | Role boundary tests pass. Test: TC-SEC-03 |
| VPS-SR-004 | The system shall lock an account after 5 consecutive failed login attempts and generate an audit event. | Security | High | Account locked after 5 failures; event logged. Test: TC-SEC-04 |
| VPS-SR-005 | License plate data and transaction records shall be encrypted at rest using AES-256 or equivalent. | Security | High | Encrypted storage verified in DB audit. Test: TC-SEC-05 |

---

## 6. Quality Attributes & Acceptance Tests

**Exit criteria for acceptance:** All high-priority functional requirements implemented and verified, no critical NFR failures, and RTM shows all planned test cases passed.

**Acceptance test suites:**
- Vehicle Entry & Exit flow (end-to-end)
- Slot availability and capacity management
- Fee calculation accuracy
- Payment processing (card, cash, digital)
- Admin dashboard and reporting
- Performance under load (concurrent entries/exits)
- Security and access control
- Accessibility compliance

---

## 7. UML Use-Case Diagrams

### 7.1 Use-Case Diagram 1 — Customer Parking Flow

```
+----------------------------------------------------------+
|                  Vehicle Parking System                  |
|                                                          |
|   [Enter Parking Lot] <---- Driver/Customer              |
|   [View Slot Availability] <---- Driver/Customer         |
|   [Make Reservation] <---- Driver/Customer               |
|   [Pay Parking Fee] <---- Driver/Customer                |
|   [Exit Parking Lot] <---- Driver/Customer               |
|   [Get Receipt] <---- Driver/Customer                    |
|                                                          |
+----------------------------------------------------------+
```

> *To be replaced with a PlantUML / draw.io diagram in the final submission.*

### 7.2 Use-Case Diagram 2 — Admin & Attendant Operations

```
+----------------------------------------------------------+
|                  Vehicle Parking System                  |
|                                                          |
|   [View Occupancy Dashboard] <---- Administrator         |
|   [Configure Rate Schedule] <---- Administrator          |
|   [Generate Reports] <---- Administrator                 |
|   [Manage User Accounts] <---- Administrator             |
|   [Manual Barrier Override] <---- Parking Attendant      |
|   [Log Maintenance Event] <---- Maintenance Technician   |
|                                                          |
+----------------------------------------------------------+
```

> *To be replaced with a PlantUML / draw.io diagram in the final submission.*

---

## 8. Requirements Traceability Matrix (RTM)

| Req ID | Requirement Short | Section Ref / Design Spec | Module | Test Case(s) | Status (N/P/A) | Comments |
|---|---|---|---|---|---|---|
| VPS-F-001 | Capture license plate at entry | 4.1 / DS-Entry-01 | EntryModule | TC-ENT-01 | N | |
| VPS-F-002 | Assign and mark slot occupied | 4.1 / DS-Slot-01 | SlotManager | TC-ENT-02 | N | |
| VPS-F-003 | Generate entry ticket | 4.1 / DS-Entry-02 | TicketService | TC-ENT-03 | N | |
| VPS-F-004 | Real-time slot availability display | 4.2 / DS-Slot-02 | SlotManager / UI | TC-SLOT-01 | N | |
| VPS-F-005 | Slot categorisation | 4.2 / DS-Slot-03 | SlotManager | TC-SLOT-02 | N | |
| VPS-F-006 | Block entry when full | 4.2 / DS-Slot-04 | EntryModule | TC-SLOT-03 | N | |
| VPS-F-007 | Calculate parking fee | 4.3 / DS-Fee-01 | FeeCalculator | TC-EXIT-01 | N | |
| VPS-F-008 | Accept payment at exit | 4.3 / DS-Pay-01 | PaymentService | TC-EXIT-02 | N | |
| VPS-F-009 | Print/send receipt | 4.3 / DS-Pay-02 | ReceiptService | TC-EXIT-03 | N | |
| VPS-F-010 | Mark slot available on exit | 4.3 / DS-Slot-05 | SlotManager | TC-EXIT-04 | N | |
| VPS-F-011 | Advance slot reservation | 4.4 / DS-Res-01 | ReservationService | TC-RES-01 | N | |
| VPS-F-012 | Auto-release expired reservation | 4.4 / DS-Res-02 | ReservationService | TC-RES-02 | N | |
| VPS-F-013 | Admin occupancy dashboard | 4.5 / DS-Adm-01 | AdminPortal | TC-ADM-01 | N | |
| VPS-F-014 | Configure rate schedules | 4.5 / DS-Adm-02 | RateService | TC-ADM-02 | N | |
| VPS-F-015 | Export occupancy/revenue reports | 4.5 / DS-Adm-03 | ReportService | TC-ADM-03 | N | |
| VPS-NF-001 | Response time ≤ 5s | 5 / DS-Perf-01 | All Modules | TC-PERF-01 | N | |
| VPS-NF-002 | 99.9% availability | 5 / DS-Rel-01 | Infrastructure | Ops reports | N | |
| VPS-NF-003 | PCI-DSS compliance | 5 / DS-Sec-01 | PaymentService | TC-SEC-01 | N | |
| VPS-NF-004 | 5-year log retention | 5 / DS-Ops-01 | LoggingService | TC-OPS-01 | N | |
| VPS-NF-005 | WCAG 2.1 AA accessibility | 5 / DS-UX-01 | UI Layer | TC-UX-01 | N | |
| VPS-SR-001 | TLS 1.2+ for all connections | 5.1.2 / DS-Sec-02 | Network Layer | TC-SEC-01 | N | |
| VPS-SR-002 | Strong password policy | 5.1.2 / DS-Sec-03 | AuthService | TC-SEC-02 | N | |
| VPS-SR-003 | RBAC enforcement | 5.1.2 / DS-Sec-04 | AuthService | TC-SEC-03 | N | |
| VPS-SR-004 | Account lockout after 5 failures | 5.1.2 / DS-Sec-05 | AuthService | TC-SEC-04 | N | |
| VPS-SR-005 | AES-256 encryption at rest | 5.1.2 / DS-Sec-06 | DataLayer | TC-SEC-05 | N | |
