package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Booking;
import com.example.movie_booking_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class BookingController {
    @Autowired
    private BookingService bookingService;
    @PostMapping("/booking/{user_id}/{showtime_id}/{seat_id}")
    public synchronized ResponseEntity<Object> booking_Movie(@PathVariable("user_id") Long user_id, @PathVariable("showtime_id") Long showtime_id, @PathVariable("seat_id")Long seat_id){
//        idhar payment hoga
//        payment ke basis pe hum logic define karenge
//        abhi happy case matlab hogaya haui
        try{

            return new ResponseEntity<Object>(bookingService.booking_Movie(user_id,showtime_id,seat_id),HttpStatus.OK);

        }catch(Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }
}
