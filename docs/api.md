# API Documentation

## Base URL

```text
http://localhost:8080
```

## Overview

The Concert Reservation API provides endpoints to:

- Create concerts and configure their booking windows and ticket inventory.
- Search for concerts whose booking windows are currently open.
- Book one or more tickets for a concert.
- Prevent ticket overselling during concurrent booking requests.

All request and response bodies use JSON.

### Date and Time Format

All date/time values use ISO-8601 format with an explicit UTC offset.

Example:

```text
2026-09-05T08:00:00+07:00
```

Requests should use this format for `bookingStartAt` and `bookingEndAt`.

The API uses `Instant` internally, so timestamps represent an absolute point in time. The offset in the request is used to represent that point in time and does not need to be `+07:00`.

Examples in this documentation use `+07:00`; the API may return UTC (`Z`) because the underlying type is `Instant`.

---

## 1. Create Concert

Creates a new concert and initializes its available ticket inventory.

### Request

```http
POST /api/concerts
Content-Type: application/json
```

### Request Body

```json
{
  "name": "EDTS Summer Concert",
  "description": "A test concert for the reservation API",
  "bookingStartAt": "2026-09-05T08:00:00+07:00",
  "bookingEndAt": "2026-09-05T10:00:00+07:00",
  "totalTickets": 10000,
  "maxTicketsPerBooking": 5
}
```

### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | String | Yes | Concert name |
| `description` | String | No | Concert description |
| `bookingStartAt` | Instant | Yes | Start of the booking window |
| `bookingEndAt` | Instant | Yes | End of the booking window |
| `totalTickets` | Integer | Yes | Total ticket inventory |
| `maxTicketsPerBooking` | Integer | Yes | Maximum tickets allowed in a single booking |

### Rules

- `name` must not be blank.
- `bookingStartAt` and `bookingEndAt` are required.
- `bookingStartAt` must be before `bookingEndAt`.
- `totalTickets` must be greater than zero.
- `maxTicketsPerBooking` must be greater than zero.
- `maxTicketsPerBooking` cannot exceed `totalTickets`.
- `availableTickets` is initialized to `totalTickets`.

<details>
<summary><strong>Successful Response — 201 Created</strong></summary>

```json
{
  "id": "7f4d7f8e-2d87-4c17-9a89-8f7e2d2a1c01",
  "name": "EDTS Summer Concert",
  "description": "A test concert for the reservation API",
  "bookingStartAt": "2026-09-05T08:00:00+07:00",
  "bookingEndAt": "2026-09-05T10:00:00+07:00",
  "totalTickets": 10000,
  "availableTickets": 10000,
  "maxTicketsPerBooking": 5
}
```

</details>

<details>
<summary><strong>Error Response — 400 Bad Request</strong></summary>

Returned when request validation or concert business validation fails.

```json
{
  "message": "maxTicketsPerBooking: must be greater than or equal to 1"
}
```

</details>

---

## 2. Find Available Concerts

Returns concerts whose booking window is currently open.

### Request

```http
GET /api/concerts
```

<details>
<summary><strong>Successful Response — 200 OK</strong></summary>

```json
[
  {
    "id": "7f4d7f8e-2d87-4c17-9a89-8f7e2d2a1c01",
    "name": "EDTS Summer Concert",
    "description": "A test concert for the reservation API",
    "availableTickets": 9998,
    "maxTicketsPerBooking": 5
  }
]
```

</details>

### Response Fields

| Field | Type | Description |
|---|---|---|
| `id` | UUID | Concert identifier |
| `name` | String | Concert name |
| `description` | String | Concert description |
| `availableTickets` | Integer | Current remaining ticket inventory |
| `maxTicketsPerBooking` | Integer | Maximum tickets allowed per booking |

### Notes

A concert is considered available when the current time is within its booking window:

```text
bookingStartAt <= current time < bookingEndAt
```

The booking end time is exclusive.

---

## 3. Book Tickets

Books one or more tickets for a concert.

### Request

```http
POST /api/bookings
Content-Type: application/json
```

### Request Body

```json
{
  "concertId": "7f4d7f8e-2d87-4c17-9a89-8f7e2d2a1c01",
  "quantity": 2
}
```

### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `concertId` | String | Yes | UUID of the concert |
| `quantity` | Integer | Yes | Number of tickets to book |

### Rules

- `concertId` must not be blank.
- `quantity` must be at least `1`.
- `quantity` cannot exceed the concert's `maxTicketsPerBooking`.
- The booking must be made while the concert's booking window is open.
- Sufficient tickets must be available.

<details>
<summary><strong>Successful Response — 201 Created</strong></summary>

```json
{
  "id": "3b3f7d91-1b7c-4e9c-8c7f-2c4f6d4a7a11",
  "concertId": "7f4d7f8e-2d87-4c17-9a89-8f7e2d2a1c01",
  "quantity": 2,
  "createdAt": "2026-09-05T08:05:00+07:00"
}
```

</details>

<details>
<summary><strong>Error Response — 400 Bad Request</strong></summary>

Returned when request validation or booking quantity validation fails.

Invalid quantity:

```json
{
  "message": "quantity: must be greater than or equal to 1"
}
```

Quantity exceeds booking limit:

```json
{
  "message": "Booking quantity exceeds the maximum allowed per booking"
}
```

</details>

<details>
<summary><strong>Error Response — 404 Not Found</strong></summary>

Returned when the concert does not exist or the supplied concert ID is invalid.

```json
{
  "message": "Concert ID 7f4d7f8e-2d87-4c17-9a89-8f7e2d2a1c01 not found"
}
```

</details>

<details>
<summary><strong>Error Response — 409 Conflict</strong></summary>

Returned when the booking window is closed or insufficient tickets are available.

Booking window example:

```json
{
  "message": "Concert booking is not currently open"
}
```

Insufficient tickets example:

```json
{
  "message": "Not enough ticket(s) available"
}
```

</details>

---

## 4. Concurrency and Ticket Reservation

Ticket inventory is protected using an atomic database update:

```sql
UPDATE concert
SET available_tickets = available_tickets - :quantity
WHERE id = :concertId
  AND available_tickets >= :quantity
```

The update succeeds only when sufficient tickets remain.

The booking operation is executed inside a database transaction. The inventory update and booking record creation therefore succeed or fail together.

This prevents concurrent requests from overselling the available ticket inventory.

For example, if a concert has 10 tickets and receives 100 concurrent requests for one ticket each:

- 10 requests can successfully reserve tickets.
- The remaining 90 requests receive `409 Conflict`.
- Available inventory ends at `0`.
- Exactly 10 booking records are created.
