package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.exception.InvalidConcertException;
import com.edts.concertreservation.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService{

    private static final String INVALID_BOOKING_WINDOW_ERROR =
        "Booking start time must be before booking end time";

    private static final String INVALID_MAX_TICKETS_ERROR =
        "Maximum tickets per booking cannot exceed total tickets";

    private final ConcertRepository concertRepository;

    @Override
    public Concert createConcert(
        String name,
        String description,
        Instant bookingStartAt,
        Instant bookingEndAt,
        Integer totalTickets,
        Integer maxTicketsPerBooking) {

        validateConcert(bookingStartAt, bookingEndAt, totalTickets, maxTicketsPerBooking);

        Concert concert = Concert.builder()
            .name(name)
            .description(description)
            .bookingStartAt(bookingStartAt)
            .bookingEndAt(bookingEndAt)
            .totalTickets(totalTickets)
            .availableTickets(totalTickets)
            .maxTicketsPerBooking(maxTicketsPerBooking)
            .build();
        return concertRepository.save(concert);
    }

    private void validateConcert(
        Instant bookingStartAt, Instant bookingEndAt, Integer totalTickets, Integer maxTicketsPerBooking
    ) {
        if (!bookingStartAt.isBefore(bookingEndAt)) {
            throw new InvalidConcertException(INVALID_BOOKING_WINDOW_ERROR);
        }

        if (maxTicketsPerBooking > totalTickets) {
            throw new InvalidConcertException(INVALID_MAX_TICKETS_ERROR);
        }
    }

    @Override
    public List<Concert> findAvailableConcerts() {
        Instant now = Instant.now();
        return concertRepository.findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(now, now);
    }
}
