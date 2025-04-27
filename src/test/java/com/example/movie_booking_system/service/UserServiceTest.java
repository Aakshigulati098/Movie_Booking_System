package com.example.movie_booking_system.service;

import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.exceptions.UserRegistrationException;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private HttpSession session;

    @Mock
    private OtpEmailController otpEmailController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_ShouldSendOtpSuccessfully() {
        // Arrange
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setPhone("1234567890");
        user.setAddress("Test Address");

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String result = userService.signup(user);

        // Assert
        assertEquals("Otp Sent Successfully", result);
        verify(userRepository, never()).save(any(Users.class));
        verify(otpEmailController, times(1)).sendOtpEmail(eq("Test User"), eq("test@example.com"), anyString());
        verify(session, times(1)).setAttribute(eq("otp"), anyString());
        verify(session, times(1)).setAttribute(eq("otpExpiry"), anyLong());
    }

    @Test
    void signup_ShouldThrowExceptionWhenEmailAlreadyExists() {
        // Arrange
        Users existingUser = new Users();
        existingUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(existingUser);

        Users newUser = new Users();
        newUser.setEmail("test@example.com");

        // Act & Assert
        UserRegistrationException exception = assertThrows(UserRegistrationException.class, () -> userService.signup(newUser));
        assertEquals("Email already exists with another account", exception.getMessage());
    }

    @Test
    void verifyOtp_ShouldReturnSuccessWhenOtpIsValid() {
        // Arrange
        String inputOtp = "1234";
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() + 60000);

        // Act
        ResponseEntity<String> response = userService.verifyOtp(inputOtp);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OTP verified successfully!", response.getBody());
        verify(session, times(1)).setAttribute("otpVerified", true);
        verify(session, times(1)).removeAttribute("otp");
        verify(session, times(1)).removeAttribute("otpExpiry");
    }

    @Test
    void verifyOtp_ShouldReturnUnauthorizedWhenOtpIsExpired() {
        // Arrange
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() - 60000);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("1234");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("OTP expired. Please request a new one.", response.getBody());
        verify(session, times(1)).removeAttribute("otp");
        verify(session, times(1)).removeAttribute("otpExpiry");
    }

    @Test
    void verifyOtp_ShouldReturnUnauthorizedWhenOtpIsInvalid() {
        // Arrange
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() + 60000);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("5678");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid OTP.", response.getBody());
        verify(session, never()).setAttribute("otpVerified", true);
    }

    @Test
    void verifyOtp_ShouldReturnUnauthorizedWhenOtpIsNull() {
        // Arrange
        when(session.getAttribute("otp")).thenReturn(null);
        when(session.getAttribute("otpExpiry")).thenReturn(null);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("1234");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("OTP expired. Please request a new one.", response.getBody());
        verify(session, times(1)).removeAttribute("otp");
        verify(session, times(1)).removeAttribute("otpExpiry");
    }
}