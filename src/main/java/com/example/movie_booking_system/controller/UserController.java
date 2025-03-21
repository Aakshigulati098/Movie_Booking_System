package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.model.Users;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET USER PROFILE
    @GetMapping("/profile")
    public Users getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    // ✅ UPDATE USER PROFILE
    @PutMapping("/update")
    public String updateUserProfile(@RequestBody Users updatedUser, Authentication authentication) {
        String email = authentication.getName();
        Users existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return "User not found";
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        userRepository.save(existingUser);

        return "Profile updated successfully";
    }
}