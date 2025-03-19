package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    // ✅ Constructor-based dependency injection (recommended)
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // ✅ Method to get all movies
    public List<Movie> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        if (movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");
        }
        return movies;
    }

    // ✅ Add a new movie
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // ✅ Get a movie by ID with `orElseThrow()`
    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with ID: " + id));
    }
}
