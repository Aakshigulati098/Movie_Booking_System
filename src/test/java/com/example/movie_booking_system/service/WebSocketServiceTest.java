package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class WebSocketServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketService webSocketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendWinnerNotification_ShouldSendBidToCorrectTopic() {
        // Arrange
        BidDTO bid = new BidDTO();
        bid.setUserId(1L);
        bid.setAmount(100L);

        // Act
        webSocketService.sendWinnerNotification(bid);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend("/topic/auction-updates", bid);
    }

    @Test
    void sendBookingTransferNotification_ShouldSendBookingIdToCorrectTopic() {
        // Arrange
        Long bookingId = 123L;

        // Act
        webSocketService.sendBookingTransferNotification(bookingId);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend("/topic/booking-transfer", bookingId);
    }

    @Test
    void sendAuctionAcceptanceUpdates_ShouldSendMessageToCorrectTopic() {
        // Arrange
        String message = "Auction Accepted";

        // Act
        webSocketService.sendAuctionAcceptanceUpdates(message);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend("/topic/auction-Accept-updates", message);
    }
}