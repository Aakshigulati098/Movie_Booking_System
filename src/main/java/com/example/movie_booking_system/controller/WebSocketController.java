package com.example.movie_booking_system.controller;




import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/auction/{auctionId}")
    @SendTo("/topic/auction/{auctionId}")
    public String broadcastBid(@DestinationVariable Long auctionId) {
        return "Leaderboard for auction " + auctionId + " has been updated.";
    }

    @MessageMapping("/auction-updates")
    @SendTo("/topic/auction-updates")
    public String updateAuction() { // agar zyada need ho toh just use a DTO here
        return "User has been notified for post auction updates !!!";
    }
}
