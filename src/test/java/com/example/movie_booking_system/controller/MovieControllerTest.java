package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.MovieDTO;
import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.service.MovieService;
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

class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void home_ShouldReturnWelcomeMessage() {
        String response = movieController.home();
        assertEquals("Welcome - You're connected to Spring Application 'Movie-Booking-System' ", response);
    }

    @Test
    void hello_ShouldReturnHelloMessage() {
        String response = MovieController.hello();
        assertEquals("Hello " + System.getProperty("user.name"), response);
    }


    @Test
    void getAllMovies_ShouldHandleResponseStatusException() {
        when(movieService.getAllMovies()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Movies not found"));

        ResponseEntity<Object> response = movieController.getAllMovies();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Movies not found", response.getBody());
        verify(movieService, times(1)).getAllMovies();
    }

    @Test
    void addMovie_ShouldReturnOkResponse() {
        Movie movie = new Movie();
        when(movieService.addMovie(movie)).thenReturn(movie);

        ResponseEntity<Object> response = movieController.addMovie(movie);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movie, response.getBody());
        verify(movieService, times(1)).addMovie(movie);
    }


    @Test
    void getMovieById_ShouldHandleGenericException() {
        when(movieService.getMovieById(1L)).thenThrow(new RuntimeException("Internal error"));

        ResponseEntity<Object> response = movieController.getMovieById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody());
        verify(movieService, times(1)).getMovieById(1L);
    }

    @Test
    void getNowShowingMovies_ShouldReturnOkResponse() {
        List<MovieDTO> movies = new ArrayList<>();
        when(movieService.nowShowing()).thenReturn(movies);

        ResponseEntity<List<MovieDTO>> response = movieController.getNowShowingMovies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movies, response.getBody());
        verify(movieService, times(1)).nowShowing();
    }

    @Test
    void getNowShowingMovies_ShouldHandleResponseStatusException() {
        when(movieService.nowShowing()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found"));

        ResponseEntity<List<MovieDTO>> response = movieController.getNowShowingMovies();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(movieService, times(1)).nowShowing();
    }

    @Test
    void getComingSoonMovies_ShouldReturnOkResponse() {
        List<MovieDTO> movies = new ArrayList<>();
        when(movieService.comingSoon()).thenReturn(movies);

        ResponseEntity<List<MovieDTO>> response = movieController.getComingSoonMovies();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(movies, response.getBody());
        verify(movieService, times(1)).comingSoon();
    }

    @Test
    void getComingSoonMovies_ShouldHandleResponseStatusException() {
        when(movieService.comingSoon()).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found"));

        ResponseEntity<List<MovieDTO>> response = movieController.getComingSoonMovies();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(null, response.getBody());
        verify(movieService, times(1)).comingSoon();
    }

    @Test
    void getUniqueCategories_ShouldReturnOkResponse() {
        List<String> categories = new ArrayList<>();
        when(movieService.getUniqueCategories()).thenReturn(categories);

        ResponseEntity<List<String>> response = movieController.getUniqueCategories();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categories, response.getBody());
        verify(movieService, times(1)).getUniqueCategories();
    }

    @Test
    void getMoviesByGenre_ShouldHandleResponseStatusException() {
        when(movieService.getMoviesByGenre("Action")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found"));

        ResponseEntity<Object> response = movieController.getMoviesByGenre("Action");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No movies found", response.getBody());
        verify(movieService, times(1)).getMoviesByGenre("Action");
    }
}