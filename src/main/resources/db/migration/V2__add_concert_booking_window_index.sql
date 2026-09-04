CREATE INDEX idx_concert_booking_window
    ON concert (booking_start_at, booking_end_at);