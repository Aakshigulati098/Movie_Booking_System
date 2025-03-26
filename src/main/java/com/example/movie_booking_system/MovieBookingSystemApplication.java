package com.example.movie_booking_system;

import com.example.movie_booking_system.service.EmailSenderService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// update readme with prerequisites with all the dependencies we need
//
@SpringBootApplication
@EnableScheduling
public class MovieBookingSystemApplication {
	@Autowired
	private EmailSenderService senderService;

	public static void main(String[] args) {

		SpringApplication.run(MovieBookingSystemApplication.class, args);
		System.out.println("hello world");
	}

//		this is for email sending
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
//	@EventListener(ApplicationReadyEvent.class)
//	public void triggerMail() throws MessagingException {
//		// Call the sendOtpEmail function
//		senderService.sendOtpEmail("abirsaha453@gmail.com", "Mannu");
//	}

	// this is for welcome user
//	@EventListener(ApplicationReadyEvent.class)
//	public void triggerMail() throws MessagingException {
//		// Call the sendWelcomeEmail function
//		senderService.sendWelcomeEmail("abirsaha453@gmail.com", "Mannu");
//	}


//@Scheduled(fixedRate = 3600000) // Check every hour
//	public void sendReminders() throws MessagingException {
//		// Logic to fetch bookings and check if a reminder needs to be sent
//		// Example:
//		String to = "abirsaha453@gmail.com";
//		String userName = "abir";
//		String movieName = "singham";
//		String showTime = "4:00 PM";
//
//		// Calculate if the reminder should be sent (e.g., 2 hours before showtime)
//		// If yes, send the reminder email
//		senderService.sendReminderEmail(to, userName, movieName, showTime);
//	}

//	@EventListener(ApplicationReadyEvent.class)
//	public void triggerMail() throws MessagingException {
//		// Call the sendOtpEmail function
//		senderService.sendCancellationEmail("aakshi1111.be22@chitkara.edu.in", "aakshi", "movie", "4:00 PM");
//		System.out.println("mail sent");
//	}

}