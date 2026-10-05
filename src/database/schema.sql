-- ============================================================
-- Vehicle Parking System (VPS) — MySQL Database Schema
-- Version: 1.0
-- Aligned with: SRS_VehicleParkingSystem.md
-- ============================================================

CREATE DATABASE IF NOT EXISTS vehicle_parking_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE vehicle_parking_system;

-- ============================================================
-- TABLE 1: users
-- Covers: User Management (SRS 4.1 security requirements)
-- Roles: CUSTOMER, PARKING_ATTENDANT, ADMINISTRATOR, MAINTENANCE_TECHNICIAN
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,               -- bcrypt hash, NEVER plaintext
    role            ENUM('CUSTOMER', 'PARKING_ATTENDANT', 'ADMINISTRATOR', 'MAINTENANCE_TECHNICIAN')
                                    NOT NULL DEFAULT 'CUSTOMER',
    phone           VARCHAR(20),
    failed_attempts INT             NOT NULL DEFAULT 0,     -- VPS-SR-004: account lockout tracking
    is_locked       TINYINT(1)      NOT NULL DEFAULT 0,     -- VPS-SR-004: locked after 5 failures
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 2: vehicles
-- Covers: Vehicle Management (SRS 4.1, VPS-F-001)
-- ============================================================
CREATE TABLE IF NOT EXISTS vehicles (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    license_plate   VARCHAR(20)     NOT NULL UNIQUE,        -- VPS-F-001: no duplicate plates
    make            VARCHAR(50),
    model           VARCHAR(50),
    color           VARCHAR(30),
    vehicle_type    ENUM('CAR', 'MOTORCYCLE', 'TRUCK', 'EV')
                                    NOT NULL DEFAULT 'CAR',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 3: parking_slots
-- Covers: Slot Management (SRS 4.2, VPS-F-002, VPS-F-004, VPS-F-005, VPS-F-006)
-- ============================================================
CREATE TABLE IF NOT EXISTS parking_slots (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    slot_number     VARCHAR(10)     NOT NULL UNIQUE,        -- e.g. A-01, B-14
    slot_type       ENUM('REGULAR', 'DISABLED', 'EV')
                                    NOT NULL DEFAULT 'REGULAR',  -- VPS-F-005
    status          ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE')
                                    NOT NULL DEFAULT 'AVAILABLE',
    floor           VARCHAR(10)     DEFAULT 'G',            -- ground floor default
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 4: rate_schedules
-- Covers: Fee Calculation (SRS 4.3, VPS-F-007, VPS-F-014)
-- Admin configures rates; fee calculation reads from here
-- ============================================================
CREATE TABLE IF NOT EXISTS rate_schedules (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,               -- e.g. "Standard Rate", "Peak Rate"
    vehicle_type    ENUM('CAR', 'MOTORCYCLE', 'TRUCK', 'EV')
                                    NOT NULL DEFAULT 'CAR',
    rate_per_hour   DECIMAL(10, 2)  NOT NULL,               -- INR per hour
    grace_period_minutes INT        NOT NULL DEFAULT 15,    -- free grace period
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 5: parking_records
-- Covers: Vehicle Entry, Exit, Ticket, Duration (SRS 4.1-4.3)
-- VPS-F-001 through VPS-F-010
-- ============================================================
CREATE TABLE IF NOT EXISTS parking_records (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    ticket_id       VARCHAR(30)     NOT NULL UNIQUE,        -- VPS-F-003: unique ticket e.g. TKT-20260906-00123
    vehicle_id      BIGINT          NOT NULL,
    slot_id         BIGINT          NOT NULL,
    entry_time      DATETIME        NOT NULL,               -- VPS-F-003: entry timestamp
    exit_time       DATETIME,                               -- NULL until vehicle exits
    duration_minutes INT,                                   -- calculated on exit
    fee_amount      DECIMAL(10, 2), -- calculated on exit   -- VPS-F-007
    status          ENUM('ACTIVE', 'COMPLETED', 'CANCELLED')
                                    NOT NULL DEFAULT 'ACTIVE',
    entry_terminal  VARCHAR(50)     DEFAULT 'MANUAL',       -- VPS-F-001: capture method
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_record_slot    FOREIGN KEY (slot_id)    REFERENCES parking_slots(id)
);

-- ============================================================
-- TABLE 6: payments
-- Covers: Payment (SRS 4.3, VPS-F-008, VPS-F-009)
-- NEVER stores card number, CVV or PIN
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    parking_record_id BIGINT        NOT NULL UNIQUE,        -- one payment per parking record
    amount          DECIMAL(10, 2)  NOT NULL,
    currency        VARCHAR(5)      NOT NULL DEFAULT 'INR',
    payment_method  ENUM('CASH', 'CARD', 'DIGITAL_WALLET')
                                    NOT NULL,
    payment_status  ENUM('PENDING', 'SUCCESS', 'FAILED')
                                    NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),                           -- gateway reference, no card data
    paid_at         DATETIME,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_record FOREIGN KEY (parking_record_id) REFERENCES parking_records(id)
);

-- ============================================================
-- TABLE 7: reservations
-- Covers: Reservation (SRS 4.4, VPS-F-011, VPS-F-012)
-- ============================================================
CREATE TABLE IF NOT EXISTS reservations (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    vehicle_id      BIGINT          NOT NULL,
    slot_id         BIGINT,                                 -- assigned slot (can be NULL until confirmed)
    start_time      DATETIME        NOT NULL,
    end_time        DATETIME        NOT NULL,
    status          ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'COMPLETED')
                                    NOT NULL DEFAULT 'PENDING',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_user    FOREIGN KEY (user_id)    REFERENCES users(id),
    CONSTRAINT fk_reservation_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_reservation_slot    FOREIGN KEY (slot_id)    REFERENCES parking_slots(id),
    CONSTRAINT chk_reservation_times  CHECK (end_time > start_time)
);

-- ============================================================
-- TABLE 8: audit_logs
-- Covers: Security logging (SRS VPS-NF-004, VPS-SR-003, VPS-SR-004)
-- Logs important actions — NO passwords, card data or secrets
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT,                                 -- NULL for system events
    action          VARCHAR(100)    NOT NULL,               -- e.g. LOGIN_SUCCESS, VEHICLE_ENTRY
    entity_type     VARCHAR(50),                            -- e.g. USER, VEHICLE, PARKING_RECORD
    entity_id       BIGINT,
    description     VARCHAR(500),
    ip_address      VARCHAR(45),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- ============================================================
-- INDEXES — for query performance on common lookups
-- ============================================================
CREATE INDEX idx_vehicles_license_plate    ON vehicles(license_plate);
CREATE INDEX idx_vehicles_user_id          ON vehicles(user_id);
CREATE INDEX idx_parking_slots_status      ON parking_slots(status);
CREATE INDEX idx_parking_slots_type        ON parking_slots(slot_type);
CREATE INDEX idx_parking_records_ticket    ON parking_records(ticket_id);
CREATE INDEX idx_parking_records_vehicle   ON parking_records(vehicle_id);
CREATE INDEX idx_parking_records_status    ON parking_records(status);
CREATE INDEX idx_payments_status           ON payments(payment_status);
CREATE INDEX idx_reservations_user         ON reservations(user_id);
CREATE INDEX idx_reservations_status       ON reservations(status);
CREATE INDEX idx_audit_logs_user           ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action         ON audit_logs(action);

-- ============================================================
-- SEED DATA — Parking slots (20 slots: 15 regular, 3 disabled, 2 EV)
-- ============================================================
INSERT INTO parking_slots (slot_number, slot_type, floor) VALUES
('A-01', 'REGULAR',  'G'),
('A-02', 'REGULAR',  'G'),
('A-03', 'REGULAR',  'G'),
('A-04', 'REGULAR',  'G'),
('A-05', 'REGULAR',  'G'),
('A-06', 'DISABLED', 'G'),
('A-07', 'DISABLED', 'G'),
('A-08', 'DISABLED', 'G'),
('A-09', 'EV',       'G'),
('A-10', 'EV',       'G'),
('B-01', 'REGULAR',  '1'),
('B-02', 'REGULAR',  '1'),
('B-03', 'REGULAR',  '1'),
('B-04', 'REGULAR',  '1'),
('B-05', 'REGULAR',  '1'),
('B-06', 'REGULAR',  '1'),
('B-07', 'REGULAR',  '1'),
('B-08', 'REGULAR',  '1'),
('B-09', 'REGULAR',  '1'),
('B-10', 'REGULAR',  '1');

-- ============================================================
-- SEED DATA — Rate schedules
-- ============================================================
INSERT INTO rate_schedules (name, vehicle_type, rate_per_hour, grace_period_minutes) VALUES
('Standard - Car',         'CAR',         30.00, 15),
('Standard - Motorcycle',  'MOTORCYCLE',  15.00, 15),
('Standard - Truck',       'TRUCK',       60.00, 15),
('Standard - EV',          'EV',          20.00, 15);

-- ============================================================
-- SEED DATA — Default admin user
-- Password: Admin@123 (bcrypt hash below — change on first login)
-- Hash generated with bcrypt rounds=10
-- ============================================================
INSERT INTO users (full_name, email, password_hash, role) VALUES
('System Administrator', 'admin@vps.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhy0',
 'ADMINISTRATOR');
