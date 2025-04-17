package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BookingReminderRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b from Booking b WHERE b.showtime.time = :twoHoursLater AND b.reminderSent = false")
    List<Booking> findBookingsForReminders(@Param("twoHoursLater") LocalDateTime twoHoursLater);

    @Modifying
    @Query("UPDATE Booking b SET b.reminderSent = true WHERE b.id = :bookingId")
    void markReminderAsSent(@Param("bookingId") Long bookingId);

}
