package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Booking;

import java.util.UUID;

public interface BookingService {

    Booking book(UUID concertId, int quantity);
}
