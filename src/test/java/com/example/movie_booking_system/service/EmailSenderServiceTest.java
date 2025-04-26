package com.example.movie_booking_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendSimpleEmail_ShouldSendEmailSuccessfully() {
        // Arrange
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ashirvadpandey123@gmail.com");
        message.setTo("test@example.com");
        message.setSubject("Test Subject");
        message.setText("Test Body");

        // Act
        emailSenderService.sendSimpleEmail("test@example.com", "Test Subject", "Test Body");

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendOtpEmail_ShouldSendOtpSuccessfully() {
        // Arrange
        String toMail = "test@example.com";
        String userName = "Test User";

        // Act
        emailSenderService.sendOtpEmail(toMail, userName);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendBookingConfirmationEmail_ShouldSendEmailSuccessfully() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        String toMail = "test@example.com";
        String userName = "Test User";
        String theaterName = "Test Theater";
        String movieName = "Test Movie";
        String date = "2023-10-01";
        String showTime = "7:00 PM";
        String seatNumber = "A1";

        // Act
        emailSenderService.sendBookingConfirmationEmail(toMail, userName, theaterName, movieName, date, showTime, seatNumber);

        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendBookingConfirmationEmail_ShouldHandleRuntimeException() {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Error sending email")).when(mailSender).send(mimeMessage);

        String toMail = "test@example.com";
        String userName = "Test User";
        String theaterName = "Test Theater";
        String movieName = "Test Movie";
        String date = "2023-10-01";
        String showTime = "7:00 PM";
        String seatNumber = "A1";

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailSenderService.sendBookingConfirmationEmail(toMail, userName, theaterName, movieName, date, showTime, seatNumber)
        );
    }

    @Test
    void sendBookingConfirmationEmail_ShouldHandleMessagingException() throws Exception {
        // Arrange
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Mock MimeMessageHelper to throw MessagingException
        MimeMessageHelper helper = mock(MimeMessageHelper.class);
        doThrow(new MessagingException("Error while sending email")).when(helper).setText(anyString(), eq(true));

        String toMail = "test@example.com";
        String userName = "Test User";
        String theaterName = "Test Theater";
        String movieName = "Test Movie";
        String date = "2023-10-01";
        String showTime = "7:00 PM";
        String seatNumber = "A1";

        // Act & Assert
        assertDoesNotThrow(() -> emailSenderService.sendBookingConfirmationEmail(toMail, userName, theaterName, movieName, date, showTime, seatNumber));
    }
}