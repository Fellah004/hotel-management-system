# Hotel Management System (HMS) - Testing Guide

## 1. Testing Frameworks
All unit, service, controller, and security test suites are implemented exclusively using:
- **JUnit 5 (Jupiter)**
- **Mockito**
- **Spring Boot Test & MockMvc**

---

## 2. Test Execution Commands

To execute tests for the entire 16-microservice suite from the root directory:
```bash
mvn clean test
```

To execute tests for a single individual microservice:
```bash
mvn test -f auth-service/pom.xml
mvn test -f guest-service/pom.xml
mvn test -f room-service/pom.xml
mvn test -f rate-service/pom.xml
mvn test -f staff-service/pom.xml
mvn test -f inventory-service/pom.xml
mvn test -f reservation-service/pom.xml
mvn test -f payment-service/pom.xml
mvn test -f billing-service/pom.xml
mvn test -f guest-experience-service/pom.xml
mvn test -f operations-service/pom.xml
mvn test -f reporting-service/pom.xml
mvn test -f notification-service/pom.xml
```

---

## 3. Coverage by Feature Area
- **Authentication & Security**: Account lock threshold, password hashing verification, JWT claim generation and validation, role and object-level permissions.
- **Reservation & Overlap**: Overlap detection algorithms, check-in/check-out lifecycle state machines, waitlist priorities, transfer/upgrade validations.
- **Payment & Idempotency**: Duplicate payment prevention using unique idempotency keys, refund limits validation, event envelope publication.
- **Billing**: Line item price aggregation, tax and discount computations, immutability of finalized bills, printable receipt rendering.
- **Operations & Housekeeping**: Housekeeper room-range assignments, shift verification, task state machine transitions, breakage reporting and approval workflows.
- **Inventory & Resilience**: Stock-in/out atomic updates, threshold breach event emission, circuit breakers and fallbacks.
