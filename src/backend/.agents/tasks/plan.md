# Implementation Plan — VPS Issues #20 & #3

## Pre-reading: what the exploration found

| Item | Status |
|---|---|
| `GlobalExceptionHandler.java` | EXISTS at `com.vps.controller` (not in a separate `exception` package). Already handles: `IllegalArgumentException→400`, `IllegalStateException→409` (WRONG — needs 403), `SecurityException→403`, `AccessDeniedException→403`, `MethodArgumentNotValidException→400`, `Exception→500`. Missing: `NoSuchElementException→404`, `DataIntegrityViolationException→409`. |
| `UserController.java` | Does NOT exist. |
| `UserProfileRequest.java` / `UserProfileResponse.java` | Do NOT exist. |
| `application-test.properties` | Does NOT exist. |
| `src/test/` | Does NOT exist at all — must be created. |
| H2 dependency in pom.xml | Missing — must be added for tests. |
| `RegisterRequest.role` field | EXISTS as `private Role role = Role.CUSTOMER` — BUG: caller can escalate. |
| `AuthService.register()` | BUG: honors `request.getRole()`. No `@Transactional`. No `DataIntegrityViolationException` catch. |
| `AuthService.login()` | Missing `isActive` check (isLocked checked; isActive only enforced in `UserDetailsServiceImpl` at token-validation time, not at login). No `@Transactional`. |
| `VehicleController.getVehicle/{id}` | BUG: scans all vehicles, no ownership check. |
| `VehicleController.searchByPlate` | BUG: no ownership check. |
| `VehicleService.deactivateVehicle` | Only allows `ADMINISTRATOR` — should also allow `PARKING_ATTENDANT`. |
| `VehicleRequest.licensePlate` pattern | `^[A-Z0-9\\-]{2,20}$` — rejects lowercase input before service can normalize. |
| `SecurityConfig` | Correct — no changes needed. |
| `JwtAuthFilter` / `UserDetailsServiceImpl` | Correctly check `isActive && !isLocked` on every request. No duplication needed. |
| Other controllers (Parking, Reservation, Admin) | No changes needed. |

---

## Ordered Implementation Plan

The plan is split into three logically grouped phases. Each item leaves the codebase in a buildable state. Dependency order: Phase 1 (infrastructure) → Phase 2 (auth/user) → Phase 3 (vehicle). Phases 2 and 3 are independent of each other but both depend on Phase 1.

---

### Phase 1 — Infrastructure (prerequisites for tests and exception fixes)

- [ ] 1. **Add H2 test dependency to pom.xml**
      The test suite uses `@SpringBootTest` with an in-memory database. H2 is not yet in pom.xml; without it the Spring context will fail to start during tests.
      Add inside `<dependencies>` after the `spring-security-test` block:
      ```xml
      <dependency>
          <groupId>com.h2database</groupId>
          <artifactId>h2</artifactId>
          <version>2.2.224</version>
          <scope>test</scope>
      </dependency>
      ```
      Files: `src/backend/pom.xml`
      Verify: `cd src/backend && mvn compile` — exits 0, BUILD SUCCESS.

- [ ] 2. **Create `application-test.properties`**
      Production `application.properties` targets MySQL with `ddl-auto=validate`. Tests must use H2 with `create-drop` so JPA creates the schema from entities. This file activates under `@ActiveProfiles("test")`.
      Content:
      ```properties
      spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
      spring.datasource.driver-class-name=org.h2.Driver
      spring.datasource.username=sa
      spring.datasource.password=
      spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
      spring.jpa.hibernate.ddl-auto=create-drop
      app.jwt.secret=test-secret-key-for-vps-unit-tests-must-be-256-bits-long-padding
      app.jwt.expiration-ms=3600000
      app.cors.allowed-origins=http://localhost:3000
      logging.level.com.vps=WARN
      ```
      Files: `src/backend/src/main/resources/application-test.properties`
      Verify: `mvn compile` still exits 0.

- [ ] 3. **Expand `GlobalExceptionHandler.java`**
      Three changes to the existing file at `com.vps.controller.GlobalExceptionHandler`:
      (a) Fix `IllegalStateException` handler: change `HttpStatus.CONFLICT` to `HttpStatus.FORBIDDEN`. Rationale: `AuthService.login()` uses `IllegalStateException` to signal locked/inactive accounts — the correct HTTP response is 403 Forbidden, not 409 Conflict.
      (b) Add handler for `java.util.NoSuchElementException` → 404 NOT_FOUND.
      (c) Add handler for `org.springframework.dao.DataIntegrityViolationException` → 409 CONFLICT with the safe message `"A record with the provided data already exists"` (never expose SQL constraint names or table details).
      Files: `src/backend/src/main/java/com/vps/controller/GlobalExceptionHandler.java`
      Verify: `mvn compile` exits 0.

---

### Phase 2 — Issue #20: User Registration, Login, and Profile

- [ ] 4. **Remove `role` field from `RegisterRequest.java`**
      Security: the existing `private Role role = Role.CUSTOMER` field allows any caller to POST `{"role":"ADMINISTRATOR"}` and gain elevated access. Removing the field entirely means no client-supplied role value ever reaches the service layer — the field is simply absent from deserialization.
      Remove: the `role` field declaration, its default value, and the `import com.vps.entity.User.Role` import if no longer used.
      Files: `src/backend/src/main/java/com/vps/dto/RegisterRequest.java`
      Verify: `mvn compile` exits 0.

- [ ] 5. **Fix `AuthService.register()` and add `@Transactional`**
      Three fixes in one method:
      (a) Replace `user.setRole(request.getRole() != null ? request.getRole() : User.Role.CUSTOMER)` with `user.setRole(User.Role.CUSTOMER)` — role is now hardcoded; `RegisterRequest` no longer carries a role field anyway.
      (b) Annotate `register()` with `@Transactional` (import `org.springframework.transaction.annotation.Transactional`) so the `existsByEmail` check and `save` execute in a single transaction boundary.
      (c) Wrap the `existsByEmail` check + `userRepository.save(user)` in a try/catch for `DataIntegrityViolationException`; catch it and throw `new IllegalArgumentException("Email already registered")`. This handles the race where two concurrent requests both pass the `existsByEmail` check before either commits — the DB unique constraint fires on the second insert, and the application converts it to a clean 400 rather than a 500.
      Files: `src/backend/src/main/java/com/vps/service/AuthService.java`
      Verify: `mvn compile` exits 0.

- [ ] 6. **Fix `AuthService.login()`: add `isActive` check and `@Transactional`**
      `UserDetailsServiceImpl` already checks `isActive && !isLocked` when validating JWT tokens on subsequent requests, but `login()` itself only checks `isLocked`. An inactive account can therefore obtain a new JWT. Fix: after the `isLocked` check, add:
      ```java
      if (!user.isActive()) {
          auditLogService.log(user, "LOGIN_FAILED_INACTIVE", "USER", user.getId(),
              "Login attempt on inactive account: " + user.getEmail());
          throw new IllegalStateException("Account is inactive. Please contact administrator.");
      }
      ```
      Also annotate `login()` with `@Transactional`.
      Files: `src/backend/src/main/java/com/vps/service/AuthService.java`
      Verify: `mvn compile` exits 0.

- [ ] 7. **Create `UserProfileResponse.java` DTO**
      A safe read-only view of the user's own profile. Fields: `Long id`, `String fullName`, `String email`, `String phone`, `User.Role role`, `boolean isActive`, `LocalDateTime createdAt`. Deliberately excludes `passwordHash`, `failedAttempts`, `isLocked`, `updatedAt`.
      Use `@Data @NoArgsConstructor` (Lombok). Package: `com.vps.dto`.
      Files: `src/backend/src/main/java/com/vps/dto/UserProfileResponse.java`
      Verify: `mvn compile` exits 0.

- [ ] 8. **Create `UserProfileRequest.java` DTO**
      Carries only the two fields a customer is permitted to update per SRS: `fullName` and `phone`. Annotate `fullName` with `@NotBlank(message = "Full name is required")`. `phone` is optional (nullable, no `@NotBlank`). Use `@Data @NoArgsConstructor`. Package: `com.vps.dto`.
      Files: `src/backend/src/main/java/com/vps/dto/UserProfileRequest.java`
      Verify: `mvn compile` exits 0.

- [ ] 9. **Add `getUserProfile()` and `updateUserProfile()` to `AuthService.java`**
      `getUserProfile(String email)`: load `User` by email (throw `IllegalArgumentException("User not found")` if absent), map to `UserProfileResponse` — copy id, fullName, email, phone, role, isActive, createdAt.
      `updateUserProfile(String email, UserProfileRequest request)`: load user, set only `fullName` and `phone`, call `userRepository.save(user)`, audit log with action `"PROFILE_UPDATED"`, return `UserProfileResponse`. Annotate with `@Transactional`. The method never allows changes to role, id, email, isActive, isLocked, failedAttempts, or passwordHash.
      Files: `src/backend/src/main/java/com/vps/service/AuthService.java`
      Verify: `mvn compile` exits 0.

- [ ] 10. **Create `UserController.java`**
       Implements the `GET /api/v1/users/me` and `PUT /api/v1/users/me` endpoints. Identity is derived exclusively from `@AuthenticationPrincipal UserDetails userDetails` — no client-supplied user ID. Both endpoints require authentication (enforced by `SecurityConfig.anyRequest().authenticated()`).
       ```java
       @RestController
       @RequestMapping("/api/v1/users")
       @RequiredArgsConstructor
       public class UserController {
           private final AuthService authService;

           @GetMapping("/me")
           public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
                   @AuthenticationPrincipal UserDetails userDetails) {
               UserProfileResponse profile = authService.getUserProfile(userDetails.getUsername());
               return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
           }

           @PutMapping("/me")
           public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
                   @Valid @RequestBody UserProfileRequest request,
                   @AuthenticationPrincipal UserDetails userDetails) {
               UserProfileResponse profile = authService.updateUserProfile(userDetails.getUsername(), request);
               return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
           }
       }
       ```
       Files: `src/backend/src/main/java/com/vps/controller/UserController.java`
       Verify: `mvn compile` exits 0.

- [ ] 11. **Create `AuthServiceTest.java` (Mockito unit tests)**
       Located at `src/test/java/com/vps/service/AuthServiceTest.java`. Create the `src/test/java/com/vps/` directory tree first.
       Use `@ExtendWith(MockitoExtension.class)`. Mock: `UserRepository`, `PasswordEncoder`, `JwtUtil`, `AuditLogService`.
       Test cases:
       - `register_success`: `existsByEmail=false`, `save` returns user, assert token in response, role = CUSTOMER, no passwordHash.
       - `register_duplicateEmail_applicationLevel`: `existsByEmail=true`, assert `IllegalArgumentException`.
       - `register_concurrentDuplicate_dbLevel`: `existsByEmail=false` but `save` throws `DataIntegrityViolationException`, assert `IllegalArgumentException`.
       - `register_roleIsAlwaysCustomer`: call `register()`, capture the `User` passed to `save` (via `ArgumentCaptor`), assert `user.getRole() == CUSTOMER`.
       - `login_success`: `matches=true`, `isLocked=false`, `isActive=true`, assert token returned, `failedAttempts` reset to 0.
       - `login_wrongPassword`: `matches=false`, assert `IllegalArgumentException`, `failedAttempts` incremented.
       - `login_lockedAccount`: `user.isLocked=true`, assert `IllegalStateException`.
       - `login_inactiveAccount`: `user.isActive=false`, assert `IllegalStateException`.
       - `login_lockAfterFiveFailures`: 4 prior failed attempts, wrong password again, assert `user.isLocked=true`.
       Files: `src/backend/src/test/java/com/vps/service/AuthServiceTest.java`
       Verify: `mvn test -Dtest=AuthServiceTest` — all 9 tests pass.

- [ ] 12. **Create `AuthControllerTest.java` (MockMvc integration tests)**
       Use `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`. Tests exercise the full HTTP stack including GlobalExceptionHandler.
       Test cases:
       - `POST /api/v1/auth/register` valid → HTTP 201, body has token, role = CUSTOMER.
       - `POST /api/v1/auth/register` duplicate email → HTTP 400.
       - `POST /api/v1/auth/register` with `"role":"ADMINISTRATOR"` in JSON body → HTTP 201, role = CUSTOMER (field ignored because RegisterRequest no longer has it).
       - `POST /api/v1/auth/register` weak password → HTTP 400.
       - `POST /api/v1/auth/login` valid credentials → HTTP 200, token present.
       - `POST /api/v1/auth/login` wrong password → HTTP 400.
       - `POST /api/v1/auth/login` locked account → HTTP 403.
       Files: `src/backend/src/test/java/com/vps/controller/AuthControllerTest.java`
       Verify: `mvn test -Dtest=AuthControllerTest` — all 7 tests pass.

- [ ] 13. **Create `UserControllerTest.java` (MockMvc integration tests)**
       Use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`. Register a user, obtain JWT, use it for requests.
       Test cases:
       - `GET /api/v1/users/me` no auth → HTTP 403.
       - `GET /api/v1/users/me` with valid JWT → HTTP 200, body has `fullName`, `email`, role; assert no `passwordHash` key in JSON.
       - `PUT /api/v1/users/me` with valid JWT and `{"fullName":"New Name","phone":"9999999999"}` → HTTP 200, `fullName` = "New Name" in response.
       - `PUT /api/v1/users/me` with blank `fullName` → HTTP 400.
       - Confirm `passwordHash` never appears in any profile response (assert `!jsonBody.contains("passwordHash")`).
       Files: `src/backend/src/test/java/com/vps/controller/UserControllerTest.java`
       Verify: `mvn test -Dtest=UserControllerTest` — all 5 tests pass.

---

### Phase 3 — Issue #3: Vehicle Ownership Enforcement and Fixes

- [ ] 14. **Fix license plate pattern in `VehicleRequest.java`**
        Change `@Pattern(regexp = "^[A-Z0-9\\-]{2,20}$", ...)` to `@Pattern(regexp = "^[A-Za-z0-9\\-]{2,20}$", ...)`. The service already calls `.toUpperCase()` before persisting; the current uppercase-only pattern blocks valid lowercase input before normalization can happen. Update the message to `"License plate must be 2-20 alphanumeric or hyphen characters"`.
        Files: `src/backend/src/main/java/com/vps/dto/VehicleRequest.java`
        Verify: `mvn compile` exits 0.

- [ ] 15. **Add `getVehicleById(Long, User)` to `VehicleService.java`**
        New method with ownership enforcement: load `Vehicle` by id (throw `IllegalArgumentException("Vehicle not found")` if absent). If `requester.getRole() == CUSTOMER` and `vehicle.getUser().getId() != requester.getId()`, throw `SecurityException("Not authorized to view this vehicle")`. ADMINISTRATOR and PARKING_ATTENDANT pass through unconditionally. Return `toResponse(vehicle)`.
        Files: `src/backend/src/main/java/com/vps/service/VehicleService.java`
        Verify: `mvn compile` exits 0.

- [ ] 16. **Add `getByLicensePlate(String, User)` overload to `VehicleService.java`**
        New two-argument overload with ownership enforcement: normalize plate to uppercase, load Vehicle (throw `IllegalArgumentException` if not found), apply same role/ownership check as item 15. The existing zero-argument `getByLicensePlate(String)` is preserved — it is called internally by `ParkingService` and `ReservationService` which are not affected by this change.
        Files: `src/backend/src/main/java/com/vps/service/VehicleService.java`
        Verify: `mvn compile` exits 0.

- [ ] 17. **Fix `deactivateVehicle` to allow PARKING_ATTENDANT in `VehicleService.java`**
        In the authorization check, change:
        ```java
        && requester.getRole() != User.Role.ADMINISTRATOR
        ```
        to:
        ```java
        && requester.getRole() != User.Role.ADMINISTRATOR
        && requester.getRole() != User.Role.PARKING_ATTENDANT
        ```
        Rationale: parking attendants manage vehicles on-site; they should be able to deactivate lost/removed vehicles. This already matches the list endpoint which allows both roles.
        Files: `src/backend/src/main/java/com/vps/service/VehicleService.java`
        Verify: `mvn compile` exits 0.

- [ ] 18. **Fix `getVehicle/{id}` in `VehicleController.java`**
        Replace the unsafe stream-over-all-vehicles implementation with a call to the new service method:
        ```java
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
                @PathVariable Long id,
                @AuthenticationPrincipal UserDetails userDetails) {
            User user = authService.getUserByEmail(userDetails.getUsername());
            VehicleResponse vehicle = vehicleService.getVehicleById(id, user);
            return ResponseEntity.ok(ApiResponse.success("Vehicle retrieved", vehicle));
        }
        ```
        Files: `src/backend/src/main/java/com/vps/controller/VehicleController.java`
        Verify: `mvn compile` exits 0.

- [ ] 19. **Fix `searchByPlate` in `VehicleController.java`**
        Inject `@AuthenticationPrincipal UserDetails`, resolve `User`, and call the two-argument overload:
        ```java
        @GetMapping("/search")
        public ResponseEntity<ApiResponse<VehicleResponse>> searchByPlate(
                @RequestParam String plate,
                @AuthenticationPrincipal UserDetails userDetails) {
            User user = authService.getUserByEmail(userDetails.getUsername());
            VehicleResponse vehicle = vehicleService.getByLicensePlate(plate, user);
            return ResponseEntity.ok(ApiResponse.success("Vehicle found", vehicle));
        }
        ```
        Files: `src/backend/src/main/java/com/vps/controller/VehicleController.java`
        Verify: `mvn compile` exits 0.

- [ ] 20. **Create `VehicleServiceTest.java` (Mockito unit tests)**
        Use `@ExtendWith(MockitoExtension.class)`. Mock `VehicleRepository` and `AuditLogService`. Helpers: `makeUser(Long id, User.Role role)` and `makeVehicle(Long id, Long ownerId)`.
        Test cases:
        - `addVehicle_success`: plate not duplicate, saved vehicle has uppercase plate and correct owner.
        - `addVehicle_duplicatePlate`: `existsByLicensePlate=true`, assert `IllegalArgumentException`.
        - `getVehicleById_ownerCanSeeOwnVehicle`: CUSTOMER, owns vehicle → returns response.
        - `getVehicleById_customerCannotSeeOtherVehicle`: CUSTOMER, different owner → `SecurityException`.
        - `getVehicleById_adminCanSeeAnyVehicle`: ADMINISTRATOR → returns response.
        - `getVehicleById_parkingAttendantCanSeeAnyVehicle`: PARKING_ATTENDANT → returns response.
        - `getByLicensePlate_ownerSees`: CUSTOMER owns the vehicle → returns response.
        - `getByLicensePlate_customerCannotSeeOther`: CUSTOMER doesn't own → `SecurityException`.
        - `deactivateVehicle_ownerCanDeactivate`: vehicle set inactive, saved.
        - `deactivateVehicle_parkingAttendantCanDeactivate`: no exception thrown.
        - `deactivateVehicle_customerCannotDeactivateOther`: CUSTOMER, different owner → `SecurityException`.
        - `updateVehicle_ownerCanUpdate`: fields updated, saved.
        - `updateVehicle_customerCannotUpdateOther`: CUSTOMER, different owner → `SecurityException`.
        Files: `src/backend/src/test/java/com/vps/service/VehicleServiceTest.java`
        Verify: `mvn test -Dtest=VehicleServiceTest` — all 13 tests pass.

- [ ] 21. **Create `VehicleControllerTest.java` (MockMvc integration tests)**
        Use `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`. Setup: register two customers (A and B), register one vehicle belonging to A. Obtain JWTs for both.
        Test cases:
        - `POST /api/v1/vehicles` as A, valid body → HTTP 201, plate stored uppercase.
        - `POST /api/v1/vehicles` as A, duplicate plate → HTTP 400 or 409.
        - `POST /api/v1/vehicles` as A, lowercase plate `"ka01ab1234"` → HTTP 201, response plate is `"KA01AB1234"`.
        - `GET /api/v1/vehicles` as A → only A's vehicles returned.
        - `GET /api/v1/vehicles/{vehicleAId}` as A → HTTP 200.
        - `GET /api/v1/vehicles/{vehicleAId}` as B → HTTP 403.
        - `GET /api/v1/vehicles/search?plate=KA01AB1234` as A → HTTP 200.
        - `GET /api/v1/vehicles/search?plate=KA01AB1234` as B → HTTP 403.
        - `DELETE /api/v1/vehicles/{vehicleAId}` as B → HTTP 403.
        - `PUT /api/v1/vehicles/{vehicleAId}` as B → HTTP 403.
        Files: `src/backend/src/test/java/com/vps/controller/VehicleControllerTest.java`
        Verify: `mvn test -Dtest=VehicleControllerTest` — all 10 tests pass.

---

## Full verification

After all items above are complete:

```
cd c:\Users\thrup\OneDrive\Desktop\SE\Project-Team-10\src\backend
mvn test
```

Expected: BUILD SUCCESS, all tests pass, zero failures.

---

## Files NOT changed (confirmed)

| File | Reason |
|---|---|
| `SecurityConfig.java` | Route rules already correct: `/api/v1/auth/**` public, everything else requires auth, `/api/v1/admin/**` requires ADMINISTRATOR. The new `/api/v1/users/me` endpoints fall under `anyRequest().authenticated()`. |
| `JwtAuthFilter.java` | Correctly validates tokens on every request. No duplication needed. |
| `UserDetailsServiceImpl.java` | Correctly checks `isActive && !isLocked` at token-validation time. The `isActive` check added to `login()` is a separate, earlier gate — not a duplication. |
| `ParkingController.java` | Not in scope. |
| `ReservationController.java` | Not in scope. |
| `AdminController.java` | Not in scope. |
| `ParkingService.java` / `ReservationService.java` | Use the zero-argument `VehicleService.getByLicensePlate(String)` internally, which is preserved unchanged. |
| `User.java` / `Vehicle.java` / `AuditLog.java` | No changes to entities needed. |
| All repositories | No new query methods needed. |
