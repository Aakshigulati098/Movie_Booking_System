package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

//    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET USER PROFILE
    @GetMapping("/{email}")
    public Users getUser(@PathVariable String email){
        System.out.println("Email: " + email);
        return userRepository.findByEmail(email);
    }
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