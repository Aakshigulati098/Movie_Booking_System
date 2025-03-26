package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.config.JwtProvider;
import com.example.movie_booking_system.model.Users;
import com.example.movie_booking_system.repository.UserRepository;
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

import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private HttpSession session;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetails customUserDetails;

    @Autowired
    private UserService userService;


//    @PostMapping("/signup")
//    public ResponseEntity<Users> signup(@Valid @RequestBody Users user) throws Exception {
//
//        Users isUserExist = userRepository.findByEmail(user.getEmail());
//
//        if(isUserExist!=null){
//            throw new Exception("Email already exist with another account");
//        }
//
//        Users newUser = new Users();
//        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
//        newUser.setName(user.getName());
//        newUser.setEmail(user.getEmail());
//        newUser.setPhone(user.getPhone());
//        newUser.setAddress(user.getAddress());
//
//        Users savedUser = userRepository.save(newUser);
//
//        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(),user.getPassword());
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String jwt = JwtProvider.generateToken(authentication);
//
//        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
//    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody Users user, HttpSession session){
        try{
            String message = userService.signup(user, session);
            return ResponseEntity.ok(message);
        }
        catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("verify-otp")
    public ResponseEntity<String> verifyOtp(@Valid @RequestBody Map<String,String> request, @Autowired HttpSession session){
        System.out.println(session.getAttributeNames());
        String inputOtp = request.get("otp");
        return userService.verifyOtp(inputOtp, session);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Users user){
        Authentication authentication = authenticate(user.getEmail(),user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = JwtProvider.generateToken(authentication);
        return ResponseEntity.ok(jwt);
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate(); // Invalidate the session
    }
    return ResponseEntity.ok("Logout successful!");
}


}
