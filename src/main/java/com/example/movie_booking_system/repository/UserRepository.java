package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, String> {
    Users findByEmail(String email);
}

