# Online Hotel Management System (HMS) / i-Transform HMS

Enterprise Microservices-Based Online Hotel Management System built with **Java 21**, **Spring Boot 3.3.4**, **Spring Cloud 2023.0.3**, **MySQL**, **RabbitMQ**, **OpenFeign**, and **Docker**.

---

## 1. Project Overview
The Online Hotel Management System (HMS) is a distributed, event-driven hotel operations and reservation platform. It automates front-desk reservations, hall bookings, dynamic rate calculations, multi-method payment processing with idempotency, deterministic billing, staff management, inventory control, guest experience workflows, operational housekeeping assignments, maintenance tracking, and comprehensive reporting.

---

## 2. Business Objectives
- Seamless online room and hall reservation lifecycle (search, quote, book, check-in, checkout, cancellation, transfer, upgrade/downgrade, waitlist).
- Real-time dynamic pricing based on seasonal peaks, weekend surcharges, holiday multipliers, occupancy levels, and last-minute booking windows.
- Robust financial transactions with multi-method payments (`CASH`, `CARD`, `UPI`, `WALLET`), strict idempotency control, immutable finalized bills, and balance-verified refunds.
- Fully automated operational workflows connecting guest check-out to room dirty state, housekeeping task creation, room range staff assignments, and room availability.
- Automated daily hotel summaries and multi-channel notification records.

---

## 3. Architecture
The system follows a strict **16-microservice architecture** adhering to the **Database-per-Service** design pattern. Synchronous operations use **Spring Cloud OpenFeign** with **Resilience4j** circuit breakers, while asynchronous operations use **RabbitMQ** topic exchanges with event envelope standardization and consumer idempotency.

---

## 4. Microservices (Exactly 16 Services)

### Infrastructure Services
1. **`config-server`** (Port `8888`): Centralized Git/native configuration repository.
2. **`eureka-server`** (Port `8761`): Service registry and discovery.
3. **`api-gateway`** (Port `8080`): Reactive API Gateway with JWT validation and correlation ID injection.

### Core Business Services
4. **`auth-service`** (Port `8081`, DB: `auth_db`): User credentials, BCrypt password hashing, RBAC, failed login tracking, account locking, JWT generation.
5. **`guest-service`** (Port `8082`, DB: `guest_db`): Guest and VIP member profiles, member codes, contact details.
6. **`room-service`** (Port `8083`, DB: `room_db`): Rooms, Halls/Resources, categories (`SINGLE`, `DOUBLE`, `DELUXE`, `SUITE`), amenities, date overlap availability calculations, operational state machine.
7. **`reservation-service`** (Port `8084`, DB: `reservation_db`): Reservations, lifecycle transitions, check-in/out, transfers, upgrades/downgrades, waitlist queue, Saga coordination.
8. **`payment-service`** (Port `8085`, DB: `payment_db`): Payment execution, unique `idempotencyKey` enforcement, refund balance limits, transaction events.
9. **`billing-service`** (Port `8086`, DB: `billing_db`): Itemized invoices, automated tax and discount calculations, immutable finalized bills, printable receipt rendering.
10. **`rate-service`** (Port `8087`, DB: `rate_db`): Base rates, first-night and extension pricing, Dynamic Pricing Engine.
11. **`staff-service`** (Port `8088`, DB: `staff_db`): Staff employee profiles, shift schedules, attendance clock-in/out, leave approvals, salary/NIC masking.
12. **`inventory-service`** (Port `8089`, DB: `inventory_db`): Stock levels, atomic stock in/out/adjustments, optimistic locking, threshold breach detection.
13. **`guest-experience-service`** (Port `8090`, DB: `guest_experience_db`): Guest service requests, QR room service, stay feedback, complaints handling, loyalty tier points ledger.
14. **`operations-service`** (Port `8091`, DB: `operations_db`): Housekeeper room-range assignments, housekeeping task lifecycle, maintenance tickets, breakage approvals, expenses, utilities, cash drawer reconciliation, shift handover.
15. **`reporting-service`** (Port `8092`, DB: `reporting_db`): Occupancy analytics, revenue streams, staff performance metrics, automated scheduled daily hotel summaries.
16. **`notification-service`** (Port `8093`, DB: `notification_db`): Event consumer, notification store, multi-channel classification (`EMAIL`, `SMS`, `IN_APP`).

---

## 5. Roles & Access Control Matrix
- **`ADMIN`**: User management, account unlock, system governance.
- **`OWNER`**: Complete business oversight, rates, dynamic pricing, room categories, staff, financial reporting, expenses, cash drawer.
- **`MANAGER`**: Operational oversight, staff shifts, housekeeping room assignments, maintenance approvals, breakage authorization, complaints resolution.
- **`RECEPTIONIST`**: Guest registration, room/hall searches, reservation creation, check-in, check-out, room transfers, upgrades, cash drawer operations.
- **`HOUSEKEEPER`**: View assigned room tasks, task lifecycle (`ACCEPT`, `START`, `COMPLETE`, `REJECT`), damage reporting.
- **`GUEST`**: Self-registration, room search, own reservations, payments, service requests, QR room ordering, feedback, complaints, loyalty balance.

---

## 6. Databases (Database-Per-Service)
Each business microservice owns an isolated MySQL database schema:
- `auth_db`, `guest_db`, `room_db`, `reservation_db`, `payment_db`, `billing_db`, `rate_db`, `staff_db`, `inventory_db`, `guest_experience_db`, `operations_db`, `reporting_db`, `notification_db`.

---

## 7. REST & OpenFeign Communication
Synchronous remote calls between microservices utilize declarative Spring Cloud OpenFeign clients with Resilience4j circuit breakers:
- `ReservationService` calls `RoomService`, `RateService`, and `GuestService`.
- `BillingService` calls `PaymentService`.
- `OperationsService` calls `StaffService` and `RoomService`.

---

## 8. RabbitMQ Asynchronous Events
Event-driven architecture connects services via topic exchange `hotel.events.exchange`:
- **Reservation Events**: `ReservationCreated`, `ReservationConfirmed`, `ReservationCancelled`, `ReservationCheckedIn`, `ReservationCheckedOut`, `ReservationNoShow`.
- **Financial Events**: `PaymentSucceeded`, `PaymentFailed`, `RefundCompleted`, `BreakageApproved`.
- **Operations Events**: `RoomMarkedDirty`, `RoomMarkedClean`, `RoomAvailable`, `HousekeepingTaskAssigned`, `HousekeepingTaskCompleted`.
- **System Events**: `LowStockDetected`, `ComplaintCreated`, `DailySummaryGenerated`, `AccountLocked`.

---

## 9. Saga Pattern & Compensation
Distributed transactions (such as Room Reservation & Payment) are managed via orchestrated Sagas:
- **Happy Path**: Reservation `PENDING` $\rightarrow$ Room Held $\rightarrow$ Payment Succeeded $\rightarrow$ Reservation `CONFIRMED` $\rightarrow$ Notification sent.
- **Compensation**: If payment fails, `PaymentFailed` triggers reservation cancellation and room hold release back to `AVAILABLE`.

---

## 10. Security Architecture
- **JWT Authentication**: Stateless Bearer tokens containing `userId`, `username`, `role`, and standard expiration claims.
- **Dual-Layer Validation**: API Gateway filters unauthenticated traffic; individual business services validate JWT signatures and enforce method-level `@PreAuthorize` security.
- **Object-Level Authorization**: Enforces that guests can only access their own reservations/profiles and housekeepers can only access their assigned tasks.
- **Account Locking**: Automatically locks accounts after exceeding configured failed login attempts and dispatches a security event.

---

## 11. Resilience4j Fault Tolerance
- **Circuit Breaker**: Prevents cascading failures when downstream services are unavailable.
- **Retry**: Retries transient read/query failures with exponential backoff (non-idempotent operations like payment creation are not blindly retried).
- **TimeLimiter & Bulkhead**: Limits concurrent remote calls and prevents thread exhaustion.
- **Fallbacks**: Returns controlled degradation responses during outages.

---

## 12. Validation
- Declarative input validation via Jakarta Validation (`@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Future`, `@Positive`).
- Service-layer business validations (date consistency, occupancy constraints, refund amount ceilings, shift eligibility).

---

## 13. Global Exception Handling
Every service includes a `@RestControllerAdvice` mapping domain exceptions to standardized `ErrorResponse` payloads with HTTP status codes, error types, user-friendly messages, request paths, and MDC trace identifiers.

---

## 14. Logging & Correlation ID
Structured logging via SLF4J and Logback. Every distributed request carries an `X-Correlation-ID` header injected by the API Gateway and propagated through Feign headers and RabbitMQ message envelopes.

---

## 15. Spring Boot Actuator
Actuator endpoints (`/actuator/health`, `/actuator/info`, `/actuator/metrics`) are enabled across all services to facilitate Docker health checks and runtime monitoring.

---

## 16. Swagger / OpenAPI Documentation
Interactive Swagger UI documentation is available on every service at `/swagger-ui.html` and aggregated via API Gateway.

---

## 17. Testing Suite
Comprehensive unit and controller test coverage across all microservices using JUnit 5, Mockito, and Spring Boot MockMvc.

---

## 18. Docker & Containerization
Every microservice includes an optimized multi-stage `Dockerfile`. The complete platform is orchestrated using root `docker-compose.yml`.

---

## 19. Configuration
Centralized configuration managed by `config-server` backed by the `config-repo/` directory.

---

## 20. Startup Order
1. **MySQL** & **RabbitMQ**
2. **Config Server** (8888)
3. **Eureka Server** (8761)
4. **API Gateway** (8080)
5. **Auth Service** (8081)
6. **Master Data Services**: Guest (8082), Room (8083), Rate (8087), Staff (8088), Inventory (8089)
7. **Business Workflow Services**: Reservation (8084), Payment (8085), Billing (8086), Guest Experience (8090), Operations (8091), Reporting (8092), Notification (8093)

---

## 21. Demo Walkthrough
1. **Admin Setup**: Admin logs in (`POST /api/auth/login`), creates hotel rooms and rates.
2. **Guest Booking**: Guest searches available rooms (`GET /api/rooms/available`), retrieves dynamic price quote (`POST /api/rates/quote`), and reserves (`POST /api/reservations`).
3. **Payment Execution**: Payment processed (`POST /api/payments`), publishing `PaymentSucceeded`, which confirms reservation.
4. **Guest Check-In**: Front desk checks in guest (`POST /api/reservations/{id}/check-in`), setting room status to `OCCUPIED`.
5. **Guest Service**: Guest orders towels via QR room service (`POST /api/service-requests`), creating a housekeeping task for the assigned housekeeper.
6. **Check-Out & Cleaning Workflow**: Receptionist initiates checkout (`POST /api/reservations/{id}/check-out`), billing finalizes charges, room is marked `DIRTY`, housekeeping task is automatically assigned, housekeeper accepts and completes task, room returns to `AVAILABLE`.
7. **Reporting & Summaries**: Reporting service generates daily operational summary (`GET /api/reports/daily-summary`).

---

## 22. Troubleshooting
- **Database Connection Refused**: Verify MySQL container is healthy and port 3306 is accessible.
- **RabbitMQ Queue Not Bound**: Ensure RabbitMQ container is healthy on port 5672.
- **Service Discovery Issues**: Check `http://localhost:8761` to confirm all services are registered with Eureka.
