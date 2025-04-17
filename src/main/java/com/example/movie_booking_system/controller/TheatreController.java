package com.example.movie_booking_system.controller;
import com.example.movie_booking_system.models.Theatre;
import com.example.movie_booking_system.service.TheatreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class TheatreController {

    @Autowired
    private TheatreService theatreService;

    @GetMapping("/theatres")
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        List<Theatre> theatres = theatreService.getAllTheatres();
        return new ResponseEntity<>(theatres, HttpStatus.OK);
    }
}
