package com.edts.concertreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "concert")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
