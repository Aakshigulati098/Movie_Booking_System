package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private AuctionAsyncService auctionAsyncService;

    @Mock
    private AuctionWinnerRepository auctionWinnerRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_ShouldSendNotificationWhenAllDataIsPresent() {
        // Arrange
        BidDTO bid = new BidDTO();
        bid.setAuctionId(1L);
        bid.setUserId(2L);
        bid.setAmount(100L);

        Auction auction = new Auction();
        Users winner = new Users();
        winner.setId(2L);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionWinnerRepository.findByAuctionID(auction)).thenReturn(Optional.empty());
        when(userRepository.findById(2L)).thenReturn(Optional.of(winner));

        // Act
        notificationService.sendNotification(bid);

        // Assert
        verify(auctionAsyncService, times(1)).saveWinnerAndBroadcast(bid, auction, winner, null);
    }

    @Test
    void sendNotification_ShouldNotSendNotificationWhenAuctionIsMissing() {
        // Arrange
        BidDTO bid = new BidDTO();
        bid.setAuctionId(1L);
        bid.setUserId(2L);

        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        notificationService.sendNotification(bid);

        // Assert
        verify(auctionAsyncService, never()).saveWinnerAndBroadcast(any(), any(), any(), any());
    }

    @Test
    void sendNotification_ShouldNotSendNotificationWhenWinnerIsMissing() {
        // Arrange
        BidDTO bid = new BidDTO();
        bid.setAuctionId(1L);
        bid.setUserId(2L);

        Auction auction = new Auction();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act
        notificationService.sendNotification(bid);

        // Assert
        verify(auctionAsyncService, never()).saveWinnerAndBroadcast(any(), any(), any(), any());
    }

    @Test
    void sendNotification_ShouldLogWhenAuctionWinnerExists() {
        // Arrange
        BidDTO bid = new BidDTO();
        bid.setAuctionId(1L);
        bid.setUserId(2L);

        Auction auction = new Auction();
        Users winner = new Users();
        winner.setId(2L);
        AuctionWinner auctionWinner = new AuctionWinner();

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(auctionWinnerRepository.findByAuctionID(auction)).thenReturn(Optional.of(auctionWinner));
        when(userRepository.findById(2L)).thenReturn(Optional.of(winner));

        // Act
        notificationService.sendNotification(bid);

        // Assert
        verify(auctionAsyncService, times(1)).saveWinnerAndBroadcast(bid, auction, winner, auctionWinner);
    }
}