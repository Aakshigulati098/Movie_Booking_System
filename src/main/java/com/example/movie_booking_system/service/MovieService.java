package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.MovieDTO;
import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.repository.MovieRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;


import java.util.List;


@Service
public class MovieService {


    private  MovieRepository movieRepository;

    @Autowired
    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }


    // Convert Movie to MovieDTO
    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setId(movie.getId());
        movieDTO.setTitle(movie.getTitle());
        movieDTO.setImage(movie.getImage());
        movieDTO.setType(movie.getType());

        return movieDTO;
    }


public List<MovieDTO> getAllMovies() {
    List<Movie> movies = movieRepository.findAll();
    if (movies.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found in the database");
    }
    return movies.stream().map(this::convertToDTO).toList();
}

    // ✅ Add a new movie
    public Movie addMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    // ✅ Get a movie by ID with `orElseThrow()`
    public MovieDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found with ID: " + id));
        return convertToDTO(movie);
    }
    public List<MovieDTO> nowShowing() {
        List<Movie> movies = movieRepository.findAllByStatus("nowShowing");
        if (movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found for now showing");
        }
        return movies.stream().map(this::convertToDTO).toList();
    }

    // Method to get the first 9 movies that are coming soon
    public List<MovieDTO> comingSoon() {
        List<Movie> movies = movieRepository.findAllByStatus("comingSoon");
        if (movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found for coming soon");
        }
        return movies.stream().map(this::convertToDTO).toList();
    }

    public List<String> getUniqueCategories() {
        List<Movie> movies = movieRepository.findAll();
        return movies.stream()
                .map(Movie::getGenre)
                .distinct()
                .toList();
    }

    public List<MovieDTO> getMoviesByGenre(String genre) {
        List<Movie> movies = movieRepository.findByGenreContainingIgnoreCase(genre);
        if (movies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No movies found for the genre: " + genre);
        }
        return movies.stream().map(this::convertToDTO).toList();
    }
}
