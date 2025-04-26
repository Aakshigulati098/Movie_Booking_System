package com.example.movie_booking_system.service;

import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private OtpEmailController otpEmailController;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSignup_UserAlreadyExists() {
        // Arrange
        Users existingUser = new Users();
        existingUser.setEmail("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(existingUser);

        Users newUser = new Users();
        newUser.setEmail("test@example.com");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.signup(newUser));
        assertEquals("Email already exist with another account", exception.getMessage());
    }

    @Test
    void testSignup_Successful() {
        // Arrange
        Users newUser = new Users();
        newUser.setEmail("test@example.com");
        newUser.setPassword("password");
        newUser.setName("Test User");

        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        // Act
        String result = userService.signup(newUser);

        // Assert
        assertEquals("Otp Sent Successfully", result);
        verify(session, times(1)).setAttribute(eq("currentUser"), any(Users.class));
        verify(otpEmailController, times(1)).sendOtpEmail(eq("Test User"), eq("test@example.com"), anyString());
    }

    @Test
    void testVerifyOtp_ExpiredOtp() {
        // Arrange
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() - 1000);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("1234");

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("OTP expired. Please request a new one.", response.getBody());
        verify(session, times(1)).removeAttribute("otp");
        verify(session, times(1)).removeAttribute("otpExpiry");
    }

    @Test
    void testVerifyOtp_InvalidOtp() {
        // Arrange
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() + 10000);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("5678");

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid OTP.", response.getBody());
    }

    @Test
    void testVerifyOtp_Successful() {
        // Arrange
        Users newUser = new Users();
        when(session.getAttribute("otp")).thenReturn("1234");
        when(session.getAttribute("otpExpiry")).thenReturn(System.currentTimeMillis() + 10000);
        when(session.getAttribute("currentUser")).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);

        // Act
        ResponseEntity<String> response = userService.verifyOtp("1234");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("OTP verified successfully!", response.getBody());
        verify(session, times(1)).removeAttribute("otp");
        verify(session, times(1)).removeAttribute("otpExpiry");
        verify(userRepository, times(1)).save(newUser);
    }
}