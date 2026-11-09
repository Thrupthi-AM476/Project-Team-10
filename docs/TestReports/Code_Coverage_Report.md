# Software Test Deliverable 2.b.iii: Code Coverage Report

**Project:** Vehicle Parking System (VPS) — Team 10  
**Artifact Version:** 1.0.0  
**Build Tooling:** Apache Maven 3.x, OpenJDK 21, JaCoCo Maven Plugin 0.8.12  
**Execution Timestamp:** 2026-11-09  
**Quality Gate Status:** **PASSED** (Instruction Coverage: **81%** ≥ 70% threshold; Line Coverage: **84%** ≥ 75% threshold)

---

## 1. Executive Summary

This Code Coverage Report documents the structural code coverage achieved across the Vehicle Parking System backend suite. Testing was performed using automated unit and integration tests executing on the Spring Boot 3.3.4 test harness with an in-memory H2 database engine and MockMvc.

Metric collection was enforced by the `jacoco-maven-plugin:0.8.12` attached to the `test` phase. Following the implementation of test fixtures for `ReservationServiceTest` and slot mutation / authorization tests in `ParkingServiceTest`, the system achieved **81% instruction coverage** and **84% line coverage**, comfortably surpassing the 70% quality gate established in Section 14 of the Software Test Plan (STP).

```
========================================================================================
                      OVERALL BACKEND COVERAGE METRICS
========================================================================================
Metric                   Covered / Total          Percentage       Target Quality Gate
----------------------------------------------------------------------------------------
Instruction Coverage     2,541 / 3,130 Instructions    81.18%      ≥ 70%  [PASSED]
Line Coverage              606 /   723 Lines           83.82%      ≥ 75%  [PASSED]
Branch Coverage            112 /   156 Decision Points 71.79%      ≥ 60%  [PASSED]
Method Coverage             99 /   146 Methods         67.81%      ≥ 65%  [PASSED]
Class Coverage              18 /    19 Classes         94.74%      ≥ 90%  [PASSED]
Complexity Coverage        138 /   224 Paths           61.61%      ≥ 60%  [PASSED]
========================================================================================
```

---

## 2. Package-Level Breakdown

The application follows a clean layered architecture (`service`, `controller`, `security`). The business service layer, containing core financial, slot allocation, and reservation algorithms, demonstrates strong coverage at **83.9%**.

| Package | Instructions (Cov / Total) | Instruction % | Branches (Cov / Total) | Branch % | Lines (Cov / Total) | Line % | Methods (Cov / Total) | Method % |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **`com.vps.service`** | 1,955 / 2,330 | **83.9%** | 99 / 132 | **75.0%** | 471 / 541 | **87.1%** | 56 / 86 | 65.1% |
| **`com.vps.controller`** | 306 / 496 | **61.7%** | 4 / 10 | **40.0%** | 73 / 115 | **63.5%** | 25 / 39 | 64.1% |
| **`com.vps.security`** | 277 / 296 | **93.6%** | 9 / 14 | **64.3%** | 61 / 64 | **95.3%** | 17 / 19 | 89.5% |
| **`com.vps` (Application)** | 3 / 8 | **37.5%** | 0 / 0 | n/a | 1 / 3 | **33.3%** | 1 / 2 | 50.0% |
| **Total** | **2,541 / 3,130** | **81.2%** | **112 / 156** | **71.8%** | **606 / 723** | **83.8%** | **99 / 146** | **67.8%** |

---

## 3. Class-by-Class Coverage Inventory

The table below details every bytecode class analyzed by the JaCoCo agent in `vps-backend`:

| Class | Package | Missed Inst. | Cov. Inst. | Inst. Cov % | Missed Lines | Cov. Lines | Line Cov % | Missed Methods | Cov. Methods |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| `ReservationService` | `com.vps.service` | 1 | 326 | **99.7%** | 0 | 67 | **100.0%** | 0 | 11 |
| `AuthService` | `com.vps.service` | 20 | 316 | **94.0%** | 0 | 66 | **100.0%** | 4 | 6 |
| `ParkingService` | `com.vps.service` | 101 | 728 | **87.8%** | 28 | 189 | **87.1%** | 9 | 21 |
| `VehicleService` | `com.vps.service` | 54 | 327 | **85.8%** | 4 | 72 | **94.7%** | 7 | 9 |
| `RateScheduleService` | `com.vps.service` | 44 | 119 | **73.0%** | 6 | 29 | **82.9%** | 3 | 6 |
| `AuditLogService` | `com.vps.service` | 8 | 25 | **75.8%** | 2 | 8 | **80.0%** | 1 | 1 |
| `AdminService` | `com.vps.service` | 147 | 114 | **43.7%** | 30 | 40 | **57.1%** | 6 | 2 |
| `AuthController` | `com.vps.controller` | 0 | 22 | **100.0%** | 0 | 5 | **100.0%** | 0 | 2 |
| `UserController` | `com.vps.controller` | 0 | 27 | **100.0%** | 0 | 7 | **100.0%** | 0 | 3 |
| `AdminController` | `com.vps.controller` | 8 | 42 | **84.0%** | 2 | 10 | **83.3%** | 1 | 4 |
| `VehicleController` | `com.vps.controller` | 33 | 84 | **71.8%** | 7 | 20 | **74.1%** | 3 | 6 |
| `ParkingController` | `com.vps.controller` | 54 | 90 | **62.5%** | 12 | 22 | **64.7%** | 4 | 7 |
| `GlobalExceptionHandler` | `com.vps.controller` | 25 | 41 | **62.1%** | 7 | 9 | **56.3%** | 4 | 3 |
| `ReservationController` | `com.vps.controller` | 70 | 0 | **0.0%** | 14 | 0 | **0.0%** | 4 | 0 |
| `SecurityConfig` | `com.vps.security` | 0 | 123 | **100.0%** | 0 | 29 | **100.0%** | 0 | 8 |
| `JwtAuthFilter` | `com.vps.security` | 0 | 61 | **100.0%** | 0 | 13 | **100.0%** | 0 | 3 |
| `UserDetailsServiceImpl` | `com.vps.security` | 8 | 40 | **83.3%** | 1 | 8 | **88.9%** | 1 | 3 |
| `JwtUtil` | `com.vps.security` | 11 | 53 | **82.8%** | 2 | 11 | **84.6%** | 1 | 5 |
| `VpsApplication` | `com.vps` | 5 | 3 | **37.5%** | 2 | 1 | **33.3%** | 1 | 1 |

---

## 4. Exclusion Rules & Architecture Boundary Definitions

Per enterprise testing guidelines and academic standards, bytecode exclusion rules were configured inside `pom.xml` under `jacoco-maven-plugin` `<configuration><excludes>`:

```xml
<excludes>
    <!-- Exclude application bootstrap class from skewing metrics -->
    <exclude>**/VpsBackendApplication.*</exclude>
    <!-- Exclude pure infrastructure configuration beans -->
    <exclude>com/vps/config/**</exclude>
    <!-- Exclude pure Data Transfer Objects (DTOs) without business logic -->
    <exclude>com/vps/dto/**</exclude>
    <!-- Exclude JPA Entities and schema mappings (validated via integration DB tests) -->
    <exclude>com/vps/entity/**</exclude>
</excludes>
```

### Rationale for Exclusions:
1. **Models and DTOs (`com.vps.entity.**`, `com.vps.dto.**`):** Contain Lombok-generated accessors, mutators, `equals`, `hashCode`, and constructors. Measuring statement coverage on generated bytecode produces an artificial inflation or dilution of test efficacy without verifying operational behavior.
2. **Configuration Beans (`com.vps.config.**`):** Spring infrastructure configuration beans (CORS setup, OpenAPI/Swagger beans) are exercised during application startup and validated through container tests rather than unit assertions.
3. **Application Bootstrap (`VpsBackendApplication`):** Contains the standard `public static void main(String[] args)` method which delegates directly to `SpringApplication.run`.

---

## 5. Audit Trail & Verification Environment

- **Operating System:** Windows 11 (build-compliant)
- **Runtime Environment:** OpenJDK 64-Bit Server VM (build 21+35)
- **Build Harness:** Maven 3.x
- **Surefire Test Plugin:** `maven-surefire-plugin:3.2.5`
- **JaCoCo Agent:** `org.jacoco:jacoco-maven-plugin:0.8.12`
- **Surefire Execution Argument:** `@{argLine} -Dnet.bytebuddy.experimental=true` (guarantees dynamic bytecode instrumentation alongside Mockito Byte Buddy agent).
- **Execution Command:** `mvn clean test jacoco:report`
- **Exit Status:** `0 (BUILD SUCCESS)`
- **Artifact Path:** `src/backend/target/site/jacoco/index.html`

