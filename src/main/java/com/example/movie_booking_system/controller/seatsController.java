package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.seats;
import com.example.movie_booking_system.service.seatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/seats")
public class seatsController {

    @Autowired
    private seatsService seatsService;

    // Endpoint to get all seats
    @GetMapping("/")
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
    public seats saveSeat(@RequestBody seats seat) {
        return seatsService.saveSeat(seat);
    }
    @PatchMapping("/{seatId}/availability")
    public seats updateSeatAvailability(@PathVariable Long seatId, @RequestParam Boolean seat_available) {
        return seatsService.updateSeatAvailability(seatId,seat_available);
    }
    @GetMapping("/showtime/{showtimeId}")
    public List<seats> getSeatsByShowtime(@PathVariable Long showtimeId) {
        return seatsService.getSeatsByShowtime(showtimeId);
    }
}
