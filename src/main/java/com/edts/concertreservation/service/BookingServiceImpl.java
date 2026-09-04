package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Booking;
import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.exception.ConcertNotFoundException;
import com.edts.concertreservation.exception.InsufficientTicketQuantityException;
import com.edts.concertreservation.exception.InvalidBookingQuantityException;
import com.edts.concertreservation.exception.InvalidBookingWindowException;
import com.edts.concertreservation.repository.BookingRepository;
import com.edts.concertreservation.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private static final String ZERO_QTY_ERROR = "Booking quantity must be greater than zero";
    private static final String MAX_QTY_EXCEEDED_ERROR = "Booking quantity exceeds the maximum allowed per booking";

    private final ConcertRepository concertRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<Concert> findAvailableConcerts() {
        Instant now = Instant.now();
        return concertRepository.findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(now, now);
    }

    @Override
    @Transactional
    public Booking book(UUID concertId, int quantity) {
        Concert concert = concertRepository.findById(concertId)
            .orElseThrow(() -> new ConcertNotFoundException(concertId));

        Instant now = Instant.now();
        validateBooking(concert, quantity, now);

        int updated = concertRepository.reserveTickets(concertId, quantity);
        if (updated == 0) {
            throw new InsufficientTicketQuantityException();
        }

        Booking booking = Booking.builder()
            .concertId(concertId)
            .quantity(quantity)
            .createdAt(now)
            .build();

        return bookingRepository.save(booking);
    }

    private void validateBooking(Concert concert, int quantity, Instant now) {
        if (quantity < 1) {
            throw new InvalidBookingQuantityException(ZERO_QTY_ERROR);
        }

        if (quantity > concert.getMaxTicketsPerBooking()) {
            throw new InvalidBookingQuantityException(MAX_QTY_EXCEEDED_ERROR);
        }

        if (now.isBefore(concert.getBookingStartAt()) || !now.isBefore(concert.getBookingEndAt())) {
            throw new InvalidBookingWindowException();
        }
    }
}
