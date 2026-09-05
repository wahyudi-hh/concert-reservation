package com.edts.concertreservation.controller;

import com.edts.concertreservation.dto.BookingRequest;
import com.edts.concertreservation.dto.BookingResponse;
import com.edts.concertreservation.entity.Booking;
import com.edts.concertreservation.exception.ConcertNotFoundException;
import com.edts.concertreservation.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.book(
            parseConcertId(request.getConcertId()),
            request.getQuantity());
        return toResponse(booking);
    }

    private UUID parseConcertId(String concertId) {
        try {
            return UUID.fromString(concertId);
        } catch (IllegalArgumentException ex) {
            throw new ConcertNotFoundException(concertId);
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
            .id(booking.getId())
            .concertId(booking.getConcertId())
            .quantity(booking.getQuantity())
            .createdAt(booking.getCreatedAt())
            .build();
    }
}
