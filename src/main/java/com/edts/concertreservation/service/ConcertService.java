package com.edts.concertreservation.service;

import com.edts.concertreservation.entity.Concert;

import java.util.List;

public interface ConcertService {

    List<Concert> findAvailableConcerts();
}
