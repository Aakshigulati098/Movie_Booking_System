package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.seats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface seatsRepository extends JpaRepository<seats, Long> {
}
