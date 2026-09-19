# Hotel Management System (HMS) - API Specification

All APIs are exposed through the **API Gateway** on port `8080` (or directly on service ports for internal/debug access) and documented via Springdoc OpenAPI Swagger UI.

## 1. OpenAPI / Swagger Endpoints
Each service provides interactive Swagger UI documentation at:
- **API Gateway**: `http://localhost:8080/swagger-ui.html`
- **Auth Service**: `http://localhost:8081/swagger-ui.html`
- **Guest Service**: `http://localhost:8082/swagger-ui.html`
- **Room Service**: `http://localhost:8083/swagger-ui.html`
- **Reservation Service**: `http://localhost:8084/swagger-ui.html`
- **Payment Service**: `http://localhost:8085/swagger-ui.html`
- **Billing Service**: `http://localhost:8086/swagger-ui.html`
- **Rate Service**: `http://localhost:8087/swagger-ui.html`
- **Staff Service**: `http://localhost:8088/swagger-ui.html`
- **Inventory Service**: `http://localhost:8089/swagger-ui.html`
- **Guest Experience Service**: `http://localhost:8090/swagger-ui.html`
- **Operations Service**: `http://localhost:8091/swagger-ui.html`
- **Reporting Service**: `http://localhost:8092/swagger-ui.html`
- **Notification Service**: `http://localhost:8093/swagger-ui.html`

---

## 2. Core API Summary Matrix

| Service | HTTP Method | Endpoint | Allowed Roles | Description |
|---|---|---|---|---|
| **Auth** | `POST` | `/api/auth/login` | Public | Authenticate user, return JWT token |
| **Auth** | `POST` | `/api/users` | `ADMIN` | Create user account with assigned role |
| **Auth** | `GET` | `/api/users` | `ADMIN` | List all users |
| **Guest** | `POST` | `/api/guests` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Register new guest profile |
| **Guest** | `GET` | `/api/guests/{id}` | `GUEST` (own), Staff | Retrieve guest details |
| **Room** | `POST` | `/api/rooms` | `OWNER`, `MANAGER` | Create room or hall |
| **Room** | `GET` | `/api/rooms/available` | Authenticated | List rooms available by category |
| **Room** | `GET` | `/api/rooms/availability/check` | Authenticated | Check room date overlap availability |
| **Room** | `PUT` | `/api/rooms/{id}/status` | `OWNER`, `MANAGER`, `RECEPTIONIST` | Update room operational status |
| **Rate** | `POST` | `/api/rates` | `OWNER`, `MANAGER` | Set base and dynamic rate rules |
| **Rate** | `POST` | `/api/rates/quote` | Authenticated | Quote dynamic pricing for stay period |
| **Reservation** | `POST` | `/api/reservations` | `GUEST` (own), `RECEPTIONIST`, `MANAGER`, `OWNER` | Create room/hall reservation |
| **Reservation** | `POST` | `/api/reservations/{id}/check-in` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Check-in guest, set room to OCCUPIED |
| **Reservation** | `POST` | `/api/reservations/{id}/check-out` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Check-out guest, trigger housekeeping |
| **Reservation** | `POST` | `/api/reservations/{id}/cancel` | `GUEST` (own), Staff | Cancel reservation & trigger refund |
| **Reservation** | `POST` | `/api/reservations/{id}/room-transfer` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Transfer reservation to new room |
| **Reservation** | `POST` | `/api/reservations/{id}/room-upgrade` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Upgrade room with rate difference |
| **Payment** | `POST` | `/api/payments` | Authenticated | Process idempotent payment |
| **Payment** | `POST` | `/api/payments/{id}/refund` | `ADMIN`, `OWNER`, `MANAGER` | Process refund with balance check |
| **Billing** | `POST` | `/api/bills` | Authenticated | Create initial bill for reservation |
| **Billing** | `POST` | `/api/bills/{id}/finalize` | `RECEPTIONIST`, `MANAGER`, `OWNER` | Finalize & lock bill against edits |
| **Billing** | `GET` | `/api/bills/{id}/print` | Authenticated | Get printable receipt representation |
| **Staff** | `POST` | `/api/staff` | `OWNER`, `MANAGER` | Register staff employee |
| **Staff** | `POST` | `/api/attendance` | Authenticated | Record attendance check-in/out |
| **Staff** | `POST` | `/api/shifts` | `OWNER`, `MANAGER` | Create staff shift schedule |
| **Staff** | `POST` | `/api/leave` | Authenticated | Submit leave request |
| **Inventory** | `POST` | `/api/inventory` | `OWNER`, `MANAGER` | Create inventory item |
| **Inventory** | `POST` | `/api/inventory/{id}/stock-in` | `OWNER`, `MANAGER` | Add stock quantity |
| **Inventory** | `POST` | `/api/inventory/{id}/stock-out` | `OWNER`, `MANAGER` | Deduct stock quantity & check threshold |
| **Guest Exp** | `POST` | `/api/service-requests` | `GUEST` (own), Staff | Submit service / QR room service request |
| **Guest Exp** | `POST` | `/api/feedback` | `GUEST` (own) | Submit stay feedback |
| **Guest Exp** | `POST` | `/api/complaints` | `GUEST` (own), Staff | Submit complaint ticket |
| **Guest Exp** | `GET` | `/api/loyalty/account` | `GUEST` (own), Staff | Retrieve loyalty tier and points balance |
| **Operations** | `POST` | `/api/room-assignments` | `MANAGER`, `OWNER` | Assign room number ranges to housekeepers |
| **Operations** | `GET` | `/api/housekeeping/tasks/my` | `HOUSEKEEPER` | View assigned housekeeping tasks |
| **Operations** | `POST` | `/api/housekeeping/tasks/{id}/accept` | `HOUSEKEEPER` | Accept task |
| **Operations** | `POST` | `/api/housekeeping/tasks/{id}/complete` | `HOUSEKEEPER` | Complete task & mark room CLEAN |
| **Operations** | `POST` | `/api/maintenance` | Staff | Log maintenance issue |
| **Operations** | `POST` | `/api/breakage` | Staff | Report breakage / damages |
| **Operations** | `POST` | `/api/breakage/{id}/approve` | `MANAGER`, `OWNER` | Approve breakage charge to bill |
| **Operations** | `POST` | `/api/cash-drawer/open` | `RECEPTIONIST`, `MANAGER` | Open shift cash drawer |
| **Operations** | `POST` | `/api/cash-drawer/close` | `RECEPTIONIST`, `MANAGER` | Close & reconcile cash drawer |
| **Reporting** | `GET` | `/api/reports/occupancy` | `OWNER`, `MANAGER`, `ADMIN` | Get hotel occupancy analytics |
| **Reporting** | `GET` | `/api/reports/revenue` | `OWNER`, `MANAGER`, `ADMIN` | Get revenue analytics & breakdown |
| **Reporting** | `GET` | `/api/reports/daily-summary` | `OWNER`, `MANAGER`, `ADMIN` | Get automated daily hotel summary |
| **Notification**| `GET` | `/api/notifications` | Authenticated | Get user and role notifications |
| **Notification**| `PUT` | `/api/notifications/{id}/read` | Authenticated | Mark notification as read |
