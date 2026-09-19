# Hotel Management System (HMS) - Architecture Overview

## 1. System Overview
The **Online Hotel Management System (HMS)** / **i-Transform Hotel Management System** is an enterprise-grade, distributed microservices architecture designed to support comprehensive end-to-end hotel operations, guest services, financial accounting, and administrative governance.

The system is constructed with **exactly 16 microservices**, adhering to pure separation of concerns and the database-per-service pattern.

---

## 2. 16 Microservice Boundaries

```
                                    +-----------------------+
                                    |     CLIENT / BROWSER  |
                                    +-----------+-----------+
                                                |
                                                v
                                    +-----------------------+
                                    |  API GATEWAY (8080)   |
                                    |  - JWT Gateway Filter |
                                    |  - Correlation ID     |
                                    +-----------+-----------+
                                                |
            +-----------------------------------+-----------------------------------+
            |                                   |                                   |
    +-------v--------+                 +--------v--------+                 +--------v--------+
    | Eureka Server  |                 |  Config Server  |                 |  Auth Service   |
    | (Port 8761)    |                 |  (Port 8888)    |                 |  (Port 8081)    |
    +----------------+                 +-----------------+                 +-----------------+
```

### Business Services:
1. **`auth-service` (Port 8081, DB: `auth_db`)**: Authentication, JWT token generation/validation, role assignment (`ADMIN`, `OWNER`, `MANAGER`, `RECEPTIONIST`, `HOUSEKEEPER`, `GUEST`), failed login attempt tracking, account lock.
2. **`guest-service` (Port 8082, DB: `guest_db`)**: Guest registration, member code management, profile CRUD, object-level ownership authorization.
3. **`room-service` (Port 8083, DB: `room_db`)**: Room and Hall/Resource inventory, room categories, amenities, availability overlap queries, room operational state machine (`AVAILABLE`, `RESERVED`, `OCCUPIED`, `DIRTY`, `CLEANING`, `CLEAN`, `MAINTENANCE`, `OUT_OF_SERVICE`).
4. **`reservation-service` (Port 8084, DB: `reservation_db`)**: Room & Hall reservations, check-in, check-out, cancellation, no-show, waitlist, room transfers, room upgrades/downgrades, early check-in, late checkout, Saga orchestrator.
5. **`payment-service` (Port 8085, DB: `payment_db`)**: Payment processing (`CASH`, `CARD`, `UPI`, `WALLET`), payment idempotency via `idempotencyKey`, refund balance validation, transaction events.
6. **`billing-service` (Port 8086, DB: `billing_db`)**: Deterministic invoice calculations, line item breakdown (room charges, service charges, taxes, discounts, breakage, late fees), finalization immutability, printable bill representation.
7. **`rate-service` (Port 8087, DB: `rate_db`)**: Base rates, first-night and extension pricing, Dynamic Pricing Engine (weekend, holiday, seasonal, occupancy, and last-minute multipliers).
8. **`staff-service` (Port 8088, DB: `staff_db`)**: Staff employee records, shift scheduling, daily attendance check-in/out, leave approvals, role-restricted salary/NIC masking.
9. **`inventory-service` (Port 8089, DB: `inventory_db`)**: Stock tracking, atomic stock movements (in/out/adjust), optimistic locking concurrency control, low-stock threshold detection.
10. **`guest-experience-service` (Port 8090, DB: `guest_experience_db`)**: Guest service requests, QR room service ordering, special preferences, stay feedback, complaint resolution lifecycle, loyalty points ledger (`SILVER`, `GOLD`, `PLATINUM`).
11. **`operations-service` (Port 8091, DB: `operations_db`)**: Housekeeping task assignments by room number ranges, housekeeping workflow, maintenance tickets, breakage charges with manager approval, operational expenses, utility tracking, cash drawer reconciliation, shift handover notes.
12. **`reporting-service` (Port 8092, DB: `reporting_db`)**: Occupancy analytics, revenue streams, expense trends, staff performance metrics, automated daily hotel summaries via Spring Scheduler.
13. **`notification-service` (Port 8093, DB: `notification_db`)**: Event-driven notification store, multi-channel classification (`EMAIL`, `SMS`, `IN_APP`), role and user notifications.

---

## 3. Communication Patterns

### Synchronous (REST + OpenFeign)
Synchronous communication is used strictly when immediate information is required for business validation:
- `reservation-service` $\rightarrow$ `room-service`: check room availability, update room status.
- `reservation-service` $\rightarrow$ `rate-service`: calculate dynamic price quotes.
- `reservation-service` $\rightarrow$ `guest-service`: validate guest profile and membership.
- `billing-service` $\rightarrow$ `payment-service`: fetch payment status and totals.
- `operations-service` $\rightarrow$ `staff-service`: verify housekeeper shift and active status.
- `operations-service` $\rightarrow$ `room-service`: update room status (`DIRTY` $\rightarrow$ `CLEANING` $\rightarrow$ `CLEAN` $\rightarrow$ `AVAILABLE`).

### Asynchronous (RabbitMQ + Spring AMQP)
Asynchronous event publishing via topic exchange `hotel.events.exchange` is used for domain events:
- `ReservationCreated`, `ReservationConfirmed`, `ReservationCancelled`, `ReservationCheckedIn`, `ReservationCheckedOut`, `ReservationNoShow`
- `PaymentSucceeded`, `PaymentFailed`, `RefundCompleted`
- `RoomMarkedDirty`, `RoomMarkedClean`, `RoomAvailable`, `RoomMaintenanceStarted`, `RoomMaintenanceCompleted`
- `ServiceRequestCreated`, `ServiceRequestAssigned`, `ServiceRequestCompleted`
- `HousekeepingTaskCreated`, `HousekeepingTaskAssigned`, `HousekeepingTaskAccepted`, `HousekeepingTaskCompleted`
- `LowStockDetected`
- `ComplaintCreated`, `ComplaintResolved`
- `BreakageApproved`
- `DailySummaryGenerated`
- `AccountLocked`
