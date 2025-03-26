package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Booking;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.service.BookingService;
import com.example.movie_booking_system.service.EmailSenderService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private EmailSenderService emailSenderService;
    @GetMapping("/helloo")
    public String getting(){
        return "hey i am here";
    }
//    i feel like here i should be having seat number and with the showtime_id and seat_number i should be getting the seat_id
    @PostMapping("/bookingMovie/{user_id}/{showtime_id}/{seat_number}")
    public  ResponseEntity<Object> booking_Movie(@PathVariable("user_id") Long user_id, @PathVariable("showtime_id") Long showtime_id, @PathVariable("seat_number")Long seat_number){
//        idhar payment hoga
//        payment ke basis pe hum logic define karenge
//        abhi happy case matlab hogaya haui
        try{

            return new ResponseEntity<Object>(bookingService.booking_Movie(user_id,showtime_id,seat_number),HttpStatus.OK);

        }catch(Exception e){
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

    @DeleteMapping("/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId) {
        // Logic to cancel the booking
        // Example:
        String to = "user@example.com";
        String userName = "User";
        String movieName = "Example Movie";
        String showTime = "4:00 PM";

        try {
            emailSenderService.sendCancellationEmail(to, userName, movieName, showTime);
        } catch (MessagingException e) {
            e.printStackTrace();
            return "Failed to send cancellation email.";
        }

        return "Booking canceled and email sent.";
    }


//    public Users orElseGet(Supplier<? extends Users> supplier) {
//        return this != null ? this : supplier.get();
//    }

    // not tested yet just made
//    @PostMapping("/bookSeats")
//    public ResponseEntity<String> bookSeats(@RequestParam String userName,
//                                            @RequestParam String userEmail,
//                                            @RequestParam String theaterName,
//                                            @RequestParam String movieName,
//                                            @RequestParam List<Long> seats,
//                                            @RequestParam Long showtimeId) {
//        try {
//            bookingService.bookSeats(userName, userEmail, theaterName, movieName, seats, showtimeId);
//            return new ResponseEntity<>("Booking successful and confirmation email sent.", HttpStatus.OK);
//        } catch (MessagingException e) {
//            return new ResponseEntity<>("Failed to send confirmation email.", HttpStatus.INTERNAL_SERVER_ERROR);
//        } catch (RuntimeException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }

    // not tested yet just made
    @PostMapping("/paymentFailed")
    public ResponseEntity<String> handlePaymentFailure(@RequestParam Long bookingId) {
        bookingService.handlePaymentFailure(bookingId);
        return new ResponseEntity<>("Payment failed. Redirecting to seats page.", HttpStatus.OK);
    }
}


//lets discuss the approach for signing with or validating with otp
//yes we can send the data to the user via email a specific code lets say xyz
//then how can i then validate that code is the question
//the user will hit a route with the code and the code will be validated with the code in the database
//wait and at that instant i will trigger a function which will remove the code from the database after a specified time


//chalo flow toh figure out kar liya maine lets break it down with some edge cases

//