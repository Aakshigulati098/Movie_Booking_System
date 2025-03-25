package com.example.movie_booking_system.service;

import com.example.movie_booking_system.emailotp.OtpEmailController;
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

    @Autowired
    private HttpSession session;

    @Autowired
    private OtpEmailController otpEmailController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String signup(Users user){

        Users isUserExist = userRepository.findByEmail(user.getEmail());
        if(isUserExist!=null){
            throw new RuntimeException("Email already exist with another account");
        }

        Users newUser = new Users();
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());
        newUser.setPhone(user.getPhone());
        newUser.setAddress(user.getAddress());

        // TODO
        // Implement EMAIl OTP verification
        String otp = String.format("%04d", new Random().nextInt(10000));
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

        session.setAttribute("currentUser", newUser);
        session.setAttribute("otp", otp);
        session.setAttribute("otpExpiry", expiryTime);

        otpEmailController.sendOtpEmail(user.getName(), user.getEmail(), otp);
        return "Otp Sent Successfully";
    }

    public ResponseEntity<String> verifyOtp(@RequestBody String inputOtp) {

        String sessionOtp = (String) session.getAttribute("otp");
        Long otpExpiry = (Long) session.getAttribute("otpExpiry");

        Users newUser = (Users) session.getAttribute("currentUser");

        if (sessionOtp == null || otpExpiry == null || System.currentTimeMillis() > otpExpiry) {
            session.removeAttribute("otp"); // Remove expired OTP
            session.removeAttribute("otpExpiry");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP expired. Please request a new one.");
        }

        if (!sessionOtp.equals(inputOtp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
        }

        session.setAttribute("otpVerified", true);
        session.removeAttribute("otp");
        session.removeAttribute("otpExpiry");

        Users savedUser = userRepository.save(newUser);

        return ResponseEntity.ok("OTP verified successfully!");
    }


}
