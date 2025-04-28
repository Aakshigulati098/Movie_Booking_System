package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Seats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * SeatsRepository provides database operations related to Seats.
 *
 * Good SQL understanding and clean coding practices help ensure better abstraction and maintainability.
 */
@Repository
public interface SeatsRepository extends JpaRepository<Seats, Long> {

    @Query("SELECT s.id FROM Seats s WHERE s.seatNumber = :seatNumber AND s.showtime.id = :showtimeId")
    Optional<Long> findSeatIdBySeatNumberAndShowtime(@Param("seatNumber") Long seatNumber,
                                                     @Param("showtimeId") Long showtimeId);

    @Query("SELECT s.id FROM Seats s WHERE s.Seatrow = :seatRow AND s.seatNumber = :seatNumber")
    Optional<Long> findSeatIdBySeatRowAndSeatNumber(@Param("seatRow") String seatRow,
                                                    @Param("seatNumber") Long seatNumber);
}
