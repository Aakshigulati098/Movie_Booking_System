package com.example.movie_booking_system.emailotp;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;


@Service
public class OtpEmailController {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String name, String email, String otp) {

        // Simulate sending OTP email (replace with real email-sending logic)
        System.out.println("Sending OTP " + otp + " to email: " + email);


        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(email);
            helper.setSubject("OTP Validation");
            helper.setText("Hello " + name + ",\n\nYour OTP is: " + otp + "\n\nPlease enter this OTP to complete your verification.\n\nThank you!");

            mailSender.send(mimeMessage);
            System.out.println("Email Validation Otp sent Successfully");

        } catch (MessagingException e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


