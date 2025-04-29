package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.service.ShowTimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ShowTimeControllerTest {

    @Mock
    private ShowTimeService showTimeService;

    @InjectMocks
    private ShowTimeController showTimeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllShows_ShouldHandleResponseStatusException() {
        when(showTimeService.getAllShows()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Shows not found"));

        ResponseEntity<Object> response = showTimeController.getAllShows();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Shows not found", response.getBody());
        verify(showTimeService, times(1)).getAllShows();
    }

    @Test
    void getShowById_ShouldHandleResponseStatusException() {
        when(showTimeService.getShowTimeById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found"));

        ResponseEntity<Object> resp = null;
        try {
            resp = showTimeController.getShowById(1L);
        } catch (ResponseStatusException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("Show not found", ex.getReason());
        }

        verify(showTimeService, times(1)).getShowTimeById(1L);
    }

    @Test
    void getShowById_ShouldHandleGenericException() {
        when(showTimeService.getShowTimeById(1L)).thenThrow(new RuntimeException("Internal error"));

        ResponseEntity<Object> response = showTimeController.getShowById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody());
        verify(showTimeService, times(1)).getShowTimeById(1L);
    }
}