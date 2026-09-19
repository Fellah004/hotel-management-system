# Hotel Management System (HMS) - Saga Pattern & Distributed Transactions

## 1. Saga Orchestration Flow: Reservation & Payment

Distributed business operations across `reservation-service`, `room-service`, `rate-service`, `payment-service`, and `notification-service` are coordinated using the **Saga Pattern with Event-Driven Choreography & Local Transactions**.

```
    [Guest / Receptionist]
               |
               v
     POST /api/reservations
               |
    [reservation-service]
    - Verify guest existence (Feign)
    - Check room date overlap (Feign)
    - Fetch rate quote (Feign)
    - Save reservation as PENDING
    - Hold room as RESERVED (Feign)
               |
               v
     POST /api/payments
               |
      [payment-service]
    - Process payment idempotently
    - Save Payment status SUCCESS / FAILED
    - Publish PaymentSucceeded OR PaymentFailed
               |
               +-----------------------+-----------------------+
               |                                               |
         [Success Path]                                 [Failure / Compensation Path]
               |                                               |
       PaymentSucceeded                                  PaymentFailed
               |                                               |
               v                                               v
     [reservation-service]                           [reservation-service]
    - Update status to CONFIRMED                    - Cancel / Release hold
    - Publish ReservationConfirmed                  - Release room to AVAILABLE
               |                                    - Publish ReservationCancelled
               v                                               |
     [notification-service]                                    v
    - Send booking confirmation                      [notification-service]
                                                    - Send payment failure alert
```

---

## 2. Failure & Compensation Scenarios

### Scenario 1: Payment Fails After Room Hold
- **Event**: `PaymentFailed` consumed by `reservation-service`.
- **Compensating Action**: `reservation-service` releases the room back to `AVAILABLE` via `RoomServiceClient` and sets the reservation status to `CANCELLED`.
- **Notification**: Alerts guest of payment decline.

### Scenario 2: Post-Confirmation Cancellation & Refund
- **Action**: Guest or Receptionist initiates `POST /api/reservations/{id}/cancel`.
- **Validation**: Check cancellation grace period and refund eligibility rules.
- **Workflow**:
  1. `reservation-service` marks reservation as `CANCELLED` and releases room to `AVAILABLE`.
  2. `payment-service` executes `POST /api/payments/{id}/refund` and verifies remaining refundable balance.
  3. `payment-service` publishes `RefundCompleted`.
  4. `notification-service` dispatches refund confirmation to the guest.
  5. `room-service` publishes `RoomAvailable`, allowing `reservation-service` waitlist processor to notify waiting guests.

### Scenario 3: Double Booking Prevention
- **Mechanism**: Synchronous atomic check via OpenFeign `GET /api/rooms/availability/check` before creating the `PENDING` reservation. If another transaction has booked the room for any overlapping night, the second request is immediately rejected with `409 Conflict: Room is not available for the requested dates`.
