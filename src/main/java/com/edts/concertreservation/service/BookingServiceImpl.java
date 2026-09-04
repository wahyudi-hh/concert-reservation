package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Booking;
import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.BookingRepository;
import com.edts.concertreservation.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{

    private final ConcertRepository concertRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Booking book(UUID concertId, int quantity) {
        Concert concert = concertRepository.findById(concertId).orElseThrow();

        int updated = concertRepository.reserveTickets(concertId, quantity);

        Booking booking = Booking.builder()
            .concertId(concertId)
            .quantity(quantity)
            .createdAt(Instant.now())
            .build();

        return bookingRepository.save(booking);
    }
}
