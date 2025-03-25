package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.config.JwtProvider;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import com.example.movie_booking_system.service.CustomUserDetails;
import com.example.movie_booking_system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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

        String jwt = JwtProvider.generateToken(authentication);

        return jwt;

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
