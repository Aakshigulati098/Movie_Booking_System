package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.movie;
import com.example.movie_booking_system.repository.movieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class movieService {

    @Autowired
    private movieRepository movieRepository;

    // Method to get all movies
    public List<movie> getAllMovies() {
        // Fetch all movies from the repository
        List<movie> movies = movieRepository.findAll();

        // If the movies list is empty, throw a ResponseStatusException
        if (movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");
        }

        return movies;
    }

    public movie addMovie(movie movie) {
        return movieRepository.save(movie);
    }
    public movie getMovieById(Long id) {
        if(movieRepository.existsById(id)) {
            return movieRepository.findById(id).get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");

    }

}
