# Software Architecture and Design Specification

**Project:** Vehicle Parking System
**Version:** 1.0
**Authors:** Thrupthi G D (PE51UG24AM476)
**Date:** 06-09-2026
**Status:** Draft

---

## Revision History

| Version | Date | Author | Change Summary |
|---|---|---|---|
| 1.0 | 06-09-2026 | Thrupthi G D | Initial SAD draft — architecture, components, tech stack |

## Approvals

| Role | Name | Signature / Date |
|---|---|---|
| Course Coordinator | | |
| Team Lead | | |

---

## Table of Contents

1. Introduction
2. Document Overview
3. Architecture
4. Design *(to be completed)*

---

## 1. Introduction

### 1.1 Purpose

This document specifies the software architecture and design of the Vehicle Parking System (VPS). It provides a structural blueprint of the system components, their interactions, technology choices, and security design for use by developers, QA engineers, and the course assessor.

### 1.2 Scope

Covers the architecture of all VPS software components: the customer entry/exit terminal, slot management engine, fee calculation service, payment integration, admin portal, and the underlying database and API layer. Hardware firmware and third-party payment gateway internals are excluded.

### 1.3 Audience

Developers, QA Engineers, Security Auditors, Course Instructors, and Maintenance Teams.

### 1.4 Definitions

| Term | Definition |
|---|---|
| VPS | Vehicle Parking System |
| ADR | Architectural Decision Record |
| PCI-DSS | Payment Card Industry Data Security Standard |
| TLS | Transport Layer Security |
| RBAC | Role-Based Access Control |
| ALPR | Automatic License Plate Recognition |
| API | Application Programming Interface |

---

## 2. Document Overview

### 2.1 How to Use This Document

This document provides architectural deliverables including component descriptions, UML diagrams, architectural decision rationale, threat model, and API design. Read Section 3 for the big-picture structure, then Section 4 for detailed design of individual components.

### 2.2 Related Documents

| Document | Location |
|---|---|
| SRS — Software Requirements Specification | `docs/SRS/SRS_VehicleParkingSystem.md` |
| STP — Software Test Plan | `docs/STP/STP_VehicleParkingSystem.md` |

---

## 3. Architecture

### 3.1 Goals & Constraints

**Goals:**
- Secure handling of vehicle and payment data
- Real-time slot tracking with < 5 second response time
- 99.9% monthly availability
- Modular design for easy maintenance and future extension

**Constraints:**
- PCI-DSS compliance for all payment flows
- ALPR camera integration is vendor-specific (must be abstracted)
- System must support degraded offline mode during network outages
- Hardware barrier APIs vary by vendor and must be wrapped behind an interface layer

---

### 3.2 Stakeholders & Concerns

| Stakeholder | Primary Concerns |
|---|---|
| Customers / Drivers | Fast entry/exit, accurate billing, clear UI |
| Parking Operators | Real-time occupancy, revenue reports, reliability |
| Bank / Payment Provider | PCI-DSS compliance, secure data handling |
| Regulators | Audit logs, data retention, accessibility |
| Developers | Modularity, clear API contracts, testability |
| Maintenance Technicians | Diagnostic access, error logging |

---

### 3.3 Component (UML) Diagram

```
+-------------------+        +-------------------+        +----------------------+
|   Entry Terminal  |        |   Exit Terminal   |        |   Admin Web Portal   |
|  (Kiosk UI)       |        |  (Kiosk UI)       |        |  (Web Browser)       |
+--------+----------+        +--------+----------+        +-----------+----------+
         |                            |                               |
         |          TLS / REST API    |                               |
         +----------------------------+-------------------------------+
                                      |
                         +------------+-------------+
                         |      API Gateway          |
                         |  (Spring Boot REST API)   |
                         +---+-------+-------+---+---+
                             |       |       |   |
               +-------------+   +---+---+   | +-+----------+
               |                 |       |   | |            |
       +-------+------+  +-------+--+  +-+--+-+--+  +------+-------+
       | Auth Service |  |  Slot    |  |   Fee    |  |   Payment    |
       | (Login/RBAC) |  | Manager  |  |Calculator|  |   Service    |
       +--------------+  +----------+  +----------+  +--------------+
                                |                           |
                         +------+------+           +--------+-------+
                         |  Database   |           | Payment Gateway|
                         | (MySQL)     |           | (External API) |
                         +-------------+           +----------------+
                                |
                         +------+------+
                         |  ALPR /     |
                         | Barrier HW  |
                         | Adapter     |
                         +-------------+
```

> *To be replaced with a draw.io / PlantUML diagram in the final submission.*

---

### 3.4 Component Descriptions

| Component | Responsibility |
|---|---|
| **Entry Terminal UI** | Captures license plate (manual or ALPR), displays slot info, prints/shows ticket |
| **Exit Terminal UI** | Scans ticket, displays fee, handles payment, prints receipt, triggers barrier |
| **Admin Web Portal** | Occupancy dashboard, rate configuration, report generation, user management |
| **API Gateway** | Single entry point for all client requests; handles routing, rate limiting, TLS termination |
| **Auth Service** | Validates credentials, issues session tokens, enforces RBAC for all roles |
| **Slot Manager** | Tracks real-time slot occupancy, handles assignment, categorisation, and availability updates |
| **Fee Calculator** | Computes parking charges based on duration and configured rate schedule |
| **Payment Service** | Processes payments via gateway, confirms transactions, triggers receipt generation |
| **Receipt Service** | Generates and delivers receipts (print or digital) |
| **Reservation Service** | Manages advance bookings and automatic grace-period release |
| **Logging & Monitoring** | Captures audit events, error logs, uptime metrics; ships to monitoring backend |
| **Database (MySQL)** | Stores vehicle records, slot states, transactions, rate schedules, user accounts |
| **ALPR / Barrier Adapter** | Abstracts vendor-specific camera and barrier hardware behind a uniform interface |
| **Payment Gateway (External)** | Third-party service for card/digital payment authorisation and settlement |

---

### 3.5 Chosen Architecture Pattern and Rationale

**Pattern chosen: Layered Architecture (with service decomposition)**

The system is structured into four layers:
1. **Presentation Layer** — Terminal UIs and Admin Web Portal
2. **Application / API Layer** — API Gateway and business services
3. **Domain / Business Logic Layer** — Slot Manager, Fee Calculator, Reservation Service
4. **Data Layer** — MySQL database and external integrations

**Rationale:**
- Layered architecture enforces clear separation of concerns, making each layer independently testable.
- Microservices were considered but rejected as overly complex for the scale and team size of this project.
- The service decomposition within the application layer (Auth, Slot, Fee, Payment as separate services) allows parallel development by team members without tight coupling.

---

### 3.6 Technology Stack & Data Stores

| Layer | Technology |
|---|---|
| Frontend (Terminal UI) | React.js (touch-optimised kiosk mode) |
| Admin Portal | React.js + Tailwind CSS |
| Backend API | Java, Spring Boot (REST) |
| Authentication | JWT tokens, Spring Security |
| Database | MySQL 8.x |
| Encryption in Transit | TLS 1.2+ (mandatory), mutual TLS for payment gateway |
| Encryption at Rest | AES-256 for sensitive fields (LP data, PAN) |
| Hardware Abstraction | Java adapter layer over vendor serial/network APIs |
| Monitoring & Logging | ELK Stack (Elasticsearch, Logstash, Kibana) |
| Deployment | Docker containers, hosted on cloud VM or on-premises server |

---

### 3.7 Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| ATM/barrier network failure | Entry/exit blocked | Offline queuing mode; local cache for slot state |
| ALPR camera misread | Wrong vehicle logged | Manual override at terminal; attendant correction flow |
| Payment gateway downtime | Customers cannot pay | Cash fallback mode; attendant manual receipt |
| Database single point of failure | System unavailable | DB replication with automated failover |
| PCI-DSS non-compliance | Legal/financial penalty | No plaintext PAN storage; TLS everywhere; annual audit |

---

### 3.8 Traceability to Requirements

| Requirement ID | Requirement Short | Component |
|---|---|---|
| VPS-F-001 | Capture license plate | ALPR/Barrier Adapter, Entry Terminal |
| VPS-F-002 | Assign slot on entry | Slot Manager |
| VPS-F-003 | Generate entry ticket | Ticket/Receipt Service |
| VPS-F-007 | Calculate parking fee | Fee Calculator |
| VPS-F-008 | Accept payment at exit | Payment Service |
| VPS-F-013 | Admin occupancy dashboard | Admin Web Portal |
| VPS-NF-001 | Response time ≤ 5s | API Gateway, all services |
| VPS-NF-003 | PCI-DSS compliance | Payment Service, Data Layer |
| VPS-SR-001 | TLS 1.2+ | API Gateway, Network Layer |
| VPS-SR-003 | RBAC | Auth Service |

---

### 3.9 Security Architecture

**Threat Model (STRIDE):**

| Threat | Example | Mitigation |
|---|---|---|
| **Spoofing** | Fake admin login | JWT authentication + RBAC; account lockout after 5 failures (VPS-SR-004) |
| **Tampering** | Modifying transaction records | DB integrity constraints; audit log with tamper-evident hashing |
| **Repudiation** | Denying a transaction occurred | Timestamped, signed audit logs retained for 5 years (VPS-NF-004) |
| **Information Disclosure** | Payment data exposed in logs | No PAN/PIN in logs; AES-256 at rest; TLS in transit (VPS-SR-001, VPS-SR-005) |
| **Denial of Service** | Flooding the API | Rate limiting at API Gateway; load balancing |
| **Elevation of Privilege** | Customer accessing admin panel | RBAC strictly enforced; roles validated server-side on every request (VPS-SR-003) |

---

## 4. Design

> **Status: In Progress** — Sequence diagrams and detailed API design to be added in the next revision.

### 4.1 Design Overview

The VPS follows a request-response model for all terminal interactions. Each transaction (entry, exit, payment) is atomic — if any step fails, the system rolls back and notifies the user. All state changes (slot occupancy, payment status) are persisted to the database before hardware actions (barrier open) are triggered, ensuring consistency even during partial failures.
