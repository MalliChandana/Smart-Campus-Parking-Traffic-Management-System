# Smart Campus Parking and Traffic Management System

A complete, robust, and functional **Java Desktop GUI Application** developed using **AWT**, **JDBC**, and **MySQL Database** for college-level demonstration and automated campus parking & traffic administration.

---

## 1. Problem Statement Summary

Modern university and college campuses accommodate thousands of students, faculty members, staff, delivery personnel, and visitors daily. Traditional manual parking entry and paper ticketing lead to severe traffic congestion at security gates, unauthorized parking, unmonitored overstays, and revenue leakages.

The **Smart Campus Parking and Traffic Management System** solves these challenges by providing a centralized desktop portal that:
* Tracks real-time parking slot occupancy across campus zones (Available, Occupied, Reserved).
* Manages advance slot reservations and prevents time-slot conflicts.
* Automates gate entry and exit logging.
* Calculates precise parking duration fees based on vehicle type (Bike, Car, Truck).
* Issues parking passes/permits for students, faculty, and regular visitors.
* Logs traffic and parking violations with associated penalty fines.
* Generates audit-ready financial, occupancy, and utilization reports using JDBC Statement types including stored procedures.

---

## 2. Key Features

1. **Operator Authentication & Login**:
   * Pre-configured administrative login screen (`admin` / `admin123`) with quick-fill demo button.
2. **Interactive Real-Time Dashboard**:
   * Summary metric cards for **Total Slots**, **Available Slots**, **Occupied Slots**, **Reserved Slots**, and **Total Revenue**.
   * Live parked vehicle table monitor and quick-action shortcuts.
3. **User Management**:
   * Register, search, update, and delete campus members across roles (`Student`, `Faculty`, `Staff`, `Visitor`, `Service`).
   * Input validation for email format, phone numbers, and duplicate entries.
4. **Vehicle Management**:
   * Register campus vehicles (`Bike`, `Car`, `Truck`) linked to registered users.
   * Unique vehicle registration number validation.
5. **Parking Zones & Slots**:
   * Multi-zone slot tracking across **Zone A (Academic)**, **Zone B (Sports & Auditorium)**, and **Zone C (Visitors)**.
   * Color-coded status badges (`AVAILABLE` in green, `OCCUPIED` in red, `RESERVED` in amber).
   * Filter slots by zone, status, or search slot number.
6. **Advance Slot Reservations**:
   * Step-by-step reservation workflow with automatic conflict checking.
   * Atomic slot reservation that locks slot status to `RESERVED`.
   * Easy cancellation workflow returning slots to `AVAILABLE`.
7. **Gate Entry & Exit Management**:
   * **Vehicle Entry**: Verifies registered vehicle, checks for double-entry, assigns available slot, and marks slot `OCCUPIED`.
   * **Vehicle Exit & Fee Calculation**: Calculates parking duration (hours rounded up) and determines fee based on vehicle type:
     * **Bike**: ₹10 per hour
     * **Car**: ₹20 per hour
     * **Truck**: ₹30 per hour
   * Automatic release of parking bay back to `AVAILABLE`.
8. **Payment Processing**:
   * Record payments for completed sessions using **Cash**, **Card**, or **UPI**.
   * Search payment history and track total collected revenue.
9. **Parking Passes & Permits**:
   * Issue long-term and temporary passes (`Daily`, `Monthly`, `Student`, `Faculty`, `Visitor`).
   * Track validity dates and cancel or renew passes.
10. **Traffic & Parking Violations**:
    * Issue violation penalty tickets (`Wrong Parking`, `Overstay`, `Reserved Slot Violation`, `Unauthorized Parking`).
    * Track penalty fines by vehicle.
11. **Comprehensive Analytics & Reports**:
    * **Occupancy & Utilization Report**: Capacity utilization percentage per zone.
    * **Zone-wise Report**: Complete inventory of slots.
    * **Vehicle-wise Report**: Session counts, hours parked, and cumulative fees paid.
    * **Revenue Breakdown**: Financial breakdown by payment method.
    * **Parking Sessions History**: In-depth historical audit log.
    * **CallableStatement Stored Procedure Demo**: Executes the MySQL stored procedure `get_total_revenue()` and displays the returned revenue.

---

## 3. Technology Stack

* **Language**: Java (JDK 8 or higher)
* **GUI Toolkit**:Java AWT (`CardLayout`, `GridBagLayout`, `BorderLayout`)
* **Database Connectivity**: Java Database Connectivity (JDBC) API
* **Database**: MySQL Server (8.x / 5.7)
* **JDBC Driver**: MySQL Connector/J (`mysql-connector-j-8.3.0.jar` included in `lib/`)

---

## 4. Project Structure

```
SmartCampusParking/
│
├── src/
│   ├── Main.java                # Application entry point, L&F setup, launches LoginFrame
│   ├── DBConnection.java        # Centralized JDBC connection factory & credentials configuration
│   ├── UITheme.java             # Modern AWT UI constants, color palette and font definitions
│   │
│   ├── LoginFrame.java          # Authentication window with demo helper
│   ├── DashboardFrame.java      # Main dashboard with sidebar navigation and metrics overview
│   │
│   ├── UserPanel.java           # User management CRUD & search (PreparedStatement)
│   ├── VehiclePanel.java        # Vehicle registration & owner link (PreparedStatement)
│   ├── ParkingPanel.java        # Zone & slot viewer with color tags (Statement)
│   ├── ReservationPanel.java    # Slot booking & conflict detection (PreparedStatement)
│   ├── EntryExitPanel.java      # Gate entry, exit & automated fee calculation
│   ├── PaymentPanel.java        # Cash/Card/UPI payment receipts (PreparedStatement)
│   ├── PassPanel.java           # Campus parking permits & passes (PreparedStatement)
│   ├── ViolationPanel.java      # Incident & penalty fine management (PreparedStatement)
│   └── ReportPanel.java         # Analytics reports & CallableStatement demo
│
├── database/
│   └── parking.sql              # Database creation, 9 relational tables, stored procedure & sample data
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar # MySQL JDBC Connector Driver
│
├── build_and_run.bat            # Windows 1-click batch build & run script
├── build_and_run.ps1            # PowerShell build & run script
├── README.md                    # Project documentation
└── .gitignore                   # Git ignore file
```

---

## 5. Database Schema & Tables

Database Name: `smart_campus_parking`

| Table Name | Description | Key Fields |
|---|---|---|
| `users` | Campus members & drivers | `user_id` (PK), `name`, `role`, `phone`, `email` |
| `vehicles` | Registered campus vehicles | `vehicle_id` (PK), `vehicle_number` (UNIQUE), `user_id` (FK), `vehicle_type` |
| `parking_zones` | Campus parking locations | `zone_id` (PK), `zone_name`, `location` |
| `parking_slots` | Individual parking bays | `slot_id` (PK), `zone_id` (FK), `slot_number` (UNIQUE), `status` |
| `reservations` | Slot advance bookings | `reservation_id` (PK), `vehicle_id` (FK), `slot_id` (FK), `reservation_date`, `start_time`, `end_time`, `status` |
| `parking_sessions`| Actual gate entry/exit | `session_id` (PK), `vehicle_id` (FK), `slot_id` (FK), `entry_time`, `exit_time`, `duration`, `fee` |
| `parking_passes` | Long-term permits | `pass_id` (PK), `user_id` (FK), `vehicle_id` (FK), `pass_type`, `start_date`, `end_date`, `status` |
| `payments` | Fee transaction receipts | `payment_id` (PK), `session_id` (FK), `amount`, `payment_method`, `payment_date`, `status` |
| `violations` | Traffic fine tickets | `violation_id` (PK), `vehicle_id` (FK), `violation_type`, `description`, `fine`, `violation_date` |

### Stored Procedure (`parking.sql`)
```sql
DELIMITER $$
CREATE PROCEDURE get_total_revenue(OUT total_rev DECIMAL(10,2))
BEGIN
    SELECT IFNULL(SUM(amount), 0.00) INTO total_rev
    FROM payments
    WHERE status = 'PAID';
END $$
DELIMITER ;
```

---

## 6. JDBC Statement Types Visibly Demonstrated

The codebase explicitly implements and documents all three primary JDBC interfaces:

### 1. `java.sql.Statement`
Used for general read-only queries, loading dropdown filters, and basic reports:
* Loading parking zones in `ParkingPanel.java`
* Calculating slot counts across zones in `DashboardFrame.java` and `ReportPanel.java`
* Fetching unfiltered table logs in `UserPanel.java` and `PassPanel.java`

### 2. `java.sql.PreparedStatement`
Used for parameterized queries to prevent SQL injection and ensure safe transactional execution:
* **INSERT**: Registering users, registering vehicles, recording entry sessions, logging payments, creating passes, and issuing violation tickets.
* **UPDATE**: Modifying user/vehicle details, updating slot status (`OCCUPIED` / `AVAILABLE` / `RESERVED`), and recording session exit times with fees.
* **DELETE**: Removing user records, vehicles, passes, and violations.
* **Parameterized SELECT**: Searching across all modules with keyword patterns (`LIKE ?`) and checking time-slot overlaps for reservation conflicts.

### 3. `java.sql.CallableStatement`
Used in `ReportPanel.java` to invoke the MySQL stored procedure `get_total_revenue(?)`:
```java
// CallableStatement Demonstration in ReportPanel.java
String callSql = "{CALL get_total_revenue(?)}";
try (Connection conn = DBConnection.getConnection();
     CallableStatement cstmt = conn.prepareCall(callSql)) {
    
    // Register OUT parameter
    cstmt.registerOutParameter(1, Types.DECIMAL);
    
    // Execute procedure
    cstmt.execute();
    
    // Retrieve result
    double totalRevenue = cstmt.getDouble(1);
}
```

---

## 7. Setup & Execution Instructions

### Prerequisites
1. **Java Development Kit (JDK 8 or higher)** installed.
2. **MySQL Server** (via XAMPP, WAMP, MySQL Installer, or Docker) running on port `3306`.

### Step 1: Database Setup
1. Open your MySQL client (MySQL Workbench, phpMyAdmin, or MySQL Command Line).
2. Execute the `database/parking.sql` script:
   ```sql
   source /path/to/SmartCampusParking/database/parking.sql;
   ```
   *Alternatively, copy and paste the contents of `database/parking.sql` into phpMyAdmin or MySQL Workbench and click **Execute**.*

### Step 2: Configure Database Credentials
Open `src/DBConnection.java`. By default, credentials are set to standard local defaults:
```java
private static final String DB_HOST = "localhost";
private static final String DB_PORT = "3306";
private static final String DB_NAME = "smart_campus_parking";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // Enter your MySQL root password if set
```

### Step 3: Compile and Run

#### Option A: 1-Click Batch Script (Windows)
Double-click `build_and_run.bat` or run in Command Prompt:
```cmd
build_and_run.bat
```

#### Option B: PowerShell
```powershell
.\build_and_run.ps1
```

#### Option C: Manual Command Line
```cmd
# 1. Compile Java files into bin directory
javac -cp "lib/mysql-connector-j-8.3.0.jar;src" -d bin src/*.java

# 2. Run the application
java -cp "bin;lib/mysql-connector-j-8.3.0.jar" Main
```

---

## 8. Demo Credentials & Demonstration Workflow

### Demo Login Credentials
* **Username**: `admin`
* **Password**: `admin123`
*(A "Fill Demo Credentials" button is provided on the login screen for 1-click auto-fill)*

### Complete Demonstration Walkthrough
1. **Login**: Click **Sign In** on the login screen with `admin` / `admin123`.
2. **Dashboard**: View live metric counters (**Total Slots**, **Available**, **Occupied**, **Reserved**, **Total Revenue**).
3. **User Management**: Add a new user (e.g. `Rohan Desai`, `Student`, `9876500001`, `rohan@campus.edu`).
4. **Vehicle Management**: Register a vehicle for Rohan (e.g. `KA-01-RD-9999`, `Car`).
5. **Zones & Slots**: Check current slot distribution across Zone A, B, and C.
6. **Slot Reservation**:
   * Select vehicle `KA-01-RD-9999`.
   * Pick **Zone A** and slot `A-02`.
   * Click **Check Availability**, then click **Confirm & Reserve Slot**.
   * Notice slot `A-02` transitions to **RESERVED**.
7. **Gate Entry**:
   * Navigate to **Gate Entry & Exit**.
   * Enter vehicle number `KA-01-RD-9999` and select slot `A-02`.
   * Click **Confirm Entry**.
   * Slot `A-02` transitions to **OCCUPIED** and active parking session starts.
8. **Gate Exit & Fee Calculation**:
   * In the **Vehicle Exit** box, select `KA-01-RD-9999`.
   * Click **Process Exit & Calculate Fee**.
   * The system computes duration (hours rounded up) and calculates fee (`duration * ₹20/hr` for Car).
   * Confirm exit: slot `A-02` is released back to **AVAILABLE**.
   * Choose **UPI** or **Cash** when prompted to immediately record payment.
9. **Reports & Stored Procedure**:
   * Navigate to **Analytics & Reports**.
   * Click **Occupancy & Utilization** to see the updated capacity percentage.
   * Click **Revenue Breakdown** to verify the recorded payment.
   * Click **CallableStatement: get_total_revenue()** to demonstrate the MySQL stored procedure execution.

---

## 9. Future Enhancements

* Automatic Number Plate Recognition (ANPR) camera integration via OpenCV.
* Fastag / RFID scanner integration at security entry gates.
* Multi-role authentication (Student self-service portal, Security guard kiosk, Dean dashboard).
* Automated SMS and Email notifications on entry, exit, and violation fines.
* Dynamic surge pricing during campus athletic tournaments and convocation events.

---

## 10. License & Attribution
Developed for academic coursework and college demonstration of Object-Oriented Desktop Software Engineering in Java with Relational Databases and JDBC.
