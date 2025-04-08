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
}
