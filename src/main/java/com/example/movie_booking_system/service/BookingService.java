package com.example.movie_booking_system.service;

import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private SeatsRepository seatsRepository;
//    seat mai bss avialable hai ke nahi woh dikhani hai
//    and the fact that woh showtime specific hai so no worries woh either available hogi ya fir nahi

    @Autowired
    private UserRepository userRepository;
//    everything related to user is being handled by om

    @Autowired
    private ShowTimeRepository showTimeRepository;

//    @Autowired
//    private PaymentService paymentService; // Inject Payment Service

    public Long getSeatId(Long seatNumber, Long showtimeId) {
        return seatsRepository.findSeatIdBySeatNumberAndShowtime(seatNumber, showtimeId)
                .orElseThrow(() -> new RuntimeException("Seat not found for seatNumber: "
                        + seatNumber + " and showtimeId: " + showtimeId));
    }


//    need to handle the runtime exception here
    @Transactional
    public Booking booking_Movie(Long user_id, Long showtime_id, Long seat_number) {

        Users user = userRepository.findById(user_id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + user_id));

        ShowTime showtime = showTimeRepository.findById(showtime_id)
                .orElseThrow(() -> new RuntimeException("Showtime not found with ID: " + showtime_id));

//        this will be throwing an exception so kindly handle it
//        i am having issues with naming the return value of function while handling exception
       Long seat_id = getSeatId(seat_number, showtime_id);


        Seats seat = seatsRepository.findById(seat_id)
                .orElseThrow(() -> new RuntimeException("Seat not found with ID: " + seat_id));

        if (!seat.getSeatAvailable()) {
            throw new RuntimeException("Seat is already booked!");
        }

        Long ticketPrice = 250L;




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

    public Booking get_booking_details(Long user_id,Long booking_id){

        Booking booking = bookingRepository.findById(booking_id).orElseThrow(() -> new RuntimeException("Booking not found with ID: " + booking_id));//custom exception handling
        if(!Objects.equals(booking.getUser().getId(), user_id)){
            throw new RuntimeException("User is not authorized to cancel this booking.");
        }

        return booking;

    }

//    here i am using transactional not for concurrency but if for any reason the refund is failed
//    i should keep the booking and not betray the user
//    !TODO-> i need to provide useful responses to frontend such that proper experience is there for the user
    @Transactional
    public Boolean Cancelling_booking_movie(Long user_id, Long booking_id) {

        //        here what i need to do is from this object will be going through the
//            seat details and will be updating it to avaiable and this will also be
//        transactional (i dont think so as no two person will have the same se

//        mujhe param se userid aur booking id lena hai and then
//        mujhe pehle check kar na hai ke isi user ne book ki hai ya nahi
//        and then if yes then pehle mujhe seat avaialable karni hai
//        and then booking table mai se woh details update karna hai by removing that booking


        // Fetch the booking, throw exception if not found
        Booking booking = bookingRepository.findById(booking_id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + booking_id));

        // Check if the booking belongs to the user
        if (!Objects.equals(booking.getUser().getId(), user_id)) {
            throw new RuntimeException("User is not authorized to cancel this booking.");
        }

        // Mark seat as available
        Seats seat = booking.getSeat();
        seat.setSeatAvailable(true);
        seatsRepository.save(seat);

        //            abhi seat toh available kar diya
//            now initiate refund
//            on successful refund just remove this booking and can create  seperate table for such user for a specific buisness use case

        // TODO: Call the payment service for refund process
        boolean refundSuccessful = true;

        if (refundSuccessful) {
            // Remove the booking from the database
            bookingRepository.deleteById(booking_id);
            return true;
        } else {
            throw new RuntimeException("Refund failed, booking is retained.");

        }

    }

}
