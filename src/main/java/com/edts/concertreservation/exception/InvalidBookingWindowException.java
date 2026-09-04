package com.edts.concertreservation.exception;

public class InvalidBookingWindowException extends RuntimeException {

    public InvalidBookingWindowException() {
        super("Concert booking is not currently open");
    }
}
