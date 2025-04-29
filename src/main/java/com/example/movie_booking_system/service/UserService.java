package com.example.movie_booking_system.service;

import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.exceptions.UserRegistrationException;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Random;

@Service
public class UserService {

    private static final String OTP_SESSION_KEY="otpExpiry";




    private HttpSession session;
    private OtpEmailController otpEmailController;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(HttpSession session, OtpEmailController otpEmailController, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.session = session;
        this.otpEmailController = otpEmailController;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String signup(Users user) {
        Users isUserExist = userRepository.findByEmail(user.getEmail());
        if (isUserExist != null) {
            throw new UserRegistrationException("Email already exists with another account");
        }

        Users newUser = new Users();
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setName(user.getName());

        newUser.setEmail(user.getEmail());
        newUser.setPhone(user.getPhone());
        newUser.setAddress(user.getAddress());

        // Implement EMAIL OTP verification
        String otp = String.format("%04d", new Random().nextInt(10000));
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

        session.setAttribute("currentUserName", newUser.getName());
        session.setAttribute("currentUserEmail", newUser.getEmail());

        session.setAttribute("currentUser", newUser);

        session.setAttribute("otp", otp);
        session.setAttribute(OTP_SESSION_KEY, expiryTime);

        otpEmailController.sendOtpEmail(user.getName(), user.getEmail(), otp);
        return "Otp Sent Successfully";
    }

    public ResponseEntity<String> verifyOtp(@RequestBody String inputOtp) {
        String sessionOtp = (String) session.getAttribute("otp");
        Long otpExpiry = (Long) session.getAttribute(OTP_SESSION_KEY);
        String userName = (String) session.getAttribute("currentUserName");
        String userEmail = (String) session.getAttribute("currentUserEmail");

        if (sessionOtp == null || otpExpiry == null || System.currentTimeMillis() > otpExpiry) {
            cleanupSession();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP expired. Please request a new one.");
        }

        if (!sessionOtp.equals(inputOtp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
        }

        try {
            Users userDetails = (Users) session.getAttribute("currentUser");
            if (userDetails == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
            }

            Users newUser = userRepository.findByEmail(userEmail);
            if (newUser != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists.");
            }



            userRepository.save(userDetails);
            session.setAttribute("otpVerified", true);
            otpEmailController.sendWelcomeEmail(userName, userEmail);
            cleanupSession();

            return ResponseEntity.ok("OTP verified successfully! Account created.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during registration: " + e.getMessage());
        }
    }


    private void cleanupSession() {
        session.removeAttribute("otp");
        session.removeAttribute(OTP_SESSION_KEY);
        session.removeAttribute("currentUser");
        session.removeAttribute("currentUserName");
        session.removeAttribute("currentUserEmail");
    }


}
