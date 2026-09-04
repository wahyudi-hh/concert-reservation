package com.edts.concertreservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BookingRequest {

    @NotBlank
    private String concertId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
