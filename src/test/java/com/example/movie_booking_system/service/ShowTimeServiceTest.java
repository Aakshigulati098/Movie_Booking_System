package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.ShowTime;
import com.example.movie_booking_system.repository.ShowTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShowTimeServiceTest {

    @Mock
    private ShowTimeRepository showTimeRepository;

    @InjectMocks
    private ShowTimeService showTimeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllShows_ShouldReturnListOfShowTimes() {
        // Arrange
        ShowTime show1 = new ShowTime();
        show1.setId(1L);
        ShowTime show2 = new ShowTime();
        show2.setId(2L);
        when(showTimeRepository.findAll()).thenReturn(Arrays.asList(show1, show2));

        // Act
        List<ShowTime> result = showTimeService.getAllShows();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(showTimeRepository, times(1)).findAll();
    }

    @Test
    void getAllShows_NoShowsFound_ShouldThrowException() {
        // Arrange
        when(showTimeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> showTimeService.getAllShows());
        assertEquals("404 NOT_FOUND \"No shows found in the database\"", exception.getMessage());
        verify(showTimeRepository, times(1)).findAll();
    }

    @Test
    void getShowTimeById_ShowTimeExists_ShouldReturnShowTime() {
        // Arrange
        ShowTime showTime = new ShowTime();
        showTime.setId(1L);
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(showTime));

        // Act
        ShowTime result = showTimeService.getShowTimeById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(showTimeRepository, times(1)).findById(1L);
    }

    @Test
    void getShowTimeById_ShowTimeDoesNotExist_ShouldThrowException() {
        // Arrange
        when(showTimeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> showTimeService.getShowTimeById(1L));
        assertEquals("404 NOT_FOUND \"ShowTime not found with ID: 1\"", exception.getMessage());
        verify(showTimeRepository, times(1)).findById(1L);
    }
}