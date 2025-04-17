package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.service.ShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class ShowTimeController {

    @Autowired
    private ShowTimeService showTimeService;

    @GetMapping("/getAllShows")
    public ResponseEntity<Object> getAllShows() {
        try{
            return new ResponseEntity<>(showTimeService.getAllShows(), HttpStatus.OK);
        }
        catch(ResponseStatusException e){
            return new ResponseEntity<>(e.getReason(),e.getStatusCode());
        }
    }

    @GetMapping("/getShowById/{showTimeId}")
    public ResponseEntity<Object> getShowById(@PathVariable("showTimeId") Long showTimeId) {
        try {
            return new ResponseEntity<>(showTimeService.getShowTimeById(showTimeId), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
