package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Theatre;
import com.example.movie_booking_system.repository.TheatreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TheatreServiceTest {

    @Mock
    private TheatreRepository theatreRepository;

    @InjectMocks
    private TheatreService theatreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllTheatres_ShouldReturnListOfTheatres() {
        // Arrange
        Theatre theatre1 = new Theatre();
        theatre1.setId(1L);
        theatre1.setName("Theatre 1");

        Theatre theatre2 = new Theatre();
        theatre2.setId(2L);
        theatre2.setName("Theatre 2");

        when(theatreRepository.findAll()).thenReturn(Arrays.asList(theatre1, theatre2));

        // Act
        List<Theatre> result = theatreService.getAllTheatres();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Theatre 1", result.get(0).getName());
        assertEquals("Theatre 2", result.get(1).getName());
        verify(theatreRepository, times(1)).findAll();
    }

    @Test
    void getAllTheatres_NoTheatresFound_ShouldReturnEmptyList() {
        // Arrange
        when(theatreRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Theatre> result = theatreService.getAllTheatres();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(theatreRepository, times(1)).findAll();
    }
}