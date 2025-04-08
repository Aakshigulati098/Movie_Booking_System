package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.BookingMovieDTO;
import com.example.movie_booking_system.dto.BookingResponseDTO;
import com.example.movie_booking_system.models.Booking;
import com.example.movie_booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin(origins = "http://localhost:5174")
@RestController

public class BookingController {
    @Autowired
    private BookingService bookingService;

    @GetMapping("/helloo")
    public String getting(){
        return "hey i am here";
    }
//    i feel like here i should be having seat number and with the showtime_id and seat_number i should be getting the seat_id

    @PostMapping("/bookingMovie/{user_id}/{movieId}")
    public ResponseEntity<Object> booking_Movie(@PathVariable("user_id") Long user_id,@PathVariable("movieId")Long movie_id, @RequestBody BookingMovieDTO bookingMovieDTO) {
        try {
            System.out.println("hey i got called in booking ");
            List<Long> seatIds = bookingMovieDTO.getSeatIds();
            System.out.println(seatIds);
//            for all the seats booking
//            for (Long seatId : seatIds) {
               return new ResponseEntity<>(bookingService.booking_Movie(user_id, seatIds,movie_id),HttpStatus.OK);
//            }
//            ab tak sare false ho chuke hai
//            ab details ke sath mail bhejo and then call it off for the api
//            pehle details lo kya kya bhejna hai .. and then request body mai dalo ..and then usko uss template pe dalo
//            and then bss service layer se uss function ko call karo
//            details kya kya chahiye ?
//            user details
//                    name,email,phone
//            booking details
//            film , theatre , showtime , seats , price
//            call the service here

//            return new ResponseEntity<>(bookingService.SuccessfulEmailsending(user_id, movie_id, seatIds), HttpStatus.OK);

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