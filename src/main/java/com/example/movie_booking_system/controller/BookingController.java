package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.BookingMovieDTO;
import com.example.movie_booking_system.dto.BookingResponseDTO;

import com.example.movie_booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;


@CrossOrigin(origins = "http://localhost:5174")
@RestController

public class BookingController {

    private static final java.util.logging.Logger bookingLogger = Logger.getLogger(BookingController.class.getName());
    @Autowired
    private BookingService bookingService;

    @GetMapping("/helloo")
    public String getting(){
        return "hey i am here";
    }


    @PostMapping("/bookingMovie/{user_id}/{movieId}")
    public ResponseEntity<Object> bookingMovie(@PathVariable("user_id") Long userId,@PathVariable("movieId")Long movieId, @RequestBody BookingMovieDTO bookingMovieDTO) {
        try {
            bookingLogger.info("hey i got called in booking ");
            List<Long> seatIds = bookingMovieDTO.getSeatIds();


               return new ResponseEntity<>(bookingService.booking_Movie(userId, seatIds,movieId),HttpStatus.OK);



        }
            catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/cancelBooking/{user_id}/{booking_id}")
    public ResponseEntity<Object> Cancelling_booking_movie(@PathVariable("user_id") Long user_id, @PathVariable("booking_id") Long booking_id){
        try{
            return new ResponseEntity<Object>(bookingService.Cancelling_booking_movie(user_id,booking_id),HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getBooking/{user_id}/{boking_id}")
    public ResponseEntity<Object> get_booking_details(@PathVariable("user_id")Long user_id, @PathVariable("boking_id")Long boking_id){
        try{
            return new ResponseEntity<Object>(bookingService.get_booking_details(user_id,boking_id),HttpStatus.OK);
        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getBooking/{userId}")
    public ResponseEntity<Object> getAllBookings(@PathVariable("userId") Long userId) {
        try {
            List<BookingResponseDTO> bookings = bookingService.getAllBookings(userId);
            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}


//lets discuss the approach for signing with or validating with otp
//yes we can send the data to the user via email a specific code lets say xyz
//then how can i then validate that code is the question
//the user will hit a route with the code and the code will be validated with the code in the database
//wait and at that instant i will trigger a function which will remove the code from the database after a specified time


//chalo flow toh figure out kar liya maine lets break it down with some edge cases