package com.edts.concertreservation.repository;

import com.edts.concertreservation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Transactional
    void deleteAllByConcertId(UUID concertId); // for concurrency test purpose
}
