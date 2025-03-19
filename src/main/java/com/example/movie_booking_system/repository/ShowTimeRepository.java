package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowTimeRepository extends JpaRepository<ShowTime,Long> {

}
