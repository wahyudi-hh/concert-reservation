# Concert Reservation API

A Spring Boot REST API for concert discovery and ticket reservation, with a focus on safe ticket inventory handling under concurrent booking requests.

## Overview

This project implements a concert ticket reservation API with:

- Concert creation and ticket inventory configuration.
- Search for concerts whose booking windows are currently open.
- Ticket booking with per-booking quantity limits.
- Booking-window validation.
- Atomic ticket inventory reservation to prevent overselling.
- Transactional booking operations.
- Automated unit and integration tests, including a concurrent booking scenario.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven
- JUnit 5
- Mockito

## Architecture

The application follows a simple layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Main components:

- **Controller**: handles HTTP requests.
- **Service**: contains business rules and the booking workflow.
- **Repository**: provides persistence operations.
- **Entity**: represents the database model.
- **DTO**: defines the API request and response models.
- **Exception Handler**: provides consistent API error responses.

## API Documentation

Detailed API documentation is available in:

[`docs/api.md`](docs/api.md)

Available endpoints:

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/concerts` | Create a concert |
| `GET` | `/api/concerts` | Find concerts currently available for booking |
| `POST` | `/api/bookings` | Book tickets for a concert |

## Database Design

The database design and schema decisions are documented in:

[`docs/database.md`](docs/database.md)

The application uses two main tables:

```text
concert
   │
   │ 1 : N
   ▼
booking
```

Flyway manages schema migrations, while Hibernate validates the database schema.

## Concurrency Strategy

Ticket reservation uses an atomic conditional database update:

```sql
UPDATE concert
SET available_tickets = available_tickets - :quantity
WHERE id = :concertId
  AND available_tickets >= :quantity;
```

The update succeeds only when enough tickets remain.

The reservation and booking creation are executed within the same transaction. If the inventory update affects zero rows, the request is rejected because there are not enough tickets available.

This avoids a read-modify-write race condition where concurrent requests could otherwise read the same inventory and oversell tickets.

### Concurrent Booking Test

The integration test simulates 100 concurrent booking requests against a concert with 10 available tickets.

Expected result:

- 10 requests succeed.
- 90 requests fail with `409 Conflict`.
- Available ticket inventory becomes `0`.
- Exactly 10 booking records are created.

## Running the Application

### Prerequisites

- Java 17+
- Maven
- Docker

### Start PostgreSQL

The project includes a Docker Compose configuration for PostgreSQL:

```bash
docker compose up -d
```

### Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

## Running Tests

Run the complete test suite with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

Integration tests use the PostgreSQL database configured for the application.

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/edts/concertreservation/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── repository/
│   │       └── service/
│   └── resources/
│       ├── db/
│       │   └── migration/
│       └── application.properties/
└── test/
    └── java/
        └── com/edts/concertreservation/
```

## Design Decisions

### Atomic Inventory Update

Inventory is updated directly in the database using a conditional `UPDATE` rather than a read-modify-write operation. This makes the availability check and inventory decrement a single database operation.

### Aggregate Ticket Inventory

The system stores ticket quantities rather than individual ticket records because the requirements only require ticket quantity reservation and do not require individual seat or ticket identity.

### Database Constraints

Database constraints protect core invariants such as:

```text
0 <= available_tickets <= total_tickets
```

and ensure valid booking quantities and booking windows.

### Schema Ownership

Flyway owns database schema creation and evolution. Hibernate is configured to validate the schema rather than generate or modify it automatically.

## Notes

- Booking windows use an inclusive start and exclusive end:
  `bookingStartAt <= current time < bookingEndAt`.
- Authentication, user management, payments, cancellation, and individual ticket/seat management are outside the scope of this assessment.
