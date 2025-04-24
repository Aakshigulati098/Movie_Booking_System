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

        // Implement EMAIl OTP verification
        String otp = String.format("%04d", new Random().nextInt(10000));
        long expiryTime = System.currentTimeMillis() + (5 * 60 * 1000);

        session.setAttribute("currentUser", newUser.toString());
        session.setAttribute("otp", otp);
        session.setAttribute(OTP_SESSION_KEY, expiryTime);

        otpEmailController.sendOtpEmail(user.getName(), user.getEmail(), otp);
        return "Otp Sent Successfully";
    }

    public ResponseEntity<String> verifyOtp(@RequestBody String inputOtp) {

        String sessionOtp = (String) session.getAttribute("otp");
        Long otpExpiry = (Long) session.getAttribute(OTP_SESSION_KEY);


        if (sessionOtp == null || otpExpiry == null || System.currentTimeMillis() > otpExpiry) {
            session.removeAttribute("otp"); // Remove expired OTP
            session.removeAttribute(OTP_SESSION_KEY);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OTP expired. Please request a new one.");
        }

        if (!sessionOtp.equals(inputOtp)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
        }

        session.setAttribute("otpVerified", true);
        session.removeAttribute("otp");
        session.removeAttribute(OTP_SESSION_KEY);


        return ResponseEntity.ok("OTP verified successfully!");
    }


}
