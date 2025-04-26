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
    void saveAuctionMetadata_ShouldSaveMetadataToRedis() {
        Long auctionId = 1L;
        String status = "ACTIVE";
        LocalDateTime endTime = LocalDateTime.now().plusHours(1);

        redisService.saveAuctionMetadata(auctionId, status, endTime);

        verify(redisTemplate).opsForHash().put("auction1", "status", status);
        verify(redisTemplate).opsForHash().put("auction1", "endTime", endTime.toString());
        verify(redisTemplate).expire(eq("auction1"), anyLong(), eq(TimeUnit.SECONDS));
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
    void addBidToLeaderboard_ShouldAddBid() {
        Long auctionId = 1L;
        BidDTO bid = new BidDTO();
        bid.setUserId(2L);
        bid.setAmount(100L);

        Auction auction = new Auction();
        auction.setEndsAt(LocalDateTime.now().plusHours(1));
        when(auctionRepository.findById(auctionId)).thenReturn(Optional.of(auction));

        redisService.addBidToLeaderboard(auctionId, bid);

        verify(zSetOperations).add("auction1:bids", bid, bid.getAmount());
        verify(redisTemplate).opsForValue().set("auction:1:user:2", bid);
        verify(redisTemplate).expire(eq("auction:1:user:2"), anyLong(), eq(TimeUnit.SECONDS));
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
    void getLeaderboard_ShouldReturnLeaderboard() {
        Long auctionId = 1L;
        BidDTO bid = new BidDTO();
        bid.setUserId(2L);
        bid.setAmount(200L);

        Set<ZSetOperations.TypedTuple<Object>> bids = new HashSet<>();
        bids.add(new DefaultTypedTuple<>(bid, 200.0));
        when(zSetOperations.reverseRangeWithScores("auction1:bids", 0, -1)).thenReturn(bids);

        Users user = new Users();
        user.setId(2L);
        user.setName("Test User");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Set<BidResponseDTO> result = redisService.getLeaderboard(auctionId);

        assertEquals(1, result.size());
        BidResponseDTO response = result.iterator().next();
        assertEquals(2L, response.getBidderId());
        assertEquals(200L, response.getAmount());
        assertEquals("Test User", response.getBidder());
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
    void getUserBid_ShouldReturnNullWhenNoBidFound() {
        Long auctionId = 1L;
        Long userId = 2L;

        when(redisTemplate.opsForValue().get("auction:" + auctionId + ":user:" + userId)).thenReturn(null);

        BidDTO result = redisService.getUserBid(auctionId, userId);

        assertNull(result);
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
    void saveAuctionMetadata_ShouldHandleExpiredAuction() {
        Long auctionId = 1L;
        String status = "EXPIRED";
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);

        redisService.saveAuctionMetadata(auctionId, status, endTime);

        verify(redisTemplate).opsForHash().put("auction1", "status", status);
        verify(redisTemplate).opsForHash().put("auction1", "endTime", endTime.toString());
        verify(redisTemplate).expire(eq("auction1"), eq(0L), eq(TimeUnit.SECONDS));
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
}