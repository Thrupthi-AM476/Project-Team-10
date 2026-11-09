# Software Test Deliverable 2.b.ii: System Validation Report

**Project:** Vehicle Parking System (VPS) — Team 10  
**Artifact Version:** 1.0.0  
**Validation Methodology:** Spring Boot Integration Testing, MockMvc HTTP Simulation, In-Memory H2 Persistence  
**Target Specifications:** Software Requirements Specification (SRS v1.0), Software Test Plan (STP v1.0)  
**Execution Timestamp:** 2026-11-09  
**System Status:** **VERIFIED & ACCEPTED (All Core End-to-End Workflows Meet Acceptance Criteria)**

---

## 1. Scope & Objective

The System Validation Report provides formal verification that the integrated components of the Vehicle Parking System backend operate in cohesion to satisfy the end-to-end user journeys defined in the SRS.

Rather than isolated unit stubs, system validation tests execute against the full Spring Boot application context (`@SpringBootTest`), routing through:
1. Servlet filters and security chains (`JwtAuthFilter`, `SecurityConfig`).
2. Spring MVC REST dispatchers and parameter validators.
3. Transactional domain services.
4. JPA Repository interfaces interacting with an active SQL engine (H2 relational test schema).

---

## 2. End-to-End Workflow Validation

### Workflow A: User Authentication & Role-Based Access Control (RBAC)
- **Relevant Requirements:** `VPS-SR-002`, `VPS-SR-003`, `VPS-SR-004`
- **Executed In:** `AuthControllerTest`, `UserControllerTest`
- **Validation Narrative:**
  1. A prospective customer submits registration details (`POST /api/auth/register`). The system enforces password complexity (rejection of weak credentials with HTTP 400), hashes the password with BCrypt, saves the user entity, and issues a signed JWT containing identity claims.
  2. If an incoming registration payload attempts privilege escalation by providing `"role": "ADMINISTRATOR"`, the service overrides this and persists the user as `CUSTOMER`.
  3. Authentication (`POST /api/auth/login`) with valid credentials yields an HTTP 200 and a valid bearer token. Consecutive invalid password submissions increment the failed counter; on the 5th attempt, the account enters a locked state and subsequent attempts return HTTP 403 until the lockout duration expires.
  4. Secured endpoints (`GET /api/users/profile`) return HTTP 403 when unauthenticated or presenting an invalid bearer token, and HTTP 200 with sanitized user details (excluding `passwordHash`) when authenticated.

### Workflow B: Vehicle Registration & Plate Management
- **Relevant Requirements:** `VPS-F-001`, `VPS-SR-003`
- **Executed In:** `VehicleControllerTest`
- **Validation Narrative:**
  1. Authenticated customer submits vehicle details (`POST /api/vehicles`). The system normalizes license plates (e.g., converting lowercase `ka01ab1234` to uppercase `KA01AB1234`) and persists the vehicle associated with the caller's user ID.
  2. Attempting to register a pre-existing license plate triggers database unique constraint checks and returns an HTTP 400 validation error.
  3. Customer A querying `GET /api/vehicles` receives only their own registered vehicles. Direct enumeration attacks by Customer B querying Customer A's vehicle by ID or license plate return HTTP 403 Forbidden.

### Workflow C: Complete Parking Lifecycle (Entry → Occupancy → Exit → Payment)
- **Relevant Requirements:** `VPS-F-001`, `VPS-F-002`, `VPS-F-003`, `VPS-F-004`, `VPS-F-007`, `VPS-F-008`, `VPS-F-010`
- **Executed In:** `ParkingControllerTest`, `ParkingServiceTest`
- **Validation Narrative:**
  1. **Entry Check-In (`POST /api/parking/entry`):**
     - Customer checks in a registered vehicle. The system verifies no active parking session exists for that vehicle.
     - An available slot matching the vehicle category (or standard fallback) is reserved, transitioning slot status from `AVAILABLE` to `OCCUPIED`.
     - An active `ParkingRecord` is created, and an entry ticket is returned within 3 seconds containing a unique ticket ID (`TKT-YYYYMMDD-XXXXX`), assigned slot number, and timestamp.
  2. **Live Inventory Visibility (`GET /api/parking/slots/available`):**
     - Public and authenticated users query live inventory. The system immediately reflects the newly occupied slot, decrementing the available count.
  3. **Exit & Payment Checkout (`POST /api/parking/exit`):**
     - Ticket is presented at exit. Duration is calculated between `entryTime` and `exitTime`.
     - The rate schedule matching the vehicle type is applied. If exit occurs within the configured grace period (15 minutes), the fee is calculated as `0.00 INR`. If elapsed duration exceeds grace period, fee is computed at the configured hourly rate.
     - Mock payment transaction is recorded (`PaymentStatus.SUCCESS`). The parking record is transitioned to `COMPLETED`, recording exit time, duration, and fee.
     - The assigned slot is released back to inventory, immediately transitioning status back to `AVAILABLE`.
     - Double-exit protection ensures a completed ticket cannot be reused (throws HTTP 400/409).

### Workflow D: Advance Reservation Management
- **Relevant Requirements:** `VPS-F-011`, `VPS-F-012`
- **Executed In:** `ReservationServiceTest`
- **Validation Narrative:**
  1. Customer requests a time-window booking (`startTime` to `endTime`). System verifies `endTime > startTime` and that the vehicle belongs to the caller.
  2. The system checks slot inventory: slots that have conflicting active reservations during the requested window are filtered out (`isSlotReservedInWindow`). An available slot is linked, and the reservation is saved with status `CONFIRMED`.
  3. Customers can cancel their own pending reservations. Non-owners are denied access (HTTP 403), while administrators possess override authority to cancel any reservation.
  4. Overdue reservations that cross their end-time without being claimed are transitioned to `EXPIRED` by the sweeper routine.

### Workflow E: Administrative Auditing & Financial Reporting
- **Relevant Requirements:** `VPS-F-013`, `VPS-F-014`, `VPS-F-015`
- **Executed In:** `AdminControllerTest`
- **Validation Narrative:**
  1. Standard customers attempting to access `/api/admin/**` endpoints are blocked with HTTP 403 Forbidden.
  2. Administrator querying `GET /api/admin/dashboard` receives aggregated statistics: total slots, occupied count, available count, revenue collected today, and revenue collected this month.
  3. Administrator querying `GET /api/admin/reports` with a date filter receives the audit trail of completed sessions, vehicles, and fees.
  4. Administrators can create (`POST /api/admin/rates`) and update (`PUT /api/admin/rates/{id}`) rate schedules and grace periods.

---

## 3. Requirements Traceability Matrix (RTM)

The table below maps each SRS requirement to its validating integration test methods:

| Req ID | SRS Requirement Description | Validating Integration Tests | Verdict |
| :--- | :--- | :--- | :---: |
| **VPS-F-001** | Vehicle entry & plate capture | `ParkingControllerTest.processEntry_ownVehicle_returns200`<br>`VehicleControllerTest.addVehicle_authenticated_returns201` | **PASSED** |
| **VPS-F-002** | Slot assignment & occupancy update | `ParkingControllerTest.processEntry_ownVehicle_returns200`<br>`ParkingServiceTest.processEntry_preferredSlotType_isSelectedAndReturned` | **PASSED** |
| **VPS-F-003** | Unique ticket generation | `ParkingControllerTest.processEntry_ownVehicle_returns200`<br>`ParkingServiceTest.processEntry_ownVehicle_succeeds` | **PASSED** |
| **VPS-F-004** | Live availability display | `ParkingControllerTest.getAvailableSlots_public_returns200`<br>`ParkingControllerTest.getAllSlots_public_returns200` | **PASSED** |
| **VPS-F-005** | Category slot allocation (EV/Disabled) | `ParkingServiceTest.createSlot_success`<br>`ParkingServiceTest.updateSlot_WithValidFields_UpdatesAllFields` | **PASSED** |
| **VPS-F-006** | Capacity check & parking full guard | `ParkingControllerTest.processEntry_alreadyParked_returns403`<br>`ParkingServiceTest.processEntry_noSlots_throwsIllegalStateException` | **PASSED** |
| **VPS-F-007** | Duration fee computation | `ParkingControllerTest.processExit_returns200WithFee`<br>`ParkingServiceTest.processExit_afterGracePeriod_calculatesFeeAndRecordsPayment` | **PASSED** |
| **VPS-F-008** | Payment simulation & status record | `ParkingControllerTest.processExit_returns200WithFee`<br>`ParkingServiceTest.processExit_ownVehicle_succeeds` | **PASSED** |
| **VPS-F-010** | Slot release upon vehicle exit | `ParkingControllerTest.processExit_returns200WithFee`<br>`ParkingServiceTest.processExit_ownVehicle_succeeds` | **PASSED** |
| **VPS-F-011** | Advance slot reservation | `ReservationServiceTest.createReservation_Success_WithPreferredType`<br>`ReservationServiceTest.createReservation_SlotReservedInWindow_ThrowsException` | **PASSED** |
| **VPS-F-012** | Reservation cancellation & expiry | `ReservationServiceTest.cancelReservation_AsOwner_Success`<br>`ReservationServiceTest.expireOverdueReservations_ProcessesExpiredList` | **PASSED** |
| **VPS-F-013** | Admin dashboard & analytics | `AdminControllerTest.getDashboard_admin_returns200`<br>`AdminControllerTest.getDashboard_customer_returns403` | **PASSED** |
| **VPS-F-014** | Rate schedule management | `AdminControllerTest.createRate_admin_returns201`<br>`AdminControllerTest.updateRate_admin_returns200` | **PASSED** |
| **VPS-F-015** | Audit & revenue reporting | `AdminControllerTest.getReport_admin_returns200`<br>`ParkingServiceTest.getParkingHistory_staffCanViewAny` | **PASSED** |
| **VPS-SR-002**| Password complexity enforcement | `AuthControllerTest.register_weakPassword_returns400` | **PASSED** |
| **VPS-SR-003**| Role-Based Access Control (RBAC) | `AdminControllerTest.getDashboard_customer_returns403`<br>`VehicleControllerTest.getVehicleById_otherCustomer_returns403` | **PASSED** |
| **VPS-SR-004**| Brute-force account lockout | `AuthControllerTest.login_lockedAccount_returns403`<br>`AuthServiceTest.login_lockAfterFiveFailures` | **PASSED** |

---

## 4. System Validation Verdict

All core functional and security requirements have been validated against the system test harness. Component integration between the HTTP layer, security filters, transactional services, and relational persistence performs predictably with zero unhandled exceptions or data consistency anomalies.

**Final System Validation Recommendation:** **ACCEPTED FOR SPRINT 2 DELIVERABLE SIGN-OFF.**

