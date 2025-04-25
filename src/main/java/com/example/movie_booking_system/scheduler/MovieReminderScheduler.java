package com.example.movie_booking_system.scheduler;

import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.models.Booking;
import com.example.movie_booking_system.repository.BookingReminderRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class MovieReminderScheduler {


    BookingReminderRepository bookingReminderRepository;
    OtpEmailController otpEmailController;

    @Autowired
    public MovieReminderScheduler(BookingReminderRepository bookingReminderRepository, OtpEmailController otpEmailController) {
        this.bookingReminderRepository = bookingReminderRepository;
        this.otpEmailController = otpEmailController;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void sendMovieReminders(){

        LocalDateTime twoHoursLater = LocalDateTime.now().plusHours(2).truncatedTo(ChronoUnit.MINUTES);
        List<Booking> bookings = bookingReminderRepository.findBookingsForReminders(twoHoursLater);

        for(Booking booking : bookings){
            String email = booking.getUser().getEmail() ;
            String name = booking.getUser().getName();
            LocalDateTime showtime = LocalDateTime.parse(booking.getShowtime().getTime());
            String movie = "Singham";

            otpEmailController.sendReminderEmail(email,movie,showtime,name);

            bookingReminderRepository.markReminderAsSent(booking.getId());
        }

    }
}
