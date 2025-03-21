package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Movie;
import com.example.movie_booking_system.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class movieController {
    @Autowired
    private MovieService movieService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }
    @GetMapping("/getAllMovies")
    public ResponseEntity<Object> getAllMovies() { //better type handling and exception handling
        try{
            return new ResponseEntity<>(movieService.getAllMovies(), HttpStatus.OK);
        }
        catch(ResponseStatusException e){
            return new ResponseEntity<>(e.getReason(),e.getStatusCode());
        }
    }
    @PostMapping("/addMovie")
    public ResponseEntity<Object> addMovie(@RequestBody Movie movie) {
        return new ResponseEntity<>(movieService.addMovie(movie), HttpStatus.OK);
    }

    @GetMapping("/getMovieById/{id}")
    public ResponseEntity<Object> getMovieById(@PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(movieService.getMovieById(id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
}
