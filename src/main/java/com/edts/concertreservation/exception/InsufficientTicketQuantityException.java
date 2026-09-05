package com.edts.concertreservation.exception;

public class InsufficientTicketQuantityException extends RuntimeException {

    public InsufficientTicketQuantityException() {
        super("Not enough ticket(s) available");
    }
}
