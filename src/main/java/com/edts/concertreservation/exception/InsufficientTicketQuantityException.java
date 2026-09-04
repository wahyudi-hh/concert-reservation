package com.edts.concertreservation.exception;

import java.util.UUID;

public class InsufficientTicketQuantityException extends RuntimeException {

    public InsufficientTicketQuantityException() {
        super("Not enough ticket(s) available");
    }
}
