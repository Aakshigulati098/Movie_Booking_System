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

        System.out.println("Sending OTP " + otp + " to email: " + email);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(email);
            helper.setSubject("OTP Validation");
//            helper.setText("Hello " + name + ",\n\nYour OTP is: " + otp + "\n\nPlease enter this OTP to complete your verification.\n\nThank you!");
            String htmlContent = buildOtpEmailTemplate(name,otp);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("Email Validation Otp sent Successfully");

        } catch (MessagingException e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String buildOtpEmailTemplate(String name, String otp) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body {" +
                "            font-family: Arial, sans-serif;" +
                "            margin: 0;" +
                "            padding: 0;" +
                "            background-color: #f9f9f9;" +
                "        }" +
                "        .container {" +
                "            max-width: 600px;" +
                "            margin: 20px auto;" +
                "            background: #ffffff;" +
                "            padding: 20px;" +
                "            border-radius: 8px;" +
                "            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);" +
                "        }" +
                "        .header {" +
                "            background-color: #4CAF50;" +
                "            color: #ffffff;" +
                "            padding: 10px;" +
                "            text-align: center;" +
                "            border-radius: 8px 8px 0 0;" +
                "        }" +
                "        .header h1 {" +
                "            margin: 0;" +
                "            font-size: 24px;" +
                "        }" +
                "        .content {" +
                "            text-align: center;" +
                "            padding: 20px;" +
                "        }" +
                "        .otp {" +
                "            font-size: 36px;" +
                "            font-weight: bold;" +
                "            margin: 20px 0;" +
                "            color: #4CAF50;" +
                "        }" +
                "        .footer {" +
                "            margin-top: 20px;" +
                "            font-size: 14px;" +
                "            color: #888;" +
                "            text-align: center;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>Wissen Entertainments</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Dear <strong>" + name + "</strong>,</p>" +
                "            <p>Thank you for registering with <strong>Wissen Entertainments</strong>! To complete your registration, please use the One-Time Password (OTP) below:</p>" +
                "            <p class='otp'>" + otp + "</p>" +
                "            <p>This OTP is valid for <strong>5 minutes</strong>. Please do not share it with anyone.</p>" +
                "            <p>We look forward to providing you with an amazing movie booking experience!</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2025 Wissen Entertainments. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

}


