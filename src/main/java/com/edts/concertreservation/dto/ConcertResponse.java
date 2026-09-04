package com.edts.concertreservation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class ConcertResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer availableTickets;
    private Integer maxTicketsPerBooking;
}
