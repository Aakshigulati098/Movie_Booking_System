package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    // Method to get all movies
    public List<Movie> getAllMovies() {
        // Fetch all movies from the repository
        List<Movie> Movies = movieRepository.findAll();

        // If the movies list is empty, throw a ResponseStatusException
        if (Movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");
        }

        return Movies;
    }

    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }
    public Movie getMovieById(Long id) {
        if(movieRepository.existsById(id)) {
            return movieRepository.findById(id).get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");

    }

}
