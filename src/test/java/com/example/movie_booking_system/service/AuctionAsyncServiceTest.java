package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockTransactionSynchronizationManager
class AuctionAsyncServiceTest {

    @Mock
    private AuctionWinnerRepository auctionWinnerRepository;

    @Mock
    private WebSocketService webSocketService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private OtpEmailController otpEmailController;

    @InjectMocks
    private AuctionAsyncService auctionAsyncService;

    // Test data
    private BidDTO bidDTO;
    private Auction auction;
    private Users winner;
    private AuctionWinner existingAuctionWinner;

    @BeforeEach
    void setUp() {
        // Setup common test data
        bidDTO = new BidDTO();
        bidDTO.setAuctionId(1L);
        bidDTO.setUserId(2L);
        bidDTO.setAmount(1000L);

        auction = new Auction();
        auction.setId(1L);

        winner = new Users();
        winner.setId(2L);
        winner.setEmail("winner@example.com");

        existingAuctionWinner = new AuctionWinner();
        existingAuctionWinner.setId(1L);
        existingAuctionWinner.setAuctionID(auction);
        existingAuctionWinner.setWinnerId(winner);
        existingAuctionWinner.setAmount(500L);
    }

    @Test
    void saveWinnerAndBroadcast_WithExistingWinner_ShouldUpdateWinner() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2L)).thenReturn(Optional.of(winner));
        when(auctionWinnerRepository.findByAuctionID(auction)).thenReturn(Optional.of(existingAuctionWinner));

        // Act
        auctionAsyncService.saveWinnerAndBroadcast(bidDTO, auction, winner, existingAuctionWinner);

        // Assert
        verify(auctionRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(auctionWinnerRepository).findByAuctionID(auction);

        ArgumentCaptor<AuctionWinner> winnerCaptor = ArgumentCaptor.forClass(AuctionWinner.class);
        verify(auctionWinnerRepository).save(winnerCaptor.capture());

        AuctionWinner capturedWinner = winnerCaptor.getValue();
        assertEquals(winner, capturedWinner.getWinnerId());
        assertEquals(bidDTO.getAmount(), capturedWinner.getAmount());
        assertEquals(auction, capturedWinner.getAuctionID());

        // Verify after-commit operations
        MockTransactionSynchronizationManager.Extension.runAfterCommitCallbacks();
        verify(otpEmailController).sendAuctionWinningMail(winner, auction);
        verify(webSocketService).sendWinnerNotification(bidDTO);
    }

    @Test
    void saveWinnerAndBroadcast_WithNoExistingWinner_ShouldCreateNewWinner() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2L)).thenReturn(Optional.of(winner));
        when(auctionWinnerRepository.findByAuctionID(auction)).thenReturn(Optional.empty());

        // Act
        auctionAsyncService.saveWinnerAndBroadcast(bidDTO, auction, winner, null);

        // Assert
        verify(auctionRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(auctionWinnerRepository).findByAuctionID(auction);

        ArgumentCaptor<AuctionWinner> winnerCaptor = ArgumentCaptor.forClass(AuctionWinner.class);
        verify(auctionWinnerRepository).save(winnerCaptor.capture());

        AuctionWinner capturedWinner = winnerCaptor.getValue();
        assertEquals(winner, capturedWinner.getWinnerId());
        assertEquals(bidDTO.getAmount(), capturedWinner.getAmount());
        assertEquals(auction, capturedWinner.getAuctionID());

        // Verify after-commit operations
        MockTransactionSynchronizationManager.Extension.runAfterCommitCallbacks();
        verify(otpEmailController).sendAuctionWinningMail(winner, auction);
        verify(webSocketService).sendWinnerNotification(bidDTO);
    }

    @Test
    void saveWinnerAndBroadcast_WhenAuctionNotFound_ShouldThrowException() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionAsyncService.saveWinnerAndBroadcast(bidDTO, auction, winner, existingAuctionWinner)
        );

        assertEquals("Auction not found: 1", exception.getMessage());
        verify(auctionRepository).findById(1L);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void saveWinnerAndBroadcast_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionAsyncService.saveWinnerAndBroadcast(bidDTO, auction, winner, existingAuctionWinner)
        );

        assertEquals("User not found: 2", exception.getMessage());
        verify(auctionRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(auctionWinnerRepository, never()).findByAuctionID(any());
    }

    @Test
    void saveWinnerAndBroadcast_WithException_ShouldPropagateException() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(userRepository.findById(2L)).thenReturn(Optional.of(winner));
        when(auctionWinnerRepository.findByAuctionID(auction)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                auctionAsyncService.saveWinnerAndBroadcast(bidDTO, auction, winner, existingAuctionWinner)
        );

        assertEquals("Database error", exception.getMessage());
        verify(auctionRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(auctionWinnerRepository).findByAuctionID(auction);
    }
}