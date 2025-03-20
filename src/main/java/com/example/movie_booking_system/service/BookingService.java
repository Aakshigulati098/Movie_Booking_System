package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

//    @Autowired
//    private PaymentService paymentService; // Inject Payment Service

    @Transactional
    public Booking booking_Movie(Long user_id, Long showtime_id, Long seat_id) {

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

        // Execute Payment
//        boolean paymentSuccess = paymentService.executePayment(user, ticketPrice);
//        if (!paymentSuccess) {
//            throw new RuntimeException("Payment failed! Booking cannot proceed.");
//        }

        // Mark seat as booked
        seat.setSeatAvailable(false);
        seatsRepository.save(seat);

        // Create and save booking
        Booking booking = new Booking();
        booking.setBooking_date(LocalDateTime.now());
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setAmount(ticketPrice);
        booking.setShowtime(showtime);

        return bookingRepository.save(booking);
    }
}
