package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;
import com.edts.concertreservation.repository.BookingRepository;
import com.edts.concertreservation.repository.ConcertRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingConcurrencyIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private UUID concertId;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        if (concertId != null) {
            bookingRepository.deleteAllByConcertId(concertId);
            concertRepository.deleteById(concertId);
        }
    }

    private Concert createConcert(int totalTickets) {
        Instant now = Instant.now();
        Concert concert = Concert.builder()
            .name("concurrency test concert")
            .description("concert for testing concurrency")
            .bookingStartAt(now.minusSeconds(60))
            .bookingEndAt(now.plusSeconds(60))
            .totalTickets(totalTickets)
            .availableTickets(totalTickets)
            .maxTicketsPerBooking(1)
            .build();

        concert = concertRepository.save(concert);
        concertId = concert.getId();

        return concert;
    }

    HttpEntity<String> generateHttpRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {
                    "concertId": "%s",
                    "quantity": 1
                }
                """.formatted(concertId);
        return new HttpEntity<>(body, headers);
    }

    List<Callable<ResponseEntity<String>>> generateConcurrentRequests(int numberOfRequest) {
        List<Callable<ResponseEntity<String>>> tasks = new ArrayList<>();

        HttpEntity<String> request = generateHttpRequest();

        for (int i=0; i<numberOfRequest; i++) {
            tasks.add(() -> restTemplate.postForEntity("/api/bookings", request, String.class));
        }

        return tasks;
    }

    @Test
    void concurrentBookings_shouldNotOversellTickets() throws Exception {
        int totalTickets = 10;
        int numberOfRequest = 100;
        Concert concert = createConcert(totalTickets);
        ExecutorService executor = Executors.newFixedThreadPool(20);

        try {
            List<Callable<ResponseEntity<String>>> tasks = generateConcurrentRequests(numberOfRequest);

            List<Future<ResponseEntity<String>>> futures = executor.invokeAll(tasks);
            long successfulBookings = 0;
            long failedBookings = 0;

            for (Future<ResponseEntity<String>> future : futures) {
                ResponseEntity<String> response = future.get();

                if (response.getStatusCode() == HttpStatus.CREATED) {
                    successfulBookings++;
                } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                    failedBookings++;
                }
            }
            assertEquals(totalTickets, successfulBookings);
            assertEquals(numberOfRequest - totalTickets, failedBookings);

            Concert updatedConcert = concertRepository.findById(concert.getId()).orElseThrow();
            assertEquals(0, updatedConcert.getAvailableTickets());
            long bookingCount = bookingRepository.findAll().stream()
                .filter(booking -> booking.getConcertId().equals(concert.getId()))
                .count();
            assertEquals(10, bookingCount);
        } finally {
            executor.shutdown();
        }
    }
}
