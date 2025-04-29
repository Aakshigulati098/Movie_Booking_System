package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.config.JwtProvider;
import com.example.movie_booking_system.models.Users;

import com.example.movie_booking_system.service.CustomUserDetails;
import com.example.movie_booking_system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {





    private PasswordEncoder passwordEncoder;


    private CustomUserDetails customUserDetails;


    private UserService userService;


    private JwtProvider jwtProvider;

    @Autowired
    public AuthController( PasswordEncoder passwordEncoder,
                         CustomUserDetails customUserDetails, UserService userService, JwtProvider jwtProvider) {

        this.passwordEncoder = passwordEncoder;
        this.customUserDetails = customUserDetails;
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token provided");
            }

            // Extract the token by removing "Bearer " prefix
            String token = authHeader.substring(7);

            // Validate the token
            if (jwtProvider.validateToken(token)) {
                // Optionally, you can return user details or additional info
                return ResponseEntity.ok().body(new HashMap<>());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Token validation error");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody Users user){
        try{
            String message = userService.signup(user);
            return ResponseEntity.ok(message);
        }
        catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody Map<String,String> request){

        String inputOtp = request.get("otp");
        return userService.verifyOtp(inputOtp);
    }

    @PostMapping("/login")
    public String login(@RequestBody Users user){
        Authentication authentication = authenticate(user.getEmail(),user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Use the autowired jwtProvider instance to call generateToken
       return jwtProvider.generateToken(authentication);


    }

    private Authentication authenticate(String username, String password) {
        UserDetails userDetails = customUserDetails.loadUserByUsername(username);
        if(userDetails==null){
            throw new BadCredentialsException("invalid email");
        }
        if(!passwordEncoder.matches(password,userDetails.getPassword())){
            throw new BadCredentialsException("Invalid Password");
        }

        return new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
    }
}