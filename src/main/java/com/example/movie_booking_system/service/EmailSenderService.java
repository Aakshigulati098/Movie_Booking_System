package com.example.movie_booking_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.security.SecureRandom;
import java.util.logging.Logger;


@Service
public class EmailSenderService {

    private JavaMailSender mailSender;

    @Autowired
    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private static final Logger logger = Logger.getLogger(EmailSenderService.class.getName());


    public void sendSimpleEmail(String toMail,
                                String subject,
                                String body){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("ashirvadpandey123@gmail.com");
        message.setTo(toMail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);

       logger.info("Mail Sent Successfully... ..");
    }


    //function to generate otp
    private String generateOTP(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // Append a random digit (0-9)
        }
        return otp.toString();
    }

    //function to send otp
    public void sendOtpEmail(String toMail, String userName) {
        String otp = generateOTP(6); // Generate a 6-digit OTP
        String subject = "Your OTP Code";
        String body = "Dear " + userName + ",\n\nYour OTP code is: " + otp + "\n\nPlease use this code to complete your verification.\n\nThank you.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com");
        message.setTo(toMail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        logger.info("OTP Sent Successfully to " + toMail + " with OTP: " + otp);
    }


    public void sendBookingConfirmationEmail(String toMail,
                                             String userName,
                                             String theaterName,
                                             String movieName,
                                             String date,
                                             String showTime,
                                             String seatNumber)  {
        MimeMessage mimeMessage = mailSender.createMimeMessage();


        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(toMail);
            helper.setSubject("Movie Booking Confirmation");


            String apiUrl = "http://www.omdbapi.com/?t=" + URLEncoder.encode(movieName, "UTF-8") + "&apikey=745b025c";

            // Create an HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create an HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .GET()
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Extract the JSON response as a String
            String jsonResponse = response.body();
            logger.info("JSON Response: " + jsonResponse);


            // Extract the Poster URL using substring
            int startIndex = jsonResponse.indexOf("\"Poster\":") + 10; // Find the start of the 'Poster' field
            int endIndex = jsonResponse.indexOf("\",", startIndex);   // Find the end of the 'Poster' value

            String posterUrl = jsonResponse.substring(startIndex, endIndex);
            // Build the HTML content
            String htmlContent = buildEmailTemplate(userName, theaterName, movieName, date, showTime, seatNumber, posterUrl);
            helper.setText(htmlContent, true); // Set "true" for HTML content



            mailSender.send(mimeMessage);

            logger.info("Booking Confirmation Email with Image Sent Successfully... ..");

        } catch (MessagingException  e) {
            logger.info("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             throw new IllegalStateException("An error occurred while sending the booking confirmation email", e);
        }
    }



    private String buildEmailTemplate(String userName, String theaterName, String movieName, String date, String showTime, String seatNumber, String posterUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <title>Movie Booking Confirmation</title>" +
                "    <style>" +
                "        body {" +
                "            font-family: Arial, sans-serif;" +
                "            margin: 0;" +
                "            padding: 0;" +
                "            background-color: #f4f4f9;" +
                "        }" +
                "        .container {" +
                "            max-width: 600px;" +
                "            margin: 20px auto;" +
                "            background: #ffffff;" +
                "            border-radius: 8px;" +
                "            overflow: hidden;" +
                "            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);" +
                "        }" +
                "        .header {" +
                "            background-color: #4CAF50;" +
                "            color: #ffffff;" +
                "            text-align: center;" +
                "            padding: 15px;" +
                "        }" +
                "        .header h1 {" +
                "            margin: 0;" +
                "            font-size: 24px;" +
                "        }" +
                "        .content {" +
                "            padding: 20px;" +
                "            text-align: center;" +
                "        }" +
                "        .poster {" +
                "            width: 100%;" +
                "            max-width: 300px;" +
                "            margin: 20px auto;" +
                "            border-radius: 5px;" +
                "            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.1);" +
                "        }" +
                "        .details {" +
                "            margin: 20px 0;" +
                "            text-align: left;" +
                "        }" +
                "        .details table {" +
                "            width: 100%;" +
                "            border-collapse: collapse;" +
                "        }" +
                "        .details table tr {" +
                "            border-bottom: 1px solid #eeeeee;" +
                "        }" +
                "        .details table td {" +
                "            padding: 8px 10px;" +
                "        }" +
                "        .details table td:first-child {" +
                "            font-weight: bold;" +
                "        }" +
                "        .footer {" +
                "            background-color: #f1f1f1;" +
                "            color: #888;" +
                "            text-align: center;" +
                "            padding: 10px;" +
                "            font-size: 14px;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <h1>🎥 Movie Booking Confirmation</h1>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <p>Dear <strong>" + userName + "</strong>,</p>" +
                "            <p>Thank you for booking with <strong>" + theaterName + "</strong>! Below are your booking details:</p>" +
                "            <img src=\"" + posterUrl + "\" alt=\"Movie Poster\" class=\"poster\">" +
                "            <div class=\"details\">" +
                "                <table>" +
                "                    <tr><td>Movie:</td><td>" + movieName + "</td></tr>" +
                "                    <tr><td>Date:</td><td>" + date + "</td></tr>" +
                "                    <tr><td>Show Time:</td><td>" + showTime + "</td></tr>" +
                "                    <tr><td>Seat(s):</td><td>" + seatNumber + "</td></tr>" +
                "                </table>" +
                "            </div>" +
                "            <p>We hope you enjoy your movie!</p>" +
                "        </div>" +
                "    <p style='color: #888;'>- Hustlers | Wissen Entertainments</p>" +
                "        <div class=\"footer\">" +
                "            <p>&copy; 2025 " + theaterName + ". All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }






}
