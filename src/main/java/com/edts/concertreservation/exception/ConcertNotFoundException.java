package com.edts.concertreservation.exception;

public class ConcertNotFoundException extends RuntimeException {

    public ConcertNotFoundException(String concertId) {
        super("Concert ID " + concertId + " not found");
    }
}
