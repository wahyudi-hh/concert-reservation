package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConcertServiceImpl implements ConcertService{

    private final ConcertRepository concertRepository;

    @Override
    public List<Concert> findAvailableConcerts() {
        Instant now = Instant.now();
        return concertRepository.findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(now, now);
    }
}
