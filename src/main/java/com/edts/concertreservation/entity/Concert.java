package com.edts.concertreservation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "concert")
@Getter
@Setter
@NoArgsConstructor
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "booking_start_at", nullable = false)
    private Instant bookingStartAt;

    @Column(name = "booking_end_at", nullable = false)
    private Instant bookingEndAt;

    @Column(name = "total_tickets", nullable = false)
    private Integer totalTickets;

    @Column(name = "available_tickets", nullable = false)
    private Integer availableTickets;

    @Column(name = "max_tickets_per_booking", nullable = false)
    private Integer maxTicketsPerBooking;
}
