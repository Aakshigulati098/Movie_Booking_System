package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
}
