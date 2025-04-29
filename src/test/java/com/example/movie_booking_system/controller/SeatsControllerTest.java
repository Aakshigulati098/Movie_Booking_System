package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Seats;
import com.example.movie_booking_system.service.SeatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SeatsControllerTest {

    @Mock
    private SeatsService seatsService;

    @InjectMocks
    private SeatsController seatsController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getting_ShouldReturnMessage() {
        String response = seatsController.getting();
        assertEquals("hey I am here working abir", response);
    }

    @Test
    void getAllSeats_ShouldReturnOkResponse() {
        List<Seats> seatsList = new ArrayList<>();
        when(seatsService.getAllSeats()).thenReturn(seatsList);

        ResponseEntity<Object> response = seatsController.getAllSeats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(seatsList, response.getBody());
        verify(seatsService, times(1)).getAllSeats();
    }

    @Test
    void getAllSeats_ShouldHandleResponseStatusException() {
        when(seatsService.getAllSeats()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seats not found"));

        ResponseEntity<Object> response = seatsController.getAllSeats();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Seats not found", response.getBody());
        verify(seatsService, times(1)).getAllSeats();
    }


    @Test
    void getSeatById_ShouldHandleResponseStatusException() {
        when(seatsService.getSeatById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        ResponseEntity<Object> response = seatsController.getSeatById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Seat not found", response.getBody());
        verify(seatsService, times(1)).getSeatById(1L);
    }

    @Test
    void getSeatById_ShouldHandleGenericException() {
        when(seatsService.getSeatById(1L)).thenThrow(new RuntimeException("Internal error"));

        ResponseEntity<Object> response = seatsController.getSeatById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody());
        verify(seatsService, times(1)).getSeatById(1L);
    }

    @Test
    void saveSeat_ShouldReturnSavedSeat() {
        Seats seat = new Seats();
        when(seatsService.saveSeat(seat)).thenReturn(seat);

        Seats response = seatsController.saveSeat(seat);

        assertEquals(seat, response);
        verify(seatsService, times(1)).saveSeat(seat);
    }

    @Test
    void updateSeatAvailability_ShouldReturnUpdatedSeat() {
        Seats seat = new Seats();
        when(seatsService.updateSeatAvailability(1L, true)).thenReturn(seat);

        Seats response = seatsController.updateSeatAvailability(1L, true);

        assertEquals(seat, response);
        verify(seatsService, times(1)).updateSeatAvailability(1L, true);
    }

    @Test
    void getSeatsByShowtime_ShouldReturnSeatsList() {
        List<Seats> seatsList = new ArrayList<>();
        when(seatsService.getSeatsByShowtime(1L)).thenReturn(seatsList);

        List<Seats> response = seatsController.getSeatsByShowtime(1L);

        assertEquals(seatsList, response);
        verify(seatsService, times(1)).getSeatsByShowtime(1L);
    }
}