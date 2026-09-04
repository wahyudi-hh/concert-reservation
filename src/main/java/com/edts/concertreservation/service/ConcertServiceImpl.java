package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService{

    private final ConcertRepository concertRepository;

    @Override
    public Concert createConcert(String name, String description, Instant bookingStartAt, Instant bookingEndAt, Integer totalTickets, Integer maxTicketsPerBooking) {
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

    @Override
    public List<Concert> findAvailableConcerts() {
        Instant now = Instant.now();
        return concertRepository.findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(now, now);
    }
}
