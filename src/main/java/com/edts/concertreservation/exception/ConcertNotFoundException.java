package com.edts.concertreservation.exception;

import java.util.UUID;

public class ConcertNotFoundException extends RuntimeException {

    public ConcertNotFoundException(UUID concertId) {
        super("Concert ID " + concertId + " not found");
    }
}
