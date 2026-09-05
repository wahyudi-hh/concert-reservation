package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.BookingRepository;
import com.edts.concertreservation.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingIntegrationTest {

    private static final String CLOSED_WINDOW_ERROR = "Concert booking is not currently open";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private Concert concert;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();
        concert = Concert.builder()
            .name("booking test integration concert")
            .description("concert for booking integration test")
            .bookingStartAt(now.minusSeconds(60))
            .bookingEndAt(now.plusSeconds(60))
            .totalTickets(10)
            .availableTickets(10)
            .maxTicketsPerBooking(5)
            .build();
    }

    @AfterEach
    void tearDown() {
        if (concert.getId() != null) {
            bookingRepository.deleteAllByConcertId(concert.getId());
            concertRepository.deleteById(concert.getId());
        }
    }

    HttpEntity<String> generateHttpRequest(int quantity) {
        String body = """
            {
                "concertId": "%s",
                "quantity": %d
            }
            """.formatted(concert.getId(), quantity);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void book_shouldSuccessfullyBookTickets() {
        concertRepository.save(concert);
        HttpEntity<String> request = generateHttpRequest(2);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/bookings", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(concert.getId().toString()));

        Concert updatedConcert = concertRepository.findById(concert.getId()).orElseThrow();
        assertEquals(8, updatedConcert.getAvailableTickets());
        long count = bookingRepository.findAll().stream()
            .filter(booking -> booking.getConcertId().equals(concert.getId()))
            .count();
        assertEquals(1, count);
    }

    @Test
    void bookWithoutConcertId_shouldSuccessfullyBookTickets() {
        String body = """
            {
                "quantity": 1
            }
            """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/bookings", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("concertId: must not be blank"));
    }

    @Test
    void bookAfterWindowEnd_shouldReturn409() {
        concert.setBookingEndAt(concert.getBookingEndAt().minusSeconds(90));
        concertRepository.save(concert);
        HttpEntity<String> request = generateHttpRequest(1);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/bookings", request, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(CLOSED_WINDOW_ERROR));

        Concert updatedConcert = concertRepository.findById(concert.getId()).orElseThrow();
        assertEquals(concert.getTotalTickets(), updatedConcert.getAvailableTickets());
        long count = bookingRepository.findAll().stream()
            .filter(booking -> booking.getConcertId().equals(concert.getId()))
            .count();
        assertEquals(0, count);
    }
}
