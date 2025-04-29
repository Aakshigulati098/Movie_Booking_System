package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.config.JwtProvider;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.service.CustomUserDetails;
import com.example.movie_booking_system.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetails customUserDetails;

    @Mock
    private UserService userService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateToken_ShouldReturnOkWhenTokenIsValid() {
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtProvider.validateToken("validToken")).thenReturn(true);

        ResponseEntity<Object> response = authController.validateToken(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof HashMap);
    }

    @Test
    void validateToken_ShouldReturnUnauthorizedWhenNoTokenProvided() {
        when(request.getHeader("Authorization")).thenReturn(null);

        ResponseEntity<Object> response = authController.validateToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No token provided", response.getBody());
    }

    @Test
    void validateToken_ShouldReturnUnauthorizedWhenTokenIsInvalid() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidToken");
        when(jwtProvider.validateToken("invalidToken")).thenReturn(false);

        ResponseEntity<Object> response = authController.validateToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }

    @Test
    void validateToken_ShouldReturnInternalServerErrorOnException() {
        when(request.getHeader("Authorization")).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = authController.validateToken(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Token validation error", response.getBody());
    }

    @Test
    void signup_ShouldReturnOkWhenSignupIsSuccessful() {
        Users user = new Users();
        when(userService.signup(user)).thenReturn("Signup successful");

        ResponseEntity<String> response = authController.signup(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Signup successful", response.getBody());
    }

    @Test
    void signup_ShouldReturnBadRequestWhenSignupFails() {
        Users user = new Users();
        when(userService.signup(user)).thenThrow(new RuntimeException("Signup failed"));

        ResponseEntity<String> response = authController.signup(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Signup failed", response.getBody());
    }

    @Test
    void verifyOtp_ShouldReturnResponseFromService() {
        Map<String, String> request = new HashMap<>();
        request.put("otp", "123456");
        ResponseEntity<String> expectedResponse = ResponseEntity.ok("OTP verified");
        when(userService.verifyOtp("123456")).thenReturn(expectedResponse);

        ResponseEntity<String> response = authController.verifyOtp(request);

        assertEquals(expectedResponse, response);
    }

    @Test
    void login_ShouldThrowExceptionWhenEmailIsInvalid() {
        Users user = new Users();
        user.setEmail("invalid@example.com");
        user.setPassword("password");
        when(customUserDetails.loadUserByUsername("invalid@example.com")).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authController.login(user));
    }

    @Test
    void login_ShouldThrowExceptionWhenPasswordIsInvalid() {
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("wrongPassword");
        when(customUserDetails.loadUserByUsername("test@example.com")).thenReturn(mock(org.springframework.security.core.userdetails.User.class));
        when(passwordEncoder.matches("wrongPassword", null)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authController.login(user));
    }

    @Test
    void validateToken_ShouldReturnUnauthorizedWhenAuthHeaderIsMalformed() {
        when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

        ResponseEntity<Object> response = authController.validateToken(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("No token provided", response.getBody());
    }

    @Test
    void login_ShouldThrowExceptionWhenAuthenticationFails() {
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("wrongPassword");

        when(customUserDetails.loadUserByUsername("test@example.com")).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authController.login(user));
    }

    @Test
    void authenticate_ShouldThrowExceptionWhenUserDetailsAreNull() {
        String email = "invalid@example.com";
        String password = "password";

        when(customUserDetails.loadUserByUsername(email)).thenReturn(null);

        assertThrows(BadCredentialsException.class, () -> authController.authenticate(email, password));
    }

    @Test
    void authenticate_ShouldThrowExceptionWhenPasswordDoesNotMatch() {
        String email = "test@example.com";
        String password = "wrongPassword";

        UserDetails userDetails = mock(UserDetails.class);
        when(customUserDetails.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("encodedPassword");
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authController.authenticate(email, password));
    }

    @Test
    void authenticate_ShouldReturnAuthenticationWhenCredentialsAreValid() {
        String email = "test@example.com";
        String password = "password";

        UserDetails userDetails = mock(UserDetails.class);
        when(customUserDetails.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("encodedPassword");
        when(passwordEncoder.matches(password, "encodedPassword")).thenReturn(true);

        Authentication authentication = authController.authenticate(email, password);

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
    }
}