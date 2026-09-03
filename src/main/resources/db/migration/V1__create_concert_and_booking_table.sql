CREATE TABLE concert (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    booking_start_at TIMESTAMPTZ NOT NULL,
    booking_end_at TIMESTAMPTZ NOT NULL,
    total_tickets INTEGER NOT NULL,
    available_tickets INTEGER NOT NULL,
    max_tickets_per_booking INTEGER NOT NULL,

    CONSTRAINT chk_concert_booking_window
        CHECK (booking_start_at < booking_end_at),

    CONSTRAINT chk_concert_total_tickets
        CHECK (total_tickets > 0),

    CONSTRAINT chk_concert_available_tickets
        CHECK (
            available_tickets >= 0
            AND available_tickets <= total_tickets
        ),

    CONSTRAINT chk_concert_max_tickets_per_booking
        CHECK (
            max_tickets_per_booking > 0
            AND max_tickets_per_booking <= total_tickets
        )
);

CREATE TABLE booking (
    id UUID PRIMARY KEY,
    concert_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_booking_concert
        FOREIGN KEY (concert_id)
        REFERENCES concert(id),

    CONSTRAINT chk_booking_quantity
        CHECK (quantity > 0)
);

CREATE INDEX idx_booking_concert_id
    ON booking(concert_id);