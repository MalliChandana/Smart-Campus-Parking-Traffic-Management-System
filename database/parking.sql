-- ============================================================================
-- Smart Campus Parking and Traffic Management System
-- Database Setup Script: parking.sql
-- Database Name: smart_campus_parking
-- ============================================================================

DROP DATABASE IF EXISTS smart_campus_parking;
CREATE DATABASE smart_campus_parking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_campus_parking;

-- ----------------------------------------------------------------------------
-- 1. Table: users
-- Roles: Student, Faculty, Staff, Visitor, Service
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role ENUM('Student', 'Faculty', 'Staff', 'Visitor', 'Service') NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- ----------------------------------------------------------------------------
-- 2. Table: vehicles
-- Types: Bike, Car, Truck
-- ----------------------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(50) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    vehicle_type ENUM('Bike', 'Car', 'Truck') NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 3. Table: parking_zones
-- Zones: Zone A, Zone B, Zone C, etc.
-- ----------------------------------------------------------------------------
CREATE TABLE parking_zones (
    zone_id INT AUTO_INCREMENT PRIMARY KEY,
    zone_name VARCHAR(50) NOT NULL UNIQUE,
    location VARCHAR(100) NOT NULL
);

-- ----------------------------------------------------------------------------
-- 4. Table: parking_slots
-- Status: AVAILABLE, OCCUPIED, RESERVED
-- ----------------------------------------------------------------------------
CREATE TABLE parking_slots (
    slot_id INT AUTO_INCREMENT PRIMARY KEY,
    zone_id INT NOT NULL,
    slot_number VARCHAR(20) NOT NULL UNIQUE,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    FOREIGN KEY (zone_id) REFERENCES parking_zones(zone_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 5. Table: reservations
-- Status: ACTIVE, CANCELLED, COMPLETED
-- ----------------------------------------------------------------------------
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    slot_id INT NOT NULL,
    reservation_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('ACTIVE', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 6. Table: parking_sessions
-- Represents actual entry, exit, duration, and calculated fee
-- ----------------------------------------------------------------------------
CREATE TABLE parking_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    slot_id INT NOT NULL,
    entry_time DATETIME NOT NULL,
    exit_time DATETIME NULL,
    duration INT NULL, -- Duration in hours (rounded up)
    fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 7. Table: parking_passes
-- Pass types: Daily, Monthly, Student, Faculty, Visitor
-- ----------------------------------------------------------------------------
CREATE TABLE parking_passes (
    pass_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    vehicle_id INT NOT NULL,
    pass_type ENUM('Daily', 'Monthly', 'Student', 'Faculty', 'Visitor') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('ACTIVE', 'EXPIRED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 8. Table: payments
-- Payment methods: Cash, Card, UPI
-- ----------------------------------------------------------------------------
CREATE TABLE payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    session_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('Cash', 'Card', 'UPI') NOT NULL,
    payment_date DATETIME NOT NULL,
    status ENUM('PAID', 'PENDING', 'FAILED') NOT NULL DEFAULT 'PAID',
    FOREIGN KEY (session_id) REFERENCES parking_sessions(session_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ----------------------------------------------------------------------------
-- 9. Table: violations
-- Types: Wrong Parking, Overstay, Reserved Slot Violation, Unauthorized Parking
-- ----------------------------------------------------------------------------
CREATE TABLE violations (
    violation_id INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id INT NOT NULL,
    violation_type ENUM('Wrong Parking', 'Overstay', 'Reserved Slot Violation', 'Unauthorized Parking') NOT NULL,
    description TEXT NOT NULL,
    fine DECIMAL(10,2) NOT NULL,
    violation_date DATE NOT NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ============================================================================
-- STORED PROCEDURE (For demonstrating CallableStatement in JDBC)
-- Procedure: get_total_revenue
-- Returns the sum of all successful payments
-- ============================================================================
DELIMITER $$
CREATE PROCEDURE get_total_revenue(OUT total_rev DECIMAL(10,2))
BEGIN
    SELECT IFNULL(SUM(amount), 0.00) INTO total_rev
    FROM payments
    WHERE status = 'PAID';
END $$
DELIMITER ;

-- ============================================================================
-- SAMPLE DATA INSERTION
-- ============================================================================

-- 1. Insert Sample Users
INSERT INTO users (user_id, name, role, phone, email) VALUES
(1, 'Dr. Rajesh Sharma', 'Faculty', '9876543210', 'r.sharma@campus.edu'),
(2, 'Ananya Patel', 'Student', '9876543211', 'ananya.p@student.campus.edu'),
(3, 'Vikram Singh', 'Staff', '9876543212', 'vikram.s@campus.edu'),
(4, 'Siddharth Rao', 'Student', '9876543213', 'sid.rao@student.campus.edu'),
(5, 'Neha Gupta', 'Visitor', '9876543214', 'neha.gupta@gmail.com'),
(6, 'Ramesh Kumar', 'Service', '9876543215', 'ramesh.delivery@service.com'),
(7, 'Prof. Meenakshi Iyer', 'Faculty', '9876543216', 'm.iyer@campus.edu'),
(8, 'Karan Verma', 'Student', '9876543217', 'karan.v@student.campus.edu');

-- 2. Insert Sample Vehicles
INSERT INTO vehicles (vehicle_id, vehicle_number, user_id, vehicle_type) VALUES
(1, 'KA-01-AB-1234', 1, 'Car'),
(2, 'KA-03-CD-5678', 2, 'Bike'),
(3, 'KA-05-EF-9012', 3, 'Car'),
(4, 'KA-04-GH-3456', 4, 'Bike'),
(5, 'DL-01-XY-7890', 5, 'Car'),
(6, 'KA-51-TR-1122', 6, 'Truck'),
(7, 'MH-02-ZZ-4321', 7, 'Car'),
(8, 'KA-02-BK-9988', 8, 'Bike');

-- 3. Insert Parking Zones
INSERT INTO parking_zones (zone_id, zone_name, location) VALUES
(1, 'Zone A', 'Near Academic Block & Library'),
(2, 'Zone B', 'Auditorium & Sports Complex'),
(3, 'Zone C', 'Main Gate & Visitor Center');

-- 4. Insert Parking Slots (18 slots across 3 zones with mixed statuses)
INSERT INTO parking_slots (slot_id, zone_id, slot_number, status) VALUES
-- Zone A (6 slots)
(1, 1, 'A-01', 'OCCUPIED'),
(2, 1, 'A-02', 'AVAILABLE'),
(3, 1, 'A-03', 'RESERVED'),
(4, 1, 'A-04', 'AVAILABLE'),
(5, 1, 'A-05', 'AVAILABLE'),
(6, 1, 'A-06', 'AVAILABLE'),

-- Zone B (6 slots)
(7, 2, 'B-01', 'OCCUPIED'),
(8, 2, 'B-02', 'AVAILABLE'),
(9, 2, 'B-03', 'AVAILABLE'),
(10, 2, 'B-04', 'RESERVED'),
(11, 2, 'B-05', 'AVAILABLE'),
(12, 2, 'B-06', 'AVAILABLE'),

-- Zone C (6 slots)
(13, 3, 'C-01', 'OCCUPIED'),
(14, 3, 'C-02', 'AVAILABLE'),
(15, 3, 'C-03', 'AVAILABLE'),
(16, 3, 'C-04', 'AVAILABLE'),
(17, 3, 'C-05', 'AVAILABLE'),
(18, 3, 'C-06', 'AVAILABLE');

-- 5. Insert Sample Reservations
INSERT INTO reservations (reservation_id, vehicle_id, slot_id, reservation_date, start_time, end_time, status) VALUES
(1, 3, 3, CURDATE(), '09:00:00', '13:00:00', 'ACTIVE'),
(2, 7, 10, CURDATE(), '10:30:00', '14:30:00', 'ACTIVE'),
(3, 4, 8, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '14:00:00', '18:00:00', 'COMPLETED');

-- 6. Insert Sample Parking Sessions (Active and Completed)
INSERT INTO parking_sessions (session_id, vehicle_id, slot_id, entry_time, exit_time, duration, fee) VALUES
-- Completed sessions
(1, 2, 2, DATE_SUB(NOW(), INTERVAL 5 HOUR), DATE_SUB(NOW(), INTERVAL 3 HOUR), 2, 20.00),
(2, 1, 1, DATE_SUB(NOW(), INTERVAL 8 HOUR), DATE_SUB(NOW(), INTERVAL 4 HOUR), 4, 80.00),
(3, 6, 13, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 22 HOUR), 2, 60.00),
-- Currently Active sessions (exit_time IS NULL)
(4, 1, 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, NULL, 0.00),
(5, 5, 7, DATE_SUB(NOW(), INTERVAL 1 HOUR), NULL, NULL, 0.00),
(6, 6, 13, DATE_SUB(NOW(), INTERVAL 3 HOUR), NULL, NULL, 0.00);

-- 7. Insert Sample Payments for Completed Sessions
INSERT INTO payments (payment_id, session_id, amount, payment_method, payment_date, status) VALUES
(1, 1, 20.00, 'UPI', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'PAID'),
(2, 2, 80.00, 'Card', DATE_SUB(NOW(), INTERVAL 4 HOUR), 'PAID'),
(3, 3, 60.00, 'Cash', DATE_SUB(NOW(), INTERVAL 22 HOUR), 'PAID');

-- 8. Insert Sample Parking Passes
INSERT INTO parking_passes (pass_id, user_id, vehicle_id, pass_type, start_date, end_date, status) VALUES
(1, 1, 1, 'Faculty', '2026-01-01', '2026-12-31', 'ACTIVE'),
(2, 2, 2, 'Student', '2026-01-15', '2026-06-30', 'ACTIVE'),
(3, 3, 3, 'Monthly', '2026-08-01', '2026-08-31', 'ACTIVE'),
(4, 5, 5, 'Visitor', CURDATE(), CURDATE(), 'ACTIVE');

-- 9. Insert Sample Violations
INSERT INTO violations (violation_id, vehicle_id, violation_type, description, fine, violation_date) VALUES
(1, 4, 'Wrong Parking', 'Parked diagonally across two motorcycle bays near Zone A.', 200.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY)),
(2, 5, 'Reserved Slot Violation', 'Parked in Dean reserved slot without valid reservation tag.', 500.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY)),
(3, 6, 'Unauthorized Parking', 'Commercial truck parked in Zone C visitor area without security pass.', 350.00, CURDATE());

-- ============================================================================
-- End of SQL Script
-- ============================================================================
