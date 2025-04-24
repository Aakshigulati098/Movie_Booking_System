package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.UserDTO;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private static final java.util.logging.Logger userLogger= Logger.getLogger(UserController.class.getName());

//    @Autowired
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ GET USER PROFILE
    @GetMapping("/{email}")
    public Users getUser(@PathVariable String email){
        userLogger.info("Email: " + email);
        return userRepository.findByEmail(email);
    }
    @GetMapping("/profile")
    public Users getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    // ✅ UPDATE USER PROFILE
    @PutMapping("/update")
    public String updateUserProfile(@RequestBody UserDTO updatedUser, Authentication authentication) {
        userLogger.fine("Updating user profile: " + updatedUser);

        userLogger.info("Name: " + updatedUser.getName());
        userLogger.info("Phone: " + updatedUser.getPhone());
        String email = authentication.getName();

        Users existingUser = userRepository.findByEmail(email);

        if (existingUser == null) {
            return "User not found";
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setPhone(updatedUser.getPhone());

        userRepository.save(existingUser);

        return "Profile updated successfully";
    }
}