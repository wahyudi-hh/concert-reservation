package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Booking;
import com.edts.concertreservation.entity.Concert;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    List<Concert> findAvailableConcerts();

    Booking book(UUID concertId, int quantity);
}
