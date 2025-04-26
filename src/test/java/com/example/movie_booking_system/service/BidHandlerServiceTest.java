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
import org.mockito.InjectMocks;
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

    @InjectMocks
    private BidHandlerService bidHandlerService;

    private BidDTO validBid;
    private Users bidder;
    private Users seller;
    private Auction auction;
    private LocalDateTime futureEndTime;
    private LocalDateTime pastEndTime;

    @BeforeEach
    void setUp() {
        // Setup common test data
        bidder = new Users();
        bidder.setId(1L);
        bidder.setName("Test Bidder");

        seller = new Users();
        seller.setId(2L);
        seller.setName("Test Seller");

        auction = new Auction();
        auction.setId(1L);
        auction.setSeller(seller);

        futureEndTime = LocalDateTime.now().plusHours(1);
        pastEndTime = LocalDateTime.now().minusHours(1);

        validBid = new BidDTO();
        validBid.setUserId(bidder.getId());
        validBid.setAuctionId(auction.getId());
        validBid.setAmount(100L);

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void testHandleBid_SuccessfulNewBid() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(futureEndTime.toString());
        when(redisService.getTopBid(1L)).thenReturn(null); // No previous bids
        when(bidsRepository.findByUserIdIdAndAuctionIdId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(bidder));

        Bids newBid = new Bids();
        when(bidsRepository.save(any(Bids.class))).thenReturn(newBid);

        // Act
        boolean result = bidHandlerService.handleBid(1L, validBid);

        // Assert
        assertTrue(result);
        verify(redisService).addBidToLeaderboard(1L, validBid);
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/1"), anyString());
        verify(bidsRepository).save(any(Bids.class));
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_SuccessfulExistingBidUpdate() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(futureEndTime.toString());

        // Previous lower bid exists
        BidDTO previousBid = new BidDTO();
        previousBid.setUserId(1L);
        previousBid.setAuctionId(1L);
        previousBid.setAmount(50L);
        when(redisService.getTopBid(1L)).thenReturn(previousBid);

        // User already has a bid
        Bids existingBid = new Bids();
        existingBid.setBidAmount(50L);
        existingBid.setUserId(bidder);
        existingBid.setAuctionId(auction);
        existingBid.setCreatedAt(LocalDateTime.now().minusHours(1));
        when(bidsRepository.findByUserIdIdAndAuctionIdId(1L, 1L)).thenReturn(Optional.of(existingBid));

        when(bidsRepository.save(any(Bids.class))).thenReturn(existingBid);

        // Act
        boolean result = bidHandlerService.handleBid(1L, validBid);

        // Assert
        assertTrue(result);
        verify(redisService).addBidToLeaderboard(1L, validBid);
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/1"), anyString());
        verify(bidsRepository).save(any(Bids.class));
        assertEquals(100, existingBid.getBidAmount()); // Check bid amount was updated
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_FailedToAcquireLock() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        // Act
        boolean result = bidHandlerService.handleBid(1L, validBid);

        // Assert
        assertFalse(result);
        verifyNoInteractions(auctionRepository);
        verifyNoInteractions(redisService);
        verifyNoInteractions(bidsRepository);
    }

    @Test
    void testHandleBid_AuctionNotFound() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, validBid);
        });
        assertEquals("Auction not found", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_SellerCannotBidOnOwnAuction() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        // Bid from the seller
        BidDTO sellerBid = new BidDTO();
        sellerBid.setUserId(seller.getId());
        sellerBid.setAuctionId(auction.getId());
        sellerBid.setAmount(100L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, sellerBid);
        });
        assertEquals("You cannot bid on your own auction.", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_AuctionNotActive() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("CLOSED"); // Auction not active

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, validBid);
        });
        assertEquals("Auction is not active.", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_AuctionEnded() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(pastEndTime.toString()); // Auction ended

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, validBid);
        });
        assertEquals("Auction has already ended.", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_BidTooLow() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(futureEndTime.toString());

        // Higher bid already exists
        BidDTO higherBid = new BidDTO();
        higherBid.setUserId(3L); // Different user
        higherBid.setAuctionId(1L);
        higherBid.setAmount(200L); // Higher than our bid
        when(redisService.getTopBid(1L)).thenReturn(higherBid);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, validBid);
        });
        assertEquals("Bid amount must be higher than the current highest bid.", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_UserNotFound() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(futureEndTime.toString());
        when(redisService.getTopBid(1L)).thenReturn(null); // No previous bids
        when(bidsRepository.findByUserIdIdAndAuctionIdId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty()); // User not found

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bidHandlerService.handleBid(1L, validBid);
        });
        assertEquals("User not found", exception.getMessage());
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_HandleGenericException() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(futureEndTime.toString());
        when(redisService.getTopBid(1L)).thenReturn(null);
        when(bidsRepository.findByUserIdIdAndAuctionIdId(1L, 1L)).thenReturn(Optional.empty());

        // Simulate a generic exception during processing
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = bidHandlerService.handleBid(1L, validBid);

        // Assert
        assertFalse(result);
        verify(redisTemplate).delete("lock:auction:1");
    }

    @Test
    void testHandleBid_NullEndTime() {
        // Arrange
        when(valueOperations.setIfAbsent(eq("lock:auction:1"), eq("locked"), eq(10L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));
        when(hashOperations.get("auction1", "status")).thenReturn("ACTIVE");
        when(hashOperations.get("auction1", "endTime")).thenReturn(null); // Null end time
        when(redisService.getTopBid(1L)).thenReturn(null); // No previous bids
        when(bidsRepository.findByUserIdIdAndAuctionIdId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(bidder));
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(auction));

        Bids newBid = new Bids();
        when(bidsRepository.save(any(Bids.class))).thenReturn(newBid);

        // Act
        boolean result = bidHandlerService.handleBid(1L, validBid);

        // Assert
        assertTrue(result);
        verify(redisService).addBidToLeaderboard(1L, validBid);
        verify(messagingTemplate).convertAndSend(eq("/topic/auction/1"), anyString());
        verify(bidsRepository).save(any(Bids.class));
        verify(redisTemplate).delete("lock:auction:1");
    }
}