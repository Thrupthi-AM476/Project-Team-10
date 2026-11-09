# Software Test Deliverable 2.b.i: Unit Test Report

**Project:** Vehicle Parking System (VPS) — Team 10  
**Artifact Version:** 1.0.0  
**Test Framework:** JUnit 5 (Jupiter), Mockito 5.x, AssertJ  
**Harness / Runner:** Maven Surefire Plugin 3.2.5  
**Execution Timestamp:** 2026-11-09  
**Result:** **100% PASSED (96 / 96 Tests)**

---

## 1. Test Run Overview

All automated unit and slice tests for the Vehicle Parking System backend were executed in a single clean test cycle. The test suite achieved a **100% pass rate** with zero test failures, zero runtime errors, and zero skipped tests.

```
========================================================================================
                               TEST EXECUTION SUMMARY
========================================================================================
Total Tests Executed:                     96
Tests Passed:                             96  (100.0%)
Failures:                                  0  (0.0%)
Errors:                                    0  (0.0%)
Skipped:                                   0  (0.0%)
Cumulative Test Duration:                 42.94 seconds
Build Result:                             BUILD SUCCESS
========================================================================================
```

> **Note on Test Cardinality:** 96 automated JUnit test methods map to 56 formal functional/security/integration test specifications in the Test Case Document, where multiple boundary, parameterised, and negative path unit tests validate a single formal Test Case ID.


---

## 2. Test Suite Breakdown by Class

The table below summarizes execution statistics across all 10 test classes:

| Test Class | Category / Layer | Total Tests | Passed | Failed | Errors | Skipped | Time (s) |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| `ParkingServiceTest` | Service Unit Test | 19 | 19 | 0 | 0 | 0 | 2.31 s |
| `ReservationServiceTest` | Service Unit Test | 17 | 17 | 0 | 0 | 0 | 0.24 s |
| `VehicleServiceTest` | Service Unit Test | 13 | 13 | 0 | 0 | 0 | 0.17 s |
| `VehicleServiceListTest` | Service Unit Test | 1 | 1 | 0 | 0 | 0 | 0.02 s |
| `AuthServiceTest` | Service Unit Test | 9 | 9 | 0 | 0 | 0 | 7.70 s |
| `VehicleControllerTest` | Controller Integration Test | 9 | 9 | 0 | 0 | 0 | 4.10 s |
| `ParkingControllerTest` | Controller Integration Test | 8 | 8 | 0 | 0 | 0 | 2.33 s |
| `AdminControllerTest` | Controller Integration Test | 8 | 8 | 0 | 0 | 0 | 23.66 s |
| `AuthControllerTest` | Controller Integration Test | 7 | 7 | 0 | 0 | 0 | 1.40 s |
| `UserControllerTest` | Controller Integration Test | 5 | 5 | 0 | 0 | 0 | 1.01 s |
| **Total** | | **96** | **96** | **0** | **0** | **0** | **42.94 s** |

---

## 3. Granular Test Case Inventory

### 3.1. `ParkingServiceTest` (19 Tests)
- `processExit_alreadyCompleted_throwsIllegalStateException` (1.766s) — Guards against double exit on completed tickets.
- `processEntry_noSlots_throwsIllegalStateException` (0.033s) — Enforces barrier closure when lot is full (VPS-F-006).
- `processExit_afterGracePeriod_calculatesFeeAndRecordsPayment` (0.024s) — Validates duration-based fee computation (VPS-F-007).
- `processExit_ownVehicle_succeeds` (0.033s) — Verifies normal customer exit lifecycle (VPS-F-010).
- `updateSlot_WithBlankStatus_DoesNotUpdateStatus` (0.095s) — Boundary check: ignores whitespace status strings.
- `updateSlot_WithValidFields_UpdatesAllFields` (0.025s) — Updates slot type, floor, and operational status.
- `processEntry_otherUserVehicle_throwsSecurityException` (0.015s) — Prevents customers from checking in vehicles they do not own.
- `updateSlot_WithNullFields_DoesNotOverwriteValue` (0.017s) — Preserves existing slot attributes when request fields are null.
- `getParkingHistory_staffCanViewAny` (0.045s) — Verifies staff role bypass for audit inspections.
- `processEntry_ownVehicle_succeeds` (0.013s) — Standard entry: assigns available slot and marks it occupied (VPS-F-002).
- `createSlot_duplicateNumber_throwsIllegalArgumentException` (0.033s) — Rejects slot numbers already in inventory.
- `createSlot_success` (0.025s) — Persists new parking slot with default floor and category (VPS-F-005).
- `processExit_otherUserVehicle_throwsSecurityException` (0.008s) — Restricts checkout to vehicle owner or admin.
- `processEntry_preferredSlotType_isSelectedAndReturned` (0.041s) — Prioritizes preferred vehicle category (EV/Disabled).
- `getParkingHistory_otherUserVehicle_throwsSecurityException` (0.018s) — Prevents cross-customer data leakage.
- `processEntry_alreadyParked_throwsIllegalStateException` (0.012s) — Prevents duplicate active sessions for the same vehicle.
- `processExit_AsAdminOrOperator_BypassesCustomerOwnershipCheck` (0.016s) — Authorizes admin/attendant manual override at exit.
- `processExit_withinGracePeriod_feeIsZero` (0.011s) — Confirms 0 INR fee when exit occurs within grace period window.
- `processExit_ticketNotFound_throwsIllegalArgumentException` (0.008s) — Rejects non-existent ticket IDs.

### 3.2. `ReservationServiceTest` (17 Tests)
- `createReservation_NoAvailableSlots_ThrowsException` (0.025s) — Handles fully booked slot capacity.
- `getUserReservations_And_GetAllReservations_ReturnResponses` (0.028s) — Validates user and system-wide reservation lists.
- `createReservation_VehicleNotOwnedByCustomer_ThrowsException` (0.017s) — Enforces vehicle-user ownership check.
- `cancelReservation_NotFound_ThrowsException` (0.000s) — Handles missing reservation IDs gracefully.
- `createReservation_Success_WithPreferredType` (0.000s) — Books slot matching preferred vehicle category (VPS-F-011).
- `createReservation_SlotReservedInWindow_ThrowsException` (0.012s) — Prevents overlapping reservations on the same slot.
- `createReservation_VehicleNotFound_ThrowsException` (0.004s) — Validates vehicle repository existence.
- `createReservation_Success_WithoutPreferredType` (0.016s) — Falls back to general available slots when type is omitted.
- `createReservation_WithBlankAndInvalidSlotTypes` (0.011s) — Handles whitespace and invalid enum strings cleanly.
- `cancelReservation_AlreadyCompleted_ThrowsException` (0.008s) — Disallows cancelling already serviced reservations.
- `expireOverdueReservations_ProcessesExpiredList` (0.007s) — Transitions unfulfilled bookings past end-time to EXPIRED.
- `expireOverdueReservations_EmptyList_DoesNothing` (0.014s) — Boundary test for sweeper with zero expired records.
- `cancelReservation_UnauthorizedUser_ThrowsException` (0.008s) — Blocks non-owner, non-admin cancellation attempts.
- `cancelReservation_AsAdmin_OverridesOwnership` (0.009s) — Allows administrators to cancel any user reservation.
- `cancelReservation_AlreadyCancelled_ThrowsException` (0.016s) — Idempotency guard on cancelled records.
- `cancelReservation_AsOwner_Success` (0.008s) — Customer cancels their own pending reservation (VPS-F-012).
- `createReservation_EndTimeNotAfterStartTime_ThrowsException` (0.022s) — Rejects backward or zero-duration reservation windows.

### 3.3. `VehicleServiceTest` & `VehicleServiceListTest` (14 Tests)
- `updateVehicle_ownerCanUpdate` (0.024s) — Customer updates model/color of their registered vehicle.
- `addVehicle_success` (0.012s) — Standard vehicle registration.
- `getByLicensePlate_customerCannotSeeOther` (0.005s) — Privacy isolation across customers.
- `deactivateVehicle_parkingAttendantCanDeactivate` (0.018s) — Staff deactivates vehicle.
- `getByLicensePlate_ownerSees` (0.008s) — Customer retrieves their vehicle by plate.
- `getVehicleById_parkingAttendantCanSeeAnyVehicle` (0.012s) — Attendant lookup access.
- `deactivateVehicle_customerCannotDeactivateOther` (0.005s) — Customer cannot deactivate other accounts' vehicles.
- `updateVehicle_customerCannotUpdateOther` (0.007s) — Customer cannot update other accounts' vehicles.
- `getVehicleById_customerCannotSeeOtherVehicle` (0.008s) — Prevents direct ID enumeration.
- `getVehicleById_ownerCanSeeOwnVehicle` (0.008s) — Owner fetches vehicle by ID.
- `getVehicleById_adminCanSeeAnyVehicle` (0.004s) — Admin universal vehicle access.
- `deactivateVehicle_ownerCanDeactivate` (0.004s) — Owner deactivates vehicle.
- `addVehicle_duplicatePlate` (0.017s) — Rejects duplicate license plate numbers.
- `getAllVehicles_includesActiveAndInactiveVehicles` (0.017s) — Verifies complete inventory filter.

### 3.4. `AuthServiceTest` (9 Tests)
- `login_lockAfterFiveFailures` (6.960s) — Enforces brute-force lockout after 5 consecutive bad passwords (VPS-SR-004).
- `register_roleCannotBeElevated` (0.142s) — Disallows self-assigning ADMIN role during registration.
- `login_inactiveAccount` (0.049s) — Prevents disabled accounts from authenticating.
- `login_success` (0.130s) — Generates valid JWT and resets failed counter upon correct credentials.
- `register_success` (0.065s) — Registers new user with hashed password and CUSTOMER role.
- `login_accountLocked` (0.068s) — Blocks authentication while lock duration remains active.
- `register_duplicateEmail` (0.075s) — Rejects pre-existing email addresses.
- `register_concurrentDuplicate` (0.043s) — Handles database unique constraint collisions.
- `login_invalidPassword` (0.025s) — Increments failure count on bad password.

### 3.5. Web Controller Integration Tests (37 Tests)
- **`AdminControllerTest` (8 tests):** Rate creation/updating, RBAC checks on dashboard and revenue reports.
- **`AuthControllerTest` (7 tests):** Registration validation, password complexity, login token emission.
- **`ParkingControllerTest` (8 tests):** Check-in, checkout, live slot availability endpoint, customer history.
- **`UserControllerTest` (5 tests):** Profile inspection, sanitized response (no password leakage), profile updates.
- **`VehicleControllerTest` (9 tests):** Plate normalization, registration endpoints, ownership restrictions.

---

## 4. Defect & Failure Log

- **Discovered Defects during Execution:** 0
- **Regression Breaches:** 0
- **Flaky Tests:** 0
- **Conclusion:** The backend test harness is completely stable and green across all 96 automated tests.

