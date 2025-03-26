package com.example.movie_booking_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.security.SecureRandom;


@Service
public class EmailSenderService {
    @Autowired
    private JavaMailSender mailSender;

    //TODO:Re-Use For OTP:
    public void sendSimpleEmail(String toMail,
                                String subject,
                                String body){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("ashirvadpandey123@gmail.com");
        message.setTo(toMail);
        message.setText(body);
        message.setSubject(subject);

        mailSender.send(message);

        System.out.println("Mail Sent Successfully... ..");
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

        System.out.println("OTP Sent Successfully to " + toMail + " with OTP: " + otp);
    }

    // cancellation mail
    public void sendCancellationEmail(String to, String userName, String movieName, String showTime) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Booking Cancellation - " + movieName);
        helper.setText("Dear " + userName + ",\n\nWe regret to inform you that your booking for the movie \"" + movieName + "\" scheduled at " + showTime + " has been canceled.\n\nWe apologize for any inconvenience caused.\n\nBest regards,\nMovie Booking Team");

        mailSender.send(message);
    }
    //reminder email
    public void sendReminderEmail(String to, String userName, String movieName, String showTime) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Reminder: Your Movie Booking");
        helper.setText("Dear " + userName + ",\n\nThis is a reminder that your movie \"" + movieName + "\" is scheduled to start at " + showTime + ".\n\nEnjoy your movie!\n\nBest regards,\nMovie Booking Team");

        mailSender.send(message);
    }

    // welcome email
    public void sendWelcomeEmail(String to, String userName) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Welcome to Movie Booking System");
        helper.setText("Dear " + userName + ",\n\nWelcome to our Movie Booking System! We are excited to have you with us.\n\nBest regards,\nMovie Booking Team");

        mailSender.send(message);
    }
    //TODO: Booking COnfirmation Sophisticated:
    public void sendBookingConfirmationEmail(String toMail,
                                             String userName,
                                             String theaterName,
                                             String movieName,
                                             String date,
                                             String showTime,
                                             String seatNumber) {
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
            System.out.println("JSON Response: " + jsonResponse);


            // Extract the Poster URL using substring
            int startIndex = jsonResponse.indexOf("\"Poster\":") + 10; // Find the start of the 'Poster' field
            int endIndex = jsonResponse.indexOf("\",", startIndex);   // Find the end of the 'Poster' value

            String posterUrl = jsonResponse.substring(startIndex, endIndex);
            // Build the HTML content
            String htmlContent = buildEmailTemplate(userName, theaterName, movieName,date, showTime, seatNumber, posterUrl);
            helper.setText(htmlContent, true); // Set "true" for HTML content

            // TODO:Confirm the poster URL --> Save The Poster Temporarily to buffer --> Send Poster As Attachment:
//    if (posterUrl != null && !posterUrl.isEmpty()) {
//        System.out.println("Poster URL retrieved successfully: " + posterUrl);
//    } else {
//        System.out.println("Poster URL not found or is empty!SHUTTING_DOWN");
//    }
//        // Attach the image
//        try {
//            // Download and save the image locally
//            URL posterURL = new URL(posterUrl); // From the previous step
//            File tempFile = new File("poster.jpg"); // Save as "poster.jpg" in the current directory
//            try (InputStream inputStream = posterURL.openStream();
//                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
//
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer);
//                }
//            }
//
//            System.out.println("Poster saved successfully: " + tempFile.getAbsolutePath());
//
//            // Attach the file to the email
//            helper.addAttachment("movie-poster.jpg", tempFile);
//
//        } catch (Exception e) {
//            System.err.println("Error while saving or attaching poster: " + e.getMessage());
//            e.printStackTrace();
//        }
            //   TODO:END||Confirm the poster URL --> Save The Poster Temporarily to buffer --> Send Poster As Attachment:

            mailSender.send(mimeMessage);

            System.out.println("Booking Confirmation Email with Image Sent Successfully... ..");

        } catch (MessagingException  e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    //TODO:Final Confirmation Mail html template:
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
