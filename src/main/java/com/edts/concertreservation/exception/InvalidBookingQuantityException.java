package com.edts.concertreservation.exception;

public class InvalidBookingQuantityException extends RuntimeException {

    public InvalidBookingQuantityException(String message) {
        super(message);
    }
}
