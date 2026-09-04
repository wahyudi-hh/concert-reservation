package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;

import java.time.Instant;
import java.util.List;

public interface ConcertService {

    Concert createConcert(
        String name,
        String Description,
        Instant bookingStartAt,
        Instant bookingEndAt,
        Integer totalTickets,
        Integer maxTicketsPerBooking
    );

    List<Concert> findAvailableConcerts();
}
