package com.edts.concertreservation.exception;

import java.util.UUID;

public class InvalidBookingQuantityException extends RuntimeException {

    public InvalidBookingQuantityException(String message) {
        super(message);
    }
}
