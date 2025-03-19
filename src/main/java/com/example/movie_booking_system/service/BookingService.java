package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatsRepository seatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowTimeRepository showTimeRepository;

//    payment ki service ka object hoga and payment ki function call hogi jis se mereko
//    confirmatioin milegya ke yeh huya ke nahi and then i will get the id

    public synchronized Booking booking_Movie(Long user_id, Long showtime_id, Long seat_id) {


        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user_id));


        ShowTime showtime = showTimeRepository.findById(showtime_id)
                .orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + showtime_id));


        Seats seat = seatsRepository.findById(seat_id)
                .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seat_id));

        if (!seat.getSeatAvailable()) {
            throw new RuntimeException("Seat is already booked!");
        }





        Long ticketPrice = 250L;


//        changes to be done to the booking schema that on successful payment
//        there we can set tyhe payemnt id
//        const payment_id=PaymentService.executePayment(arguments);

        seat.setSeatAvailable(false);
        seatsRepository.save(seat);

        Booking booking = new Booking(
        );

        booking.setBooking_date(LocalDateTime.now());
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setAmount(1235L);
        booking.setShowtime(showtime);

//        exception handling
        return bookingRepository.save(booking);
    }
}
