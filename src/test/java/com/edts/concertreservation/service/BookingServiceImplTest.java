package com.edts.concertreservation.service;


import com.edts.concertreservation.entity.Booking;
import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.exception.ConcertNotFoundException;
import com.edts.concertreservation.exception.InsufficientTicketQuantityException;
import com.edts.concertreservation.exception.InvalidBookingQuantityException;
import com.edts.concertreservation.exception.InvalidBookingWindowException;
import com.edts.concertreservation.repository.BookingRepository;
import com.edts.concertreservation.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    private static final String ZERO_QTY_ERROR = "Booking quantity must be greater than zero";
    private static final String MAX_QTY_EXCEEDED_ERROR = "Booking quantity exceeds the maximum allowed per booking";

    @Mock
    private ConcertRepository concertRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(concertRepository, bookingRepository);
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(concertRepository);
        verifyNoMoreInteractions(bookingRepository);
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
    void bookDoesNotExistConcert_shouldThrowNotFoundException() {
        UUID concertId = UUID.randomUUID();

        when(concertRepository.findById(concertId)).thenReturn(Optional.empty());

        assertThrows(ConcertNotFoundException.class, () -> bookingService.book(concertId, 1));

        verify(concertRepository).findById(concertId);
    }

    @Test
    void bookZeroQuantity_shouldThrowInvalidBookingQuantityException() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        InvalidBookingQuantityException exception = assertThrows(
            InvalidBookingQuantityException.class,
            () -> bookingService.book(concertId, 0));
        assertEquals(ZERO_QTY_ERROR, exception.getMessage());

        verify(concertRepository).findById(concertId);
    }

    @Test
    void bookMoreThanMaxQuantity_shouldThrowInvalidBookingQuantityException() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        InvalidBookingQuantityException exception = assertThrows(
            InvalidBookingQuantityException.class,
            () -> bookingService.book(concertId, concert.getMaxTicketsPerBooking()+1));
        assertEquals(MAX_QTY_EXCEEDED_ERROR, exception.getMessage());

        verify(concertRepository).findById(concertId);
    }

    @Test
    void bookBeforeStartAt_shouldThrowInvalidBookingWindowException() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);
        concert.setBookingStartAt(Instant.now().plusSeconds(60));

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        assertThrows(InvalidBookingWindowException.class, () -> bookingService.book(concertId, 1));

        verify(concertRepository).findById(concertId);
    }

    @Test
    void bookAfterEndAt_shouldThrowInvalidBookingWindowException() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);
        concert.setBookingEndAt(Instant.now().minusSeconds(60));

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));

        assertThrows(InvalidBookingWindowException.class, () -> bookingService.book(concertId, 1));

        verify(concertRepository).findById(concertId);
    }

    @Test
    void bookNotEnoughTickets_shouldThrowInsufficientTicketException() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);
        int qty = 3;

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(concertRepository.reserveTickets(concertId, qty)).thenReturn(0);

        assertThrows(InsufficientTicketQuantityException.class, () -> bookingService.book(concertId, qty));

        verify(concertRepository).findById(concertId);
        verify(concertRepository).reserveTickets(concertId, qty);
    }

    @Test
    void bookSuccess_shouldReturnBooking() {
        UUID concertId = UUID.randomUUID();
        Concert concert = generateValidConcert(concertId);
        int qty = 3;

        Booking savedBooking = Booking.builder()
            .id(UUID.randomUUID())
            .concertId(concertId)
            .quantity(qty)
            .createdAt(Instant.now())
            .build();

        when(concertRepository.findById(concertId)).thenReturn(Optional.of(concert));
        when(concertRepository.reserveTickets(concertId, qty)).thenReturn(1);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        Booking result = bookingService.book(concertId, qty);
        assertEquals(savedBooking, result);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());

        Booking bookingArg = captor.getValue();

        assertEquals(concertId, bookingArg.getConcertId());
        assertEquals(qty, bookingArg.getQuantity());
        assertNotNull(bookingArg.getCreatedAt());

        verify(concertRepository).findById(concertId);
        verify(concertRepository).reserveTickets(concertId, 3);
    }
}