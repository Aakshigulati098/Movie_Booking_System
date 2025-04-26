package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.MovieDTO;
import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.repository.MovieRepository;
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

class MovieServiceTest {

    @InjectMocks
    private MovieService movieService;

    @Mock
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllMovies_ShouldReturnMovies() {
        // Arrange
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Movie 1");
        Movie movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("Movie 2");
        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie1, movie2));

        // Act
        List<MovieDTO> movies = movieService.getAllMovies();

        // Assert
        assertEquals(2, movies.size());
        assertEquals("Movie 1", movies.get(0).getTitle());
        assertEquals("Movie 2", movies.get(1).getTitle());
    }

    @Test
    void getAllMovies_NoMoviesFound_ShouldThrowException() {
        // Arrange
        when(movieRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> movieService.getAllMovies());
        assertEquals("404 NOT_FOUND \"No movies found in the database\"", exception.getMessage());
    }

    @Test
    void addMovie_ShouldAddMovie() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("New Movie");
        when(movieRepository.save(movie)).thenReturn(movie);

        // Act
        Movie result = movieService.addMovie(movie);

        // Assert
        assertNotNull(result);
        assertEquals("New Movie", result.getTitle());
        verify(movieRepository, times(1)).save(movie);
    }

    @Test
    void getMovieById_ShouldReturnMovie() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        // Act
        MovieDTO movieDTO = movieService.getMovieById(1L);

        // Assert
        assertNotNull(movieDTO);
        assertEquals(1L, movieDTO.getId());
        assertEquals("Test Movie", movieDTO.getTitle());
    }

    @Test
    void getMovieById_NotFound_ShouldThrowException() {
        // Arrange
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> movieService.getMovieById(1L));
        assertEquals("404 NOT_FOUND \"Movie not found with ID: 1\"", exception.getMessage());
    }

    @Test
    void nowShowing_ShouldReturnMovies() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Now Showing Movie");
        when(movieRepository.findAllByStatus("nowShowing")).thenReturn(Collections.singletonList(movie));

        // Act
        List<MovieDTO> movies = movieService.nowShowing();

        // Assert
        assertEquals(1, movies.size());
        assertEquals("Now Showing Movie", movies.get(0).getTitle());
    }

    @Test
    void nowShowing_NoMoviesFound_ShouldThrowException() {
        // Arrange
        when(movieRepository.findAllByStatus("nowShowing")).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> movieService.nowShowing());
        assertEquals("404 NOT_FOUND \"No movies found for now showing\"", exception.getMessage());
    }

    @Test
    void comingSoon_ShouldReturnMovies() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Coming Soon Movie");
        when(movieRepository.findAllByStatus("comingSoon")).thenReturn(Collections.singletonList(movie));

        // Act
        List<MovieDTO> movies = movieService.comingSoon();

        // Assert
        assertEquals(1, movies.size());
        assertEquals("Coming Soon Movie", movies.get(0).getTitle());
    }

    @Test
    void comingSoon_NoMoviesFound_ShouldThrowException() {
        // Arrange
        when(movieRepository.findAllByStatus("comingSoon")).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> movieService.comingSoon());
        assertEquals("404 NOT_FOUND \"No movies found for coming soon\"", exception.getMessage());
    }

    @Test
    void getUniqueCategories_ShouldReturnCategories() {
        // Arrange
        Movie movie1 = new Movie();
        movie1.setGenre("Action");
        Movie movie2 = new Movie();
        movie2.setGenre("Drama");
        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie1, movie2));

        // Act
        List<String> categories = movieService.getUniqueCategories();

        // Assert
        assertEquals(2, categories.size());
        assertTrue(categories.contains("Action"));
        assertTrue(categories.contains("Drama"));
    }

    @Test
    void getMoviesByGenre_ShouldReturnMovies() {
        // Arrange
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Genre Movie");
        movie.setGenre("Action");
        when(movieRepository.findByGenreContainingIgnoreCase("Action")).thenReturn(Collections.singletonList(movie));

        // Act
        List<MovieDTO> movies = movieService.getMoviesByGenre("Action");

        // Assert
        assertEquals(1, movies.size());
        assertEquals("Genre Movie", movies.get(0).getTitle());
    }

    @Test
    void getMoviesByGenre_NoMoviesFound_ShouldThrowException() {
        // Arrange
        when(movieRepository.findByGenreContainingIgnoreCase("Action")).thenReturn(Collections.emptyList());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> movieService.getMoviesByGenre("Action"));
        assertEquals("404 NOT_FOUND \"No movies found for the genre: Action\"", exception.getMessage());
    }
}