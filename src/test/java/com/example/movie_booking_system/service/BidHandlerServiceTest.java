package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.Bids;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.BidsRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidHandlerServiceTest {

    @Mock
    private BidsRepository bidsRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuctionRepository auctionRepository;

    private BidHandlerService bidHandlerService;

    private BidDTO bidDTO;
    private Users user;
    private Users seller;
    private Auction auction;
    private Bids existingBid;
    private final Long auctionId = 1L;
    private final Long userId = 2L;
    private final Long sellerId = 3L;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);

        bidHandlerService = new BidHandlerService(
                bidsRepository,
                redisService,
                redisTemplate,
                messagingTemplate,
                userRepository,
                auctionRepository
        );

        // Setup common test data
        bidDTO = new BidDTO();
        bidDTO.setAuctionId(auctionId);
        bidDTO.setUserId(userId);
        bidDTO.setAmount(1000L);

        user = new Users();
        user.setId(userId);

        seller = new Users();
        seller.setId(sellerId);

        auction = new Auction();
        auction.setId(auctionId);
        auction.setSeller(seller);

        existingBid = new Bids();
        existingBid.setUserId(user);
        existingBid.setAuctionId(auction);
        existingBid.setBidAmount(800L);
    }

    @Test
    void handleBid_FailsToAcquireLock() {
        // Arrange
        String lockKey = "lock:auction:" + auctionId;
        lenient().when(valueOperations.setIfAbsent(eq(lockKey), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("locked"), eq(10L), eq(TimeUnit.SECONDS));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void handleBid_ValidBidExistingBidder_Success() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        setupValidTopBid(800.0);
        when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.of(existingBid));

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertTrue(result);
        ArgumentCaptor<Bids> bidsCaptor = ArgumentCaptor.forClass(Bids.class);
        verify(bidsRepository).save(bidsCaptor.capture());
        assertEquals(1000L, bidsCaptor.getValue().getBidAmount());
        verify(redisService).addBidToLeaderboard(eq(auctionId), eq(bidDTO));
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/" + auctionId), anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_ValidBidNewBidder_Success() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        setupValidTopBid(800.0);
        lenient().when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.empty());
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertTrue(result);
        ArgumentCaptor<Bids> bidsCaptor = ArgumentCaptor.forClass(Bids.class);
        verify(bidsRepository).save(bidsCaptor.capture());
        assertEquals(1000L, bidsCaptor.getValue().getBidAmount());
        verify(redisService).addBidToLeaderboard(eq(auctionId), eq(bidDTO));
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/" + auctionId), anyString());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_AuctionOwnerBidding_Failure() {
        // Arrange
        setupSuccessfulLock();
        bidDTO.setUserId(sellerId); // Set bidder as auction owner
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_LowerBidAmount_Failure() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        // Setup a higher existing top bid
        BidDTO higherBid = new BidDTO();
        higherBid.setAmount(1200L);
        lenient().when(redisService.getTopBid(auctionId)).thenReturn(higherBid);

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_InactiveAuction_Failure() {
        // Arrange
        setupSuccessfulLock();
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        lenient().when(redisService.getTopBid(auctionId)).thenReturn(null);
        lenient().when(hashOperations.get("auction" + auctionId, "status")).thenReturn("CLOSED");

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_AuctionAlreadyEnded_Failure() {
        // Arrange
        setupSuccessfulLock();
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        lenient().when(redisService.getTopBid(auctionId)).thenReturn(null);
        lenient().when(hashOperations.get("auction" + auctionId, "status")).thenReturn("ACTIVE");
        // Set end time to be in the past
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        lenient().when(hashOperations.get("auction" + auctionId, "endTime")).thenReturn(pastTime.toString());

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_AuctionNotFound_Failure() {
        // Arrange
        setupSuccessfulLock();
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_UserNotFound_Failure() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        setupValidTopBid(800.0);
        lenient().when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.empty());
        lenient().when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_GeneralException_Failure() {
        // Arrange
        setupSuccessfulLock();
        lenient().when(auctionRepository.findById(auctionId)).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertFalse(result);
        verify(bidsRepository, never()).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_NoEndTimeSpecified_Success() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        setupValidTopBid(800.0);
        when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.of(existingBid));
        // Explicitly set no end time
        when(hashOperations.get("auction" + auctionId, "endTime")).thenReturn(null);

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertTrue(result);
        verify(bidsRepository).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_ValidFutureEndTime_Success() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        setupValidTopBid(800.0);
        when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.of(existingBid));
        // Set end time in the future
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        when(hashOperations.get("auction" + auctionId, "endTime")).thenReturn(futureTime.toString());

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertTrue(result);
        verify(bidsRepository).save(any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    void handleBid_NoTopBid_Success() {
        // Arrange
        setupSuccessfulLock();
        setupValidAuction(false);
        // No top bid yet
        when(redisService.getTopBid(auctionId)).thenReturn(null);
        when(bidsRepository.findByUserIdIdAndAuctionIdId(userId, auctionId))
                .thenReturn(Optional.of(existingBid));

        // Act
        boolean result = bidHandlerService.handleBid(auctionId, bidDTO);

        // Assert
        assertTrue(result);
        verify(bidsRepository).save(any());
        verify(redisTemplate).delete(anyString());
    }

    // Helper methods for test setup
    private void setupSuccessfulLock() {
        String lockKey = "lock:auction:" + auctionId;
        when(valueOperations.setIfAbsent(eq(lockKey), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
    }

    private void setupValidAuction(boolean isOwner) {
        lenient().when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));
        lenient().when(hashOperations.get("auction" + auctionId, "status")).thenReturn("ACTIVE");
    }

    private void setupValidTopBid(double amount) {
        BidDTO topBid = new BidDTO();
        topBid.setAmount((long)amount);
        when(redisService.getTopBid(auctionId)).thenReturn(topBid);
    }
}