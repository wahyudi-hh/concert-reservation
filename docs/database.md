# Database Design

## Overview

The Concert Reservation API uses PostgreSQL as its relational database.

The database contains two application tables:

- `concert` — stores concert information, booking windows, and ticket inventory.
- `booking` — stores successful ticket bookings and the quantity reserved.

Database schema changes are managed using Flyway migrations.

```text
concert
   │
   │ 1 : N
   ▼
booking
```

A concert can have multiple bookings, while each booking belongs to exactly one concert.

---

## Entity Relationship

```text
┌──────────────────────────────────────┐
│               concert                │
├──────────────────────────────────────┤
│ PK  id                    UUID       │
│     name                  VARCHAR    │
│     description           VARCHAR    │
│     booking_start_at      TIMESTAMPTZ│
│     booking_end_at        TIMESTAMPTZ│
│     total_tickets         INTEGER    │
│     available_tickets     INTEGER    │
│     max_tickets_per_booking INTEGER  │
└──────────────────┬───────────────────┘
                   │
                   │ 1 : N
                   │
┌──────────────────▼───────────────────┐
│               booking                │
├──────────────────────────────────────┤
│ PK  id                    UUID       │
│ FK  concert_id            UUID       │
│     quantity              INTEGER    │
│     created_at            TIMESTAMPTZ│
└──────────────────────────────────────┘
```

---

## 1. `concert` Table

Stores the concert definition and its current ticket inventory.

| Column | Type | Nullable | Description |
|---|---|---:|---|
| `id` | UUID | No | Primary key and unique concert identifier |
| `name` | VARCHAR(255) | No | Concert name |
| `description` | VARCHAR(255) | Yes | Optional concert description |
| `booking_start_at` | TIMESTAMPTZ | No | Start of the booking window |
| `booking_end_at` | TIMESTAMPTZ | No | End of the booking window |
| `total_tickets` | INTEGER | No | Total ticket inventory for the concert |
| `available_tickets` | INTEGER | No | Current remaining ticket inventory |
| `max_tickets_per_booking` | INTEGER | No | Maximum number of tickets allowed in one booking |

### Constraints

#### Booking window

```sql
CHECK (booking_start_at < booking_end_at)
```

The booking start time must be before the booking end time.

#### Total tickets

```sql
CHECK (total_tickets > 0)
```

A concert must have at least one ticket.

#### Available tickets

```sql
CHECK (
    available_tickets >= 0
    AND available_tickets <= total_tickets
)
```

This protects the basic inventory invariant:

```text
0 <= available_tickets <= total_tickets
```

#### Maximum tickets per booking

```sql
CHECK (
    max_tickets_per_booking > 0
    AND max_tickets_per_booking <= total_tickets
)
```

A booking must allow at least one ticket, and the per-booking limit cannot exceed the total concert inventory.

---

## 2. `booking` Table

Stores each successful booking transaction.

| Column | Type | Nullable | Description |
|---|---|---:|---|
| `id` | UUID | No | Primary key and unique booking identifier |
| `concert_id` | UUID | No | Foreign key referencing `concert.id` |
| `quantity` | INTEGER | No | Number of tickets reserved by the booking |
| `created_at` | TIMESTAMPTZ | No | Time when the booking was created |

### Constraints

#### Concert relationship

```sql
FOREIGN KEY (concert_id)
REFERENCES concert(id)
```

Every booking must reference an existing concert.

#### Booking quantity

```sql
CHECK (quantity > 0)
```

A booking must reserve at least one ticket.

---

## 3. Ticket Inventory Design

The system does not create an individual database row for every physical ticket.

Instead, the concert stores aggregate inventory:

```text
total_tickets
available_tickets
```

and each booking stores the quantity reserved:

```text
booking.quantity
```

For example:

```text
Concert:
total_tickets     = 10
available_tickets = 7

Bookings:
Booking A → quantity = 2
Booking B → quantity = 1
```

The remaining inventory is maintained directly through `available_tickets`.

This approach is sufficient because the requirements only need ticket quantity reservation. Seat identity is not required.

---

## 4. Ticket Reservation and Concurrency

The most important database operation in the reservation flow is the atomic inventory update:

```sql
UPDATE concert
SET available_tickets = available_tickets - :quantity
WHERE id = :concertId
  AND available_tickets >= :quantity;
```

The update succeeds only when the requested quantity is still available.

The repository method returns the number of affected rows:

- `1` → inventory was successfully reserved.
- `0` → the concert does not have enough tickets available.

The booking is then created within the same transaction.

Conceptually:

```text
BEGIN TRANSACTION

    Atomic inventory update
              │
              ├── success → create booking
              │
              └── failure → rollback and return conflict

COMMIT
```

This avoids a read-modify-write race such as:

```text
Read available tickets
        ↓
Check availability
        ↓
Subtract quantity
        ↓
Save
```

The atomic conditional update allows PostgreSQL to perform the availability check and decrement as one database operation.

---

## 6. Indexes

### `idx_booking_concert_id`

```sql
CREATE INDEX idx_booking_concert_id
    ON booking(concert_id);
```

This index supports queries that locate bookings belonging to a particular concert.

### `idx_concert_booking_window`

```sql
CREATE INDEX idx_concert_booking_window
    ON concert (booking_start_at, booking_end_at);
```

This index supports the application's available-concert search, which filters concerts based on their booking window.

---

## 7. Schema Management

Flyway manages database schema changes through versioned migration scripts:

```text
src/main/resources/db/migration/
├── V1__create_concert_and_booking_table.sql
└── V2__add_concert_booking_window_index.sql
```

---

## Design Summary

The database design focuses on maintaining ticket inventory safely under concurrent booking requests while keeping the schema simple.

Key decisions:

- PostgreSQL is used as the relational database.
- `concert.available_tickets` stores current inventory.
- `booking.quantity` stores the quantity reserved by each booking.
- A foreign key maintains the concert-to-booking relationship.
- Database constraints protect core inventory.
- An atomic conditional update prevents ticket overselling during concurrent requests.
- The inventory update and booking creation occur within one transaction.
- Flyway owns schema evolution, while Hibernate validates the schema.
