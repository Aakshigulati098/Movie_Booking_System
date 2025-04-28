package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForHash()).thenReturn(mock(HashOperations.class));
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
    }



    @Test
    void getAllActiveAuctions_ShouldReturnActiveAuctions() {
        Set<String> keys = new HashSet<>(Arrays.asList("auction1", "auction2"));
        when(redisTemplate.keys("auction*")).thenReturn(keys);
        when(redisTemplate.opsForHash().get("auction1", "status")).thenReturn("ACTIVE");
        when(redisTemplate.opsForHash().get("auction2", "status")).thenReturn("INACTIVE");

        Map<Long, Map<String, String>> result = redisService.getAllActiveAuctions();

        assertEquals(1, result.size());
        assertTrue(result.containsKey(1L));
    }

    @Test
    void createLeaderboard_ShouldSetTTLForLeaderboard() {
        Long auctionId = 1L;
        Auction auction = new Auction();
        auction.setEndsAt(LocalDateTime.now().plusHours(1));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        redisService.createLeaderboard(auctionId);

        verify(redisTemplate).expire(eq("auction1:bids"), anyLong(), eq(TimeUnit.SECONDS));
    }


    @Test
    void getUserBid_ShouldReturnUserBid() {
        Long auctionId = 1L;
        Long userId = 2L;
        BidDTO bid = new BidDTO();
        bid.setUserId(userId);
        bid.setAmount(100L);

        when(redisTemplate.opsForValue().get("auction:1:user:2")).thenReturn(bid);

        BidDTO result = redisService.getUserBid(auctionId, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(100L, result.getAmount());
    }

    @Test
    void getTopBid_ShouldReturnTopBid() {
        Long auctionId = 1L;
        BidDTO bid = new BidDTO();
        bid.setUserId(2L);
        bid.setAmount(200L);

        Set<ZSetOperations.TypedTuple<Object>> topBids = new HashSet<>();
        topBids.add(new DefaultTypedTuple<>(bid, 200.0));
        when(zSetOperations.reverseRangeWithScores("auction1:bids", 0, 0)).thenReturn(topBids);

        BidDTO result = redisService.getTopBid(auctionId);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals(200L, result.getAmount());
    }


    @Test
    void createAndSaveLeaderboard_ShouldSaveLeaderboard() {
        Long auctionId = 1L;
        BidResponseDTO bidResponse = new BidResponseDTO();
        bidResponse.setBidderId(2L);
        bidResponse.setAmount(200L);

        Set<BidResponseDTO> leaderboard = new HashSet<>();
        leaderboard.add(bidResponse);

        redisService.createAndSaveLeaderboard(auctionId, leaderboard);

        verify(redisTemplate.opsForZSet(), times(1)).add(eq("auction:1:leaderboard"), anySet());
        verify(redisTemplate).expire(eq("auction:1:leaderboard"), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    void deleteBidderFromLeaderboard_ShouldRemoveBidder() {
        Long auctionId = 1L;
        BidDTO bid = new BidDTO();
        bid.setUserId(2L);
        bid.setAuctionId(auctionId);

        redisService.deleteBidderFromLeaderboard(bid);

        verify(redisTemplate).delete("auction:1:user:2");
    }
    @Test
    void getTopBidForKafka_ShouldReturnTopBid() {
        Long auctionId = 1L;
        BidResponseDTO bidResponse = new BidResponseDTO();
        bidResponse.setBidderId(2L);
        bidResponse.setAmount(300L);

        Set<ZSetOperations.TypedTuple<Object>> topBids = new HashSet<>();
        topBids.add(new DefaultTypedTuple<>(bidResponse, 300.0));
        when(zSetOperations.reverseRangeWithScores("auction:" + auctionId + ":leaderboard", 0, 0)).thenReturn(topBids);

        BidDTO result = redisService.getTopBidForKafka(auctionId);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals(300L, result.getAmount());
    }
    @Test
    void deleteBidderFromLeaderboard_ShouldRemoveBidderFromLeaderboard() {
        Long auctionId = 1L;
        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(2L);
        bidDTO.setAuctionId(auctionId);

        redisService.deleteBidderFromLeaderboard(bidDTO);

        verify(redisTemplate).delete("auction:" + auctionId + ":user:" + bidDTO.getUserId());
    }
    @Test
    void createAndSaveLeaderboard_ShouldHandleEmptyLeaderboard() {
        Long auctionId = 1L;

        redisService.createAndSaveLeaderboard(auctionId, Collections.emptySet());

        verify(redisTemplate, never()).opsForZSet();
    }
    @Test
    void getLeaderboard_ShouldThrowExceptionForInvalidBidData() {
        Long auctionId = 1L;

        Set<ZSetOperations.TypedTuple<Object>> bids = new HashSet<>();
        bids.add(new DefaultTypedTuple<>(null, 200.0));
        when(zSetOperations.reverseRangeWithScores("auction" + auctionId + ":bids", 0, -1)).thenReturn(bids);

        assertThrows(IllegalArgumentException.class, () -> redisService.getLeaderboard(auctionId));
    }

    @Test
    void getTopBid_ShouldReturnNullWhenNoBidsExist() {
        Long auctionId = 1L;
        when(zSetOperations.reverseRangeWithScores("auction1:bids", 0, 0)).thenReturn(Collections.emptySet());

        BidDTO result = redisService.getTopBid(auctionId);

        assertNull(result);
    }
    @Test
    void getAllActiveAuctions_ShouldReturnEmptyMapWhenNoKeysExist() {
        when(redisTemplate.keys("auction*")).thenReturn(Collections.emptySet());

        Map<Long, Map<String, String>> result = redisService.getAllActiveAuctions();

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteBidderFromLeaderboard_ShouldHandleNoEntries() {
        Long auctionId = 1L;
        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(2L);
        bidDTO.setAuctionId(auctionId);

        when(redisTemplate.opsForZSet().range("auction:" + auctionId + ":leaderboard", 0, -1)).thenReturn(Collections.emptySet());

        redisService.deleteBidderFromLeaderboard(bidDTO);

        verify(redisTemplate).delete("auction:" + auctionId + ":user:" + bidDTO.getUserId());
    }

    @Test
    void shouldPopulateBidResponseDTO() {
        // Arrange
        BidDTO bidDTO = new BidDTO();
        bidDTO.setAuctionId(1L);
        bidDTO.setUserId(2L);
        bidDTO.setAmount(300L);

        Users user = new Users();
        user.setId(2L);
        user.setName("Test User");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        // Act
        BidResponseDTO bidResponseDTO = new BidResponseDTO();
        bidResponseDTO.setAuctionId(bidDTO.getAuctionId().toString());
        bidResponseDTO.setBidder(userRepository.findById(bidDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + bidDTO.getUserId()))
                .getName());
        bidResponseDTO.setAmount(bidDTO.getAmount());

        // Assert
        assertEquals("1", bidResponseDTO.getAuctionId());
        assertEquals("Test User", bidResponseDTO.getBidder());
        assertEquals(300L, bidResponseDTO.getAmount());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        BidDTO bidDTO = new BidDTO();
        bidDTO.setAuctionId(1L);
        bidDTO.setUserId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            BidResponseDTO bidResponseDTO = new BidResponseDTO();
            bidResponseDTO.setAuctionId(bidDTO.getAuctionId().toString());
            bidResponseDTO.setBidder(userRepository.findById(bidDTO.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + bidDTO.getUserId()))
                    .getName());
            bidResponseDTO.setAmount(bidDTO.getAmount());
        });

        assertEquals("User not found for ID: 2", exception.getMessage());
    }
    @Test
    void shouldReturnAndCacheUserBidFromLeaderboard() {
        // Arrange
        Long auctionId = 1L;
        Long userId = 2L;
        String leaderboardKey = "auction" + auctionId + ":bids";
        String userKey = "auction:" + auctionId + ":user:" + userId;

        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(userId);
        bidDTO.setAmount(300L);

        Set<Object> bids = new HashSet<>();
        bids.add(bidDTO);

        Auction auction = new Auction();
        auction.setEndsAt(LocalDateTime.now().plusHours(1));

        when(redisTemplate.opsForZSet().range(leaderboardKey, 0, -1)).thenReturn(bids);
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        // Act
        BidDTO result = redisService.getUserBid(auctionId, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(300L, result.getAmount());
        verify(redisTemplate.opsForValue()).set(eq(userKey), eq(bidDTO));
        verify(redisTemplate).expire(eq(userKey), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldThrowExceptionWhenAuctionNotFound() {
        // Arrange
        Long auctionId = 1L;
        Long userId = 2L;

        when(auctionRepository.findById(auctionId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            redisService.getUserBid(auctionId, userId);
        });

        assertEquals("Auction not found", exception.getMessage());
    }

    @Test
    void shouldRemoveMatchingBidFromLeaderboard() {
        // Arrange
        String key = "auction:1:leaderboard";
        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(2L);
        bidDTO.setAuctionId(1L);

        BidResponseDTO matchingBid = new BidResponseDTO();
        matchingBid.setBidderId(2L);
        matchingBid.setAmount(300L);

        Set<Object> entries = new HashSet<>();
        entries.add(matchingBid);

        when(redisTemplate.opsForZSet().range(key, 0, -1)).thenReturn(entries);

        // Act
        redisService.deleteBidderFromLeaderboard(bidDTO);

        // Assert
        verify(redisTemplate.opsForZSet()).remove(key, matchingBid);
        verify(redisTemplate).delete("auction:1:user:2");
    }

    @Test
    void shouldNotRemoveWhenNoMatchingBidFound() {
        // Arrange
        String key = "auction:1:leaderboard";
        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(3L);
        bidDTO.setAuctionId(1L);

        BidResponseDTO nonMatchingBid = new BidResponseDTO();
        nonMatchingBid.setBidderId(2L);
        nonMatchingBid.setAmount(300L);

        Set<Object> entries = new HashSet<>();
        entries.add(nonMatchingBid);

        when(redisTemplate.opsForZSet().range(key, 0, -1)).thenReturn(entries);

        // Act
        redisService.deleteBidderFromLeaderboard(bidDTO);

        // Assert
        verify(redisTemplate.opsForZSet(), never()).remove(eq(key), any());
        verify(redisTemplate).delete("auction:1:user:3");
    }

    @Test
    void shouldHandleNullEntries() {
        // Arrange
        String key = "auction:1:leaderboard";
        BidDTO bidDTO = new BidDTO();
        bidDTO.setUserId(2L);
        bidDTO.setAuctionId(1L);

        when(redisTemplate.opsForZSet().range(key, 0, -1)).thenReturn(null);

        // Act
        redisService.deleteBidderFromLeaderboard(bidDTO);

        // Assert
        verify(redisTemplate.opsForZSet(), never()).remove(eq(key), any());
        verify(redisTemplate).delete("auction:1:user:2");
    }
}