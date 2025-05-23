package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}

