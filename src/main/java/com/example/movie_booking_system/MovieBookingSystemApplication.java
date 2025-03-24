package com.example.movie_booking_system;

import com.example.movie_booking_system.service.EmailSenderService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

// update readme with prerequisites with all the dependencies we need
//
@SpringBootApplication
public class MovieBookingSystemApplication {
    @Autowired
	private EmailSenderService senderService;

	public static void main(String[] args) {

		SpringApplication.run(MovieBookingSystemApplication.class, args);
		System.out.println("hello world");
	}

////	this is for email sending
//		@EventListener(ApplicationReadyEvent.class)
//		public void triggerMail() throws MessagingException {
//
//
//			senderService.sendBookingConfirmationEmail(
//					"manansharma1209@gmail.com",   // Recipient's email
//					"Manan",           // UserName
//					"PVR Cinemas",               // TheaterName
//					"Azhar",         // MovieName
//					"2025-03-21",                // Date
//					"7:30 PM",                   // ShowTime
//					"A12, A13"                   // SeatNumber
//			);
//		}

// this is for otp sending
	@EventListener(ApplicationReadyEvent.class)
	public void triggerMail() throws MessagingException {
		// Call the sendOtpEmail function
		senderService.sendOtpEmail("abirsaha453@gmail.com", "Mannu");
	}
	}
