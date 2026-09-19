# Hotel Management System (HMS) - Database Architecture & Schema

## 1. Database-Per-Service Topology
In accordance with strict microservices architecture, there are **no shared databases**, **no cross-service joins**, and **no cross-service foreign keys**. Cross-service references use logical identifiers.

| Database Name | Owning Microservice | Primary Tables |
|---|---|---|
| `auth_db` | `auth-service` | `users`, `roles`, `user_roles` |
| `guest_db` | `guest-service` | `guests` |
| `room_db` | `room-service` | `rooms`, `room_categories`, `amenities`, `room_amenities` |
| `reservation_db` | `reservation-service` | `reservations`, `waitlist_entries` |
| `payment_db` | `payment-service` | `payments`, `refunds` |
| `billing_db` | `billing-service` | `bills`, `bill_items` |
| `rate_db` | `rate-service` | `rates`, `dynamic_pricing_rules` |
| `staff_db` | `staff-service` | `staff_members`, `shifts`, `staff_shifts`, `attendance_records`, `leave_requests` |
| `inventory_db` | `inventory-service` | `inventory_items`, `stock_movements`, `reorder_rules` |
| `guest_experience_db` | `guest-experience-service` | `service_requests`, `service_request_history`, `feedback`, `complaints`, `loyalty_accounts`, `loyalty_transactions` |
| `operations_db` | `operations-service` | `housekeeping_tasks`, `housekeeping_task_history`, `room_assignments`, `maintenance_issues`, `breakage_records`, `expenses`, `utility_records`, `cash_drawer_sessions`, `shift_handovers` |
| `reporting_db` | `reporting-service` | `occupancy_snapshots`, `revenue_records`, `staff_performance_records`, `daily_summaries` |
| `notification_db` | `notification-service` | `notifications` |

---

## 2. Integrity and Concurrency Controls
1. **Optimistic Locking**: Handled via `@Version` columns on concurrency-sensitive entities such as `InventoryItem` to prevent lost updates during simultaneous stock modifications.
2. **Financial Immutability**: `bills` once marked `FINALIZED` are locked against direct mutations. Further adjustments require explicit line item credit entries or refund records.
3. **Idempotency Keys**: Unique database constraint on `payment_db.payments(idempotency_key)` to guarantee exactly-once payment processing under duplicate client retries.
4. **Auditability**: All state mutations in housekeeping, service requests, inventory movements, loyalty points, and payments maintain immutable ledger/history tables.
