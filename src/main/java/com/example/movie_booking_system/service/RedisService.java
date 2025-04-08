package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.models.Bids;
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
import java.util.stream.Collectors;

@Service
public class RedisService {

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
                .filter(key -> !key.contains(":bids") && "ACTIVE".equals(redisTemplate.opsForHash().get(key, "status")))
                .collect(Collectors.toMap(
                        key -> Long.parseLong(key.replace("auction", "")),
                        key -> redisTemplate.<String, String>opsForHash().entries(key)
                ));
    }
    public void createLeaderboard(Long auctionId) {
        String key = "auction" + auctionId + ":bids";
        // No need to add null to initialize the ZSET
    }
//    hey modify this function such that if bids is null that means the auctioin has just started and there is no
    public void addBidToLeaderboard(Long auctionId, BidDTO bid) {
        String key = "auction" + auctionId + ":bids";
        redisTemplate.opsForZSet().add(key, bid, bid.getAmount());
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

    //    need to find what happened here
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
    }}