package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcertServiceImplTest {

    @Mock
    private ConcertRepository concertRepository;

    private ConcertServiceImpl concertService;

    @BeforeEach
    void setUp() {
        concertService = new ConcertServiceImpl(concertRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(concertRepository);
    }

    private Concert generateValidConcert(UUID id) {
        Instant now = Instant.now();
        return Concert.builder()
            .id(id)
            .name("concert")
            .description("concert description")
            .totalTickets(100)
            .availableTickets(50)
            .maxTicketsPerBooking(3)
            .bookingStartAt(now.minusSeconds(60))
            .bookingEndAt(now.plusSeconds(60))
            .build();
    }

    @Test
    void findAvailableConcerts_shouldReturnAvailableConcerts() {
        Concert concert = generateValidConcert(UUID.randomUUID());

        when(concertRepository
            .findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(any(Instant.class), any(Instant.class)))
            .thenReturn(Collections.singletonList(concert));

        List<Concert> concerts = concertService.findAvailableConcerts();

        assertEquals(1, concerts.size());
        assertEquals(concert, concerts.get(0));

        verify(concertRepository)
            .findAllByBookingStartAtLessThanEqualAndBookingEndAtGreaterThan(any(Instant.class), any(Instant.class));
    }
}