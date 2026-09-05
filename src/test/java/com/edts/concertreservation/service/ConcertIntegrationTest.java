package com.edts.concertreservation.service;

import com.edts.concertreservation.dto.CreateConcertResponse;
import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcertIntegrationTest {

    private static final String INVALID_BOOKING_WINDOW_ERROR =
        "Booking start time must be before booking end time";

    private static final String INVALID_MAX_TICKETS_ERROR =
        "Maximum tickets per booking cannot exceed total tickets";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConcertRepository concertRepository;

    private String identifier;
    private Concert concert;

    @AfterEach
    void tearDown() {
        if (concert != null) {
            concertRepository.deleteById(concert.getId());
        }
    }

    HttpEntity<String> generateHttpRequest(
        Instant bookingStartAt, Instant bookingEndAt, Integer maxTicketsPerBooking) {

        Instant now = Instant.now();
        identifier = String.valueOf(now.toEpochMilli());

        if (bookingStartAt == null) {
            bookingStartAt = now.minusSeconds(60).atOffset(ZoneOffset.ofHours(7)).toInstant();
        }
        if (bookingEndAt == null) {
            bookingEndAt = now.plusSeconds(60).atOffset(ZoneOffset.ofHours(7)).toInstant();
        }
        if (maxTicketsPerBooking == null) {
            maxTicketsPerBooking = 5;
        }

        String body = """
                {
                    "name": "Create concert integration %s",
                    "bookingStartAt": "%s",
                    "bookingEndAt": "%s",
                    "totalTickets": 100,
                    "maxTicketsPerBooking": %d
                }
                """.formatted(identifier, bookingStartAt, bookingEndAt, maxTicketsPerBooking);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    @Test
    void createConcert_shouldCreateConcertSuccessfully() {
        HttpEntity<String> request = generateHttpRequest(null, null, null);
        ResponseEntity<CreateConcertResponse> response = restTemplate.postForEntity("/api/concerts", request, CreateConcertResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        concert = concertRepository.findAll().stream()
            .filter(con -> con.getName().contains(identifier))
            .findFirst()
            .orElseThrow();
        assertEquals(concert.getId(), response.getBody().getId());
    }

    @Test
    void createConcertOverlapStartTime_shouldReturn400() {
        Instant now = Instant.now();

        HttpEntity<String> request = generateHttpRequest(now.plusSeconds(60), now, null);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/concerts", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(INVALID_BOOKING_WINDOW_ERROR));

        long count = concertRepository.findAll().stream()
            .filter(con -> con.getName().contains(identifier))
            .count();
        assertEquals(0, count);
    }

    @Test
    void createConcertInvalidMaxTicketsPerBooking_shouldReturn400() {
        Instant now = Instant.now();

        HttpEntity<String> request = generateHttpRequest(null, null, 101);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/concerts", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        assertTrue(response.getBody().contains(INVALID_MAX_TICKETS_ERROR));

        long count = concertRepository.findAll().stream()
            .filter(con -> con.getName().contains(identifier))
            .count();
        assertEquals(0, count);
    }
}
