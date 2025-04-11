package com.example.movie_booking_system.service;
import com.example.movie_booking_system.dto.BidDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendWinnerNotification(BidDTO bid) {
        messagingTemplate.convertAndSend("/topic/auction-updates", bid);
    }
}
