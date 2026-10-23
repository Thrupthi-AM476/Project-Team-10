# Vehicle Parking System — Project Team 10

| Field | Details |
|---|---|
| **Project** | Vehicle Parking System (VPS) |
| **Team** | Team 10 |
| **Version** | 2.0 |
| **Status** | In Progress |
| **Repository** | [GitHub](https://github.com/Thrupthi-AM476/Project-Team-10) |

## Project Description

A full-stack web application for managing parking slots and vehicle tracking. The system handles vehicle entry/exit, real-time slot availability, fee calculation, payments, reservations, and administrative monitoring.

## Technology Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.3.4, Spring Security, Spring Data JPA |
| **Authentication** | JWT (jjwt 0.12.3), BCrypt password hashing |
| **Database** | MySQL 8.x (production), H2 in-memory (tests) |
| **Frontend** | React 18, Vite 5, React Router 6 |
| **Build** | Maven (backend), npm/Vite (frontend) |
| **CI** | GitHub Actions |

## Repository Structure

```
Project-Team-10/
├── .github/workflows/ci.yml       ← CI pipeline
├── docs/
│   ├── SRS/                        ← Software Requirements Specification
│   ├── SAD/                        ← Software Architecture & Design
│   ├── STP/                        ← Software Test Plan
│   └── TestCases/                  ← Test case workbook
├── src/
│   ├── backend/                    ← Spring Boot REST API
│   │   ├── pom.xml
│   │   └── src/
│   │       ├── main/java/com/vps/
│   │       │   ├── controller/      ← REST controllers
│   │       │   ├── service/         ← Business logic
│   │       │   ├── entity/          ← JPA entities
│   │       │   ├── repository/      ← Spring Data JPA repositories
│   │       │   ├── dto/             ← Request/Response DTOs
│   │       │   └── security/        ← JWT, Spring Security config
│   │       ├── main/resources/
│   │       │   ├── application.properties
│   │       │   └── application-test.properties
│   │       └── test/java/com/vps/   ← Unit & integration tests
│   ├── frontend/                    ← React + Vite SPA
│   │   ├── package.json
│   │   ├── vite.config.js
│   │   └── src/
│   │       ├── api/                 ← API client
│   │       ├── components/          ← Reusable UI components
│   │       ├── context/            ← Auth context
│   │       ├── pages/              ← Route pages
│   │       └── index.css           ← Global styles
│   └── database/
│       └── schema.sql              ← MySQL schema + seed data
└── README.md
```

## Database Schema

The system uses 8 tables aligned with the SRS:

| Table | Purpose |
|---|---|
| `users` | User accounts with roles (CUSTOMER, PARKING_ATTENDANT, ADMINISTRATOR, MAINTENANCE_TECHNICIAN) |
| `vehicles` | Registered vehicles with unique license plates |
| `parking_slots` | Parking slots (REGULAR, DISABLED, EV) with status (AVAILABLE, OCCUPIED, RESERVED, OUT_OF_SERVICE) |
| `parking_records` | Parking sessions (entry/exit, ticket, duration, fee) |
| `payments` | Payment records (PENDING, SUCCESS, FAILED) — no card data stored |
| `rate_schedules` | Configurable parking rates per vehicle type with grace period |
| `reservations` | Advance slot reservations |
| `audit_logs` | Security audit trail |

Seed data includes 20 parking slots, 4 rate schedules, and a default admin user (`admin@vps.com` / `Admin@123`).

## API Endpoints

### Authentication (public)
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new customer |
| POST | `/api/v1/auth/login` | Login and receive JWT |

### User Profile (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/users/me` | Get own profile |
| PUT | `/api/v1/users/me` | Update full name and phone |

### Vehicles (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/vehicles` | List vehicles (own for customers, all for staff) |
| POST | `/api/v1/vehicles` | Register a vehicle |
| GET | `/api/v1/vehicles/{id}` | Get vehicle by ID (ownership enforced) |
| GET | `/api/v1/vehicles/search?plate=...` | Search by plate (ownership enforced) |
| PUT | `/api/v1/vehicles/{id}` | Update vehicle (owner or admin) |
| DELETE | `/api/v1/vehicles/{id}` | Deactivate vehicle (owner, admin, or attendant) |

### Parking (public slots, authenticated for operations)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/parking/slots` | All slots (public) |
| GET | `/api/v1/parking/slots/available` | Available slots (public) |
| POST | `/api/v1/parking/slots` | Create slot (staff/admin) |
| PUT | `/api/v1/parking/slots/{id}` | Update slot (staff/admin) |
| DELETE | `/api/v1/parking/slots/{id}` | Deactivate slot (staff/admin) |
| POST | `/api/v1/parking/entry` | Park vehicle (ownership enforced) |
| POST | `/api/v1/parking/exit` | Exit + fee + payment (ownership enforced) |
| GET | `/api/v1/parking/history` | Parking history (own/all) |
| GET | `/api/v1/parking/active-session` | Active session for user |
| GET | `/api/v1/parking/history/vehicle/{id}` | Vehicle history (ownership enforced) |

### Reservations (authenticated)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/reservations` | List reservations |
| POST | `/api/v1/reservations` | Create reservation |
| DELETE | `/api/v1/reservations/{id}` | Cancel reservation |
| POST | `/api/v1/reservations/expire` | Expire overdue (staff/admin) |

### Admin (ADMINISTRATOR only)
| Method | Path | Description |
|---|---|---|
| GET | `/api/v1/admin/dashboard` | Dashboard statistics |
| GET | `/api/v1/admin/reports` | Date-filtered parking report |
| GET | `/api/v1/admin/rates` | List rate schedules |
| POST | `/api/v1/admin/rates` | Create rate schedule |
| PUT | `/api/v1/admin/rates/{id}` | Update rate schedule |

## Setup Instructions

### Prerequisites
- Java 21+ (JDK)
- Maven 3.8+
- MySQL 8.x
- Node.js 20+ and npm

### Backend

1. Create the MySQL database:
   ```bash
   mysql -u root -p < src/database/schema.sql
   ```

2. Configure credentials in `src/backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.password=your_actual_password
   app.jwt.secret=your-strong-secret-key-at-least-256-bits-long
   ```

3. Run the backend:
   ```bash
   cd src/backend
   mvn spring-boot:run
   ```
   The API starts on `http://localhost:8080`.

### Frontend

1. Copy the environment file and configure:
   ```bash
   cd src/frontend
   cp .env.example .env
   ```

2. Install and run:
   ```bash
   npm install
   npm run dev
   ```
   The frontend starts on `http://localhost:3000`.

## Test Execution

### Backend tests (H2 in-memory, no MySQL required)
```bash
cd src/backend
mvn test
```

### Frontend build
```bash
cd src/frontend
npm run build
```

## Demo Workflow

1. **Register a customer** at `http://localhost:3000/register`
2. **Log in** with the new account
3. **Register a vehicle** from the Vehicles page
4. **View parking availability** on the Parking page
5. **Park the vehicle** — allocate a slot and get a ticket
6. **Process exit** — see duration, calculated fee, and pay (simulated)
7. **View receipt** with all details
8. **Log in as admin** (`admin@vps.com` / `Admin@123`) to access the admin dashboard, reports, and rate configuration

## Security Features

- Public registration always assigns CUSTOMER role (no privilege escalation)
- BCrypt password hashing — passwords never stored or returned in plaintext
- JWT-based stateless authentication
- Role-based access control (RBAC) enforced server-side
- Account lockout after 5 failed login attempts
- Inactive/locked accounts cannot authenticate
- Ownership enforcement on vehicles, parking sessions, and history
- No stack traces, SQL, or internal details in error responses
- No card numbers, CVV, or PINs stored — payment is simulated
- Audit logging for security-relevant actions (no secrets logged)

## Documents

| Document | Description | Location |
|---|---|---|
| SRS | Software Requirements Specification | [docs/SRS/SRS_Vechicle_Parking_System.pdf](docs/SRS/SRS_Vechicle_Parking_System.pdf) |
| SAD | Software Architecture & Design | [docs/SAD/SAD_VehicleParkingSystem.pdf](docs/SAD/SAD_VehicleParkingSystem.pdf) |
| STP | Software Test Plan | [docs/STP/STP_VehicleParkingSystem.md](docs/STP/STP_VehicleParkingSystem.md) |
| Test Cases | Test case workbook | [docs/TestCases/Vehicle_Parking_System_Test_Cases.xlsx](docs/TestCases/Vehicle_Parking_System_Test_Cases.xlsx) |

## Known Limitations

- Payment processing is simulated (no real payment gateway integration)
- ALPR (Automatic License Plate Recognition) is not integrated; plates are entered manually
- Report export is available via API (CSV/PDF export not yet implemented)
- Reservation auto-expiry requires manual triggering via the `/reservations/expire` endpoint
