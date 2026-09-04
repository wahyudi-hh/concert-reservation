package com.edts.concertreservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CreateConcertRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Instant bookingStartAt;

    @NotNull
    private Instant bookingEndAt;

    @NotNull
    @Min(1)
    private Integer totalTickets;

    @NotNull
    @Min(1)
    private Integer maxTicketsPerBooking;
}
