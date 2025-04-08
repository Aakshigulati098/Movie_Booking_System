package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private static final Logger logger = Logger.getLogger(RedisService.class.getName());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    public void saveAuctionMetadata(Long auctionId, String status, LocalDateTime endTime) {
        String key = "auction" + auctionId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "endTime", endTime.toString());
        redisTemplate.expire(key, TimeUnit.HOURS.toSeconds(1), TimeUnit.SECONDS); // Set TTL for 1 hour
    }

    public Map<Long, Map<String, String>> getAllActiveAuctions() {
        System.out.println("hey i am here in getAllActiveAuctions redis");
        Set<String> keys = redisTemplate.keys("auction*");
        System.out.println("Keys of all active auctions: " + keys);
        if (keys == null) {
            return Collections.emptyMap();
        }
        return keys.stream()
                .filter(key -> !key.contains(":bids")&& !key.contains(":user:") && "ACTIVE".equals(redisTemplate.opsForHash().get(key, "status")))
                .collect(Collectors.toMap(
                        key -> Long.parseLong(key.replace("auction", "")),
                        key -> redisTemplate.<String, String>opsForHash().entries(key)
                ));
    }

    public void createLeaderboard(Long auctionId) {
        // No need to initialize the ZSET, it will be created when the first bid is added
        String key = "auction" + auctionId + ":bids";
        redisTemplate.expire(key, TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS); // Set TTL for 24 hours
    }

    /**
     * Add a bid to the leaderboard, ensuring only one entry per user
     * If the user has already bid, their previous bid will be replaced
     */
    public void addBidToLeaderboard(Long auctionId, BidDTO bid) {
        String key = "auction" + auctionId + ":bids";
        String userKey = "auction:" + auctionId + ":user:" + bid.getUserId();

        // First, check if this user has a previous bid in this auction
        BidDTO existingBid = getUserBid(auctionId, bid.getUserId());

        if (existingBid != null) {
            // Remove the old bid
            logger.info("Removing previous bid for user " + bid.getUserId() + " in auction " + auctionId);
            redisTemplate.opsForZSet().remove(key, existingBid);
        }

        // Add the new bid
        redisTemplate.opsForZSet().add(key, bid, bid.getAmount());

        // Store the user's latest bid in a separate key for quick lookup
        redisTemplate.opsForValue().set(userKey, bid);
        redisTemplate.expire(userKey, TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);

        logger.info("Added/updated bid for user " + bid.getUserId() + " in auction " + auctionId + " with amount " + bid.getAmount());
    }

    /**
     * Get a user's current bid for an auction
     */
    public BidDTO getUserBid(Long auctionId, Long userId) {
        String userKey = "auction:" + auctionId + ":user:" + userId;
        Object userBid = redisTemplate.opsForValue().get(userKey);

        if (userBid != null) {
            return (BidDTO) userBid;
        }

        // If not found in the direct lookup, search in the leaderboard
        String leaderboardKey = "auction" + auctionId + ":bids";
        Set<Object> bids = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);

        if (bids != null) {
            for (Object bidObj : bids) {
                BidDTO bidDTO = (BidDTO) bidObj;
                if (bidDTO.getUserId().equals(userId)) {
                    // Cache this for future lookups
                    redisTemplate.opsForValue().set(userKey, bidDTO);
                    redisTemplate.expire(userKey, TimeUnit.HOURS.toSeconds(24), TimeUnit.SECONDS);
                    return bidDTO;
                }
            }
        }

        return null;
    }

    public BidDTO getTopBid(Long auctionId) {
        String key = "auction" + auctionId + ":bids";
        Set<ZSetOperations.TypedTuple<Object>> topBids = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 0);

        if (topBids != null && !topBids.isEmpty()) {
            ZSetOperations.TypedTuple<Object> topBid = topBids.iterator().next();
            if (topBid != null) {
                return (BidDTO) topBid.getValue();
            }
        }

        return null; // Return null if there are no bids
    }

    public Set<BidResponseDTO> getLeaderboard(Long auctionId) {
        System.out.println("hey i am here in leaderboard");
        String key = "auction" + auctionId + ":bids";
        System.out.println("Key: " + key);
        Set<ZSetOperations.TypedTuple<Object>> bids = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        System.out.println("Fetched bids: " + bids);
        if (bids == null || bids.isEmpty()) {
            return Collections.emptySet();
        }
        return bids.stream().map(bid -> {
            BidDTO bidDTO = (BidDTO) bid.getValue();
            if (bidDTO == null) {
                throw new IllegalArgumentException("Invalid bid data for auction ID: " + auctionId);
            }
            BidResponseDTO bidResponseDTO = new BidResponseDTO();
            bidResponseDTO.setAuctionId(bidDTO.getAuctionId().toString());
            bidResponseDTO.setBidder(userRepository.findById(bidDTO.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + bidDTO.getUserId()))
                    .getName());
            bidResponseDTO.setAmount(bidDTO.getAmount());
            return bidResponseDTO;
        }).collect(Collectors.toSet());
    }
}