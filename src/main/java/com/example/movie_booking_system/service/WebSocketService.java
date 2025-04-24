package com.example.movie_booking_system.service;
import com.example.movie_booking_system.dto.BidDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {


    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendWinnerNotification(BidDTO bid) {
        messagingTemplate.convertAndSend("/topic/auction-updates", bid);
    }

    public void sendBookingTransferNotification(Long bookingId) {
        messagingTemplate.convertAndSend("/topic/booking-transfer", bookingId);
    }
    public void sendAuctionAcceptanceUpdates(String kuchbhi){
        messagingTemplate.convertAndSend("/topic/auction-Accept-updates", kuchbhi);
    }
}
