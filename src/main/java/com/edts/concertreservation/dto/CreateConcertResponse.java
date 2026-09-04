package com.edts.concertreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class CreateConcertResponse {
    private UUID id;
    private String name;
    private String description;
    private Instant bookingStartAt;
    private Instant bookingEndAt;
    private Integer totalTickets;
    private Integer availableTickets;
    private Integer maxTicketsPerBooking;
}
