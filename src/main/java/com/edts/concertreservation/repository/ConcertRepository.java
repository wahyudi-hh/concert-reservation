package com.edts.concertreservation.repository;

import com.edts.concertreservation.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

    List<Concert> findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(Instant startTime, Instant endTime);

    @Modifying
    @Query("""
        UPDATE Concert
        SET availableTickets = availableTickets - :quantity
        WHERE id = :concertId AND availableTickets >= :quantity
        """)
    int reserveTickets(@Param("concertId") UUID concertId, @Param("quantity") int quantity);
}
