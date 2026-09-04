package com.edts.concertreservation.controller;

import com.edts.concertreservation.dto.ConcertResponse;
import com.edts.concertreservation.dto.CreateConcertRequest;
import com.edts.concertreservation.dto.CreateConcertResponse;
import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateConcertResponse createConcert(@Valid @RequestBody CreateConcertRequest request) {
        Concert concert = concertService.createConcert(
            request.getName(),
            request.getDescription(),
            request.getBookingStartAt(),
            request.getBookingEndAt(),
            request.getTotalTickets(),
            request.getMaxTicketsPerBooking()
        );
        return toCreateConcertResponse(concert);
    }

    @GetMapping
    public List<ConcertResponse> findAvailableConcerts() {
        return concertService.findAvailableConcerts().stream()
            .map(this::toConcertResponse)
            .toList();
    }

    private CreateConcertResponse toCreateConcertResponse(Concert concert) {
        return CreateConcertResponse.builder()
            .id(concert.getId())
            .name(concert.getName())
            .description(concert.getDescription())
            .bookingStartAt(concert.getBookingStartAt())
            .bookingEndAt(concert.getBookingEndAt())
            .totalTickets(concert.getTotalTickets())
            .availableTickets(concert.getAvailableTickets())
            .maxTicketsPerBooking(concert.getMaxTicketsPerBooking())
            .build();
    }

    private ConcertResponse toConcertResponse(Concert concert) {
        return ConcertResponse.builder()
            .id(concert.getId())
            .name(concert.getName())
            .description(concert.getDescription())
            .availableTickets(concert.getAvailableTickets())
            .maxTicketsPerBooking(concert.getMaxTicketsPerBooking())
            .build();
    }
}
