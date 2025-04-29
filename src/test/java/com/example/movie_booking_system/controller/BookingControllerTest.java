package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.BookingMovieDTO;
import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getting_ShouldReturnMessage() {
        String response = bookingController.getting();
        assertEquals("hey i am here", response);
    }


    @Test
    void bookingMovie_ShouldReturnNotFoundOnException() {
        BookingMovieDTO bookingMovieDTO = new BookingMovieDTO();
        List<Long> seatIds = new ArrayList<>();
        seatIds.add(1L);
        bookingMovieDTO.setSeatIds(seatIds);

        when(bookingService.bookingMovie(1L, seatIds, 2L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = bookingController.bookingMovie(1L, 2L, bookingMovieDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(bookingService, times(1)).bookingMovie(1L, seatIds, 2L);
    }


    @Test
    void cancellingBookingMovie_ShouldReturnNotFoundOnException() {
        when(bookingService.cancellingBookingMovie(1L, 2L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = bookingController.cancellingBookingMovie(1L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(bookingService, times(1)).cancellingBookingMovie(1L, 2L);
    }

    @Test
    void getBookingDetails_ShouldReturnNotFoundOnException() {
        when(bookingService.getBookingDetails(1L, 2L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = bookingController.getBookingDetails(1L, 2L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Error", response.getBody());
        verify(bookingService, times(1)).getBookingDetails(1L, 2L);
    }

    @Test
    void getAllBookings_ShouldReturnOkResponse() {
        List<BookingResponseDTO> bookings = new ArrayList<>();
        when(bookingService.getAllBookings(1L)).thenReturn(bookings);

        ResponseEntity<Object> response = bookingController.getAllBookings(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookings, response.getBody());
        verify(bookingService, times(1)).getAllBookings(1L);
    }

    @Test
    void getAllBookings_ShouldReturnNotFoundOnException() {
        when(bookingService.getAllBookings(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = bookingController.getAllBookings(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(bookingService, times(1)).getAllBookings(1L);
    }
}