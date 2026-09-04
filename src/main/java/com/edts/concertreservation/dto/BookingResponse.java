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
public class BookingResponse {
    private UUID id;
    private UUID concertId;
    private Integer quantity;
}
