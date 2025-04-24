package com.example.movie_booking_system.emailotp;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.Users;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;

import java.time.LocalDateTime;


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

    public void sendAuctionWinningMail(Users user, Auction auction) {
        System.out.println("Sending Auction Winner mail with name " + user.getName() + " to email: " + user.getEmail());
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("Auction Winning Acceptance Link");
            String emailContent = buildPremiumAuctionWinnerTemplate(
                    user.getName(),
                    auction.getBookingId().getMovie().getTitle(),
                    auction.getBookingId().getShowtime().getTheatre().getName(),
                    auction.getBookingId().getShowtime().getTime(),
                    auction.getFinalAmount().doubleValue()
            );
            helper.setText(emailContent, true);

            mailSender.send(mimeMessage);
            System.out.println("AuctionWinner Acceptance link Email sent Successfully");

        } catch (MessagingException e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendWelcomeEmail(String name, String email){
        System.out.println("Sending Welcome email to email: " + email);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(email);
            helper.setSubject("Welcome Mail");
            String htmlContent = buildWelcomeEmailTemplate(name);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("Welcome Email sent Successfully");

        } catch (MessagingException e) {
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendReminderEmail(String email, String movieName, LocalDateTime showTime, String name) {
        try{
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setFrom("ashirvadpandey123@gmail.com");
            helper.setTo(email);
            helper.setSubject("Movie show reminder");
            String htmlContent = buildMovieReminderTemplate(name,movieName,showTime.toString());
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("Reminder Message sent Successfully");

        }
        catch(MessagingException e){
            System.err.println("Error while sending email: " + e.getMessage());
            e.printStackTrace();
        }
        catch (Exception e){
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
                "            background-color: #1a2d58;" +
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
                "            color: #1a2d58;" +
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

    private String buildWelcomeEmailTemplate(String name) {
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
                "            background-color: #1a2d58;" +
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
                "            color: #1a2d58;" +
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
                "            <h1>Welcome to Wissen Entertainments</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Dear <strong>" + name + "</strong>,</p>" +
                "            <p>We are thrilled to have you with us. Thank you to joining our community. We are commited to providing with the best experience possible.</p>" +
                "            <p>If you have any queries or need assistance, feel free to reach out to our support team.</p>" +
                "            <p>Best Regards, </p>" +
                "             <p>The Admin Team </p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2025 Wissen Entertainments. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildMovieReminderTemplate(String name, String movie, String showtime) {
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
                "            background-color: #1a2d58;" +
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
                "        .movie-details {" +
                "            font-size: 18px;" +
                "            font-weight: bold;" +
                "            margin: 20px 0;" +
                "            color: #1a2d58;" +
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
                "            <h1>Movie Reminder</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <p>Dear <strong>" + name + "</strong>,</p>" +
                "            <p>Just a reminder that you have an upcoming movie show.</p>" +
                "            <p class='movie-details'>" + movie + " at " + showtime + "</p>" +
                "            <p>We hope you have a great time!</p>" +
                "            <p>If you have any questions, feel free to contact us.</p>" +
                "            <p>Best Regards,</p>" +
                "            <p>The Wissen Entertainments Team</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2025 Wissen Entertainments. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String buildPremiumAuctionWinnerTemplate(String name, String movieTitle, String theatre, String showTime, double amount) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <style>" +
                "        body {" +
                "            font-family: 'Helvetica Neue', Arial, sans-serif;" +
                "            margin: 0;" +
                "            padding: 0;" +
                "            background-color: #f8f9fa;" +
                "        }" +
                "        .container {" +
                "            max-width: 600px;" +
                "            margin: 20px auto;" +
                "            background: #ffffff;" +
                "            padding: 0;" +
                "            border-radius: 12px;" +
                "            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);" +
                "        }" +
                "        .header {" +
                "            background: linear-gradient(135deg, #0a1128 0%, #1a2d58 100%);" +
                "            color: #ffffff;" +
                "            padding: 40px 20px;" +
                "            text-align: center;" +
                "            border-radius: 12px 12px 0 0;" +
                "            position: relative;" +
                "            overflow: hidden;" +
                "        }" +
                "        .header::after {" +
                "            content: '';" +
                "            position: absolute;" +
                "            top: 0;" +
                "            left: 0;" +
                "            right: 0;" +
                "            bottom: 0;" +
                "            background: url('data:image/svg+xml,<svg width=\"20\" height=\"20\" viewBox=\"0 0 20 20\" xmlns=\"http://www.w3.org/2000/svg\"><circle cx=\"2\" cy=\"2\" r=\"2\" fill=\"rgba(255,255,255,0.1)\"/></svg>') repeat;" +
                "        }" +
                "        .header h1 {" +
                "            margin: 0;" +
                "            font-size: 32px;" +
                "            font-weight: 300;" +
                "            letter-spacing: 3px;" +
                "            text-transform: uppercase;" +
                "            position: relative;" +
                "            z-index: 1;" +
                "        }" +
                "        .content {" +
                "            padding: 40px 30px;" +
                "            text-align: center;" +
                "        }" +
                "        .movie-details {" +
                "            background: #f8f9fa;" +
                "            margin: 25px 0;" +
                "            padding: 25px;" +
                "            border-radius: 8px;" +
                "            border: 1px solid #e9ecef;" +
                "        }" +
                "        .movie-title {" +
                "            font-size: 24px;" +
                "            color: #1a2d58;" +
                "            margin-bottom: 15px;" +
                "        }" +
                "        .details-row {" +
                "            margin: 10px 0;" +
                "            color: #495057;" +
                "        }" +
                "        .amount {" +
                "            font-size: 28px;" +
                "            color: #1a2d58;" +
                "            font-weight: bold;" +
                "            margin: 20px 0;" +
                "        }" +
                "        .timer-box {" +
                "            background: #fff3cd;" +
                "            color: #856404;" +
                "            padding: 15px;" +
                "            border-radius: 8px;" +
                "            margin: 20px 0;" +
                "            font-weight: 500;" +
                "        }" +
                "        .action-button {" +
                "            display: inline-block;" +
                "            background: linear-gradient(135deg, #1a2d58 0%, #2a4374 100%);" +
                "            color: white;" +
                "            padding: 18px 40px;" +
                "            text-decoration: none;" +
                "            border-radius: 50px;" +
                "            font-weight: 500;" +
                "            font-size: 16px;" +
                "            letter-spacing: 1px;" +
                "            margin: 30px 0;" +
                "            transition: all 0.3s ease;" +
                "            box-shadow: 0 4px 15px rgba(26, 45, 88, 0.2);" +
                "        }" +
                "        .action-button:hover {" +
                "            transform: translateY(-2px);" +
                "            box-shadow: 0 8px 25px rgba(26, 45, 88, 0.3);" +
                "        }" +
                "        .footer {" +
                "            border-top: 1px solid #e9ecef;" +
                "            margin-top: 40px;" +
                "            padding: 20px;" +
                "            font-size: 14px;" +
                "            color: #6c757d;" +
                "            text-align: center;" +
                "        }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <div class='header'>" +
                "            <h1>🎉 Congratulations " + name + "! 🎉</h1>" +
                "        </div>" +
                "        <div class='content'>" +
                "            <h2 style='color: #1a2d58;'>You've Won the Auction!</h2>" +
                "            <div class='movie-details'>" +
                "                <div class='movie-title'>" + movieTitle + "</div>" +
                "                <div class='details-row'>🏛️ " + theatre + "</div>" +
                "                <div class='details-row'>🕒 " + showTime + "</div>" +
                "                <div class='amount'>₹" + String.format("%.2f", amount) + "</div>" +
                "            </div>" +
                "            <div class='timer-box'>" +
                "                ⏳ You have 15 minutes to claim your tickets" +
                "            </div>" +
                "            <a href='http://localhost:5173/main/pending-auctions' class='action-button'>" +
                "                CLAIM YOUR TICKETS NOW" +
                "            </a>" +
                "            <p style='color: #6c757d; font-size: 14px;'>" +
                "                Click the button above to complete your purchase and secure your tickets." +
                "            </p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>© 2025 Wissen Entertainments</p>" +
                "            <p>Premium Entertainment Experience</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

}