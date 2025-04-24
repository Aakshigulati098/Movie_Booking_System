package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Seats;

import com.example.movie_booking_system.service.SeatsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.logging.Logger;


@CrossOrigin(origins = "http://localhost:5173")
@RestController

public class SeatsController {

    private static final java.util.logging.Logger seatLogger = Logger.getLogger(SeatsController.class.getName());

    @Autowired
    private SeatsService seatsService;

    @GetMapping("/abir")
    public String getting(){
        return "hey i am here working abir";
    }

    // Endpoint to get all seats
    @GetMapping("/allSeats")
    public ResponseEntity<Object> getAllSeats() {

        try{
            return new ResponseEntity<>(seatsService.getAllSeats(), HttpStatus.OK);
        }
        catch(ResponseStatusException e){
            return new ResponseEntity<>(e.getReason(),e.getStatusCode());
        }
    }

    // Endpoint to get a specific seat by ID
    @GetMapping("/{seatId}")
    public ResponseEntity<Object> getSeatById(@PathVariable Long seat_Id) {
        try {
            return new ResponseEntity<>(seatsService.getSeatById(seat_Id), HttpStatus.OK);
        } catch (ResponseStatusException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public Seats saveSeat(@RequestBody Seats seat) {
        return seatsService.saveSeat(seat);
    }
    @PatchMapping("/{seatId}/availability")
    public Seats updateSeatAvailability(@PathVariable Long seatId, @RequestParam Boolean seat_available) {
        return seatsService.updateSeatAvailability(seatId,seat_available);
    }
    @GetMapping("/showtime/{showtimeId}")
    public List<Seats> getSeatsByShowtime(@PathVariable Long showtimeId) {
        seatLogger.info("hey i got called ");
        return seatsService.getSeatsByShowtime(showtimeId);
    }
}
